package rahulstech.android.recipebook.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.dailyquotes.ui.theme.RecipeBookTheme
import rahulstech.android.recipebook.ARG_ID
import rahulstech.android.recipebook.repository.Repositories
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia

class ViewRecipeViewModel: ViewModel() {

    private val repo = Repositories.recipeRepository

    private var _recipeState = MutableStateFlow<UIState<Recipe>>(UIState.Loading())

    val recipe: StateFlow<UIState<Recipe>> = _recipeState

    fun findRecipeById(id: String) {
        viewModelScope.launch {
            repo.getRecipeById(id).collectLatest { recipe ->
                _recipeState.value = if (null == recipe) {
                    UIState.NotFound()
                }
                else {
                    UIState.Success(recipe)
                }
            }
        }
    }
}

class ViewRecipeActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.extras?.getString(ARG_ID) ?: ""

        enableEdgeToEdge()
        setContent {
            RecipeBookTheme {
                Scaffold { innerPadding ->
                    ViewRecipeRoute(
                        id = id,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ViewRecipeRoute(id: String,
                    modifier: Modifier) {
    val viewModel = viewModel<ViewRecipeViewModel>()

    // TODO: where should i start loading Recipe by id?
    LaunchedEffect(id) {
        viewModel.findRecipeById(id)
    }

    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    ViewRecipeScreen(recipe)
}
@Composable
fun ViewRecipeScreen(recipe: UIState<Recipe>,
                     ) {
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
            .verticalScroll(state = rememberScrollState())
    ) {
        when(recipe) {
            is UIState.Success<Recipe> -> {
                val data = recipe.data
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = data.note ?: ""
                )

                Text(
                    text = "Ingredients"
                )

                Text(
                    text = data.ingredients ?: ""
                )

                if (data.medias.isNotEmpty()) {
                    LazyRow (
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(144.dp).fillMaxWidth()
                    ) {
                        items(items = data.medias, key = { it.id }) { media ->
                            RecipeMediaItem(
                                media = media
                            )
                        }
                    }
                }

                Text(
                    text = "Steps"
                )
                Text(
                    text = data.steps ?: ""
                )
            }
            else -> {}
        }
    }
}

@Composable
fun RecipeMediaItem(media: RecipeMedia,
                    onClickMediaItem: ((RecipeMedia)->Unit)? = null,
                    ) {
    val context = LocalContext.current
    val request = remember(media.data) {
        ImageRequest.Builder(context)
            .data(media.data)
            .build()
    }
    AsyncImage(
        modifier = Modifier.size(120.dp)
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .clickable(
                onClick = { onClickMediaItem?.invoke(media) }
            ),
        model = request,
        contentDescription = media.caption,
        alignment = Alignment.Center,
        contentScale = ContentScale.Crop,
    )
}