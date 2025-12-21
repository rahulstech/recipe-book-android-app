package rahulstech.android.recipebook

import androidx.compose.material3.SnackbarDuration

data class SnackBarAction(
    val label: String,
    val onAction: ()-> Unit = {},
    val canDismiss: Boolean = true,
    val onDismiss: () -> Unit = {},
)

data class SnackBarEvent(
    val message: String,
    val action: SnackBarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Long
)