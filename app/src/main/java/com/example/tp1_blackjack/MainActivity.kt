package com.example.tp1_blackjack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tp1_blackjack.ui.BlackjackApp
import com.example.tp1_blackjack.ui.theme.TP1_BlackJackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TP1_BlackJackTheme {
                BlackjackApp()
            }
        }
    }
}
