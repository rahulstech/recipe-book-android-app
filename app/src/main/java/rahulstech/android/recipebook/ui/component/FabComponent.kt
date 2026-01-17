package rahulstech.android.recipebook.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed interface FloatingActionButton {
    val onClick: ()-> Unit
    val enabled: Boolean
    val position: FabPosition

    data class Default(
        val icon: IconValue,
        val text: String,
        override val onClick: () -> Unit,
        override val position: FabPosition = FabPosition.End,
        override val enabled: Boolean = true
    ): FloatingActionButton
}

@Composable
fun FabComponent(fab: FloatingActionButton) {
    when(fab) {
        is FloatingActionButton.Default -> DefaultFabComponent(fab)
    }
}

@Composable
fun DefaultFabComponent(fab: FloatingActionButton.Default) {
    val icon = fab.icon
    val text = fab.text

    ExtendedFloatingActionButton(
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(percent = 50),
        onClick = fab.onClick
    ) {
        when(icon) {
            is IconValue.PainterIconValue -> {
                Icon(icon.painter, icon.contentDescription)
            }
            is IconValue.VectorIconValue -> {
                Icon(icon.vector, icon.contentDescription)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(text)
    }
}