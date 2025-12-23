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
class RecipeDaoTest {

    lateinit var db: RecipeDatabase
    lateinit var dao: RecipeDao

    @Before
    fun setUp() {
        db = createInMemoryRecipeDatabase(FAKE_DATA_1)
        dao = db.recipeDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertTest() = runBlocking {
        val recipe = RecipeEntity(
            id = "recipe-100",
            title = "title",
            note = "note"
        )

        dao.insert(recipe)

        val recipeFromDb = dao.observeRecipeById(recipe.id).first()

        assertEquals(recipe, recipeFromDb)
    }

    @Test
    fun observeAllRecipesTest() = runBlocking {
        val expected = listOf(
            RecipeItem(
                id = "recipe-1",
                title = "title 1",
                note = "note 1",
            ),
            RecipeItem(
                id = "recipe-2",
                title = "title 2",
                note = "note 2",
                coverPhoto = "cover-photo-1"
            )
        )
        val actual = dao.observeAllRecipes().first()

        assertEquals(expected,actual)
    }

    @Test
    fun updateTest() = runBlocking{
        val recipe = RecipeEntity(
            id = "recipe-1",
            title = "title 1",
            note = "note 1",
            ingredients = "1. ingredient",
            steps = "1. step",
            coverPhoto = "cover-photo"
        )

        dao.update(recipe)

        val recipeFromDb = dao.observeRecipeById(recipe.id).first()

        assertEquals(recipe, recipeFromDb)
    }

    @Test
    fun deleteTest() = runBlocking {
        val recipe = RecipeEntity(
            id = "recipe-1",
            title = "title 1",
            note = "note 1"
        )

        dao.delete(recipe)

        val recipeFromDb = dao.observeRecipeById(recipe.id).first()

        assertNull(recipeFromDb)
    }
}