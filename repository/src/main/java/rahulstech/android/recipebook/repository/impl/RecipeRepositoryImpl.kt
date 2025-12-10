package rahulstech.android.recipebook.repository.impl

import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.model.Recipe
import java.util.UUID

class RecipeRepositoryImpl: RecipeRepository {


    private val recipes = mutableMapOf(
        "recipe1" to Recipe(
            id = "recipe1",
            title = "First Recipe",
            note = "This is my first recipe",
            coverPhoto = "file:///android_asset/recipe1.jpg".toUri()
        ),

        "recipe2" to Recipe(
            id = "recipe2",
            title = "Second Recipe",
            note = "This is my second recipe",
            coverPhoto = "file:///android_asset/recipe2.jpg".toUri()
        ),

        "recipe3" to Recipe(
            id = "recipe3",
            title = "Third Recipe",
            note = "This is my third recipe",
            coverPhoto = "file:///android_asset/recipe3.jpg".toUri()
        ),

        "recipe4" to Recipe(
            id = "recipe4",
            title = "Fourth Recipe",
            note = "This is my fourth recipe",
            coverPhoto = "file:///android_asset/recipe4.jpg".toUri()
        ),
    )

    override fun addRecipe(recipe: Recipe): Recipe {
        val copy = recipe.copy(id = UUID.randomUUID().toString())
        recipes[copy.id] = copy
        return copy
    }

    override fun editRecipe(recipe: Recipe): Recipe? {
        if (recipes.containsKey(recipe.id)) {
            recipes[recipe.id] = recipe
            return recipe
        }
        return null
    }

    override fun getAllRecipes(): Flow<List<Recipe>> = flowOf(recipes.values.toList())

    override fun getRecipeById(id: String): Flow<Recipe?> = flowOf(recipes[id])

    override fun deleteRecipe(recipe: Recipe): Boolean = recipes.remove(recipe.id, recipe)
}