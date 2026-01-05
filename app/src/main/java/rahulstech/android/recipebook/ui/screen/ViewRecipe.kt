package rahulstech.android.recipebook.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rahulstech.android.recipebook.ui.theme.RecipeBookTheme
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.SnackBarCallback
import rahulstech.android.recipebook.SnackBarEvent
import rahulstech.android.recipebook.TopBackCallback
import rahulstech.android.recipebook.TopBarState
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia
import rahulstech.android.recipebook.ui.UIState
import rahulstech.android.recipebook.ui.theme.SimmerColor
import rahulstech.android.recipebook.ui.toRoundPx
import javax.inject.Inject

@HiltViewModel
class ViewRecipeViewModel @Inject constructor(
    private val repo: RecipeRepository
): ViewModel() {

    private var _recipeState = MutableStateFlow<UIState<Recipe>>(UIState.Idle())

    val recipeState = _recipeState.asStateFlow()

    fun findRecipeById(id: String) {
        viewModelScope.launch {
            repo.getRecipeById(id)
                .onStart { _recipeState.tryEmit(UIState.Loading()) }
                .catch { cause -> UIState.Error(cause) }
                .collectLatest { recipe ->
                    _recipeState.value = if (null == recipe) {
                        UIState.NotFound()
                    }
                    else {
                        UIState.Success(recipe)
                    }
            }
        }
    }

    private val _deleteRecipeState = MutableSharedFlow<UIState<Recipe>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deleteRecipeState = _deleteRecipeState.asSharedFlow()

    fun removeRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                _deleteRecipeState.tryEmit(UIState.Loading())
                repo.deleteRecipe(recipe)
                recipe
            }
                .onFailure {_deleteRecipeState.tryEmit(UIState.Error(it)) }
                .onSuccess { _deleteRecipeState.tryEmit(UIState.Success(it)) }
        }
    }
}

@Composable
fun ViewRecipeRoute(id: String,
                    onEditRecipeClick: (Recipe)-> Unit,
                    performExit: ()-> Unit,
                    updateTopBar: TopBackCallback,
                    showSnackBar: SnackBarCallback,

                    // production code will use Hilt created ViewModel;
                    // but instrumentation test i can use an instance.
                    // thus i am not hard-wired to hilt during instrumentation test
                    viewModel: ViewRecipeViewModel = hiltViewModel()
                    ) {

    val context = LocalContext.current
    var showDeleteWarningDialog by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        viewModel.findRecipeById(id)
    }

    val deleteState by viewModel.deleteRecipeState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(deleteState) {
        when(deleteState) {
            is UIState.Success<Recipe> -> {
                val recipe = (deleteState as UIState.Success<Recipe>).data
                showSnackBar(
                    SnackBarEvent(
                        message = context.getString(R.string.message_recipe_delete_successful, recipe.title),
                    )
                )
                performExit()
            }
            else -> {}
        }
    }

    val recipeState by viewModel.recipeState.collectAsStateWithLifecycle()
    when(recipeState) {
        is UIState.Success<Recipe> -> {
            val recipe = (recipeState as UIState.Success<Recipe>).data

            // top bar
            updateTopBar(
                TopBarState(
                    title = stringResource(R.string.app_title_view_recipe),
                    actions = {
                        // edit recipe
                        TextButton (
                            onClick = {
                                onEditRecipeClick(recipe)
                            },
                            modifier = Modifier.testTag("menu_edit")
                        ) {
                            Text(stringResource(R.string.label_edit))
                        }

                        // delete recipe
                        TextButton (
                            onClick = {
                                showDeleteWarningDialog = true
                            },
                            modifier = Modifier.testTag("menu_delete")
                        ) {
                            Text(stringResource(R.string.label_delete))
                        }
                    }
                )
            )

            // content screen
            RecipeContentScreen(recipe  = recipe)

            if (showDeleteWarningDialog) {
                AlertDialog(
                    modifier = Modifier.testTag("recipe_delete_warning_dialog"),
                    onDismissRequest = { showDeleteWarningDialog = false },
                    title = { Text(stringResource(R.string.title_delete_recipe_warning)) },
                    text = { Text(stringResource(R.string.message_delete_recipe_warning, recipe.title)) },
                    dismissButton = {
                        TextButton(
                            modifier = Modifier.testTag("recipe_delete_warning_dialog_yes_button"),
                            onClick = {
                                showDeleteWarningDialog = false
                                viewModel.removeRecipe(recipe)
                            }
                        ) {
                            Text(stringResource(R.string.label_yes))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteWarningDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.label_no))
                        }
                    }
                )
            }
        }
        else -> {
            updateTopBar(
                TopBarState(
                    title = stringResource(R.string.app_title_view_recipe),
                )
            )
        }
    }
}

@Composable
fun RecipeContentScreen(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("recipe_content_screen"),
    ) {

        RecipeTitle(recipe.title)

        Spacer(modifier = Modifier.height(16.dp))

        RecipeCoverImage(recipe.coverPhoto)

        if (recipe.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            RecipeNote(recipe.note)
        }

        if (recipe.medias.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            RecipeMediaSection(
                medias = recipe.medias,
                onMediaClick = { media ->
                    // handle click (navigate / preview / fullscreen later)
                }
            )
        }

        if (recipe.ingredients.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            RecipeSection(
                title = stringResource(R.string.label_recipe_ingredients),
                content = recipe.ingredients,
                modifier = Modifier.testTag("section_ingredients")
            )
        }

        if (recipe.steps.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            RecipeSection(
                title = stringResource(R.string.label_recipe_steps),
                content = recipe.steps
            )
        }
    }
}

@Composable
fun RecipeCoverImage(coverPhoto: Uri?) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(size = 32.dp)
    val imageRequest = remember(coverPhoto) {
        ImageRequest.Builder(context)
            .data(coverPhoto)
            .crossfade(true)
            .placeholder(SimmerColor.toArgb().toDrawable())
            .fallback(R.mipmap.empty_image)
            .error(R.mipmap.empty_image)
            .build()
    }

    Box(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
            .aspectRatio(16f / 9f)
            .clip(shape)
            .testTag("cover_photo"),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier.clip(shape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun RecipeTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun RecipeNote(note: String) {
    Text(
        text = note,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp,
    )
}

@Composable
fun RecipeMediaSection(
    medias: List<RecipeMedia>,
    onMediaClick: (RecipeMedia) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .testTag("medias_row"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = medias,
            key = { it.id }
        ) { media ->
            RecipeMediaItem(
                media = media,
                onClick = { onMediaClick(media) }
            )
        }
    }
}

@Composable
fun RecipeMediaItem(
    media: RecipeMedia,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val sizeDp = 120.dp
    val imageSizePx = sizeDp.toRoundPx()

    val request = remember(media.data) {
        ImageRequest.Builder(context)
            .data(media.data)
            .size(imageSizePx)
            .crossfade(true)
            .placeholder(SimmerColor.toArgb().toDrawable())
            .fallback(R.mipmap.empty_image)
            .error(R.mipmap.empty_image)
            .build()
    }

    Column(
        modifier = Modifier
            .width(sizeDp)
            .padding(8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {

        AsyncImage(
            model = request,
            contentDescription = media.caption,
            modifier = Modifier
                .size(sizeDp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        if (!media.caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = media.caption ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecipeSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
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

@Preview(
    showBackground = true,
    device = PIXEL_7
)
@Composable
fun ViewRecipeScreenPreview() {
    val recipe = Recipe(
        id = "recipe4",
        title = "Fourth Recipe",
        note = "This is my fourth recipe",
        coverPhoto = "file:///android_asset/recipe4.jpg".toUri(),
        ingredients = "1. ingredient 1 of recipe 1\n" +
                "2. ingredient 2 of recipe 4\n" +
                "3. ingredient 3 of recipe 4\n" +
                "4. ingredient 4 of recipe 4\n" +
                "5. ingredient 5 of recipe 4\n",
        steps = "1. step 1 of recipe 4\n" +
                "2. step 2 of recipe 4\n" +
                "3. step 3 of recipe 4\n" +
                "4. step 4 of recipe 4\n" +
                "5. step 5 of recipe 4\n" +
                "6. step 6 of recipe 4\n",
    )
    RecipeBookTheme {
        RecipeContentScreen(
            recipe = recipe
        )
    }
}

