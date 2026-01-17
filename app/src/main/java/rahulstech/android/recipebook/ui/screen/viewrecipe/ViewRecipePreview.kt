package rahulstech.android.recipebook.ui.screen.viewrecipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.repository.model.RecipeMedia
import rahulstech.android.recipebook.ui.component.AppScaffold
import rahulstech.android.recipebook.ui.component.ScaffoldState
import rahulstech.android.recipebook.ui.theme.RecipeBookTheme

@Preview(
    showBackground = true,
    device = PIXEL_7
)
@Composable
fun ViewRecipeContentPreview() {
    val recipe = Recipe(
        id = "recipe4",
        title = "Fourth Recipe",
        note = "This is my fourth recipe",
        coverPhoto = "file:///android_asset/recipe4.jpg".toUri(),
        medias = listOf(
            RecipeMedia("1", "file:///android_asset/recipe4.jpg".toUri(), "this is the caption for first media"),
            RecipeMedia("2", "file:///android_asset/recipe4.jpg".toUri(), "this is the caption for first media")
        ),
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
        AppScaffold(ScaffoldState(), {}) {
            RecipeContent(recipe)
        }
    }
}

@Preview(
    showBackground = true,
    device = PIXEL_7
)
@Composable
fun ViewRecipeScreenShimmerPreview() {
    RecipeBookTheme {
        AppScaffold(ScaffoldState(showTitleShimmer = true), {}) {
            RecipeShimmer()
        }
    }
}