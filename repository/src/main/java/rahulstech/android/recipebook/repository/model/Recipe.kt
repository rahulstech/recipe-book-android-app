package rahulstech.android.recipebook.repository.model

import android.net.Uri

data class Recipe(
    val id: String = "",
    val title: String,
    val coverPhoto: Uri? = null,
    val note: String? = null,
    val ingredients: String? = null,
    val steps: String? = null,
    val medias: List<RecipeMedia> = emptyList(),
)