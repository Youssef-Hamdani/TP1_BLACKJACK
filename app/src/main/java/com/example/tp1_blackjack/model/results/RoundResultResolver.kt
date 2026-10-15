package com.example.tp1_blackjack.model.results

import com.example.tp1_blackjack.model.game.Hand
import com.example.tp1_blackjack.model.game.PlayerHandState
import com.example.tp1_blackjack.model.game.PlayerHands

object RoundResultResolver {
    fun resolve(
        playerHands: PlayerHands,
        dealerHand: Hand,
    ): List<HandOutcome> = playerHands.hands.map { playerHandState ->
        resolveHand(playerHandState, dealerHand)
    }

    private fun resolveHand(
        playerHandState: PlayerHandState,
        dealerHand: Hand,
    ): HandOutcome {
        val playerHand = playerHandState.hand
        val playerHasNatural = playerHand.isNaturalBlackjack && !playerHandState.isFromSplit
        val dealerHasNatural = dealerHand.isNaturalBlackjack

        return when {
            playerHand.isBust -> HandOutcome.DEALER_WIN
            playerHasNatural && dealerHasNatural -> HandOutcome.PUSH
            playerHasNatural -> HandOutcome.PLAYER_BLACKJACK
            dealerHasNatural -> HandOutcome.DEALER_WIN
            dealerHand.isBust -> HandOutcome.PLAYER_WIN
            playerHand.score > dealerHand.score -> HandOutcome.PLAYER_WIN
            playerHand.score < dealerHand.score -> HandOutcome.DEALER_WIN
            else -> HandOutcome.PUSH
        }
    }
}
