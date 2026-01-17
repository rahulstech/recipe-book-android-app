package rahulstech.android.recipebook.ui.screen.viewrecipe

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import rahulstech.android.recipebook.NavigationCallback
import rahulstech.android.recipebook.NavigationEvent
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.RecipeRoute
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia
import rahulstech.android.recipebook.ui.UIEffect
import rahulstech.android.recipebook.ui.UIState
import rahulstech.android.recipebook.ui.component.IconValue
import rahulstech.android.recipebook.ui.component.ScaffoldState
import rahulstech.android.recipebook.ui.component.ScaffoldStateCallback
import rahulstech.android.recipebook.ui.component.SnackBarCallback
import rahulstech.android.recipebook.ui.component.SnackBarEvent
import rahulstech.android.recipebook.ui.component.TopBarAction
import rahulstech.android.recipebook.ui.component.YesNoDialog
import rahulstech.android.recipebook.ui.component.shimmer
import rahulstech.android.recipebook.ui.screen.rememberAppImageRequest

@Composable
fun ViewRecipeRoute(id: String,
                    navigationCallback: NavigationCallback,
                    snackBarCallback: SnackBarCallback,
                    scaffoldStateCallback: ScaffoldStateCallback,
                    // production code will use Hilt created ViewModel;
                    // but instrumentation test i can use an instance.
                    // thus i am not hard-wired to hilt during instrumentation test
                    viewModel: ViewRecipeViewModel = hiltViewModel(),
                    )
{
    val context = LocalContext.current

    LaunchedEffect(id) {
        viewModel.findRecipeById(id)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when(effect) {
                is UIEffect.ShowSnackBar -> {
                    snackBarCallback(SnackBarEvent(
                        message = context.getString(effect.messageResId)
                    ))
                }

                is UIEffect.Exit -> { navigationCallback(NavigationEvent.Exit()) }

                else -> {}
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    scaffoldStateCallback(ScaffoldState(
        title = state.currentRecipe?.title ?: "",
        showTitleShimmer = state.isLoading,
        showNavUpAction = true,
        actions = listOf(
            // edit
            TopBarAction.IconAction(
                icon = IconValue.VectorIconValue(Icons.Default.Edit),
                enabled = state.isActionEnabled,
                onClick = { navigationCallback(NavigationEvent.ForwardTo(RecipeRoute.EditRecipe.create(id))) },
            ),

            // delete
            TopBarAction.OverflowAction(
                stringResource(R.string.label_delete),
                enabled = state.isActionEnabled,
                onClick = { viewModel.showDeleteRecipeDialog() }
            )
        )
    ))

    when(state.recipeState) {
        is UIState.Loading -> {
            RecipeShimmer()
        }
        is UIState.Success -> {
            RecipeContent((state.recipeState as UIState.Success<Recipe>).data)
        }
        else -> {}
    }

    if (state.showDeleteRecipeDialog) {
        YesNoDialog(
            onDismiss = { viewModel.hideDeleteRecipeDialog() },
            title = stringResource(R.string.title_delete_recipe_warning),
            message = stringResource(R.string.message_delete_recipe_warning),
            onYes = { viewModel.removeRecipe(state.currentRecipe!!) },
        )
    }
}

@Composable
fun RecipeShimmer()
{
    Column(
        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.extraLarge).shimmer()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.size(width = 80.dp , height = 24.dp).shimmer().align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium).shimmer())

            Box(modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium).shimmer())

            Box(modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium).shimmer())

            Box(modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium).shimmer())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().height(240.dp).clip(MaterialTheme.shapes.large).shimmer())

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().height(240.dp).clip(MaterialTheme.shapes.large).shimmer())
    }
}

@Composable
fun RecipeContent(recipe: Recipe)
{
    LazyColumn(
        modifier = Modifier.padding(16.dp)
            .testTag("recipe_content_screen"),
    ) {
        item {
            RecipeCoverImage(recipe.coverPhoto)
        }

        if (recipe.note.isNotBlank()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                RecipeNote(recipe.note)
            }
        }

        if (recipe.medias.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                RecipeMediaSection(
                    medias = recipe.medias,
                    onMediaClick = {}
                )
            }

        }

        if (recipe.ingredients.isNotBlank()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                RecipeSection(
                    title = stringResource(R.string.label_recipe_ingredients),
                    content = recipe.ingredients,
                    modifier = Modifier.testTag("section_ingredients")
                )
            }
        }

        if (recipe.steps.isNotBlank()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                RecipeSection(
                    title = stringResource(R.string.label_recipe_steps),
                    content = recipe.steps
                )
            }
        }
    }
}

@Composable
fun RecipeCoverImage(coverPhoto: Uri?) {
    val shape = MaterialTheme.shapes.extraLarge
    val imageRequest = rememberAppImageRequest(coverPhoto)
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
            .testTag("cover_photo"),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize().clip(shape),
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
fun RecipeNote(note: String) {
    Text(note,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun RecipeMediaSection(medias: List<RecipeMedia>,
                       onMediaClick: (RecipeMedia) -> Unit,
                       )
{
    LazyRow(
        modifier = Modifier.testTag("medias_row"),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = medias, key = { it.id }) { media ->
            RecipeMediaItem(media){ onMediaClick(media) }
        }
    }
}

@Composable
fun RecipeMediaItem(media: RecipeMedia,
                    onClick: (RecipeMedia) -> Unit = {},
                    )
{
    val shape = MaterialTheme.shapes.medium
    val sizeDp = 160.dp
    val imageRequest = rememberAppImageRequest(media.data, sizeDp)

    Card(
        modifier = Modifier.width(sizeDp).clickable(onClick = { onClick(media) }),
        shape = shape,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column {
            AsyncImage(
                modifier = Modifier.size(sizeDp).clip(shape = shape),
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )

            if (!media.caption.isNullOrBlank()) {
                Box(
                    modifier = Modifier.padding(6.dp).fillMaxWidth().height(40.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(media.caption!!,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

}


@Composable
fun RecipeSection(title: String,
                  content: String,
                  modifier: Modifier = Modifier,
                  )
{
    Column(
        modifier = modifier.fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.large
            )
            .padding(16.dp),
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}


