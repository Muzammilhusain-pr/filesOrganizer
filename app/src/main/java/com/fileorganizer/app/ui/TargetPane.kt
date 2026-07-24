package com.fileorganizer.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fileorganizer.app.model.TargetRow

@Composable
fun TargetPane(viewModel: OrganizerViewModel, modifier: Modifier = Modifier) {
    val rows = viewModel.visibleTargetRows()
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text("Target folders", style = MaterialTheme.typography.labelMedium)
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(rows, key = { it.item.file.path }) { row ->
                TargetRowView(
                    row = row,
                    expanded = viewModel.expandedTargetPaths.contains(row.item.file.path),
                    onToggleExpand = { viewModel.toggleTargetExpand(row.item) },
                    onMoveHere = { viewModel.moveSelectionTo(row.item.file) }
                )
            }
        }
    }
}

@Composable
private fun TargetRowView(
    row: TargetRow,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onMoveHere: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (12 + row.depth * 20).dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expand/collapse is a distinct control from the move action below,
        // so browsing the tree never accidentally triggers a move.
        IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        // Tapping the folder name/row itself is the "move selection here" action —
        // immediate, no confirmation dialog, per the Organizer Mode spec.
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onMoveHere)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(row.item.name, maxLines = 1)
        }
    }
}
