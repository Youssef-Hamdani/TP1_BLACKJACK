package com.example.tp1_blackjack

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.tp1_blackjack.model.Card
import com.example.tp1_blackjack.model.Rank
import com.example.tp1_blackjack.model.Suit
import com.example.tp1_blackjack.ui.components.CardView
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme
import org.junit.Rule
import org.junit.Test

class CardViewTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun faceAndBack_haveDistinctAccessibleGraphics() {
        composeRule.setContent {
            TP1_BlackJackTheme {
                Row {
                    CardView(
                        card = Card(Rank.KING, Suit.HEARTS),
                        isFaceDown = false,
                    )
                    CardView(
                        card = null,
                        isFaceDown = true,
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription("Roi de cœur").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Carte cachée").assertIsDisplayed()
    }
}
