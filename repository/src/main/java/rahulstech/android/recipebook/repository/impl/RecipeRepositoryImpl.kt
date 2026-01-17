package rahulstech.android.recipebook.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
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

private const val TAG = "RecipeRepositoryImpl"

class RecipeRepositoryImpl @Inject constructor(
    val db: IRecipeDatabase,
    val store: RecipeMediaStore
): RecipeRepository {

    private val recipeDao = db.recipeDao

    private val recipeMediaDao = db.recipeMediaDao

    override suspend fun addRecipe(recipe: Recipe): Recipe = db.runInTransaction {
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
                createdMediaIds += RecipeMediaStore.getMediaId(mediaUri)!!
                media.copy(
                    id = UUID.randomUUID().toString(),
                    data = mediaUri
                )
            }

            // insert recipe and recipe medias
            val newRecipe = recipe.copy(
                id = UUID.randomUUID().toString(),
                coverPhoto = coverPhoto,
                medias = medias
            )

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
        catch (cause: Throwable) {
            // delete medias
            // TODO: explain why NonCancellable
            withContext(NonCancellable + Dispatchers.IO) {
                for (id in createdMediaIds) {
                    store.delete(id)
                }
            }
            throw RecipeRepositoryException.addRecipeFail(recipe,cause)
        }
    }

    override suspend fun editRecipe(recipe: Recipe): Recipe? = db.runInTransaction {
        val createdMediaIds = mutableListOf<String>()      // rollback only
        val deleteAfterCommit = mutableListOf<String>()    // cleanup only
        try {
            // ---------- load current state ----------
            val recipeId = recipe.id

            val oldRecipe = recipeDao.observeRecipeById(recipeId).first() ?: return@runInTransaction null

            val oldMedias = recipeMediaDao.observeMediasForRecipe(recipeId).first()

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

            // ---------- delete old files AFTER commit ----------
            deleteAfterCommit.forEach { store.delete(it) }

            // ---------- build result ----------
            updatedRecipeEntity.toRecipe(
                coverPhoto = finalCoverId?.let { store.resolve(it) },
                medias = (newMediaEntities + toUpdate).map {
                    it.toRecipeMedia(store.resolve(it.data))
                }
            )
        }
        catch (cause: Throwable) {
            // ---------- rollback ----------
            withContext(NonCancellable + Dispatchers.IO) {
                createdMediaIds.forEach { store.delete(it) }
            }
            throw RecipeRepositoryException.editRecipeFail(recipe, cause)
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

    override suspend fun deleteRecipe(recipe: Recipe): Boolean = db.runInTransaction {

        val recipeId = recipe.id

        // get the recipe and recipe medias
        val recipeEntity = recipeDao.observeRecipeById(recipeId).first() ?: return@runInTransaction true
        val mediaEntities = recipeMediaDao.observeMediasForRecipe(recipeId).first()

        try {
            // delete media db entries
            recipeMediaDao.deleteMultiple(mediaEntities)

            // delete recipe db entry
            recipeDao.delete(recipeEntity)

            // delete cover photo file
            if (recipeEntity.coverPhoto != null) {
                store.delete(recipeEntity.coverPhoto!!)
            }

            // delete recipe media files
            for (media in mediaEntities) {
                store.delete(media.data)
            }

            true
        }
        catch (cause: Throwable) {
            throw RecipeRepositoryException.deleteRecipeFail(recipe, cause)
        }
    }
}