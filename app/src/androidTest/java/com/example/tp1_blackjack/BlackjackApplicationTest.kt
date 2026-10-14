package com.example.tp1_blackjack

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlackjackApplicationTest {
    @Test
    fun applicationContext_usesBlackjackApplication() {
        val application = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .applicationContext

        assertTrue(application is BlackjackApplication)
        application as BlackjackApplication
        assertSame(application.repository, application.repository)
    }
}
