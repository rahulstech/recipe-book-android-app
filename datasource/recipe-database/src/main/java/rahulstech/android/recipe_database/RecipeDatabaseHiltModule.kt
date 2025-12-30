package rahulstech.android.recipe_database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecipeDatabaseHiltModule {

    @Provides
    @Singleton
    fun getRecipeDatabase(@ApplicationContext context: Context): IRecipeDatabase =
        RecipeDatabase.getInstance(context)
}