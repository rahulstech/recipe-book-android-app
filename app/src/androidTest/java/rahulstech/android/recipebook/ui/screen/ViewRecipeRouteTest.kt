package rahulstech.android.recipebook.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rahulstech.android.recipebook.RouteContent
import rahulstech.android.recipebook.ui.component.SnackBarEvent
import rahulstech.android.recipebook.TopBarState
import rahulstech.android.recipebook.ui.screen.viewrecipe.ViewRecipeRoute
import rahulstech.android.recipebook.ui.screen.viewrecipe.ViewRecipeViewModel

class ViewRecipeRouteTest {

    @get:Rule
    val composeRule = createComposeRule()

    lateinit var testRepo: RecipeRepositoryTestImpl

    lateinit var viewModel: ViewRecipeViewModel

    @Before
    fun setUp() {
        testRepo = RecipeRepositoryTestImpl()
        viewModel = ViewRecipeViewModel(testRepo)
    }

    @Test
    fun topBarTitle_isUpdated() {
        var topBarState: TopBarState? = null

        composeRule.setContent {
            ViewRecipeRoute(
                id = "1",
                onEditRecipeClick = {},
                performExit = {},
                updateTopBar = { topBarState = it },
                showSnackBar = {},
                viewModel = viewModel
            )
        }

        // run while Compose is done all pending work and ui thread is idle
        composeRule.runOnIdle {
            // change observable state on idle always
            testRepo.emit_getRecipeById(
                fakeRecipe(
                    id = "1",
                    title = "Test Recipe",
                )
            )

            assertNotNull(topBarState?.title)
        }
    }

    @Test
    fun editAndDeleteButtons_visible_onSuccess() {
        composeRule.setContent {
            // menu buttons are part of the ViewRecipeRoute but the RouteContent
            // therefore until RouteContent is not rendered, menu buttons will never exist
            RouteContent { topBarCallback,_ ->
                ViewRecipeRoute(
                    id = "1",
                    onEditRecipeClick = {},
                    performExit = {},
                    updateTopBar = topBarCallback,
                    showSnackBar = {},
                    viewModel = viewModel
                )
            }
        }

        composeRule.runOnIdle {
            testRepo.emit_getRecipeById(fakeRecipe())
        }

        // blocks test thread and keep checking this condition until it becomes true, or fail after timeout.
        composeRule.waitUntil {
            composeRule
                // query all semantic nodes with the given test tag on current ui tree
                .onAllNodesWithTag("menu_delete")
                // it actually returns a list of found nodes
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule
            .onNodeWithTag("menu_edit")
            .assertIsDisplayed()

        composeRule
            .onNodeWithTag("menu_delete")
            .assertIsDisplayed()
    }

    @Test
    fun deleteDialog_isShown_onDeleteClick() {

        composeRule.setContent {
            RouteContent { updateTopBar, _ ->
                ViewRecipeRoute(
                    id = "1",
                    onEditRecipeClick = {},
                    performExit = {},
                    updateTopBar = updateTopBar,
                    showSnackBar = {},
                    viewModel = viewModel
                )
            }
        }

        composeRule.runOnIdle {
            testRepo.emit_getRecipeById(fakeRecipe())
        }

        composeRule.waitUntil {
            composeRule.onAllNodesWithTag("menu_delete")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule
            .onNodeWithTag("menu_delete")
            .performClick()

        composeRule
            .onNodeWithTag("recipe_delete_warning_dialog")
            .assertIsDisplayed()
    }

    @Test
    fun deleteSuccess_showsSnackBar_and_exits() {
        var snackBarEvent: SnackBarEvent? = null
        var exited = false

        composeRule.setContent {
            RouteContent { topBarCallback,_ ->
                ViewRecipeRoute(
                    id = "1",
                    onEditRecipeClick = {},
                    performExit = { exited = true },
                    updateTopBar = topBarCallback,
                    showSnackBar = { snackBarEvent = it },
                    viewModel = viewModel
                )
            }
        }

        composeRule.runOnIdle {
            testRepo.emit_getRecipeById(fakeRecipe())
        }

        composeRule.waitUntil {
            composeRule.onAllNodesWithTag("menu_delete")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule
            .onNodeWithTag("menu_delete")
            .performClick()

        composeRule
            .onNodeWithTag("recipe_delete_warning_dialog_yes_button")
            .performClick()

        composeRule.runOnIdle {
            assertNotNull(snackBarEvent)
            assertTrue(exited)
        }
    }
}