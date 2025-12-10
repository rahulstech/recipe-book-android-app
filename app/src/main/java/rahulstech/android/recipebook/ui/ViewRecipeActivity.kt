package rahulstech.android.recipebook.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import rahulstech.android.dailyquotes.ui.theme.RecipeBookTheme
import rahulstech.android.recipebook.repository.Repositories
import rahulstech.android.recipebook.repository.model.Recipe

class ViewRecipeViewModel: ViewModel() {

    private val repo = Repositories.recipeRepository

    private val _recipe: Flow<Recipe?>? = null

    fun findRecipeById(id: String) {

    }
}

class ViewRecipeActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeBookTheme {

            }
        }
    }
}

@Composable
fun ViewRecipeScreen(modifier: Modifier = Modifier) {

}