package com.example.tp1_blackjack.model

import com.example.tp1_blackjack.model.betting.*
import com.example.tp1_blackjack.model.cards.*
import com.example.tp1_blackjack.model.game.*
import com.example.tp1_blackjack.model.results.*

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HandTest {
    @Test
    fun faceCards_areWorthTen() {
        assertEquals(10, Rank.JACK.blackjackValue)
        assertEquals(10, Rank.QUEEN.blackjackValue)
        assertEquals(10, Rank.KING.blackjackValue)
    }

    @Test
    fun multipleAces_useBestValueWithoutBust() {
        val hand = Hand(
            listOf(
                card(Rank.ACE),
                card(Rank.ACE),
                card(Rank.NINE),
            ),
        )

        assertEquals(21, hand.score)
        assertFalse(hand.isBust)
    }

    @Test
    fun scoreOverTwentyOne_isBust() {
        val hand = Hand(listOf(card(Rank.KING), card(Rank.QUEEN), card(Rank.TWO)))

        assertEquals(22, hand.score)
        assertTrue(hand.isBust)
    }

    @Test
    fun naturalBlackjack_requiresExactlyTwoCards() {
        val natural = Hand(listOf(card(Rank.ACE), card(Rank.KING)))
        val threeCardTwentyOne = Hand(listOf(card(Rank.SEVEN), card(Rank.SEVEN), card(Rank.SEVEN)))

        assertTrue(natural.isNaturalBlackjack)
        assertFalse(threeCardTwentyOne.isNaturalBlackjack)
    }

    @Test
    fun deckIndex_distinguishesOtherwiseEqualCards() {
        val firstDeck = Card(Rank.ACE, Suit.SPADES, deckIndex = 0)
        val secondDeck = Card(Rank.ACE, Suit.SPADES, deckIndex = 1)

        assertNotEquals(firstDeck, secondDeck)
    }

    @Test
    fun playerHands_replacesOnlyTheActiveHand() {
        val first = PlayerHandState(Hand(listOf(card(Rank.FIVE))))
        val second = PlayerHandState(Hand(listOf(card(Rank.SIX))))
        val playerHands = PlayerHands(listOf(first, second), activeHandIndex = 1)

        val updated = playerHands.replaceActiveHand(
            second.copy(hand = second.hand.add(card(Rank.SEVEN))),
        )

        assertEquals(first, updated.hands[0])
        assertEquals(13, updated.hands[1].hand.score)
    }

    private fun card(rank: Rank): Card = Card(rank, Suit.CLUBS)
}
