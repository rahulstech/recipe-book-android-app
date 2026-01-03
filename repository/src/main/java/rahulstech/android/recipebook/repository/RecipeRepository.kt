package rahulstech.android.recipebook.repository

import kotlinx.coroutines.flow.Flow
import rahulstech.android.recipebook.repository.model.Recipe

interface RecipeRepository {

    companion object {
        const val MAX_RECIPE_MEDIAS = 10
    }

    suspend fun addRecipe(recipe: Recipe): Recipe

    suspend fun editRecipe(recipe: Recipe): Recipe?

    fun getAllRecipes(): Flow<List<Recipe>>

    fun getRecipeById(id: String): Flow<Recipe?>

    suspend fun deleteRecipe(recipe: Recipe): Boolean
}