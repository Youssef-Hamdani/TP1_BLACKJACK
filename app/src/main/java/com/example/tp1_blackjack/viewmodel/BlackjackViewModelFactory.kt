package com.example.tp1_blackjack.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tp1_blackjack.BlackjackApplication

val BlackjackViewModelFactory = viewModelFactory {
    initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) as BlackjackApplication
        GameViewModel(repository = app.repository)
    }
    initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) as BlackjackApplication
        RecordsViewModel(app.repository)
    }
}
