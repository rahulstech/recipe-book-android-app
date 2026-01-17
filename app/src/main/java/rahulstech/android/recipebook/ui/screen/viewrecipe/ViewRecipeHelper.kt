package rahulstech.android.recipebook.ui.screen.viewrecipe

import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.ui.UIState

data class ViewRecipeState(
    val recipeState: UIState<Recipe> = UIState.Idle,
    val showDeleteRecipeDialog: Boolean = false,
) {
    val isLoading: Boolean get() = recipeState is UIState.Loading

    val isActionEnabled: Boolean get() = null != currentRecipe

    val currentRecipe: Recipe? get() = when(recipeState) {
        is UIState.Success -> recipeState.data
        else -> null
    }
}

