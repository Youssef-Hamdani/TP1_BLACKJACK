package com.example.tp1_blackjack.model.game

enum class PlayerHandStatus {
    ACTIVE,
    STOOD,
    BUST,
}

data class PlayerHandState(
    val hand: Hand = Hand(),
    val status: PlayerHandStatus = PlayerHandStatus.ACTIVE,
    val isFromSplit: Boolean = false,
) {
    val isFinished: Boolean
        get() = status != PlayerHandStatus.ACTIVE
}
