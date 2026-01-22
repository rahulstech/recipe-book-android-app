package rahulstech.android.recipebook.ui.screen.recipeinput

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rahulstech.android.recipebook.NavigationEvent
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.RecipeRoute
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia
import rahulstech.android.recipebook.ui.UIEffect
import javax.inject.Inject

private const val TAG = "InputRecipeViewModel"

@HiltViewModel
class InputRecipeViewModel @Inject constructor(private val repo: RecipeRepository): ViewModel() {

    private val _state = MutableStateFlow(InputRecipeState())
    
    val state: StateFlow<InputRecipeState> = _state.asStateFlow()
    
    val currentState: InputRecipeState get() = _state.value
    
    private val _effect = Channel<UIEffect>(Channel.BUFFERED)
    
    val effect: Flow<UIEffect> = _effect.receiveAsFlow()
    
    fun changeIsSaving(isSaving: Boolean) {
        _state.value = currentState.copy(isSaving = isSaving)
    }

    fun onInputRecipeEvent(event: InputRecipeEvent) {
        when(event) {
            is InputRecipeEvent.UpdateRecipeEvent -> {
                updateRecipe(event.recipe)
            }

            is InputRecipeEvent.SaveRecipeEvent -> {
                if (event.isEdit) {
                    edit(event.recipe)
                }
                else {
                    add(event.recipe)
                }
            }

            is InputRecipeEvent.MediaClickEvent -> {
                showMediaDialog(event.media)
            }

            is InputRecipeEvent.CoverPhotoClickEvent -> {
                showCoverPhotoOptionsDialog()
            }
        }
    }

    fun showCoverPhotoOptionsDialog() {
        _state.value = _state.value.copy(showCoverPhotoOptionsDialog = true)
    }

    fun hideCoverPhotoOptionsDialog() {
        _state.value = _state.value.copy(showCoverPhotoOptionsDialog = false)
    }

    fun showMediaDialog(media: RecipeMedia) {
        _state.value = _state.value.copy(showMediaDialog = true, selectedMedia = media)
    }

    fun hideMediaDialog() {
        _state.value = _state.value.copy(showMediaDialog = false, selectedMedia = null)
    }

    fun updateRecipe(recipe: Recipe) {
        _state.value = _state.value.copy(recipe = recipe)
    }

    private suspend fun sendSideEffect(effect: UIEffect) {
        _effect.send(effect)
    }

    fun add(recipe: Recipe) {
        viewModelScope.launch {
            try {
                changeIsSaving(true)
                val newRecipe = repo.addRecipe(recipe.prepare())
                sendSideEffect(UIEffect.ShowSnackBar(R.string.message_recipe_save_successful))
                sendSideEffect(UIEffect.NavigateTo(NavigationEvent.Exit()))
                sendSideEffect(UIEffect.NavigateTo(
                    NavigationEvent.ForwardTo(RecipeRoute.ViewRecipe.create(newRecipe.id))
                ))
            }
            catch (cause: Throwable) {
                Log.e(TAG,"add recipe error", cause)
                changeIsSaving(false)
                sendSideEffect(UIEffect.ShowSnackBar(R.string.message_recipe_save_error))
            }
        }
    }

    fun edit(recipe: Recipe) {
        viewModelScope.launch {
            try {
                changeIsSaving(true)
                repo.editRecipe(recipe.prepare())
                sendSideEffect(UIEffect.ShowSnackBar(R.string.message_recipe_save_successful))
                sendSideEffect(UIEffect.NavigateTo(NavigationEvent.Exit()))
            }
            catch (cause: Throwable) {
                Log.e(TAG,"edit recipe error", cause)
                changeIsSaving(false)
                sendSideEffect(UIEffect.ShowSnackBar(R.string.message_recipe_save_error))
            }
        }
    }

    private var lastLoadedRecipeId = ""

    fun findRecipeById(id: String) {
        if (lastLoadedRecipeId == id) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val recipe = repo.getRecipeById(id).first()
                lastLoadedRecipeId = id
                if (null == recipe) {
                    sendSideEffect(UIEffect.ShowSnackBar(
                        messageResId = R.string.message_recipe_not_found
                    ))
                    sendSideEffect(UIEffect.Exit)
                }
                else {
                    _state.value = _state.value.copy(recipe = recipe, isLoading = false)
                }
            }
            catch (error: Throwable) {
                Log.e(TAG,"find recipe by id error", error)
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}