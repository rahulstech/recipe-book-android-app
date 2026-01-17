package rahulstech.android.recipe_database

interface IRecipeDatabase {

    suspend fun <V> runInTransaction(queries: suspend () -> V): V

    val recipeDao: RecipeDao

    val recipeMediaDao: RecipeMediaDao
}