package com.example.tp1_blackjack.model.game

import com.example.tp1_blackjack.model.betting.Bet
import com.example.tp1_blackjack.model.results.DrawnCard
import com.example.tp1_blackjack.model.results.HandOutcome

data class GameState(
    val phase: GamePhase = GamePhase.AWAITING_START,
    val selectedBet: Bet = Bet.Default,
    val balance: Int = 1_000,
    val deckCount: Int = 1,
    val committedBets: List<Bet> = emptyList(),
    val isRoundSettled: Boolean = false,
    val playerHands: PlayerHands = PlayerHands(),
    val dealerHand: Hand = Hand(),
    val isDealerHoleCardRevealed: Boolean = false,
    val outcomes: List<HandOutcome> = emptyList(),
    val drawnCards: List<DrawnCard> = emptyList(),
) {
    val canSelectDeckCount: Boolean
        get() = phase == GamePhase.AWAITING_START || phase == GamePhase.COMPLETE

    val availableActions: Set<GameAction>
        get() = when (phase) {
            GamePhase.AWAITING_START,
            GamePhase.COMPLETE,
            -> if (selectedBet.amount <= balance) {
                setOf(GameAction.START_ROUND)
            } else {
                emptySet()
            }

            GamePhase.PLAYER_TURN -> if (playerHands.activeHand.status == PlayerHandStatus.ACTIVE) {
                buildSet {
                    add(GameAction.HIT)
                    add(GameAction.STAND)
                    if (canSplit) add(GameAction.SPLIT)
                }
            } else {
                emptySet()
            }

            GamePhase.DEALING,
            GamePhase.DEALER_TURN,
            -> emptySet()
        }

    private val canSplit: Boolean
        get() {
            val hand = playerHands.hands.singleOrNull() ?: return false
            val bet = committedBets.singleOrNull() ?: return false
            val cards = hand.hand.cards
            return !hand.isFromSplit && cards.size == 2 &&
                cards[0].rank == cards[1].rank && balance >= bet.amount
        }

    fun requireAction(action: GameAction) {
        check(action in availableActions) {
            "$action is not allowed during $phase"
        }
    }
}
