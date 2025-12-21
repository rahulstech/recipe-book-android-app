package rahulstech.android.recipebook.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rahulstech.android.dailyquotes.ui.theme.RecipeBookTheme
import rahulstech.android.recipebook.ACTION_EDIT
import rahulstech.android.recipebook.ARG_ACTION
import rahulstech.android.recipebook.ARG_ID
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.repository.Repositories
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia

class ViewRecipeViewModel: ViewModel() {

    private val repo = Repositories.recipeRepository

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

class ViewRecipeActivity: ComponentActivity() {

    val viewModel by viewModels<ViewRecipeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.extras?.getString(ARG_ID) ?: ""

        enableEdgeToEdge()
        setContent {
            RecipeBookTheme {
                ViewRecipeRoute(
                    id = id,
                    viewModel = viewModel,
                    onEditRecipeClick = { recipe ->
                        startActivity(
                            Intent(
                                this@ViewRecipeActivity,
                                InputRecipeActivity::class.java
                            ).apply {
                                putExtra(ARG_ACTION, ACTION_EDIT)
                                putExtra(ARG_ID, id)
                        })
                    }
                )
            }
        }

        lifecycleScope.launch {
            viewModel.deleteRecipeState.collectLatest { state ->
                when(state) {
                    is UIState.Success<Recipe> -> {
                        Toast.makeText(this@ViewRecipeActivity, "${state.data.title} delete", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ViewRecipeRoute(id: String,
                    viewModel: ViewRecipeViewModel,
                    onEditRecipeClick: (Recipe)-> Unit) {

    LaunchedEffect(id) {
        viewModel.findRecipeById(id)
    }

    val recipeState by viewModel.recipeState.collectAsStateWithLifecycle()

    ViewRecipeScreen(
        recipeState = recipeState,
        onEditRecipeClick = onEditRecipeClick,
        onDeleteRecipeClick = { recipe ->
            viewModel.removeRecipe(recipe)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewRecipeScreen(
    recipeState: UIState<Recipe>,
    onEditRecipeClick: (Recipe) -> Unit,
    onDeleteRecipeClick: (Recipe) -> Unit,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {

                    // edit recipe
                    IconButton (
                        enabled = recipeState is UIState.Success<Recipe>,
                        onClick = {
                            if (recipeState is UIState.Success<Recipe>) {
                                onEditRecipeClick(recipeState.data)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit recipe"
                        )
                    }

                    // delete recipe
                    IconButton (
                        enabled = recipeState is UIState.Success<Recipe>,
                        onClick = {
                            if (recipeState is UIState.Success<Recipe>) {
                                onDeleteRecipeClick(recipeState.data)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete recipe"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            when(recipeState) {
                is UIState.Success<Recipe> -> RecipeContentScreen(recipeState.data)
                else -> {}
            }
        }
    }
}


@Composable
fun RecipeContentScreen(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(16.dp)
            .widthIn(max = context.resources.getInteger(R.integer.max_content_width).dp).fillMaxHeight()
            .verticalScroll(rememberScrollState()),
    ) {

        RecipeTitle(recipe.title)

        Spacer(modifier = Modifier.height(16.dp))

        RecipeCoverImage(recipe.coverPhoto)

        Column {

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
                    title = "Ingredients",
                    content = recipe.ingredients
                )
            }

            if (recipe.steps.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                RecipeSection(
                    title = "Directions",
                    content = recipe.steps
                )
            }
        }
    }
}

@Composable
fun RecipeCoverImage(coverPhoto: Uri?) {
    val context = LocalContext.current

    val request = remember(coverPhoto) {
        ImageRequest.Builder(context)
            .data(coverPhoto)
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape = RoundedCornerShape(size = 32.dp)),
        contentScale = ContentScale.FillBounds
    )
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
    Column {
        Text(
            text = "Photos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
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
}

@Composable
fun RecipeMediaItem(
    media: RecipeMedia,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val request = remember(media.data) {
        ImageRequest.Builder(context)
            .data(media.data)
            .crossfade(true)
            .build()
    }

    Column(
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {

        AsyncImage(
            model = request,
            contentDescription = media.caption,
            modifier = Modifier
                .size(120.dp)
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
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ),
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier.padding(8.dp),
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
        ViewRecipeScreen(
            recipeState = UIState.Success(recipe),
            onEditRecipeClick = {},
            onDeleteRecipeClick = {}
        )
    }
}

