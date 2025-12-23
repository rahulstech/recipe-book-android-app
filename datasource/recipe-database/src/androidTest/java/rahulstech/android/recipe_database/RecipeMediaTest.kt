package rahulstech.android.recipe_database

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeMediaTest {

    lateinit var db: RecipeDatabase

    lateinit var dao: RecipeMediaDao

    @Before
    fun setUp() {
        db = createInMemoryRecipeDatabase(FAKE_DATA_1)
        dao = db.recipeMediaDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertMultipleTest() = runBlocking{
        val media = RecipeMediaEntity(
            id = "media-100",
            recipeId = "recipe-1",
            data = "media-100-data",
        )

        dao.insertMultiple(listOf(media))

        val actual = dao.observeMediaById("media-100").first()

        assertEquals(media,actual)
    }

    @Test
    fun observeMediasForRecipeTest() = runBlocking {
        val expected = listOf(
            RecipeMediaEntity(
                id = "media-1",
                recipeId = "recipe-2",
                data = "media-1-data",
            ),
            RecipeMediaEntity(
                id = "media-2",
                recipeId = "recipe-2",
                data = "media-2-data",
                caption = "caption"
            )
        )

        val actual = dao.observeMediasForRecipe("recipe-2").first()

        assertEquals(expected,actual)
    }

    @Test
    fun updateMultipleTest() = runBlocking{
        val media = RecipeMediaEntity(
            id = "media-1",
            recipeId = "recipe-2",
            data = "media-1-data",
            caption = "caption added"
        )

        dao.updateMultiple(listOf(media))

        val actual = dao.observeMediaById("media-1").first()

        assertEquals(media, actual)
    }

    @Test
    fun deleteMultipleTest() = runBlocking{
        dao.deleteMultiple(listOf("media-1"))
        val media = dao.observeMediaById("media-1").first()
        assertNull(media)
    }
}