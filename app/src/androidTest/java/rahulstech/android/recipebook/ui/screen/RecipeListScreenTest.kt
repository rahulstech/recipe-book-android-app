package rahulstech.android.recipebook.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import rahulstech.android.recipebook.repository.model.Recipe

/**
 * IMPORT: keep the device screen on during test. if the following error encountered make sure
 * first device is not sleeping and screen is on
 *
 * java.lang.IllegalStateException: No compose hierarchies found in the app. Possible reasons include...
 */

class RecipeListScreenTest {

    // use createComposeRule() for pure compose tests
    // use createAndroidComposeRule<ActivityName::class>() or simply
    // createAndroidComposeRule<ComponentActivity::class>() when impure compose testing
    // eg: navigation / hilt / activity behaviour
    //
    // NOTE 2: no need for @RunWith(AndroidJUnit4::class) when using only createComposeRule()
    // but required when using createAndroidComposeRule<...>() or
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_isDisplayed_whenRecipesEmpty() {
        composeTestRule.setContent {
            RecipesListScreen(
                recipes = emptyList(),
                onRecipeClick = {},
                onAddRecipeClick = {}
            )
        }

        composeTestRule
            .onNodeWithTag("empty_view") // use Modifier.testTag(String) on composable node to find node by tag
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("No Recipes")
            .assertIsDisplayed()
    }

    @Test
    fun grid_isDisplayed_whenRecipesAvailable() {
        val recipes = fakeRecipes(1)

        composeTestRule.setContent {
            RecipesListScreen(
                recipes = recipes,
                onRecipeClick = {},
                onAddRecipeClick = {}
            )
        }

        composeTestRule
            .onNodeWithTag("recipes_grid")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(recipes.first().title)
            .assertIsDisplayed()
    }

    @Test
    fun clickingRecipe_triggersCallback() {
        val recipes = fakeRecipes()
        var clickedRecipe: Recipe? = null

        composeTestRule.setContent {
            RecipesListScreen(
                recipes = recipes,
                onRecipeClick = { clickedRecipe = it },
                onAddRecipeClick = {}
            )
        }

        composeTestRule
            .onNodeWithTag("recipe_item_${recipes[0].id}")
            .performClick()

        assert(clickedRecipe == recipes[0])
    }

    @Test
    fun clickingAddButton_triggersCallback() {
        var clicked = false

        composeTestRule.setContent {
            RecipesListScreen(
                recipes = emptyList(),
                onRecipeClick = {},
                onAddRecipeClick = { clicked = true }
            )
        }

        composeTestRule
            .onNodeWithTag("button_add_recipe")
            .performClick()

        assert(clicked)
    }

    @Test
    fun grid_scrolls_andDisplaysLastItem() {
        val recipes = (1..20).map {
            Recipe(
                id = it.toString(),
                title = "Recipe $it",
                note = "Note $it",
                coverPhoto = null
            )
        }

        composeTestRule.setContent {
            RecipesListScreen(
                recipes = recipes,
                onRecipeClick = {},
                onAddRecipeClick = {}
            )
        }

        composeTestRule
            .onNodeWithTag("recipes_grid")
            .performScrollToNode(
                hasText("Recipe 20")
            )

        composeTestRule
            .onNodeWithText("Recipe 20")
            .assertIsDisplayed()
    }


}
