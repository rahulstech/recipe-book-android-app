package rahulstech.android.recipebook.repository.model

import android.net.Uri

data class RecipeMedia(
    val id: String = "",
    val data: Uri,
    val caption: String? = null,
)
