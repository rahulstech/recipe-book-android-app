package rahulstech.android.recipebook.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import rahulstech.android.dailyquotes.ui.theme.RecipeBookTheme
import rahulstech.android.recipebook.repository.Repositories
import rahulstech.android.recipebook.repository.model.Recipe

class RecipesListViewModel: ViewModel() {

    private val repo = Repositories.recipeRepository

    val recipes: StateFlow<List<Recipe>> by lazy {
        repo.getAllRecipes().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )
    }
}

class MainActivity: ComponentActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeBookTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RecipesListScreen(
                        onClickRecipeItem = this::onClickRecipe,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun onClickRecipe(recipe: Recipe) {
        Log.d(TAG,"clicked ${recipe.title}")
    }
}

@Composable
fun RecipesListScreen(modifier: Modifier = Modifier,
                       onClickRecipeItem: ((Recipe) -> Unit)? = null,
                      itemContent: (@Composable (Recipe, (Recipe)-> Unit)-> Unit) =
                          { recipe, itemClickHandler -> RecipeListItem(recipe =  recipe,itemClickHandler) }
                      ) {

    val viewModel: RecipesListViewModel = viewModel()
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = recipes, key = { recipe -> recipe.id }) { recipe ->
            RecipeListItem(recipe = recipe, onClickRecipeItem = onClickRecipeItem)
        }
    }
}

@Composable
fun RecipeListItem(recipe: Recipe, onClickRecipeItem: ((Recipe)->Unit)? = null) {
    Row(
        modifier = Modifier.height(88.dp)
            .fillMaxWidth()
            .padding(end = 16.dp, top = 12.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(corner = CornerSize(10.dp))),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        val context = LocalContext.current
        val imageRequest = remember(recipe.coverPhoto) {
            ImageRequest.Builder(context)
                .data(recipe.coverPhoto)
                .build()
        }

        AsyncImage(
            modifier = Modifier.width(56.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)),
            model = imageRequest,
            contentDescription = "cover photo of recipe ${recipe.title}",
            alignment = Alignment.Center,
            contentScale = ContentScale.FillBounds,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().weight(1f).clickable(
                onClick = { onClickRecipeItem?.invoke(recipe) }
            )
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = recipe.note ?: "",
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}