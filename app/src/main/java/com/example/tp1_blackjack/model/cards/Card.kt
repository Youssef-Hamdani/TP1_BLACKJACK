package com.example.tp1_blackjack.model.cards

data class Card(
    val rank: Rank,
    val suit: Suit,
    val deckIndex: Int = 0,
) {
    init {
        require(deckIndex >= 0) { "deckIndex must not be negative" }
    }
}
