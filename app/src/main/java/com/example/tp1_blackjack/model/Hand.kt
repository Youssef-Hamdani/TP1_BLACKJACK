package com.example.tp1_blackjack.model

data class Hand(
    val cards: List<Card> = emptyList(),
) {
    val score: Int
        get() {
            var total = cards.sumOf { it.rank.blackjackValue }
            var flexibleAces = cards.count { it.rank == Rank.ACE }

            while (total > BLACKJACK && flexibleAces > 0) {
                total -= ACE_ADJUSTMENT
                flexibleAces--
            }

            return total
        }

    val isBust: Boolean
        get() = score > BLACKJACK

    val isNaturalBlackjack: Boolean
        get() = cards.size == 2 && score == BLACKJACK

    fun add(card: Card): Hand = copy(cards = cards + card)

    private companion object {
        const val BLACKJACK = 21
        const val ACE_ADJUSTMENT = 10
    }
}
