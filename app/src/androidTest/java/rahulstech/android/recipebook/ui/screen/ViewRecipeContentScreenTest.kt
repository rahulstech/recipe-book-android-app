package rahulstech.android.recipebook.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import rahulstech.android.recipebook.ui.screen.viewrecipe.RecipeContentScreen

class ViewRecipeContentScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recipeTitle_isDisplayed() {
        val recipe = fakeRecipe(
            title = "Paneer Butter Masala",
        )

        composeRule.setContent {
            RecipeContentScreen(recipe = recipe)
        }

        composeRule
            .onNodeWithText("Paneer Butter Masala")
            .assertIsDisplayed()
    }

    @Test
    fun ingredientsSection_notShown_whenIngredientsBlank() {
        val recipe = fakeRecipe()

        composeRule.setContent {
            RecipeContentScreen(recipe)
        }

        // IMPORTANT: in this case section_ingredients will never display therefore i can only assert for existence
        // if i try to scroll to the section, which obviously does not exists, will throw exception
//        composeRule
//            .onNodeWithTag("recipe_content_screen")
//            .performScrollToNode(
//                hasTestTag("section_ingredients")
//            )

        composeRule
            .onNodeWithTag("section_ingredients")
            .assertDoesNotExist()
    }

    @Test
    fun ingredientsSection_shown_whenIngredientsPresent() {
        val textIngredients = "Salt\nPepper"
        val recipe = fakeRecipe(ingredients = textIngredients,)

        composeRule.setContent {
            RecipeContentScreen(recipe)
        }

        // IMPORTANT: section_ingredients stays out of screen initially
        // due to the other nodes like title, cover image, medias sections.
        // if i am asserting node display then first scroll to the node, otherwise test fail
        // if i am asserting node existence then no need to scroll.
        composeRule
            .onNodeWithTag("recipe_content_screen")
            .performScrollToNode(
                hasTestTag("section_ingredients")
            )

        // assert section displayed
        composeRule
            .onNodeWithTag("section_ingredients")
            .assertIsDisplayed()

        // assert the ingredient text displayed
        composeRule
            .onNodeWithText(textIngredients)
            .assertIsDisplayed()

//        composeRule
//            .onNodeWithTag("section_ingredients")
//            .assertExists()
    }
}