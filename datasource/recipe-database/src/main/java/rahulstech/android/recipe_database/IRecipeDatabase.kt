package rahulstech.android.recipe_database

interface IRecipeDatabase {

    fun runInTransaction(queries: () -> Unit)

    val recipeDao: RecipeDao

    val recipeMediaDao: RecipeMediaDao
}