package rahulstech.android.recipebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import rahulstech.android.recipebook.ui.theme.RecipeBookTheme

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeBookTheme {
                MainNavigation()
            }
        }
    }
}