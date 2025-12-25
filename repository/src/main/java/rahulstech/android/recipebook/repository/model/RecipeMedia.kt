package rahulstech.android.recipebook.repository.model

import android.net.Uri
import rahulstech.android.recipe_database.RecipeMediaEntity
import rahulstech.android.recipe_media_store.RecipeMediaStore
import rahulstech.android.recipebook.repository.RecipeRepositoryException

data class RecipeMedia(
    val id: String = "",
    val data: Uri,
    val caption: String? = null,
){

    fun toRecipeMediaEntity(recipeId: String = ""): RecipeMediaEntity =
        RecipeMediaEntity(
            id = id,
            recipeId = recipeId,
            data = run {
                val mediaId = RecipeMediaStore.getMediaId(data)
                if (mediaId == null) {
                    throw RecipeRepositoryException.errorGetMediaId(data)
                }
                mediaId
            },
            caption = caption
        )
}

fun RecipeMediaEntity.toRecipeMedia(data: Uri): RecipeMedia =
    RecipeMedia(
        id = id,
        data = data,
        caption = caption
    )
