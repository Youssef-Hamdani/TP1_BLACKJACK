package com.example.tp1_blackjack

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.tp1_blackjack.repository.PlayerProfileRepository
import com.example.tp1_blackjack.ui.BlackjackApp
import com.example.tp1_blackjack.ui.screens.CREATE_PLAYER_ACTION_TEST_TAG
import com.example.tp1_blackjack.ui.screens.PLAYER_NAME_INPUT_TEST_TAG
import com.example.tp1_blackjack.ui.screens.START_ROUND_TEST_TAG
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme
import com.example.tp1_blackjack.viewmodel.GameViewModel
import com.example.tp1_blackjack.viewmodel.PlayerProfileViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlayerCreationFlowTest {
    @get:Rule val composeRule = createComposeRule()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repo = PlayerProfileRepository(context)

    @Before fun clearProfile() {
        context.getSharedPreferences("blackjack_player_profile", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @After fun resetProfile() {
        context.getSharedPreferences("blackjack_player_profile", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test fun firstLaunchCreatesOnePersistentPlayerAndOpensSetup() {
        val profile = PlayerProfileViewModel(repo)
        val game = GameViewModel()
        composeRule.setContent {
            TP1_BlackJackTheme {
                BlackjackApp(gameViewModel = game, playerProfileViewModel = profile)
            }
        }
        composeRule.onNodeWithTag(PLAYER_NAME_INPUT_TEST_TAG).performTextInput("  Jean  ")
        composeRule.onNodeWithTag(CREATE_PLAYER_ACTION_TEST_TAG).performClick()
        composeRule.onNodeWithTag(START_ROUND_TEST_TAG).assertIsDisplayed()
        assertEquals("Jean", repo.readName())
        assertEquals(1_000, game.uiState.value.balance)
        assertEquals("Jean", PlayerProfileViewModel(repo).playerName.value)
    }
}
