package rahulstech.android.recipebook.repository

import rahulstech.android.recipebook.repository.impl.RecipeRepositoryImpl

object Repositories {

    private var _recipeRepo: RecipeRepository? = null

    val recipeRepository: RecipeRepository
        get() {
            if (null == _recipeRepo) {
                _recipeRepo = RecipeRepositoryImpl()
            }
            return _recipeRepo!!
        }


}