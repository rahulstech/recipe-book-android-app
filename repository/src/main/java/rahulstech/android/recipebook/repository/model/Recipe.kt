package rahulstech.android.recipebook.repository.model

import android.net.Uri
import rahulstech.android.recipe_database.RecipeEntity
import rahulstech.android.recipe_database.RecipeItem
import rahulstech.android.recipe_media_store.RecipeMediaStore
import rahulstech.android.recipebook.repository.RecipeRepositoryException

data class Recipe(
    val id: String = "",
    val title: String,
    val coverPhoto: Uri? = null,
    val note: String = "",
    val ingredients: String = "",
    val steps: String = "",
    val medias: List<RecipeMedia> = emptyList(),
) {
    fun toRecipeEntity(): RecipeEntity =
        RecipeEntity(
            id = id,
            title = title,
            note = note,
            ingredients = ingredients,
            steps = steps,
            coverPhoto = coverPhoto?.let { uri ->
                    val mediaId = RecipeMediaStore.getMediaId(uri)
                    if (null == mediaId) {
                        throw RecipeRepositoryException.errorGetMediaId(uri)
                    }
                    mediaId
                }
        )
}

fun RecipeEntity.toRecipe(coverPhoto: Uri? = null, medias: List<RecipeMedia> = emptyList()): Recipe =
    Recipe(
        id = id,
        title = title,
        note = note ?: "",
        ingredients = ingredients ?: "",
        steps = steps ?: "",
        coverPhoto = coverPhoto,
        medias = medias
    )

fun RecipeItem.toRecipe(coverMedia: Uri? = null): Recipe =
    Recipe(
        id = id,
        title = title,
        note = note ?: "",
        coverPhoto = coverMedia
    )