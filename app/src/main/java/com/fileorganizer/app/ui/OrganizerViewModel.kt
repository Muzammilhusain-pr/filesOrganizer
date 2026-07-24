package com.fileorganizer.app.ui

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.fileorganizer.app.model.FileItem
import com.fileorganizer.app.model.TargetRow
import java.io.File

/**
 * Holds all state for the Organizer screen:
 *  - the source folder currently being browsed (left pane)
 *  - the current multi-selection of files
 *  - the target folder tree used as move destinations (right pane)
 *
 * NOTE: file listing/move operations run synchronously on the main thread for
 * simplicity. For folders with very large file counts, move listDir() and
 * moveSelectionTo() onto a background coroutine (Dispatchers.IO) and expose a
 * loading state instead — flagged here rather than guessed at, since the
 * right threading approach depends on real-world folder sizes you test with.
 */
class OrganizerViewModel : ViewModel() {

    private val storageRoot: File = Environment.getExternalStorageDirectory()

    var sourceDir by mutableStateOf(storageRoot)
        private set

    var sourceFiles by mutableStateOf<List<FileItem>>(emptyList())
        private set

    var selectedPaths by mutableStateOf<Set<String>>(emptySet())
        private set

    var expandedTargetPaths by mutableStateOf<Set<String>>(emptySet())
        private set

    /** Bumped on every move operation so the UI can trigger a Snackbar even if the count repeats. */
    var operationId by mutableStateOf(0)
        private set

    var lastMoveCount by mutableStateOf(0)
        private set

    var lastError by mutableStateOf<String?>(null)
        private set

    init {
        refreshSource()
    }

    private fun listDir(dir: File): List<FileItem> {
        val entries = dir.listFiles() ?: return emptyList()
        return entries
            .filter { !it.name.startsWith(".") }
            .sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
            .map { FileItem(it) }
    }

    fun refreshSource() {
        sourceFiles = listDir(sourceDir)
        // Drop any selected paths that no longer exist in the current source listing.
        selectedPaths = selectedPaths.filter { path -> sourceFiles.any { it.file.path == path } }.toSet()
    }

    /** Organizer Mode: tapping a folder in the SOURCE pane navigates into it. */
    fun openSourceFolder(item: FileItem) {
        if (!item.isDirectory) return
        sourceDir = item.file
        selectedPaths = emptySet()
        refreshSource()
    }

    fun navigateSourceUp() {
        val parent = sourceDir.parentFile ?: return
        sourceDir = parent
        selectedPaths = emptySet()
        refreshSource()
    }

    /** Organizer Mode: tapping a FILE in the source pane selects/deselects it — never opens it. */
    fun toggleSelect(item: FileItem) {
        if (item.isDirectory) return
        selectedPaths = if (selectedPaths.contains(item.file.path)) {
            selectedPaths - item.file.path
        } else {
            selectedPaths + item.file.path
        }
    }

    fun clearSelection() {
        selectedPaths = emptySet()
    }

    /** Expand/collapse a folder in the TARGET tree — separate from the move action. */
    fun toggleTargetExpand(item: FileItem) {
        val path = item.file.path
        expandedTargetPaths = if (expandedTargetPaths.contains(path)) {
            expandedTargetPaths - path
        } else {
            expandedTargetPaths + path
        }
    }

    /** Flattens the target tree (from device storage root) into visible rows for a LazyColumn. */
    fun visibleTargetRows(): List<TargetRow> {
        val rows = mutableListOf<TargetRow>()
        fun addChildren(dir: File, depth: Int) {
            for (child in listDir(dir).filter { it.isDirectory }) {
                rows.add(TargetRow(child, depth))
                if (expandedTargetPaths.contains(child.file.path)) {
                    addChildren(child.file, depth + 1)
                }
            }
        }
        addChildren(storageRoot, 0)
        return rows
    }

    /**
     * Organizer Mode's core action: tapping a target folder row moves every
     * currently selected file there in one batch, immediately, with no dialog.
     * The source pane stays exactly where it was.
     */
    fun moveSelectionTo(destination: File) {
        if (selectedPaths.isEmpty()) return

        var moved = 0
        var failed = 0
        val filesToMove = sourceFiles.filter { selectedPaths.contains(it.file.path) }

        for (item in filesToMove) {
            val target = File(destination, item.file.name)
            val ok = try {
                when {
                    target.exists() -> false
                    item.file.renameTo(target) -> true
                    else -> {
                        // renameTo fails across different storage volumes — fall back to copy+delete.
                        item.file.copyTo(target, overwrite = false)
                        item.file.delete()
                        true
                    }
                }
            } catch (e: Exception) {
                false
            }
            if (ok) moved++ else failed++
        }

        lastMoveCount = moved
        lastError = if (failed > 0) "$failed file(s) skipped (name conflict or permission)." else null
        operationId++
        selectedPaths = emptySet()
        refreshSource()
    }
}
