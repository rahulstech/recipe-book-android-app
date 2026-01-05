package rahulstech.android.recipebook.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.TopBackCallback
import rahulstech.android.recipebook.TopBarState
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.ui.theme.SimmerColor
import javax.inject.Inject

@HiltViewModel
class RecipesListViewModel @Inject constructor(
    private val repo: RecipeRepository
): ViewModel() {

    val recipes: StateFlow<List<Recipe>> by lazy {
        repo.getAllRecipes().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )
    }
}

@Composable
fun RecipesListRoute(onRecipeItemClick: (Recipe) -> Unit,
                     onAddRecipeClick: ()-> Unit,
                     updateTopBar: TopBackCallback,
                     ) {
    val viewModel: RecipesListViewModel = hiltViewModel()
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()

    updateTopBar(
        TopBarState(
            title = stringResource(R.string.app_title_recipes_list)
        )
    )

    RecipesListScreen (
        recipes = recipes,
        onRecipeClick = onRecipeItemClick,
        onAddRecipeClick = onAddRecipeClick,
    )
}

@Composable
fun RecipesListScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onAddRecipeClick: () -> Unit
) {
    val gridState = rememberLazyGridState()

    Scaffold(
        floatingActionButton = {
            // FIXME: FAB not hiding on scroll
            Button(
                onClick = onAddRecipeClick,
                modifier = Modifier.testTag("button_add_recipe"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = stringResource(R.string.label_add_recipe))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ){ innerPadding ->
        if (recipes.isEmpty()) {
            EmptyRecipeView(
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyVerticalGrid(
                modifier = Modifier.padding(innerPadding)
                    .testTag("recipes_grid"),
                state = gridState,
                columns = GridCells.Adaptive(minSize = 200.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = recipes,
                    key = { it.id }
                ) { recipe ->
                    RecipeGridItem(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) }
                    )
                }
            }
        }


    }
}

@Composable
fun EmptyRecipeView(modifier: Modifier = Modifier
                    )
{
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("empty_view"), // composeTestRule use this tag to find this node during test
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.outline_menu_book_24),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.label_empty_recipes_list),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start adding your favorite dishes and build your recipe book.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun RecipeGridItem(recipe: Recipe,
                   onClick: () -> Unit)
{
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageRequest = remember(recipe.coverPhoto) {
        ImageRequest.Builder(context)
            .data(recipe.coverPhoto)
            .crossfade(true)
            .lifecycle(lifecycleOwner.lifecycle)
            .placeholder(SimmerColor.toArgb().toDrawable()) // show when loading
            .fallback(R.mipmap.empty_image) // show when data == null
            .error(R.mipmap.empty_image) // show when error loading
            .build()
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("recipe_item_${recipe.id}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = imageRequest,
                contentDescription = recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // square image, stable grid
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = recipe.note,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(
    name = "Phone",
    showBackground = true,
    widthDp = 525,
)
@Composable
fun RecipeHomeScreenPreview() {
    val previewRecipes = listOf(
        Recipe(
            id = "1",
            title = "Paneer Butter Masala",
            note = "Rich, creamy tomato gravy with soft paneer cubes.",
            coverPhoto = "https://picsum.photos/400/400?1".toUri()
        ),
        Recipe(
            id = "2",
            title = "Chicken Biryani",
            note = "Aromatic basmati rice layered with spiced chicken.",
            coverPhoto = "https://picsum.photos/400/400?2".toUri()
        ),
        Recipe(
            id = "3",
            title = "Vegetable Pasta",
            note = "Italian-style pasta tossed with fresh vegetables.",
            coverPhoto = "https://picsum.photos/400/400?3".toUri()
        )
    )

    MaterialTheme {
        RecipesListScreen(
            recipes = previewRecipes,
            onRecipeClick = {},
            onAddRecipeClick = {},
        )
    }
}