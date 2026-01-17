package rahulstech.android.recipebook.ui.screen.recipelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import rahulstech.android.recipebook.NavigationCallback
import rahulstech.android.recipebook.NavigationEvent
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.RecipeRoute
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.ui.UIState
import rahulstech.android.recipebook.ui.component.FloatingActionButton
import rahulstech.android.recipebook.ui.component.IconValue
import rahulstech.android.recipebook.ui.component.ScaffoldState
import rahulstech.android.recipebook.ui.component.ScaffoldStateCallback
import rahulstech.android.recipebook.ui.component.shimmer
import rahulstech.android.recipebook.ui.screen.rememberAppImageRequest


@Composable
fun RecipesListRoute(navigationCallback: NavigationCallback,
                     scaffoldStateCallback: ScaffoldStateCallback
                     ) {
    val viewModel: RecipesListViewModel = hiltViewModel()
    val recipesState by viewModel.recipesState.collectAsStateWithLifecycle()

    scaffoldStateCallback(ScaffoldState(
            showNavUpAction = false,
            title = stringResource(R.string.app_title_recipes_list),
            fab = FloatingActionButton.Default(
                icon = IconValue.VectorIconValue(Icons.Default.Add),
                text = stringResource(R.string.label_add_recipe),
                onClick = {
                    navigationCallback(NavigationEvent.ForwardTo(RecipeRoute.CreateRecipe.route))
                },
                position = FabPosition.Center
            )
        )
    )

    RecipesListScreen (
        recipesState = recipesState,
        onRecipeClick = { recipe ->
            navigationCallback(NavigationEvent.ForwardTo(
                RecipeRoute.ViewRecipe.create(recipe.id)
            ))
        }
    )
}

@Composable
fun RecipesListScreen(recipesState: UIState<List<Recipe>>,
                      onRecipeClick: (Recipe) -> Unit,
                      )
{
    val gridState = rememberLazyGridState()
    LazyVerticalGrid(
        modifier = Modifier.testTag("recipes_grid"),
        state = gridState,
        columns = GridCells.Adaptive(minSize = 200.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when(recipesState) {
            is UIState.Loading -> {
                items(10) {
                    RecipeShimmerItem()
                }
            }

            is UIState.Success -> {
                items(items = recipesState.data, key = { it.id }) { recipe ->
                    RecipeGridItem(recipe) { onRecipeClick(recipe) }
                }
            }

            is UIState.NotFound -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyRecipeListComponent()
                }
            }

            else -> {}
        }
    }
}

@Composable
fun EmptyRecipeListComponent()
{
    Column(
        modifier = Modifier.fillMaxSize().height(480.dp).padding(24.dp)
            .testTag("empty_view"), // composeTestRule use this tag to find this node during test
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            painter = painterResource(id = R.drawable.outline_menu_book_24),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurface
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
    val imageRequest = rememberAppImageRequest(recipe.coverPhoto)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .testTag("recipe_item_${recipe.id}"),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column {
            AsyncImage(model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
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

@Composable
fun RecipeShimmerItem()
{
    Box(
        modifier = Modifier
            .size(width = 300.dp, height = 240.dp)
            .clip(MaterialTheme.shapes.large)
            .shimmer()
    )
}
