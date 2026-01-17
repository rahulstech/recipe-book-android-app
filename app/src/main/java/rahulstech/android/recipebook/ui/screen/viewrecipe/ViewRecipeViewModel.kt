package rahulstech.android.recipebook.ui.screen.viewrecipe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.ui.UIEffect
import rahulstech.android.recipebook.ui.UIState
import javax.inject.Inject

private const val TAG = "ViewRecipeViewModel"

@HiltViewModel
class ViewRecipeViewModel @Inject constructor(private val repo: RecipeRepository): ViewModel() {

    private val _state = MutableStateFlow(ViewRecipeState())

    val state: StateFlow<ViewRecipeState> = _state.asStateFlow()

    private val _effect = Channel<UIEffect>(Channel .BUFFERED)

    val effect: Flow<UIEffect> = _effect.receiveAsFlow()

    private suspend fun sendSideEffect(effect: UIEffect) {
        _effect.send(effect)
    }

    fun showDeleteRecipeDialog() {
        _state.value = _state.value.copy(showDeleteRecipeDialog = true)
    }

    fun hideDeleteRecipeDialog() {
        _state.value = _state.value.copy(showDeleteRecipeDialog = false)
    }

    fun findRecipeById(id: String) {
        viewModelScope.launch {
            repo.getRecipeById(id)
                .onStart { updateRecipeState(UIState.Loading) }
                .catch { cause -> updateRecipeState(UIState.Error(cause)) }
                .collectLatest { recipe ->
                    if (null == recipe) {
                        sendSideEffect(UIEffect.ShowSnackBar(
                            messageResId = R.string.message_recipe_not_found
                        ))
                        sendSideEffect(UIEffect.Exit)
                    }
                    else {
                        updateRecipeState(UIState.Success(recipe))
                    }
                }
        }
    }

    private fun updateRecipeState(newState: UIState<Recipe>) {
        _state.value = _state.value.copy(recipeState = newState)
    }

    fun removeRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                repo.deleteRecipe(recipe)
                sendSideEffect(UIEffect.ShowSnackBar(
                    messageResId = R.string.message_recipe_delete_successful
                ))
                sendSideEffect(UIEffect.Exit)
            }
            catch (cause: Throwable) {
                Log.e(TAG,"remove recipe error", cause)
                sendSideEffect(UIEffect.ShowSnackBar(
                    messageResId = R.string.message_recipe_delete_error
                ))
            }
        }
    }
}