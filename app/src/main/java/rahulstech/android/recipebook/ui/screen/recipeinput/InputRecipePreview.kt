package rahulstech.android.recipebook.ui.screen.recipeinput

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia
import rahulstech.android.recipebook.ui.component.AppScaffold
import rahulstech.android.recipebook.ui.component.ScaffoldState
import rahulstech.android.recipebook.ui.theme.RecipeBookTheme
import androidx.core.net.toUri

@Preview(showBackground = true)
@Composable
fun RecipeInputScreenPreview() {

    val inputState = InputRecipeState(
        recipe = Recipe(
            title = "Chicken Drum Stick",
            medias = listOf(
                RecipeMedia(id = "1", "file:///android_asset/recipe1.jpg".toUri(),
                    "this is first line of caption\r\nthis is the second line of caption"
                ),
                RecipeMedia(id = "2", "file:///android_asset/recipe1.jpg".toUri(),
                    "this is first line of caption\r\nthis is the second line of caption"
                )
            )
        )
    )

    RecipeBookTheme {
        AppScaffold(ScaffoldState(), navigationCallback = {}) {
            RecipeInputContent(
                state = inputState,
                pickCoverPhotoCallback = {},
                pickMediaCallback = {},
                onEvent = {}
            )
        }
    }
}