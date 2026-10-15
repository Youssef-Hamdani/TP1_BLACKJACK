package com.example.tp1_blackjack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tp1_blackjack.repository.PlayerProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerProfileViewModel(private val repository: PlayerProfileRepository) : ViewModel() {
    private val _playerName = MutableStateFlow(repository.readName())
    val playerName: StateFlow<String?> = _playerName.asStateFlow()

    fun createPlayer(input: String): Boolean {
        val name = input.trim()
        if (name.isEmpty() || name.length > 32) return false
        return runCatching {
            repository.saveName(name)
            _playerName.value = name
        }.isSuccess
    }
}
