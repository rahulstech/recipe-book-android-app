package rahulstech.android.recipebook.ui.screen

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.core.net.toUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import rahulstech.android.recipebook.RouteContent
import rahulstech.android.recipebook.repository.RecipeRepository.Companion.MAX_RECIPE_MEDIAS
import rahulstech.android.recipebook.repository.model.Recipe

class RecipeInputScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun saveButton_disabled_whenTitleEmpty() {
        composeRule.setContent {
            RouteContent { topBarCallback,_ ->
                RecipeInputScreen(
                    appTitle = "New Recipe",
                    onSaveRecipe = {},
                    updateTopBar = topBarCallback
                )
            }
        }

        composeRule
            .onNodeWithTag("menu_save")
            .assertIsNotEnabled()
    }

    @Test
    fun saveButton_enabled_whenTitleEntered() {
        composeRule.setContent {
            RouteContent { topBarCallback,_ ->
                RecipeInputScreen(
                    appTitle = "New Recipe",
                    onSaveRecipe = {},
                    updateTopBar = topBarCallback,
                )
            }
        }

        composeRule
            .onNodeWithTag("title_input")
            .performTextInput("Pasta")

        composeRule
            .onNodeWithTag("menu_save")
            .assertIsEnabled()
    }

    @Test
    fun saveButton_disabled_whenTitleCleared() {
        composeRule.setContent {
            RouteContent { topBarCallback,_ ->
                RecipeInputScreen(
                    appTitle = "New Recipe",
                    onSaveRecipe = {},
                    updateTopBar = topBarCallback
                )
            }
        }

        composeRule.onNodeWithTag("title_input")
            .performTextInput("Temp")

        composeRule.onNodeWithTag("title_input")
            .performTextClearance()

        composeRule.onNodeWithTag("menu_save")
            .assertIsNotEnabled()
    }

    @Test
    fun saveButton_emitsRecipeWithEnteredData() {
        var savedRecipe: Recipe? = null

        composeRule.setContent {
            RouteContent { topBarCallback,_ ->
                RecipeInputScreen(
                    appTitle = "New Recipe",
                    onSaveRecipe = { savedRecipe = it },
                    updateTopBar = topBarCallback
                )
            }
        }

        composeRule.onNodeWithTag("title_input").performTextInput("Pasta")
        composeRule.onNodeWithTag("menu_save").performClick()

        composeRule.runOnIdle {
            assertNotNull(savedRecipe)
            assertEquals("Pasta", savedRecipe!!.title)
        }
    }

    @Test
    fun initialRecipe_prefillsUI() {
        val recipe = fakeRecipe(
            title = "Pizza",
            note = "Classic",
            ingredients = "Cheese",
            steps = "Bake",
        )

        composeRule.setContent {
            RecipeInputScreen(
                appTitle = "Edit Recipe",
                initialRecipe = recipe,
                onSaveRecipe = {},
                updateTopBar = {}
            )
        }

        composeRule.onNodeWithText("Pizza").assertExists()
        composeRule.onNodeWithText("Classic").assertExists()
    }

    @Test
    fun textFields_acceptInput() {
        composeRule.setContent {
            RecipeInputScreen(
                appTitle = "New Recipe",
                onSaveRecipe = {},
                updateTopBar = {}
            )
        }

        composeRule.onNodeWithTag("title_input")
            .performTextInput("Burger")

        composeRule.onNodeWithTag("ingredients_input")
            .performTextInput("Bun, Patty")

        composeRule.onNodeWithTag("steps_input")
            .performTextInput("Cook and assemble")

        composeRule.onNodeWithText("Burger").assertExists()
        composeRule.onNodeWithText("Bun, Patty").assertExists()
        composeRule.onNodeWithText("Cook and assemble").assertExists()
    }

    @Test
    fun saveButton_enabled_whenInitialRecipeProvided() {
        val recipe = fakeRecipe(title = "Pancake")

        composeRule.setContent {
            RouteContent { topBarCallback,_ ->
                RecipeInputScreen(
                    appTitle = "Edit Recipe",
                    initialRecipe = recipe,
                    onSaveRecipe = {},
                    updateTopBar = topBarCallback
                )
            }
        }

        composeRule.onNodeWithTag("menu_save")
            .assertIsEnabled()
    }

    @Test
    fun editingRecipe_preservesId() {
        val recipe = fakeRecipe(id = "42", title = "Soup")
        var saved: Recipe? = null

        composeRule.setContent {
            RouteContent { topBarCallback,_ ->
                RecipeInputScreen(
                    appTitle = "Edit Recipe",
                    initialRecipe = recipe,
                    onSaveRecipe = { saved = it },
                    updateTopBar = topBarCallback
                )
            }
        }

        composeRule.onNodeWithTag("title_input")
            .performTextClearance()

        composeRule.onNodeWithTag("title_input")
            .performTextInput("Hot Soup")

        composeRule.onNodeWithTag("menu_save")
            .performClick()

        composeRule.runOnIdle {
            assertEquals("42", saved?.id)
            assertEquals("Hot Soup", saved?.title)
        }
    }

//    @Test
//    fun coverPhoto_click_showsDialog_whenPhotoExists() {
//        val recipe = fakeRecipe(coverPhoto = "content://test".toUri())
//
//        composeRule.setContent {
//            RecipeInputScreen(
//                appTitle = "Edit Recipe",
//                initialRecipe = recipe,
//                onSaveRecipe = {},
//                updateTopBar = {}
//            )
//        }
//
//        composeRule.runOnIdle {
//            composeRule.onNodeWithTag("cover_photo")
//                .performClick()
//
//            composeRule.onNodeWithTag("cover_photo_picker_dialog")
//                .assertIsDisplayed()
//        }
//    }

    @Test
    fun addMediaButton_disabled_whenMaxReached() {
        val medias = List(MAX_RECIPE_MEDIAS) {
            RecipeMediaParcelable("content://$it".toUri())
        }

        composeRule.setContent {
            RecipeMediaInputSection(
                medias = medias,
                onAddMedia = {},
                onRemoveMedia = {},
                onMediaClick = {}
            )
        }

        composeRule.onNodeWithTag("add_media_button")
            .assertIsNotEnabled()
    }

    @Test
    fun removeMedia_removesItemFromUI() {
        val media = RecipeMediaParcelable("content://1".toUri())
        val medias = mutableListOf(media)

        composeRule.setContent {
            RecipeMediaInputSection(
                medias = medias,
                onAddMedia = {},
                onRemoveMedia = { medias.remove(it) },
                onMediaClick = {}
            )
        }

        composeRule.onNodeWithTag("remove_media_button", useUnmergedTree = true )
            .performClick()

        composeRule.runOnIdle {
            assertTrue(medias.isEmpty())
        }
    }
}