package com.fileorganizer.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fileorganizer.app.model.FileItem

@Composable
fun SourcePane(viewModel: OrganizerViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        PaneHeader(
            title = "Source",
            path = viewModel.sourceDir.path,
            onUp = { viewModel.navigateSourceUp() }
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.sourceFiles, key = { it.file.path }) { item ->
                val selected = viewModel.selectedPaths.contains(item.file.path)
                FileRow(
                    item = item,
                    selected = selected,
                    onClick = {
                        // Organizer Mode: folders navigate, files select — never open on a single tap.
                        if (item.isDirectory) {
                            viewModel.openSourceFolder(item)
                        } else {
                            viewModel.toggleSelect(item)
                        }
                    },
                    onLongClick = {
                        // Reserved for preview only (per spec). Wire up a viewer/share
                        // intent here, e.g. via FileProvider + ACTION_VIEW.
                    }
                )
            }
        }
    }
}

@Composable
fun PaneHeader(title: String, path: String, onUp: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onUp) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Up one folder")
        }
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(path, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRow(
    item: FileItem,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(item.name, maxLines = 1)
    }
}
