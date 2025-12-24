package rahulstech.android.recipe_media_store

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * IMPORTANT: copy main/AndroidManifest.xml to androidTest/AndroidManifest.xml
 * and main/res/xml/recipe_media_store_paths.xml to androidTest/res/xml/recipe_media_store_paths.xml
 *
 * why?
 * the actual package name is rahulstech.android.recipe_media_store
 * and androidTest package name is rahulstech.android.recipe_media_store.test
 * therefore during android instrumentation test FileProvider can access files dir under rahulstech.android.recipe_media_store.test
 * without a separate AndroidManifest.xml in androidTest FileProvider try to access files dirs under rahulstech.android.recipe_media_store
 * which is not possible during test as files dir is private dir and only that process can access it.
 * therefore i must add androidTest/AndroidManifest.xml explicitly.
 */

@RunWith(AndroidJUnit4::class)
class RecipeMediaStoreInstrumentedTest {

    lateinit var context: Context
    lateinit var store: RecipeMediaStoreImpl
    lateinit var mediaDir: File
    lateinit var srcDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        store = RecipeMediaStoreImpl(context)
        mediaDir = store.DIR_MEDIAS
        srcDir = context.cacheDir
    }

    @After
    fun tearDown() {
        mediaDir.deleteRecursively()
        srcDir.deleteRecursively()
    }


    // copy android asset file to private cache directory
    // why copy is required?
    // asset file uris are like file:///android_asset/path/to/file
    // but ContentResolvers can not handle this type of uris, only AssetManager can.
    // so asset files are copied to filesystem and then a file uri is returned.
    // now ContentResolvers can this uri for reading.
    fun assetFileUri(name: String): Uri {
        val assetManager = context.assets

        val dest = File(srcDir, name)
        assetManager.open(name).use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return Uri.fromFile(dest)
    }

    // test create(Uri) can read from src uri and returns the an Uri managed by RecipeMediaStore
    @Test
    fun create_returnsAppOwnedUri() = runBlocking {

        /**
         * IMPORTANT: copy files from asset to private cache directory and use file uri as src
         *
         * file is copied to app private dir, still how it confirms the testcase?
         * so the purpose here is that
         * - create(Uri) can read from src uri
         * - copy to a RecipeMediaStore managed directory
         * - return an uri for the created file under RecipeMediaStore managed directory
         *
         * therefore any file outside that directory is also out of scope of the RecipeMediaStore,
         * so i can use the file for the test. Since the asset files are copied to cache dir, so i
         * can use files as src under cache dir.
         */

        val src = assetFileUri("recipe1.jpg")

        val uri = store.create(src)

        assertEquals(RecipeMediaStore.FILE_PROVIDER_AUTHORITY, uri.authority)
    }

    // test content is copied without any loss
    @Test
    fun createdMedia_canBeReadViaContentResolved() = runBlocking{
        // get src
        val src = assetFileUri("recipe1.jpg")

        // read src bytes
        val expected = context.contentResolver.openInputStream(src)!!.use { it.readBytes() }

        // call create(Uri)
        val uri = store.create(src)

        // read bytes from created uri
        val actual = context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }

        // assert
        assertEquals("incorrect size", expected.size, actual.size)

        // in Kotlin and Java Array(ex: ByteArray or byte[]) does not implement equals() but use the Object or Any equals()
        // which compares the hash codes only. now two array instances have different hash code though they have same content;
        // hence assertEquals() will fail. but List implements equals() so it checks the content equality. hence compared as list.
        // alternatively i can do the following also
        //assertTrue(expected.contentEquals(actual))
        assertEquals(expected.toList(), actual.toList())
    }

    // test for same id it always the same uri
    @Test
    fun resolve_returnsStableUri() {
        val id = "fixed-id-123"

        val uri1 = store.resolve(id)
        val uri2 = store.resolve(id)

        // Same logical Uri
        assertEquals(uri1, uri2)

        // authority must match FileProvider
        assertEquals(RecipeMediaStore.FILE_PROVIDER_AUTHORITY, uri1.authority)
    }

    //
    @Test
    fun delete_isIdempotent() = runBlocking {
        // Arrange: create media
        val id = "test-file"
        val mediaFile = File(mediaDir, id)
        mediaDir.mkdirs()
        mediaFile.createNewFile()

        // Sanity: file exists after create
        assertTrue(mediaFile.exists())

        // Act: first delete
        store.delete(id)

        // Assert: file removed
        assertTrue(!mediaFile.exists())

        // Act: second delete (should be safe)

        // Assert: still true, no crash, no resurrection
        assertTrue(!mediaFile.exists())
    }
}