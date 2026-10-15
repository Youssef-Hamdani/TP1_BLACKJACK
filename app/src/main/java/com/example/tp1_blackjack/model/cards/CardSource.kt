package com.example.tp1_blackjack.model.cards

interface CardSource {
    val remainingCount: Int

    fun draw(): Card

    fun prepareForNewRound(): Boolean
}
