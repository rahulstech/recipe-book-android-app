package rahulstech.android.recipe_media_store

import android.net.Uri

interface RecipeMediaStore {

    companion object {
        const val FILE_PROVIDER_AUTHORITY = "rahulstech.android.recipebook.recipemediastore"
        fun getMediaId(uri: Uri): String? {
            if (uri.authority != FILE_PROVIDER_AUTHORITY) return null
            return uri.lastPathSegment
        }
    }

    suspend fun create(src: Uri): Uri

    fun resolve(id: String): Uri

    suspend fun delete(id: String)
}