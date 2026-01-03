package rahulstech.android.recipebook.ui.screen

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia

fun fakeRecipes(count: Int = 20): List<Recipe> =
    (1..count).map { index ->
        Recipe(
            id = index.toString(),
            title = "Recipe $index",
            note = "Note for recipe $index",
            coverPhoto = "file:///$index.jpg".toUri() // DO NOT USE HTTP URL
        )
    }

fun fakeRecipe(
    id: String = "",
    title: String = "",
    note: String = "",
    ingredients: String = "",
    steps: String = "",
    medias: List<RecipeMedia> = emptyList(),
    coverPhoto: Uri? = null,
    ): Recipe =
    Recipe(
        id = id,
        title = title,
        note = note,
        ingredients = ingredients,
        steps =  steps,
        medias = medias
    )

class RecipeRepositoryTestImpl: RecipeRepository {

    private val recipeByIdFlow = MutableStateFlow<Recipe?>(null)

    fun emit_getRecipeById(recipe: Recipe?) {
        recipeByIdFlow.value = recipe
    }

    override suspend fun addRecipe(recipe: Recipe): Recipe = recipe

    override suspend fun editRecipe(recipe: Recipe): Recipe? = recipe

    override fun getAllRecipes(): Flow<List<Recipe>> = emptyFlow()

    override fun getRecipeById(id: String): Flow<Recipe?> = recipeByIdFlow

    override suspend fun deleteRecipe(recipe: Recipe): Boolean = false
}