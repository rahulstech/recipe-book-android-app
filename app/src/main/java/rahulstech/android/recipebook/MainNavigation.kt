package rahulstech.android.recipebook

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import rahulstech.android.recipebook.ui.component.AppScaffold
import rahulstech.android.recipebook.ui.component.ScaffoldState
import rahulstech.android.recipebook.ui.component.ScaffoldStateCallback
import rahulstech.android.recipebook.ui.screen.recipeinput.CreateRecipeRoute
import rahulstech.android.recipebook.ui.screen.recipeinput.EditRecipeRout
import rahulstech.android.recipebook.ui.screen.recipelist.RecipesListRoute
import rahulstech.android.recipebook.ui.screen.viewrecipe.ViewRecipeRoute


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

sealed interface NavigationEvent {

    data class ForwardTo(val route: String): NavigationEvent

    data class Exit(val results: Bundle = bundleOf()): NavigationEvent
}

typealias NavigationCallback = (NavigationEvent)-> Unit

fun handleNavigationEvent(navController: NavController, event: NavigationEvent) {
    when(event) {
        is NavigationEvent.ForwardTo -> {
            navController.navigate(event.route)
        }
        is NavigationEvent.Exit -> {
            navController.popBackStack()
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    var scaffoldState by remember { mutableStateOf(ScaffoldState()) }
    val scaffoldStateCallback: ScaffoldStateCallback = { scaffoldState = it }
    val navigationCallback: NavigationCallback = { handleNavigationEvent(navController, it) }

    AppScaffold (scaffoldState, navigationCallback){ snackBarCallback ->
        NavHost(
            navController = navController,
            startDestination = RecipeRoute.RecipesList.route
        ) {
            // recipes_list
            composable(
                route =  RecipeRoute.RecipesList.route
            ) {
                RecipesListRoute(
                    navigationCallback = navigationCallback,
                    scaffoldStateCallback = scaffoldStateCallback
                )
            }

            // create_recipe
            composable(
                route =  RecipeRoute.CreateRecipe.route
            ) {
                CreateRecipeRoute(
                    navigationCallback = navigationCallback,
                    snackBarCallback = snackBarCallback,
                    scaffoldStateCallback = scaffoldStateCallback
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
                    navigationCallback = navigationCallback,
                    snackBarCallback = snackBarCallback,
                    scaffoldStateCallback = scaffoldStateCallback
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
                    navigationCallback = navigationCallback,
                    snackBarCallback = snackBarCallback,
                    scaffoldStateCallback = scaffoldStateCallback
                )
            }
        }
    }
}
