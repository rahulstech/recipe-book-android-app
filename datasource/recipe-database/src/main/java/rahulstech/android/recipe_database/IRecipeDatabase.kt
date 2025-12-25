package rahulstech.android.recipe_database

interface IRecipeDatabase {

    fun runInTransaction(queries: () -> Unit)

    fun <V> runInTransaction(queries: () -> V): V

    val recipeDao: RecipeDao

    val recipeMediaDao: RecipeMediaDao
}