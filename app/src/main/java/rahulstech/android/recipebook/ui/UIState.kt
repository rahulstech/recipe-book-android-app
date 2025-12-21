package rahulstech.android.recipebook.ui

sealed interface UIState<out T> {

    class Idle(): UIState<Nothing>

    class Loading(): UIState<Nothing>

    class Success<T>(val data: T): UIState<T>

    class Empty<T>(val placeholder: T? = null): UIState<T>

    class NotFound(): UIState<Nothing>

    class Error(val cause: Throwable): UIState<Nothing>
}

