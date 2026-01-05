package rahulstech.android.recipebook.ui.screen

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
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
import kotlinx.parcelize.Parcelize
import rahulstech.android.recipebook.ui.theme.RecipeBookTheme
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.SnackBarCallback
import rahulstech.android.recipebook.SnackBarEvent
import rahulstech.android.recipebook.TopBackCallback
import rahulstech.android.recipebook.TopBarState
import rahulstech.android.recipebook.repository.RecipeRepository
import rahulstech.android.recipebook.repository.RecipeRepository.Companion.MAX_RECIPE_MEDIAS
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia
import rahulstech.android.recipebook.ui.UIState
import rahulstech.android.recipebook.ui.theme.SimmerColor
import rahulstech.android.recipebook.ui.toRoundPx
import javax.inject.Inject

private const val TAG = "InputRecipe"

@HiltViewModel
class InputRecipeViewModel @Inject constructor(
    private val repo: RecipeRepository
): ViewModel() {

    companion object {
        private val TAG = InputRecipeViewModel::class.simpleName
    }

    private val _saveState = MutableSharedFlow<UIState<Recipe>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val saveState = _saveState.asSharedFlow()

    fun add(recipe: Recipe) {
        saveRecipe {
            repo.addRecipe(recipe)
        }
    }

    fun edit(recipe: Recipe) {
        saveRecipe {
            repo.editRecipe(recipe)
        }
    }

    private fun saveRecipe(action: suspend ()->Recipe?) {
        Log.d(TAG, "saveRecipe")
        viewModelScope.launch(Dispatchers.IO) {
            _saveState.tryEmit(UIState.Loading())
            runCatching {
                action()
            }
                .onFailure { cause ->
                    Log.e(TAG, "saveRecipe: fail", cause)
                    _saveState.tryEmit(UIState.Error(cause))
                }
                .onSuccess { recipe ->
                    Log.d(TAG, "saveRecipe: successful = $recipe")
                    _saveState.tryEmit(if (recipe == null) {
                        UIState.NotFound()
                    }
                    else {
                        UIState.Success(recipe)
                    })
                }
        }
    }


    private val _recipeState = MutableStateFlow<UIState<Recipe>>(UIState.Idle())
    val recipeState = _recipeState.asStateFlow()

    fun findRecipeById(id: String) {
        viewModelScope.launch {
            repo.getRecipeById(id)
                .onStart{
                    _recipeState.tryEmit(UIState.Loading())
                }
                .catch { cause -> _recipeState.tryEmit(UIState.Error(cause)) }
                .collect { recipe ->
                    val state = if (null == recipe) {
                        UIState.NotFound()
                    }
                    else {
                        UIState.Success(recipe)
                    }
                    _recipeState.tryEmit(state)
                }
        }
    }
}

@Composable
private fun HandleRecipeSaveState(viewModel: InputRecipeViewModel,
                                  showSnackBar: SnackBarCallback,
                                  performExit: ()-> Unit) {
    val context = LocalContext.current

    // use LaunchedEffect when state will perform some action (side effect) not UI rendering
    LaunchedEffect(Unit) {
        viewModel.saveState.collectLatest { state ->
            when (state) {
                is UIState.Success -> {
                    showSnackBar(
                        SnackBarEvent(
                            message = context.getString(R.string.message_recipe_save_successful),
                        ))
                    performExit()
                }
                is UIState.Error -> {
                    Log.e(TAG,"recipe save error", state.cause)
                    showSnackBar(
                        SnackBarEvent(
                            message = context.getString(R.string.message_recipe_save_error),
                        ))
                }
                else -> Unit
            }
        }
    }
}

@Composable
fun CreateRecipeRoute(updateTopBar: TopBackCallback,
                      showSnackBar: SnackBarCallback,
                      performExit: ()-> Unit,
                      viewModel: InputRecipeViewModel = hiltViewModel()) {

    HandleRecipeSaveState(
        viewModel = viewModel,
        showSnackBar = showSnackBar,
        performExit = performExit,
    )

    RecipeInputScreen(
        appTitle = "New Recipe",
        onSaveRecipe = { viewModel.add(it) },
        updateTopBar = updateTopBar
    )
}

@Composable
fun EditRecipeRout(id: String,
                   updateTopBar: TopBackCallback,
                   showSnackBar: SnackBarCallback,
                   performExit: ()-> Unit,
                   viewModel: InputRecipeViewModel = hiltViewModel()) {

    LaunchedEffect(id) {
        viewModel.findRecipeById(id)
    }

    HandleRecipeSaveState(
        viewModel = viewModel,
        showSnackBar = showSnackBar,
        performExit = performExit,
    )

    // use collectAsState or its siblings like below when state will only re-render the ui
    val recipeState by viewModel.recipeState.collectAsStateWithLifecycle()
    when(recipeState) {
        is UIState.Loading -> {
            // TODO: show shimmer
        }
        is UIState.Success<Recipe> -> {
            RecipeInputScreen(
                appTitle = stringResource(R.string.app_title_edit_recipe),
                initialRecipe = (recipeState as UIState.Success<Recipe>).data,
                onSaveRecipe = { viewModel.edit(it) },
                updateTopBar = updateTopBar
            )
        }
        is UIState.NotFound -> {
            showSnackBar(
                SnackBarEvent(
                    message = stringResource(R.string.message_recipe_not_found)
                )
            )
            performExit()
        }
        is UIState.Error -> {
            performExit()
        }
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeInputScreen(
    appTitle: String,
    modifier: Modifier = Modifier,
    initialRecipe: Recipe? = null,
    onSaveRecipe: (Recipe) -> Unit,
    updateTopBar: TopBackCallback,
)
{
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable { mutableStateOf("") }
    var steps by rememberSaveable { mutableStateOf("") }
     var coverPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
     var showCoverPhotoDialog by remember { mutableStateOf(false) }
    // don't use mutableStateListOf inside rememberSavable{ },
    // because rememberSavable don't know how to parcelize MutableStateList
    // Note: since medias is not rememberSaveable therefore it can not survive process death
    val medias = remember { mutableStateListOf<RecipeMediaParcelable>() }
    var selectedMedia by remember { mutableStateOf<RecipeMediaParcelable?>(null) }

     val pickCoverPhotoLauncher = rememberLauncherForActivityResult(
             contract = ActivityResultContracts.PickVisualMedia()
         ) { uri: Uri? ->
             // IMPORTANT: only update if user actually picked something
             if (uri != null) {
                 coverPhoto = uri
             }
         }

     val pickMediaLauncher = rememberLauncherForActivityResult(
             contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_RECIPE_MEDIAS)
         ) { uris ->
             val allowed = MAX_RECIPE_MEDIAS - medias.size
             uris.take(allowed).forEach { uri ->
                 medias.add(RecipeMediaParcelable(data = uri))
             }
         }

    updateTopBar(
        TopBarState(
            title = appTitle,
            actions = {
                TextButton(
                    modifier = Modifier.testTag("menu_save"),
                    enabled = title.isNotBlank(),
                    onClick = {
                        onSaveRecipe(
                            Recipe(
                                id = initialRecipe?.id ?: "",
                                title = title,
                                note = note,
                                coverPhoto = coverPhoto,
                                ingredients = ingredients,
                                steps = steps,
                                medias = medias.map { it.toRecipeMedia() }
                            )
                        )
                    },
                ) {
                    Text(text = stringResource(R.string.label_save))
                }
            }
        )
    )

     LaunchedEffect(initialRecipe) {
         initialRecipe?.let { recipe ->
             title = recipe.title
             note = recipe.note
             ingredients = recipe.ingredients
             steps = recipe.steps
             coverPhoto = recipe.coverPhoto
             medias.clear()
             medias.addAll(recipe.medias.map { it.toRecipeMediaParcelable() })
         }
     }

     Column(
         modifier = modifier
             .padding(16.dp)
             .verticalScroll(rememberScrollState())
     ) {

         CoverPhotoInput(
             coverPhoto = coverPhoto,
             onClick = {
                 if (null == coverPhoto) {
                     val request = PickVisualMediaRequest(
                         mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                     )
                     pickCoverPhotoLauncher.launch(request)
                 }
                 else {
                     showCoverPhotoDialog = true
                 }
             }
         )

         Spacer(modifier = Modifier.height(24.dp))

         RecipeMediaInputSection(
             medias = medias,
             onAddMedia = {
                 val request = PickVisualMediaRequest(
                     mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                     maxItems = MAX_RECIPE_MEDIAS - medias.size
                 )
                 pickMediaLauncher.launch(request)
             },
             onRemoveMedia = { medias.remove(it) },
             onMediaClick = { selectedMedia = it }
         )

         Spacer(modifier = Modifier.height(24.dp))

         RecipeTextField(title, { title = it }, stringResource(R.string.label_recipe_title), testTag = "title_input")

         Spacer(modifier = Modifier.height(16.dp))

         RecipeTextField(note, { note = it }, stringResource(R.string.label_recipe_note), 2, 3, testTag = "note_input")

         Spacer(modifier = Modifier.height(24.dp))

         RecipeTextField(ingredients, { ingredients = it }, stringResource(R.string.label_recipe_ingredients), 4, testTag = "ingredients_input")

         Spacer(modifier = Modifier.height(24.dp))

         RecipeTextField(steps, { steps = it }, stringResource(R.string.label_recipe_steps), 6, testTag = "steps_input")
     }

     // -------- Dialog --------
     if (showCoverPhotoDialog) {
         AlertDialog(
             modifier = Modifier.testTag("cover_photo_picker_dialog"),
             onDismissRequest = { showCoverPhotoDialog = false },
             title = { Text(stringResource(R.string.title_cover_photo_picker)) },
             text = { Text(stringResource(R.string.message_cover_photo_picker)) },
             confirmButton = {
                 TextButton(
                     onClick = {
                         showCoverPhotoDialog = false
                         val request = PickVisualMediaRequest(
                             mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                         )
                         pickCoverPhotoLauncher.launch(request)
                     }
                 ) {
                     Text(stringResource(R.string.label_pick_new))
                 }
             },
             dismissButton = {
                 TextButton(
                     onClick = {
                         coverPhoto = null
                         showCoverPhotoDialog = false
                     }
                 ) {
                     Text(stringResource(R.string.label_remove))
                 }
             }
         )
     }

     // -------- Media Caption BootSheet --------
     if (selectedMedia != null) {
         MediaCaptionBottomSheet(
             media = selectedMedia!!,
             onDismiss = { selectedMedia = null },
             onSave = { updated ->
                 val index = medias.indexOfFirst { it.data == updated.data }
                 if (index != -1) {
                     medias[index] = updated
                 }
                 selectedMedia = null
             }
         )
     }
}


@Composable
fun CoverPhotoInput(
    coverPhoto: Uri?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(16.dp)

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
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .testTag("cover_photo"),
        contentAlignment = Alignment.Center
    ) {

        if (coverPhoto != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_add_photo_alternate_24),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.label_add_cover_photo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecipeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().testTag(testTag),
        label = { Text(label) },
        minLines = minLines,
        maxLines = maxLines
    )
}

@Composable
fun RecipeMediaInputSection(
    medias: List<RecipeMediaParcelable>,
    onAddMedia: () -> Unit,
    onRemoveMedia: (RecipeMediaParcelable) -> Unit,
    onMediaClick: (RecipeMediaParcelable) -> Unit
) {
    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_more_photos_with_progress, medias.size, MAX_RECIPE_MEDIAS),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = onAddMedia,
                enabled = medias.size < MAX_RECIPE_MEDIAS,
                modifier = Modifier.testTag("add_media_button")
            ) {
                Text(stringResource(R.string.label_add))
            }
        }

        if (medias.isEmpty()) {
            Text(
                text = stringResource(R.string.message_add_max_recipe_medias, MAX_RECIPE_MEDIAS),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.padding(8.dp).testTag("media_list"),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = medias,
                    key = { it.data.toString() }
                ) { media ->
                    RecipeMediaInputItem(
                        media = media,
                        onRemove = { onRemoveMedia(media) },
                        onClick = { onMediaClick(media) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeMediaInputItem(
    media: RecipeMediaParcelable,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(12.dp)
    val sizeDp = 120.dp
    val imageSizePx = sizeDp.toRoundPx()

    val imageRequest = remember(media.data) {
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
            .clip(shape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(shape = shape)
                .clickable(onClick = onClick)
        ) {

            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.label_remove),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(50)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { onRemove() }
                    }
                    .padding(4.dp)
                    .testTag("remove_media_button"),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if (!media.caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                modifier = Modifier.padding(8.dp),
                text = media.caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Parcelize
data class RecipeMediaParcelable(
    val data: Uri,
    val caption: String? = null,
    val id: String = "",
): Parcelable {
    fun toRecipeMedia(): RecipeMedia = RecipeMedia(data = data, caption = caption, id = id)
}

fun RecipeMedia.toRecipeMediaParcelable(): RecipeMediaParcelable = RecipeMediaParcelable(id = id, data = data, caption = caption)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaCaptionBottomSheet(
    media: RecipeMediaParcelable,
    onDismiss: () -> Unit,
    onSave: (RecipeMediaParcelable) -> Unit
) {
    var caption by rememberSaveable { mutableStateOf(media.caption) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.label_cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        onSave(media.copy(caption = caption?.trim()))
                    }
                ) {
                    Text(stringResource(R.string.label_save))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AsyncImage(
                model = media.data,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = caption ?: "",
                onValueChange = { caption = it },
                label = { Text(stringResource(R.string.label_caption)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeInputScreenPreview() {
    RecipeBookTheme {
        RecipeInputScreen(
            appTitle = stringResource(R.string.app_title_create_recipe),
            onSaveRecipe = {},
            updateTopBar = {}
        )
    }
}
