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

    val hasUnfinishedHand: Boolean
        get() = hands.any { !it.isFinished }

    fun replaceActiveHand(hand: PlayerHandState): PlayerHands = copy(
        hands = hands.mapIndexed { index, existingHand ->
            if (index == activeHandIndex) hand else existingHand
        },
    )

    fun finishActiveHand(status: PlayerHandStatus): PlayerHands {
        require(status != PlayerHandStatus.ACTIVE) { "A finished hand needs a terminal status" }

        val updatedHands = replaceActiveHand(activeHand.copy(status = status))
        val nextHandIndex = updatedHands.hands.indexOfFirst { !it.isFinished }
        return if (nextHandIndex >= 0) {
            updatedHands.copy(activeHandIndex = nextHandIndex)
        } else {
            updatedHands
        }
    }
}
