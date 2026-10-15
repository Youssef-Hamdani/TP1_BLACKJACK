package com.example.tp1_blackjack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tp1_blackjack.R
import com.example.tp1_blackjack.ui.components.TableSurface

const val PLAYER_NAME_INPUT_TEST_TAG = "player-name-input"
const val CREATE_PLAYER_ACTION_TEST_TAG = "create-player-action"

@Composable
fun PlayerCreationScreen(onCreate: (String) -> Boolean) {
    var name by rememberSaveable { mutableStateOf("") }
    var saveFailed by rememberSaveable { mutableStateOf(false) }

    TableSurface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(color = Color.Black.copy(alpha = 0.72f), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        stringResource(R.string.player_creation_title),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(stringResource(R.string.player_creation_explanation), color = Color.White)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; saveFailed = false },
                        label = { Text(stringResource(R.string.player_creation_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag(PLAYER_NAME_INPUT_TEST_TAG),
                    )
                    if (saveFailed) {
                        Text(stringResource(R.string.player_creation_error), color = Color(0xFFFF8A80))
                    }
                    Button(
                        onClick = { saveFailed = !onCreate(name) },
                        enabled = name.trim().isNotEmpty() && name.trim().length <= 32,
                        modifier = Modifier.align(Alignment.End).testTag(CREATE_PLAYER_ACTION_TEST_TAG),
                    ) {
                        Text(stringResource(R.string.player_creation_action))
                    }
                }
            }
        }
    }
}
