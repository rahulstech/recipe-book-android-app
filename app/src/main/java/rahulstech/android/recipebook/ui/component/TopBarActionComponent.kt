package rahulstech.android.recipebook.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import rahulstech.android.recipebook.NavigationEvent


sealed interface IconValue {

    val contentDescription: String?

    data class PainterIconValue(
        val painter: Painter,
        override val contentDescription: String? = null,
    ): IconValue

    data class VectorIconValue(
        val vector: ImageVector,
        override val contentDescription: String? = null
    ): IconValue
}

sealed interface TopBarAction {
    val onClick: () -> Unit

    val enabled: Boolean

    data class OverflowAction(
        val text: String,
        override val onClick: () -> Unit,
        override val enabled: Boolean = true,
    ): TopBarAction

    data class IconAction(
        val icon: IconValue,
        override val onClick: () -> Unit,
        override val enabled: Boolean = true,
    ): TopBarAction
}

data class NavUpAction(val icon: IconValue = IconValue.VectorIconValue(Icons.AutoMirrored.Default.ArrowBack, null),
                       val onClick: () -> NavigationEvent = { NavigationEvent.Exit() })

@Composable
fun OverflowActionComponent(onClick: () -> Unit, enabled: Boolean = true) {
    IconActionComponent(TopBarAction.IconAction(
        IconValue.VectorIconValue(Icons.Default.MoreVert), onClick,enabled
    ))
}

@Composable
fun OverflowMenuComponent(actions: List<TopBarAction.OverflowAction>, onDismiss: () -> Unit, onClose: ()-> Unit = onDismiss) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().sizeIn(maxWidth = 360.dp, minHeight = 100.dp, maxHeight = 400.dp).padding(top = 16.dp)
        ) {
            itemsIndexed(items = actions) { index, action ->
                if (index > 0) {
                    HorizontalDivider()
                }

                OverflowMenuItemComponent(action.text, action.enabled) {
                    onClose()
                    action.onClick()
                }
            }
        }
    }
}

@Composable
fun OverflowMenuItemComponent(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 12.dp).clickable(enabled, onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text)
    }
}

@Composable
fun IconActionComponent(action: TopBarAction.IconAction) {
    IconButton(
        enabled = action.enabled,
        onClick = action.onClick
    ) {
        when(action.icon) {
            is IconValue.VectorIconValue -> {
                Icon(action.icon.vector, contentDescription = action.icon.contentDescription)
            }
            is IconValue.PainterIconValue -> {
                Icon(action.icon.painter, contentDescription = action.icon.contentDescription)
            }
        }
    }
}

