package rahulstech.android.recipebook.ui.component

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import rahulstech.android.recipebook.ui.theme.ShimmerColor

@Composable
fun Modifier.shimmer(color: Color = ShimmerColor) = background(color)