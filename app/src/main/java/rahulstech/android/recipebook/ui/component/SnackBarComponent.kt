package rahulstech.android.recipebook.ui.component

data class SnackBarEvent(
    val message: String,
    val hasAction: Boolean = false,
    val actionLabel: String = "",
    val onAction: () -> Unit = {}
)

typealias SnackBarCallback = (SnackBarEvent) -> Unit