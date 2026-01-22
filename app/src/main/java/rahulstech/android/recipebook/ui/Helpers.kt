package rahulstech.android.recipebook.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import rahulstech.android.recipebook.NavigationEvent

@Composable
fun Dp.toRoundPx(): Int = with(LocalDensity.current) { roundToPx() }

sealed interface UIState<out T> {

    data object Idle: UIState<Nothing>

    data object Loading: UIState<Nothing>

    data class Success<T>(val data: T): UIState<T>

    data object NotFound: UIState<Nothing>

    data class Error(val cause: Throwable): UIState<Nothing>
}

sealed interface UIEffect {

    data class ShowSnackBar(@StringRes val messageResId: Int, val args: List<Any> = emptyList(), val onAction: ()-> Unit = {}): UIEffect

    data object Exit: UIEffect

    data class NavigateTo(val event: NavigationEvent): UIEffect
}