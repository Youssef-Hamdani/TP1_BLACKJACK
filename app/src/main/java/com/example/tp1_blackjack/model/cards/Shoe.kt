package com.example.tp1_blackjack.model.cards

import kotlin.random.Random

class Shoe(
    val deckCount: Int = 1,
    private val random: Random = Random.Default,
) : CardSource {
    private val remainingCards = ArrayDeque<Card>()

    init {
        require(deckCount in SUPPORTED_DECK_COUNTS) {
            "deckCount must be one of $SUPPORTED_DECK_COUNTS"
        }
        rebuild()
    }

    override val remainingCount: Int
        get() = remainingCards.size

    override fun draw(): Card = remainingCards.removeFirstOrNull()
        ?: error("Cannot draw from an empty shoe")

    override fun prepareForNewRound(): Boolean {
        if (remainingCount >= RENEWAL_THRESHOLD) return false

        rebuild()
        return true
    }

    private fun rebuild() {
        val shuffledCards = buildList {
            repeat(deckCount) { deckIndex ->
                Suit.entries.forEach { suit ->
                    Rank.entries.forEach { rank ->
                        add(Card(rank = rank, suit = suit, deckIndex = deckIndex))
                    }
                }
            }
        }.shuffled(random)

        remainingCards.clear()
        remainingCards.addAll(shuffledCards)
    }

    companion object {
        val SUPPORTED_DECK_COUNTS = listOf(1, 2, 4, 6)
        const val RENEWAL_THRESHOLD = 20
    }
}
