package rahulstech.android.recipebook.ui.theme

import android.graphics.drawable.Drawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toDrawable

val ShimmerColor: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

val ShimmerDrawable: Drawable @Composable get() = ShimmerColor.toArgb().toDrawable()

val ThemeTopBarColors @Composable get() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    scrolledContainerColor = MaterialTheme.colorScheme.primary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
)