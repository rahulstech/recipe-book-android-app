package rahulstech.android.recipebook.ui.screen.recipeinput

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import rahulstech.android.recipebook.NavigationCallback
import rahulstech.android.recipebook.NavigationEvent
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.repository.RecipeRepository.Companion.MAX_RECIPE_MEDIAS
import rahulstech.android.recipebook.repository.model.RecipeMedia
import rahulstech.android.recipebook.ui.UIEffect
import rahulstech.android.recipebook.ui.component.IconValue
import rahulstech.android.recipebook.ui.component.ScaffoldState
import rahulstech.android.recipebook.ui.component.ScaffoldStateCallback
import rahulstech.android.recipebook.ui.component.SnackBarCallback
import rahulstech.android.recipebook.ui.component.SnackBarEvent
import rahulstech.android.recipebook.ui.component.TopBarAction
import rahulstech.android.recipebook.ui.component.shimmer
import rahulstech.android.recipebook.ui.screen.rememberAppImageRequest

private const val TAG = "InputRecipe"

@Composable
fun CreateRecipeRoute(navigationCallback: NavigationCallback,
                      snackBarCallback: SnackBarCallback,
                      scaffoldStateCallback: ScaffoldStateCallback,
                      viewModel: InputRecipeViewModel = hiltViewModel())
{
    InputRecipeRoute(navigationCallback, snackBarCallback,scaffoldStateCallback, false, viewModel)
}

@Composable
fun EditRecipeRout(id: String,
                   navigationCallback: NavigationCallback,
                   snackBarCallback: SnackBarCallback,
                   scaffoldStateCallback: ScaffoldStateCallback,
                   viewModel: InputRecipeViewModel = hiltViewModel())
{
    LaunchedEffect(id) {
        viewModel.findRecipeById(id)
    }

    InputRecipeRoute(navigationCallback, snackBarCallback,scaffoldStateCallback, true, viewModel)
}


@Composable
fun InputRecipeRoute(navigationCallback: NavigationCallback,
                     snackBarCallback: SnackBarCallback,
                     scaffoldStateCallback: ScaffoldStateCallback,
                     isEdit: Boolean = false,
                     viewModel: InputRecipeViewModel)
{
    // use collectAsState or its siblings like below when state will only re-render the ui
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pickCoverPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // IMPORTANT: only update if user actually picked something
        if (uri != null) {
            viewModel.updateRecipe(state.updateCoverPhoto(uri))
        }
    }

    val pickCoverPhotoCallback: ()-> Unit = {
        val request = PickVisualMediaRequest(
            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
        )
        pickCoverPhotoLauncher.launch(request)
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_RECIPE_MEDIAS)
    ) { viewModel.updateRecipe(state.addMedias(it)) }

    val pickMediaCallback: ()-> Unit = {
        val request = PickVisualMediaRequest(
            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
            maxItems = MAX_RECIPE_MEDIAS - state.recipe.medias.size
        )
        pickMediaLauncher.launch(request)
    }


    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when(effect) {
                is UIEffect.ShowSnackBar -> {
                    snackBarCallback(SnackBarEvent(
                            message = context.getString(effect.messageResId, effect.args.toTypedArray())
                        )
                    )
                }

                is UIEffect.Exit -> navigationCallback(NavigationEvent.Exit())

                is UIEffect.NavigateTo -> { navigationCallback(effect.event) }
            }
        }
    }

    scaffoldStateCallback(ScaffoldState(
        title = if(isEdit) stringResource(R.string.app_title_edit_recipe) else stringResource(R.string.app_title_create_recipe),
        showNavUpAction = true,
        actions = listOf(
            TopBarAction.IconAction(
                IconValue.VectorIconValue(Icons.Default.Check, stringResource(R.string.message_save_recipe)),
                {
                    if (isEdit) viewModel.edit(state.recipe)
                    else viewModel.add(state.recipe)
                },
                enabled = state.canSave
            )
        ),
        )
    )

    if (isEdit  && state.isLoading) {
        RecipeInputShimmer()
    }
    else {
        RecipeInputContent(
            state,
            pickCoverPhotoCallback = pickCoverPhotoCallback,
            pickMediaCallback = pickMediaCallback,
            onEvent = viewModel::onInputRecipeEvent
        )
    }

    // -------- Dialog --------
    if (state.showCoverPhotoOptionsDialog) {
        CoverPhotoOptionsDialog(
            onDismiss = { viewModel.hideCoverPhotoOptionsDialog() },
            onPickNew = { pickCoverPhotoCallback() },
            onRemoveCoverPhoto = { viewModel.updateRecipe(state.updateCoverPhoto(null)) }
        )
    }

    // -------- Media Caption BootSheet --------
    if (state.showMediaDialog) {
        MediaCaptionBottomSheet(
            media = state.selectedMedia!!,
            onDismiss = { viewModel.hideMediaDialog() },
            onSave = { viewModel.updateRecipe(state.editMedia(it)) }
        )
    }
}

@Composable
fun RecipeInputShimmer()
{
    Column(
        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
            .aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.extraLarge).shimmer()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.size(width = 80.dp , height = 24.dp).shimmer().align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium).shimmer()
            )

            Box(
                modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium).shimmer()
            )

            Box(
                modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium).shimmer()
            )

            Box(
                modifier = Modifier.size(120.dp).clip(MaterialTheme.shapes.medium).shimmer()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().height(240.dp).clip(MaterialTheme.shapes.large).shimmer())

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().height(240.dp).clip(MaterialTheme.shapes.large).shimmer())
    }
}

@Composable
fun RecipeInputContent(state: InputRecipeState,
                       pickCoverPhotoCallback: ()-> Unit,
                       pickMediaCallback: ()-> Unit,
                       onEvent: (InputRecipeEvent)-> Unit,
                      )
{
    val recipe = state.recipe
    val medias = recipe.medias

    LazyColumn (
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        // cover photo
        item {
            CoverPhotoInput(
                coverPhoto = state.recipe.coverPhoto,
                onClick = {
                    if (null == recipe.coverPhoto) {
                        pickCoverPhotoCallback()
                    }
                    else {
                        onEvent(InputRecipeEvent.CoverPhotoClickEvent)
                    }
                }
            )
        }

        // other medias
        item {
            Spacer(modifier = Modifier.height(24.dp))

            RecipeMediaInputSection(
                medias = medias,
                onAddMedia = {
                    pickMediaCallback()
                },
                onRemoveMedia = { onEvent(InputRecipeEvent.UpdateRecipeEvent(state.removeMedia(it))) },
                onMediaClick = { onEvent(InputRecipeEvent.MediaClickEvent(it)) }
            )
        }

        // title
        item {
            Spacer(modifier = Modifier.height(16.dp))

            RecipeTextField(recipe.title,
                { onEvent(InputRecipeEvent.UpdateRecipeEvent(state.updateTitle(it))) },
                stringResource(R.string.label_recipe_title),
                testTag = "title_input"
            )
        }

        // note
        item {
            Spacer(modifier = Modifier.height(16.dp))

            RecipeTextField(recipe.note,
                { onEvent(InputRecipeEvent.UpdateRecipeEvent(state.updateNote(it))) },
                stringResource(R.string.label_recipe_note), 2, 3, hasClearButton = true,
                testTag = "note_input"
            )
        }

        // ingredients
        item {
            Spacer(modifier = Modifier.height(16.dp))

            RecipeTextField(recipe.ingredients,
                { onEvent(InputRecipeEvent.UpdateRecipeEvent(state.updateIngredients(it))) },
                stringResource(R.string.label_recipe_ingredients), 4, hasClearButton = true,
                testTag = "ingredients_input"
            )
        }

        // steps
        item {
            Spacer(modifier = Modifier.height(16.dp))

            RecipeTextField(recipe.steps,
                { onEvent(InputRecipeEvent.UpdateRecipeEvent(state.updateSteps(it))) },
                stringResource(R.string.label_recipe_steps), 6, hasClearButton = true,
                testTag = "steps_input"
            )
        }
    }
}


@Composable
fun CoverPhotoInput(coverPhoto: Uri?,
                    onClick: () -> Unit
                    )
{
    val shape = MaterialTheme.shapes.large
    val imageRequest = rememberAppImageRequest(coverPhoto)

    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(shape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
            .clickable(onClick = onClick)
            .testTag("cover_photo"),
        contentAlignment = Alignment.Center
    ) {

        if (coverPhoto != null) {
            AsyncImage(
                modifier = Modifier.fillMaxSize().clip(shape),
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    painter = painterResource(id = R.drawable.baseline_add_photo_alternate_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.label_add_cover_photo),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecipeTextField(value: String,
                    onValueChange: (String) -> Unit,
                    label: String,
                    minLines: Int = 1,
                    maxLines: Int = Int.MAX_VALUE,
                    hasClearButton: Boolean = false,
                    testTag: String = "",
                    )
{
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        label = { Text(label) },
        minLines = minLines,
        maxLines = maxLines,
        trailingIcon = {
            if (hasClearButton && value.isNotBlank()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Clear, stringResource(R.string.message_clear_text))
                }
            }
        }
    )
}

@Composable
fun RecipeMediaInputSection(medias: List<RecipeMedia>,
                            onAddMedia: () -> Unit,
                            onRemoveMedia: (RecipeMedia) -> Unit,
                            onMediaClick: (RecipeMedia) -> Unit
                            )
{
    Column {
        // media section header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = stringResource(R.string.label_more_photos_with_progress, medias.size, MAX_RECIPE_MEDIAS),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            FilledTonalIconButton(
                onClick = onAddMedia,
                enabled = medias.size < MAX_RECIPE_MEDIAS,
                modifier = Modifier.testTag("add_media_button")
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.message_add_more_medias))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (medias.isEmpty()) {
                Text(stringResource(R.string.message_add_max_recipe_medias, MAX_RECIPE_MEDIAS))
            }
            else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().testTag("media_list"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = medias, key = { it.hashCode() }) { media ->
                        RecipeMediaInputItem(
                            media = media,
                            onRemove = { onRemoveMedia(media) },
                            onEdit = { onMediaClick(media) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeMediaInputItem(media: RecipeMedia,
                         onRemove: () -> Unit,
                         onEdit: () -> Unit
                         )
{
    val shape = MaterialTheme.shapes.medium
    val sizeDp = 160.dp
    val actionSize = 24.dp
    val imageRequest = rememberAppImageRequest(media.data, sizeDp)

    Card(
        modifier = Modifier.width(sizeDp),
        shape = shape,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column {
            Box(
                modifier = Modifier.size(sizeDp).clip(shape = shape)
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // delete
                    Icon(imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.label_remove),
                        modifier = Modifier.size(actionSize)
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

                    // edit
                    Icon(imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.label_edit),
                        modifier = Modifier.size(actionSize)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(50)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures { onEdit() }
                            }
                            .padding(4.dp)
                            .testTag("edit_media_button"),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

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
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}


@Composable
fun CoverPhotoOptionsDialog(onDismiss: () -> Unit,
                            onPickNew: ()-> Unit,
                            onRemoveCoverPhoto: ()-> Unit
                            )
{
    AlertDialog(
        modifier = Modifier.testTag("cover_photo_picker_dialog"),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_cover_photo_picker)) },
        text = { Text(stringResource(R.string.message_cover_photo_picker)) },

        // pick new cover photo
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onPickNew()
                }
            ) {
                Text(stringResource(R.string.label_pick_new))
            }
        },

        // remove cover photo
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onRemoveCoverPhoto()
                }
            ) {
                Text(stringResource(R.string.label_remove))
            }
        }
    )
}

@Composable
fun MediaCaptionBottomSheet(media: RecipeMedia,
                            onDismiss: () -> Unit,
                            onSave: (RecipeMedia) -> Unit,
                            )
{
    var caption by rememberSaveable { mutableStateOf(media.caption ?: "") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
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
                        onDismiss()
                        onSave(media.copy(caption = caption.trim()))
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
                value = caption,
                onValueChange = { caption = it },
                label = { Text(stringResource(R.string.label_caption)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }
    }
}


