package com.fileorganizer.app.model

/**
 * One visible row in the target-folder tree.
 * [depth] is used purely for indentation in the UI.
 */
data class TargetRow(val item: FileItem, val depth: Int)
