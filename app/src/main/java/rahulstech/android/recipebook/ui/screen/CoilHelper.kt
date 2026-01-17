package rahulstech.android.recipebook.ui.screen

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.request.ImageRequest
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.ui.theme.ShimmerDrawable
import rahulstech.android.recipebook.ui.toRoundPx

fun appImageRequestBuilder(context: Context, data: Any?, placeholderDrawable: Drawable? = null): ImageRequest.Builder {
    return ImageRequest.Builder(context)
        .crossfade(true)
        .placeholder(placeholderDrawable) // show when loading
        .fallback(R.drawable.empty_image) // show when data == null
        .error(R.drawable.empty_image) // show when error loading
        .data(data)
}

@Composable
fun rememberAppImageRequest(data: Any?, size: Dp = 0.dp, lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current): ImageRequest {
    val context = LocalContext.current
    val sizePx = size.toRoundPx()
    val placeholderDrawable: Drawable = ShimmerDrawable
    val request = remember(data) {
        appImageRequestBuilder(context, data, placeholderDrawable).apply {
            if (sizePx > 0) { size(sizePx) }

        }
            .lifecycle(lifecycleOwner)
            .build()
    }
    return request
}