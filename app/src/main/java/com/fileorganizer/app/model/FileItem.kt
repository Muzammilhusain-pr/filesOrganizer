package com.fileorganizer.app.model

import java.io.File

/**
 * Lightweight wrapper around [File] used throughout the UI layer.
 */
data class FileItem(val file: File) {
    val name: String get() = file.name
    val isDirectory: Boolean get() = file.isDirectory
    val sizeBytes: Long get() = if (file.isDirectory) 0L else file.length()
}
