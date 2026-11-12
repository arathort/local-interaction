package com.arathort.localinteraction.data.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultipartFileHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun isLocalUri(uriString: String): Boolean {
        val trimmed = uriString.trim()
        return trimmed.startsWith("content://", ignoreCase = true) ||
            trimmed.startsWith("file://", ignoreCase = true)
    }

    fun createPartFromUri(partName: String, uriString: String, index: Int = 0): MultipartBody.Part? =
        runCatching {
            val uri = Uri.parse(uriString)
            val contentResolver = context.contentResolver
            val mimeType = when (uri.scheme?.lowercase()) {
                "content" -> contentResolver.getType(uri) ?: "image/jpeg"
                "file" -> {
                    val ext = File(uri.path.orEmpty()).extension.lowercase()
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "image/jpeg"
                }
                else -> "image/jpeg"
            }

            val bytes = when (uri.scheme?.lowercase()) {
                "content" -> contentResolver.openInputStream(uri)?.use { it.readBytes() }
                "file" -> uri.path?.let { File(it).takeIf(File::exists)?.readBytes() }
                else -> null
            } ?: return null

            if (bytes.isEmpty()) return null

            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            val fileName = "${partName}_${System.currentTimeMillis()}_$index.$extension"
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, fileName, requestBody)
        }.getOrNull()

    fun createPartsFromUris(partName: String, uriStrings: List<String>): List<MultipartBody.Part> =
        uriStrings
            .filter(::isLocalUri)
            .mapIndexedNotNull { index, uriString ->
                createPartFromUri(partName = partName, uriString = uriString, index = index)
            }
}
