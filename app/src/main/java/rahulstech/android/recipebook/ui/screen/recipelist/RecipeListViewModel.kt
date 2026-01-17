package rahulstech.android.recipebook.ui.screen.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.ui.UIState
import javax.inject.Inject

@HiltViewModel
class RecipesListViewModel @Inject constructor(private val repo: RecipeRepository): ViewModel() {

    private val _recipesState = MutableStateFlow<UIState<List<Recipe>>>(UIState.Idle)
    val recipesState: StateFlow<UIState<List<Recipe>>> = _recipesState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAllRecipes()
                .onStart { _recipesState.value = UIState.Loading }
                .catch { _recipesState.value = UIState.Error(it) }
                .collectLatest { list ->
                    _recipesState.value = UIState.Loading

                    delay(800)

                    if (list.isEmpty()) {
                        _recipesState.value = UIState.NotFound()
                    }
                    else {
                        _recipesState.value = UIState.Success(list)
                    }
                }
        }
    }
}