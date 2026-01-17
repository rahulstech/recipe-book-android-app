package rahulstech.android.recipebook.repository

import android.net.Uri
import rahulstech.android.recipebook.repository.model.Recipe

class RecipeRepositoryException(message: String? = null,
                                cause: Throwable? = null):
    Exception(message,cause)
{
    companion object {

        fun errorGetMediaId(uri: Uri): RecipeRepositoryException =
            RecipeRepositoryException("can not get media id from media uri $uri")

        fun addRecipeFail(recipe: Recipe, cause: Throwable?): RecipeRepositoryException =
            RecipeRepositoryException("fail to add recipe $recipe", cause)

        fun editRecipeFail(recipe: Recipe, cause: Throwable?): RecipeRepositoryException =
            RecipeRepositoryException("fail to edit recipe $recipe",cause)

        fun deleteRecipeFail(recipe: Recipe, cause: Throwable?): RecipeRepositoryException =
            RecipeRepositoryException("fail to delete recipe $recipe", cause)
    }
}

