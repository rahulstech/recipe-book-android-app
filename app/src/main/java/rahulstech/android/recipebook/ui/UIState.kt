package rahulstech.android.recipebook.ui

sealed interface UIState<T> {

    class Loading(): UIState<Nothing>

    class Success<T>(val data: T): UIState<T>

    class NotFound(): UIState<Nothing>

    class Error(val cause: Throwable): UIState<Nothing>
}

