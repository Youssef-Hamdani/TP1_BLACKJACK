package com.example.tp1_blackjack.repository

import android.content.Context

/** One local player: a simple preference, separate from the Room game history. */
class PlayerProfileRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "blackjack_player_profile", Context.MODE_PRIVATE,
    )

    fun readName(): String? = preferences.getString("player_name", null)
        ?.trim()?.takeIf { it.isNotEmpty() }

    fun saveName(value: String) {
        val name = value.trim()
        require(name.isNotEmpty() && name.length <= 32)
        check(preferences.edit().putString("player_name", name).commit()) {
            "Could not save player name"
        }
    }
}
