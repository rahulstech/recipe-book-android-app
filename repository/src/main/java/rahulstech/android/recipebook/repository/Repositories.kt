package rahulstech.android.recipebook.repository

import android.content.Context
import rahulstech.android.recipe_database.RecipeDatabase
import rahulstech.android.recipe_media_store.RecipeMediaStoreImpl
import rahulstech.android.recipebook.repository.impl.RecipeRepositoryImpl

object Repositories {

    private var _recipeRepo: RecipeRepository? = null

//    val recipeRepository: RecipeRepository
//        get() {
//            if (null == _recipeRepo) {
//                _recipeRepo = RecipeRepositoryImpl()
//            }
//            return _recipeRepo!!
//        }

    fun getRepository(context: Context): RecipeRepository {
        if (null == _recipeRepo) {
            val db = RecipeDatabase.getInstance(context)
            val store = RecipeMediaStoreImpl(context)
            _recipeRepo = RecipeRepositoryImpl(db,store)
        }
        return _recipeRepo!!
    }


}