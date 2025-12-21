package rahulstech.android.recipebook.ui

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.parcelize.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rahulstech.android.dailyquotes.ui.theme.RecipeBookTheme
import rahulstech.android.recipebook.ACTION_CREATE
import rahulstech.android.recipebook.ACTION_EDIT
import rahulstech.android.recipebook.ARG_ACTION
import rahulstech.android.recipebook.ARG_ID
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.repository.Repositories
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia

private const val MAX_RECIPE_MEDIAS = 10

class InputRecipeViewModel: ViewModel() {

    companion object {
        private val TAG = InputRecipeViewModel::class.simpleName
    }

    private val repo = Repositories.recipeRepository

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

    private fun saveRecipe(action: ()->Recipe?) {
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


class InputRecipeActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeBookTheme {
                val action = intent.getStringExtra(ARG_ACTION) ?: ACTION_CREATE
                when(action) {
                    ACTION_CREATE -> CreateRecipeRoute()
                    ACTION_EDIT -> {
                        val id = intent.getStringExtra(ARG_ID) ?: ""
                        EditRecipeRout(id)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateRecipeRoute() {
    val viewModel = viewModel<InputRecipeViewModel>()
    val context = LocalContext.current

//    var coverPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
//    var showCoverPhotoDialog by remember { mutableStateOf(false) }
//
//    val pickCoverPhotoLauncher =
//        rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.GetContent()
//        ) { uri: Uri? ->
//            // IMPORTANT: only update if user actually picked something
//            if (uri != null) {
//                coverPhoto = uri
//            }
//        }
//
//    val medias = rememberSaveable { mutableStateListOf<RecipeMediaParcelable>() }
//    var selectedMedia by remember { mutableStateOf<RecipeMediaParcelable?>(null) }
//
//    val pickMediaLauncher =
//        rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_RECIPE_MEDIAS)
//        ) { uris ->
//            val allowed = MAX_RECIPE_MEDIAS - medias.size
//            uris.take(allowed).forEach { uri ->
//                medias.add(RecipeMediaParcelable(data = uri))
//            }
//        }

    LaunchedEffect(Unit) {
        viewModel.saveState.collectLatest { state ->
            when (state) {
                is UIState.Success -> {
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                }
                is UIState.Error -> {
                    Toast.makeText(
                        context,
                        "Error: ${state.cause.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> Unit
            }
        }
    }

    RecipeInputScreen(
//        coverPhoto = coverPhoto,
//        onCoverPhotoClick = {
//            if (coverPhoto == null) {
//                // First time → directly open picker
//                pickCoverPhotoLauncher.launch("image/*")
//            } else {
//                // Image already exists → ask user
//                showCoverPhotoDialog = true
//            }
//        },
//        medias = medias,
//        onAddMedia = {
//            if (medias.size < 10) {
//                val request = PickVisualMediaRequest(
//                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
//                    maxItems = MAX_RECIPE_MEDIAS - medias.size
//                )
//                pickMediaLauncher.launch(request)
//            }
//        },
//        onRemoveMedia = { uri ->
//            medias.remove(uri)
//        },
//        onMediaClick = { selectedMedia = it },
        onSaveRecipe = { recipe ->
            viewModel.add(recipe)
//            viewModel.add(
//                recipe.copy(
//                    coverPhoto = coverPhoto,
//                    medias = medias.map { it.toRecipeMedia() }
//                )
//            )
        }
    )

//    // -------- Dialog --------
//    if (showCoverPhotoDialog) {
//        AlertDialog(
//            onDismissRequest = { showCoverPhotoDialog = false },
//            title = { Text("Cover photo") },
//            text = { Text("What would you like to do?") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        showCoverPhotoDialog = false
//                        pickCoverPhotoLauncher.launch("image/*")
//                    }
//                ) {
//                    Text("Pick new")
//                }
//            },
//            dismissButton = {
//                TextButton(
//                    onClick = {
//                        coverPhoto = null
//                        showCoverPhotoDialog = false
//                    }
//                ) {
//                    Text("Remove")
//                }
//            }
//        )
//    }
//
//    // -------- Media Caption BootSheet --------
//    if (selectedMedia != null) {
//        MediaCaptionBottomSheet(
//            media = selectedMedia!!,
//            onDismiss = { selectedMedia = null },
//            onSave = { updated ->
//                val index = medias.indexOfFirst { it.data == updated.data }
//                if (index != -1) {
//                    medias[index] = updated
//                }
//                selectedMedia = null
//            }
//        )
//    }
}

@Composable
fun EditRecipeRout(id: String) {
    val viewModel = viewModel<InputRecipeViewModel>()
    val context = LocalContext.current

//    LaunchedEffect(Unit) {
//        viewModel.saveState.collectLatest { state ->
//            when (state) {
//                is UIState.Success -> {
//                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
//                }
//                is UIState.Error -> {
//                    Toast.makeText(
//                        context,
//                        "Error: ${state.cause.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//                else -> Unit
//            }
//        }
//    }

    LaunchedEffect(id) {
        viewModel.findRecipeById(id)
    }

    val recipeState by viewModel.recipeState.collectAsStateWithLifecycle()
    when(recipeState) {
        is UIState.Loading -> {

        }
        is UIState.Success<Recipe> -> {
            Log.d("InputRecipeActivity", "recipe loaded")
            RecipeInputScreen(
                initialRecipe = (recipeState as UIState.Success<Recipe>).data,
                onSaveRecipe = { viewModel.edit(it) }
            )
            }
        is UIState.NotFound -> {}
        is UIState.Error -> {}
        else -> {}
    }

    val saveState by viewModel.saveState.collectAsStateWithLifecycle(UIState.Idle())
    when(saveState) {
        is UIState.Loading -> {}
        is UIState.Success -> {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        }
        is UIState.Error -> {
            Toast.makeText(
                context,
                "Error: ${(saveState as UIState.Error).cause.message}",
                Toast.LENGTH_LONG
            ).show()
        }
        else -> {}
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeInputScreen(
    modifier: Modifier = Modifier,
    initialRecipe: Recipe? = null,
    onSaveRecipe: (Recipe) -> Unit
)
 {
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable { mutableStateOf("") }
    var steps by rememberSaveable { mutableStateOf("") }

     var coverPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
     var showCoverPhotoDialog by remember { mutableStateOf(false) }

     val pickCoverPhotoLauncher =
         rememberLauncherForActivityResult(
             contract = ActivityResultContracts.PickVisualMedia()
         ) { uri: Uri? ->
             // IMPORTANT: only update if user actually picked something
             if (uri != null) {
                 coverPhoto = uri
             }
         }

     val medias = rememberSaveable { mutableStateListOf<RecipeMediaParcelable>() }
     var selectedMedia by remember { mutableStateOf<RecipeMediaParcelable?>(null) }

     val pickMediaLauncher =
         rememberLauncherForActivityResult(
             contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_RECIPE_MEDIAS)
         ) { uris ->
             val allowed = MAX_RECIPE_MEDIAS - medias.size
             uris.take(allowed).forEach { uri ->
                 medias.add(RecipeMediaParcelable(data = uri))
             }
         }

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

     Scaffold(
         modifier = modifier,
         topBar = {
             TopAppBar(
                 title = {
                     Text(if (null == initialRecipe) "New Recipe" else "Edit Recipe")
                 },
                 actions = {
                     TextButton(
                         enabled = title.isNotBlank(),
                         onClick = {
                             onSaveRecipe(
                                 Recipe(
                                     id = initialRecipe?.id ?: "",
                                     title = title.trim(),
                                     coverPhoto = coverPhoto,
                                     note = note.trim(),
                                     ingredients = ingredients.trim(),
                                     steps = steps.trim(),
                                     medias = medias.map{ it.toRecipeMedia() }
                                 )
                             )
                         }
                     ) {
                         Text("Save")
                     }
                 }
             )
         },
     ) { innerPadding ->

         Box(
             modifier = Modifier
                 .padding(innerPadding)
                 .fillMaxSize(),
             contentAlignment = Alignment.TopCenter,
         ) {
             Column(
                 modifier = modifier
                     .padding(16.dp)
                     .widthIn(max = 650.dp)
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

                 RecipeTextField(title, { title = it }, "Title")

                 Spacer(modifier = Modifier.height(16.dp))

                 RecipeTextField(note, { note = it }, "Description", 2, 3)

                 Spacer(modifier = Modifier.height(24.dp))

                 RecipeTextField(ingredients, { ingredients = it }, "Ingredients", 4)

                 Spacer(modifier = Modifier.height(24.dp))

                 RecipeTextField(steps, { steps = it }, "Directions", 6)
             }
         }
     }

     // -------- Dialog --------
     if (showCoverPhotoDialog) {
         AlertDialog(
             onDismissRequest = { showCoverPhotoDialog = false },
             title = { Text("Cover photo") },
             text = { Text("What would you like to do?") },
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
                     Text("Pick new")
                 }
             },
             dismissButton = {
                 TextButton(
                     onClick = {
                         coverPhoto = null
                         showCoverPhotoDialog = false
                     }
                 ) {
                     Text("Remove")
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        if (coverPhoto != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(coverPhoto)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
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
                    text = "Add cover photo",
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
    maxLines: Int = Int.MAX_VALUE
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
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
                text = "More photos (${medias.size}/${MAX_RECIPE_MEDIAS})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = onAddMedia,
                enabled = medias.size < MAX_RECIPE_MEDIAS
            ) {
                Text("Add")
            }
        }

        if (medias.isEmpty()) {
            Text(
                text = "You can add up to 10 photos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.padding(8.dp),
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

    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(shape = shape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(shape = shape)
                .clickable(onClick = onClick)
        ) {

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(media.data)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
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
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if (!media.caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
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
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        onSave(media.copy(caption = caption?.trim()))
                    }
                ) {
                    Text("Save")
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
                label = { Text("Caption") },
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
            onSaveRecipe = {},
        )
    }
}
