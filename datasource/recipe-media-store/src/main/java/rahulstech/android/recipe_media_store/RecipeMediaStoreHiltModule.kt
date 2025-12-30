package rahulstech.android.recipe_media_store

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RecipeMediaStoreHiltModule {

    @Binds
    @Singleton
    fun bindRecipeMediaStore(impl: RecipeMediaStoreImpl): RecipeMediaStore
}