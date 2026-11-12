package com.arathort.localinteraction.presentation.components

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import com.arathort.localinteraction.R
import com.arathort.localinteraction.presentation.util.Dimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object AppImageMemoryCache {
    private const val BASE_BACKEND_URL = "https://alloy-r7qa.onrender.com"
    private const val MAX_ENTRIES = 60

    private val cache = object : LinkedHashMap<String, ImageBitmap>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean =
            size > MAX_ENTRIES
    }

    fun resolveUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return when {
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("content://", ignoreCase = true) ||
                trimmed.startsWith("file://", ignoreCase = true) -> trimmed
            trimmed.startsWith("/") -> "$BASE_BACKEND_URL$trimmed"
            else -> "$BASE_BACKEND_URL/$trimmed"
        }
    }

    @Synchronized
    fun get(key: String): ImageBitmap? = cache.get(key)

    @Synchronized
    fun put(key: String, bitmap: ImageBitmap) {
        cache.put(key, bitmap)
    }

    @Synchronized
    fun invalidate(rawUrl: String?) {
        if (rawUrl.isNullOrBlank()) return
        val resolved = resolveUrl(rawUrl)
        cache.remove(rawUrl)
        cache.remove(resolved)
    }
}

@Composable
fun AppAsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: (@Composable () -> Unit)? = null,
    fallback: (@Composable () -> Unit)? = null
) {
    val isPreview = LocalInspectionMode.current
    val trimmedModel = model?.trim().orEmpty()

    if (trimmedModel.isBlank() || isPreview) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            if (fallback != null) {
                fallback()
            } else {
                DefaultImageFallback()
            }
        }
        return
    }

    val resolvedKey = remember(trimmedModel) { AppImageMemoryCache.resolveUrl(trimmedModel) }
    var bitmap by remember(resolvedKey) { mutableStateOf(AppImageMemoryCache.get(resolvedKey)) }
    var isLoading by remember(resolvedKey) { mutableStateOf(bitmap == null) }
    var hasFailed by remember(resolvedKey) { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(resolvedKey) {
        val cached = AppImageMemoryCache.get(resolvedKey)
        if (cached != null) {
            bitmap = cached
            isLoading = false
            hasFailed = false
            return@LaunchedEffect
        }

        isLoading = true
        hasFailed = false

        val loadedBitmap = withContext(Dispatchers.IO) {
            runCatching {
                when {
                    resolvedKey.startsWith("content://", ignoreCase = true) -> {
                        val uri = Uri.parse(resolvedKey)
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                            val maxDim = maxOf(info.size.width, info.size.height)
                            if (maxDim > 1600) {
                                decoder.setTargetSampleSize(maxDim / 800)
                            }
                        }.asImageBitmap()
                    }
                    resolvedKey.startsWith("file://", ignoreCase = true) -> {
                        val uri = Uri.parse(resolvedKey)
                        val file = File(uri.path.orEmpty())
                        if (file.exists()) {
                            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                        } else {
                            null
                        }
                    }
                    else -> {
                        val connection = (URL(resolvedKey).openConnection() as HttpURLConnection).apply {
                            connectTimeout = 10_000
                            readTimeout = 10_000
                            instanceFollowRedirects = true
                            doInput = true
                        }
                        try {
                            connection.connect()
                            if (connection.responseCode in 200..299) {
                                val bytes = connection.inputStream.use { it.readBytes() }
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } else {
                                null
                            }
                        } finally {
                            connection.disconnect()
                        }
                    }
                }
            }.getOrNull()
        }

        if (loadedBitmap != null) {
            AppImageMemoryCache.put(resolvedKey, loadedBitmap)
            bitmap = loadedBitmap
            isLoading = false
            hasFailed = false
        } else {
            isLoading = false
            hasFailed = true
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            isLoading -> {
                if (placeholder != null) {
                    placeholder()
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.progressIndicatorSize),
                        strokeWidth = Dimensions.progressIndicatorStrokeWidth
                    )
                }
            }
            hasFailed -> {
                if (fallback != null) {
                    fallback()
                } else {
                    DefaultImageFallback()
                }
            }
        }
    }
}

@Composable
private fun DefaultImageFallback() {
    Icon(
        painter = painterResource(R.drawable.ic_photo_library),
        contentDescription = null,
        modifier = Modifier.size(Dimensions.iconMedium),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
