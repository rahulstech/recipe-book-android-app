package rahulstech.android.recipebook.ui.screen.recipelist

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import rahulstech.android.recipebook.repository.model.Recipe
import rahulstech.android.recipebook.ui.UIState
import rahulstech.android.recipebook.ui.component.AppScaffold
import rahulstech.android.recipebook.ui.component.ScaffoldState
import rahulstech.android.recipebook.ui.theme.RecipeBookTheme


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

    RecipeBookTheme {
        AppScaffold(ScaffoldState(showNavUpAction = false), {}) {
            RecipesListScreen(
                recipesState = UIState.Success(previewRecipes),
                onRecipeClick = {},
            )
        }
    }
}