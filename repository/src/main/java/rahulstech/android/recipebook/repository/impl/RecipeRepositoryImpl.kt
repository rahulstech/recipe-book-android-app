package rahulstech.android.recipebook.repository.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rahulstech.android.recipe_database.IRecipeDatabase
import rahulstech.android.recipe_media_store.RecipeMediaStore
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.RecipeRepositoryException
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.toRecipe
import rahulstech.android.recipebook.repository.model.toRecipeMedia
import java.util.UUID
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    val db: IRecipeDatabase,
    val store: RecipeMediaStore
): RecipeRepository {

    companion object {
        private val TAG = RecipeRepositoryImpl::class.simpleName
    }

    private val recipeDao = db.recipeDao
    private val recipeMediaDao = db.recipeMediaDao

    override suspend fun addRecipe(recipe: Recipe): Recipe = coroutineScope {
        val createdMediaIds = mutableListOf<String>()
        try {
            // create medias
            val coverPhoto = recipe.coverPhoto?.let { uri ->
                val mediaUri = store.create(uri)
                createdMediaIds += RecipeMediaStore.getMediaId(mediaUri)!!
                mediaUri
            }

            val medias = recipe.medias.map { media ->
                val mediaUri = store.create(media.data)
                synchronized(createdMediaIds) {
                    createdMediaIds += RecipeMediaStore.getMediaId(mediaUri)!!
                }
                media.copy(
                    id = UUID.randomUUID().toString(),
                    data = mediaUri
                )
            }

            // insert recipe and recipe medias
            withContext(Dispatchers.IO) {
                db.runInTransaction<Recipe> {

                    val newRecipe = recipe.copy(
                        id = UUID.randomUUID().toString(),
                        coverPhoto = coverPhoto,
                        medias = medias
                    )

                    Log.d(TAG, "newRecipe = $newRecipe")

                    db.recipeDao.insert(newRecipe.toRecipeEntity())

                    if (newRecipe.medias.isNotEmpty()) {
                        db.recipeMediaDao.insertMultiple(newRecipe.medias.map {
                            it.toRecipeMediaEntity(
                                recipeId = newRecipe.id
                            )
                        })
                    }

                    newRecipe
                }
            }
        }
        catch (th: Throwable) {
            Log.e(TAG, "add recipe fail", th)
            // delete medias
            // TODO: explain why NonCancellable
            withContext(NonCancellable + Dispatchers.IO) {
                for (id in createdMediaIds) {
                    store.delete(id)
                }
            }

            throw RecipeRepositoryException.addRecipeFail(recipe)
        }
    }

    override suspend fun editRecipe(recipe: Recipe): Recipe? = coroutineScope {

        val createdMediaIds = mutableListOf<String>()      // rollback only
        val deleteAfterCommit = mutableListOf<String>()    // cleanup only

        try {
            // ---------- load current state ----------
            val recipeId = recipe.id

            val oldRecipe = recipeDao.observeRecipeById(recipeId).first()
                ?: return@coroutineScope null

            val oldMedias = recipeMediaDao
                .observeMediasForRecipe(recipeId)
                .first()

            // ---------- cover photo reconciliation ----------
            val oldCoverId = oldRecipe.coverPhoto
            val newCoverUri = recipe.coverPhoto
            val newCoverId = newCoverUri?.let { RecipeMediaStore.getMediaId(it) }

            val finalCoverId: String? = when {

                // removed
                newCoverUri == null -> {
                    oldCoverId?.let { deleteAfterCommit += it }
                    null
                }

                // replaced with new external uri
                newCoverId == null -> {
                    val uri = store.create(newCoverUri)
                    val id = RecipeMediaStore.getMediaId(uri)!!
                    createdMediaIds += id
                    oldCoverId?.let { deleteAfterCommit += it }
                    id
                }

                // unchanged
                else -> oldCoverId
            }

            val updatedRecipeEntity = recipe.copy(
                coverPhoto = finalCoverId?.let { store.resolve(it) }
            ).toRecipeEntity()

            // ---------- media diff ----------
            val oldById = oldMedias.associateBy { it.id }
            val incomingById = recipe.medias.filter { it.id.isNotBlank() }.associateBy { it.id }

            val toRemove = oldMedias.filter { it.id !in incomingById }
            val toUpdate = recipe.medias
                .filter { it.id in oldById }
                .map { it.toRecipeMediaEntity(recipeId) }

            // default id attribute for new RecipeMedia is blank
            val toAdd = recipe.medias.filter { it.id.isBlank() }

            // mark old media files for deletion AFTER commit
            toRemove.forEach { deleteAfterCommit += it.data }

            // ---------- create new media files ----------
            val newMediaEntities = toAdd.map { media ->
                val uri = store.create(media.data)
                val mediaId = RecipeMediaStore.getMediaId(uri)!!
                createdMediaIds += mediaId
                media.copy(
                    id = UUID.randomUUID().toString(),
                    data = store.resolve(mediaId)
                ).toRecipeMediaEntity(recipeId)
            }

            // ---------- DB transaction ----------
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    if (updatedRecipeEntity != oldRecipe) {
                        recipeDao.update(updatedRecipeEntity)
                    }

                    if (newMediaEntities.isNotEmpty()) {
                        recipeMediaDao.insertMultiple(newMediaEntities)
                    }

                    if (toUpdate.isNotEmpty()) {
                        recipeMediaDao.updateMultiple(toUpdate)
                    }

                    if (toRemove.isNotEmpty()) {
                        recipeMediaDao.deleteMultiple(toRemove)
                    }
                }
            }

            // ---------- delete old files AFTER commit ----------
            withContext(Dispatchers.IO) {
                deleteAfterCommit.forEach { store.delete(it) }
            }

            // ---------- build result ----------
            updatedRecipeEntity.toRecipe(
                coverPhoto = finalCoverId?.let { store.resolve(it) },
                medias = (newMediaEntities + toUpdate).map {
                    it.toRecipeMedia(store.resolve(it.data))
                }
            )
        }
        catch (th: Throwable) {
            Log.e(TAG, "edit recipe fail", th)
            // ---------- rollback ----------
            withContext(NonCancellable + Dispatchers.IO) {
                createdMediaIds.forEach { store.delete(it) }
            }
            throw RecipeRepositoryException.editRecipeFail(recipe)
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllRecipes(): Flow<List<Recipe>> =
        recipeDao.observeAllRecipes()
            .mapLatest { items ->
                items.map { item ->
                    val coverMedia = item.coverPhoto?.let { uri -> store.resolve(uri) }
                    item.toRecipe(coverMedia)
                }
            }

    override fun getRecipeById(id: String): Flow<Recipe?> {
        return combine(
            recipeDao.observeRecipeById(id),
            recipeMediaDao.observeMediasForRecipe(id)
        ) { recipeEntity, mediaEntities ->
            recipeEntity?.let { entity ->
                entity.toRecipe(
                    coverPhoto = recipeEntity.coverPhoto?.let { mediaId -> store.resolve(mediaId) },
                    medias = mediaEntities.map {
                        it.toRecipeMedia(
                            store.resolve(it.data)
                        )
                    }
                )
            }
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe): Boolean = coroutineScope {
        val mediaIds = mutableListOf<String>()
        val recipeId = recipe.id

        // get the recipe and recipe medias
        val recipeEntity = recipeDao.observeRecipeById(recipeId).first() ?: return@coroutineScope true

        val mediaEntities = recipeMediaDao.observeMediasForRecipe(recipeId).first()
        recipeEntity.coverPhoto?.let { mediaIds += it }
        mediaEntities.forEach { mediaIds += it.data }

        try {

            // delete recipe and recipe medias
            launch(Dispatchers.IO) {
                db.runInTransaction {
                    recipeDao.delete(recipeEntity)

                    recipeMediaDao.deleteMultiple(mediaEntities)
                }
            }.join()

            // delete media files
            launch(Dispatchers.IO) {
                for (id in mediaIds) {
                    store.delete(id)
                }
            }
            true
        }
        catch (th: Throwable) {
            Log.e(TAG,"delete recipe fail", th)
            throw RecipeRepositoryException.addRecipeFail(recipe)
        }
    }
}