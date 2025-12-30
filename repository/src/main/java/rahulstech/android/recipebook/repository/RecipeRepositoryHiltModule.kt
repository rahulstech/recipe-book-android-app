package rahulstech.android.recipebook.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rahulstech.android.recipebook.repository.impl.RecipeRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RecipeRepositoryHiltModule {

    @Binds
    @Singleton
    fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository
}