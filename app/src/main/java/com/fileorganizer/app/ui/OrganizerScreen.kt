package com.fileorganizer.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Fixed 50/50 split screen: SOURCE on the left (what you're organizing),
 * TARGET tree on the right (where files go). Nothing here ever navigates
 * away from the source folder when a move happens.
 */
@Composable
fun OrganizerScreen(viewModel: OrganizerViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.operationId) {
        if (viewModel.operationId == 0) return@LaunchedEffect
        val base = "Moved ${viewModel.lastMoveCount} file(s)"
        val message = viewModel.lastError?.let { "$base — $it" } ?: base
        snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (viewModel.selectedPaths.isNotEmpty()) {
                SelectionBar(
                    count = viewModel.selectedPaths.size,
                    onClear = { viewModel.clearSelection() }
                )
            }
            Row(modifier = Modifier.weight(1f)) {
                SourcePane(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                TargetPane(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun SelectionBar(count: Int, onClear: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$count file(s) selected — tap a folder on the right to move")
            TextButton(onClick = onClear) { Text("Clear") }
        }
    }
}
