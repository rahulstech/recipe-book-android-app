package rahulstech.android.recipebook.repository.impl

import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia
import java.util.UUID

class RecipeRepositoryImpl: RecipeRepository {


    private val recipes = mutableMapOf(
        "recipe1" to Recipe(
            id = "recipe1",
            title = "First Recipe",
            note = "This is my first recipe",
            coverPhoto = "file:///android_asset/recipe1.jpg".toUri(),
            ingredients = "1. ingredient 1 of recipe 1\n" +
                        "2. ingredient 2 of recipe 1\n" +
                        "3. ingredient 3 of recipe 1\n" +
                        "4. ingredient 4 of recipe 1\n" +
                        "5. ingredient 5 of recipe 1\n" +
                        "6. ingredient 6 of recipe 1\n" +
                        "7. ingredient 7 of recipe 1\n",
            steps = "1. step 1 of recipe 1\n" +
                    "2. step 2 of recipe 1\n" +
                    "3. step 3 of recipe 1\n" +
                    "4. step 4 of recipe 1\n" +
                    "5. step 5 of recipe 1\n" +
                    "6. step 6 of recipe 1\n" +
                    "7. step 7 of recipe 1\n",
            medias = listOf(
                RecipeMedia( id = "r1m1", data = "file:///android_asset/recipe1.jpg".toUri(), caption = "first media of recipe 1"),
                RecipeMedia( id = "r1m2", data = "file:///android_asset/recipe2.jpg".toUri(), caption = "second media of recipe 1"),
            )
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
            coverPhoto = "file:///android_asset/recipe3.jpg".toUri(),
            medias = listOf(
                RecipeMedia( id = "r3m1", data = "file:///android_asset/recipe3.jpg".toUri(), caption = "first media of recipe 3"),
                RecipeMedia( id = "r3m2", data = "file:///android_asset/recipe1.jpg".toUri(), caption = "second media of recipe 3"),
                RecipeMedia( id = "r3m3", data = "file:///android_asset/recipe4.jpg".toUri(), caption = "third media of recipe 3")
            )
        ),

        "recipe4" to Recipe(
            id = "recipe4",
            title = "Fourth Recipe",
            note = "This is my fourth recipe",
            coverPhoto = "file:///android_asset/recipe4.jpg".toUri(),
            ingredients = "1. ingredient 1 of recipe 1\n" +
                    "2. ingredient 2 of recipe 4\n" +
                    "3. ingredient 3 of recipe 4\n" +
                    "4. ingredient 4 of recipe 4\n" +
                    "5. ingredient 5 of recipe 4\n",
            steps = "1. step 1 of recipe 4\n" +
                    "2. step 2 of recipe 4\n" +
                    "3. step 3 of recipe 4\n" +
                    "4. step 4 of recipe 4\n" +
                    "5. step 5 of recipe 4\n" +
                    "6. step 6 of recipe 4\n",
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

    override fun getAllRecipes(): Flow<List<Recipe>> =
        flowOf(recipes.values.toList())
            .flowOn(Dispatchers.IO)

    override fun getRecipeById(id: String): Flow<Recipe?> =
        flowOf(recipes[id])
            .flowOn(Dispatchers.IO)

    override fun deleteRecipe(recipe: Recipe): Boolean = recipes.remove(recipe.id, recipe)
}