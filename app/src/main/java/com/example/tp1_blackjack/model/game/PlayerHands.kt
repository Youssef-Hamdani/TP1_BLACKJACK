package com.example.tp1_blackjack.model.game

data class PlayerHands(
    val hands: List<PlayerHandState> = listOf(PlayerHandState()),
    val activeHandIndex: Int = 0,
) {
    init {
        require(hands.isNotEmpty()) { "A player must have at least one hand" }
        require(activeHandIndex in hands.indices) { "activeHandIndex must identify an existing hand" }
    }

    val activeHand: PlayerHandState
        get() = hands[activeHandIndex]

    fun replaceActiveHand(hand: PlayerHandState): PlayerHands = copy(
        hands = hands.mapIndexed { index, existingHand ->
            if (index == activeHandIndex) hand else existingHand
        },
    )
}
