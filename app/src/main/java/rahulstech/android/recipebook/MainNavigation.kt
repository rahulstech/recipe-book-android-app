package rahulstech.android.recipebook

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import rahulstech.android.recipebook.ui.screen.CreateRecipeRoute
import rahulstech.android.recipebook.ui.screen.EditRecipeRout
import rahulstech.android.recipebook.ui.screen.RecipesListRoute
import rahulstech.android.recipebook.ui.screen.ViewRecipeRoute

sealed class RecipeRoute(val route: String) {
    data object RecipesList : RecipeRoute("recipes_list")

    data object CreateRecipe : RecipeRoute("create_recipe")

    data object EditRecipe : RecipeRoute("edit_recipe/{id}") {
        fun create(id: String) = "edit_recipe/$id"
    }

    data object ViewRecipe : RecipeRoute("view_recipe/{id}") {
        fun create(id: String) = "view_recipe/$id"
    }
}

typealias SnackBarCallback = (SnackBarEvent) -> Unit

data class TopBarState(
    val title: String = "",
    val actions: @Composable RowScope.()-> Unit = {}
)

typealias TopBackCallback = (TopBarState) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteContent(modifier: Modifier = Modifier,
                 content: @Composable (TopBackCallback, SnackBarCallback)->Unit
                 ) {
    var topBarState by remember { mutableStateOf(TopBarState()) }
    var snackBarEvent by remember { mutableStateOf<SnackBarEvent?>(null) }
    var snackBarHostState by remember { mutableStateOf(SnackbarHostState()) }

    LaunchedEffect(snackBarEvent) {
        snackBarEvent?.let { event ->
            snackBarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.action?.label,
                duration = event.duration
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(data)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = topBarState.title
                    )
                },
                actions = topBarState.actions
            )
        },

    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = integerResource(R.integer.max_content_width).dp)
            ) {
                content({ topBarState = it },
                    { snackBarEvent = it }
                )
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    RouteContent { updateTopBar, showSnackBar ->
        NavHost(
            navController = navController,
            startDestination = RecipeRoute.RecipesList.route
        ) {
            // recipes_list
            composable(
                route =  RecipeRoute.RecipesList.route
            ) {
                RecipesListRoute(
                    onAddRecipeClick = {
                        navController.navigate(
                            RecipeRoute.CreateRecipe.route
                        )
                    },
                    onRecipeItemClick = { recipe ->
                        navController.navigate(
                            RecipeRoute.ViewRecipe.create(recipe.id)
                        )
                    },
                    updateTopBar = updateTopBar,
                )
            }

            // create_recipe
            composable(
                route =  RecipeRoute.CreateRecipe.route
            ) {
                CreateRecipeRoute(
                    updateTopBar = updateTopBar,
                    showSnackBar = showSnackBar,
                    performExit = {
                        navController.popBackStack()
                    }
                )
            }

            // edit_recipe
            composable(
                route =  RecipeRoute.EditRecipe.route,
                arguments = listOf(
                    navArgument( "id"){ type = NavType.StringType }
                )
            ) { backstackEntry ->
                val id = backstackEntry.arguments?.getString("id") ?: return@composable
                EditRecipeRout(
                    id = id,
                    updateTopBar = updateTopBar,
                    showSnackBar = showSnackBar,
                    performExit = {
                        navController.popBackStack()
                    }
                )
            }

            // view_recipe
            composable(
                route =  RecipeRoute.ViewRecipe.route,
                arguments = listOf(
                    navArgument( "id"){ type = NavType.StringType }
                )
            ) { backstackEntry ->
                val id = backstackEntry.arguments?.getString("id") ?: return@composable
                ViewRecipeRoute(
                    id = id,
                    onEditRecipeClick = { recipe ->
                        navController.navigate(
                            RecipeRoute.EditRecipe.create(id)
                        )
                    },
                    performExit = {
                        navController.popBackStack()
                    },
                    updateTopBar =  updateTopBar,
                    showSnackBar = showSnackBar,
                )
            }
        }
    }
}

