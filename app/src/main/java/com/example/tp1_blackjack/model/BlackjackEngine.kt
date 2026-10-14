package com.example.tp1_blackjack.model

class BlackjackEngine(
    private var cardSource: CardSource = Shoe(),
    initialBalance: Int = INITIAL_BALANCE,
    initialBet: Int = Bet.DEFAULT_AMOUNT,
    initialDeckCount: Int = (cardSource as? Shoe)?.deckCount ?: DEFAULT_DECK_COUNT,
    private val cardSourceFactory: (Int) -> CardSource = { deckCount -> Shoe(deckCount) },
) {
    init {
        require(initialBalance >= 0) { "Initial balance cannot be negative" }
        require(initialDeckCount in Shoe.SUPPORTED_DECK_COUNTS) {
            "Initial deck count must be one of ${Shoe.SUPPORTED_DECK_COUNTS}"
        }
    }

    var state: GameState = GameState(
        selectedBet = requireNotNull(Bet.from(initialBet)) {
            "Initial bet must be between ${Bet.MIN_AMOUNT} and ${Bet.MAX_AMOUNT}"
        },
        balance = initialBalance,
        deckCount = initialDeckCount,
    )
        private set

    fun restoreWallet(wallet: Wallet): GameState {
        check(state.phase == GamePhase.AWAITING_START) { "Restore is only allowed before the first round" }
        require(wallet.balance >= 0)
        state = state.copy(
            balance = wallet.balance,
            selectedBet = requireNotNull(Bet.from(wallet.defaultBet)),
        )
        return state
    }

    fun selectBet(amount: Int): GameState {
        check(state.phase == GamePhase.AWAITING_START || state.phase == GamePhase.COMPLETE) {
            "Bet can only be changed between rounds"
        }
        val bet = requireNotNull(Bet.from(amount)) {
            "Bet must be between ${Bet.MIN_AMOUNT} and ${Bet.MAX_AMOUNT}"
        }
        state = state.copy(selectedBet = bet)
        return state
    }

    fun selectDeckCount(deckCount: Int): GameState {
        check(state.canSelectDeckCount) {
            "Deck count can only be changed between rounds"
        }
        require(deckCount in Shoe.SUPPORTED_DECK_COUNTS) {
            "Deck count must be one of ${Shoe.SUPPORTED_DECK_COUNTS}"
        }
        if (deckCount == state.deckCount) return state

        cardSource = cardSourceFactory(deckCount)
        state = state.copy(deckCount = deckCount)
        return state
    }

    fun startRound(): GameState {
        state.requireAction(GameAction.START_ROUND)
        cardSource.prepareForNewRound()
        state = state.copy(
            phase = GamePhase.DEALING,
            balance = state.balance - state.selectedBet.amount,
            committedBets = listOf(state.selectedBet),
            isRoundSettled = false,
            playerHands = PlayerHands(),
            dealerHand = Hand(),
            isDealerHoleCardRevealed = false,
            outcomes = emptyList(),
            drawnCards = emptyList(),
        )

        var playerHand = Hand().add(drawCard(CardOwner.PLAYER, 0))
        var dealerHand = Hand().add(drawCard(CardOwner.DEALER, observed = false))
        playerHand = playerHand.add(drawCard(CardOwner.PLAYER, 0))
        dealerHand = dealerHand.add(drawCard(CardOwner.DEALER))

        state = state.copy(
            phase = GamePhase.PLAYER_TURN,
            playerHands = PlayerHands(
                hands = listOf(PlayerHandState(hand = playerHand)),
            ),
            dealerHand = dealerHand,
            isDealerHoleCardRevealed = false,
        )

        val playerHasNatural = playerHand.isNaturalBlackjack
        val dealerHasNatural = dealerHand.isNaturalBlackjack
        if (playerHasNatural || dealerHasNatural) {
            completeRound(revealDealerHoleCard = true)
        }
        return state
    }

    fun split(): GameState {
        state.requireAction(GameAction.SPLIT)
        val originalCards = state.playerHands.activeHand.hand.cards
        val additionalBet = state.committedBets.single()
        val movedOrder = state.drawnCards.filter { it.owner == CardOwner.PLAYER }[1].drawOrder
        state = state.copy(drawnCards = state.drawnCards.map {
            if (it.drawOrder == movedOrder) it.copy(handIndex = 1) else it
        })
        val splitHands = originalCards.mapIndexed { index, card ->
            PlayerHandState(
                hand = Hand(listOf(card, drawCard(CardOwner.PLAYER, index))),
                // A two-card 21 after splitting receives the ordinary win payout.
                isFromSplit = true,
            )
        }
        state = state.copy(
            balance = state.balance - additionalBet.amount,
            committedBets = state.committedBets + additionalBet,
            playerHands = PlayerHands(hands = splitHands, activeHandIndex = 0),
        )
        return state
    }

    fun hit(): GameState {
        state.requireAction(GameAction.HIT)
        val currentHandState = state.playerHands.activeHand
        val updatedHand = currentHandState.hand.add(drawCard(CardOwner.PLAYER, state.playerHands.activeHandIndex))
        val updatedHandState = currentHandState.copy(
            hand = updatedHand,
            status = if (updatedHand.isBust) PlayerHandStatus.BUST else PlayerHandStatus.ACTIVE,
        )

        state = state.copy(
            playerHands = state.playerHands.replaceActiveHand(updatedHandState),
        )

        if (updatedHandState.status == PlayerHandStatus.BUST) {
            finishCurrentPlayerHand()
        }
        return state
    }

    fun stand(): GameState {
        state.requireAction(GameAction.STAND)
        state = state.copy(
            playerHands = state.playerHands.replaceActiveHand(
                state.playerHands.activeHand.copy(status = PlayerHandStatus.STOOD),
            ),
        )
        finishCurrentPlayerHand()
        return state
    }

    private fun finishCurrentPlayerHand() {
        val currentIndex = state.playerHands.activeHandIndex
        val nextIndex = state.playerHands.hands.indices
            .drop(currentIndex + 1)
            .firstOrNull { index ->
                state.playerHands.hands[index].status == PlayerHandStatus.ACTIVE
            }

        if (nextIndex != null) {
            state = state.copy(
                playerHands = state.playerHands.copy(activeHandIndex = nextIndex),
            )
            return
        }

        playDealerTurn()
    }

    private fun playDealerTurn() {
        val allPlayerHandsBust = state.playerHands.hands.all {
            it.status == PlayerHandStatus.BUST
        }
        if (allPlayerHandsBust) {
            completeRound(revealDealerHoleCard = false)
            return
        }

        state = state.copy(
            phase = GamePhase.DEALER_TURN,
            isDealerHoleCardRevealed = true,
        )

        var dealerHand = state.dealerHand
        while (dealerHand.score < DEALER_STAND_SCORE) {
            dealerHand = dealerHand.add(drawCard(CardOwner.DEALER))
        }

        state = state.copy(dealerHand = dealerHand)
        completeRound(revealDealerHoleCard = true)
    }

    private fun completeRound(revealDealerHoleCard: Boolean) {
        check(!state.isRoundSettled) { "Round has already been settled" }
        val outcomes = RoundResultResolver.resolve(
            playerHands = state.playerHands,
            dealerHand = state.dealerHand,
        )
        check(outcomes.size == state.committedBets.size) {
            "Every player hand must have one committed bet"
        }
        val grossPayout = outcomes.zip(state.committedBets).sumOf { (outcome, bet) ->
            PayoutCalculator.grossPayout(bet, outcome)
        }
        state = state.copy(
            phase = GamePhase.COMPLETE,
            isDealerHoleCardRevealed = revealDealerHoleCard,
            outcomes = outcomes,
            balance = state.balance + grossPayout,
            isRoundSettled = true,
            drawnCards = state.drawnCards.map {
                if (it.owner == CardOwner.DEALER && revealDealerHoleCard) it.copy(isObserved = true) else it
            },
        )
    }

    private fun drawCard(owner: CardOwner, handIndex: Int? = null, observed: Boolean = true): Card {
        val card = cardSource.draw()
        state = state.copy(drawnCards = state.drawnCards + DrawnCard(
            state.drawnCards.size, card, owner, handIndex, observed,
        ))
        return card
    }

    private companion object {
        const val DEALER_STAND_SCORE = 17
        const val INITIAL_BALANCE = 1_000
        const val DEFAULT_DECK_COUNT = 1
    }
}
