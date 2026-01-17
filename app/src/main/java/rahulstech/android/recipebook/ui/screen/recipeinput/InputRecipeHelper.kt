package rahulstech.android.recipebook.ui.screen.recipeinput

import android.net.Uri
import rahulstech.android.recipebook.repository.RecipeRepository.Companion.MAX_RECIPE_MEDIAS
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia
import java.util.UUID

data class InputRecipeState(
    val recipe: Recipe = Recipe(title = ""),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showCoverPhotoOptionsDialog: Boolean = false,
    val showMediaDialog: Boolean = false,
    val selectedMedia: RecipeMedia? = null,
) {

    val canSave: Boolean get() = !isSaving && recipe.title.isNotBlank()

    fun updateCoverPhoto(coverPhoto: Uri?): Recipe = recipe.copy(coverPhoto = coverPhoto)

    fun updateTitle(title: String): Recipe = recipe.copy(title = title)

    fun updateNote(note: String): Recipe = recipe.copy(note = note)

    fun updateIngredients(ingredients: String): Recipe = recipe.copy(ingredients = ingredients)

    fun updateSteps(steps: String): Recipe = recipe.copy(steps = steps)

    fun addMedias(uris: List<Uri>): Recipe {
        val allowed = MAX_RECIPE_MEDIAS - recipe.medias.size
        val medias = uris.take(allowed).map {
            RecipeMedia(data = it)
        }
        val newMedias = recipe.medias + medias
        return recipe.copy(medias = newMedias)
    }

    fun editMedia(media: RecipeMedia): Recipe {
        val newMedias = recipe.medias.map { old ->
            if (old.id == media.id) media else old
        }
        return  recipe.copy(medias = newMedias)
    }

    fun removeMedia(media: RecipeMedia): Recipe {
        val newMedias = recipe.medias - media
        return recipe.copy(medias = newMedias)
    }
}

sealed interface InputRecipeEvent {

    data class SaveRecipeEvent(
        val recipe: Recipe,
        val isEdit: Boolean = false
    ): InputRecipeEvent

    data class UpdateRecipeEvent(val recipe: Recipe): InputRecipeEvent

    data class MediaClickEvent(val media: RecipeMedia): InputRecipeEvent

    data object CoverPhotoClickEvent: InputRecipeEvent
}