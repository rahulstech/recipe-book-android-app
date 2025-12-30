package rahulstech.android.recipe_media_store

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rahulstech.android.recipe_media_store.RecipeMediaStore.Companion.FILE_PROVIDER_AUTHORITY
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class RecipeMediaStoreImpl @Inject constructor(
    @ApplicationContext val context: Context
): RecipeMediaStore {

    private val contentResolver = context.contentResolver

    internal val DIR_MEDIAS = File(context.filesDir, "recipe_medias")

    override suspend fun create(src: Uri): Uri = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val dest = buildFile(id)
        val temp = buildFile(id, "tmp")

        try {
            contentResolver.openInputStream(src)?.use { input ->
                temp.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw RecipeMediaStoreException("Cannot read from $src")

            if (!temp.renameTo(dest)) {
                throw RecipeMediaStoreException("Atomic rename failed for $src")
            }

            buildUri(id)
        } catch (e: IOException) {
            throw RecipeMediaStoreException("IO failure while creating media from $src", e)
        } catch (e: RecipeMediaStoreException) {
            throw e
        } catch (e: Throwable) {
            throw RecipeMediaStoreException("Unknow error occurred; can not create media from $src", e)
        }
        finally {
            // delete the temporary file
            temp.delete()
        }
    }


    override fun resolve(id: String): Uri = buildUri(id)

    override suspend fun delete(id: String) = withContext(Dispatchers.IO){
        val file = buildFile(id)
        try {
            file.delete()
        }
        catch (ex: Exception) {
            throw RecipeMediaStoreException("", ex)
        }
        Unit
    }

    private fun buildFile(id: String, suffix: String = ""): File {
        val dir = DIR_MEDIAS
        if (!dir.exists()) {
            // IMPORTANT: use mkdirs() because in dir multiple directories may not exists and all should be created
            if (!dir.mkdirs()) {
                throw RecipeMediaStoreException("unable to create directory $dir")
            }
        }
        val fileName = if (suffix.isNotBlank()) "$id.$suffix" else id
        return File(dir, fileName)
    }

    private fun buildUri(id: String): Uri =
        FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, buildFile(id))
}