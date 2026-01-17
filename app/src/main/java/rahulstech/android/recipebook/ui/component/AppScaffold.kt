package rahulstech.android.recipebook.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rahulstech.android.recipebook.NavigationCallback
import rahulstech.android.recipebook.R
import rahulstech.android.recipebook.ui.theme.ThemeTopBarColors

data class ScaffoldState(
    val title: String = "",
    val showTitleShimmer: Boolean = false,
    val actions: List<TopBarAction> = emptyList(),
    val showNavUpAction: Boolean = true,
    val navUpAction: NavUpAction = NavUpAction(),
    val fab: FloatingActionButton? = null,
)

typealias ScaffoldStateCallback = (ScaffoldState)-> Unit

@Composable
fun AppScaffold(state: ScaffoldState,
                navigationCallback: NavigationCallback,
                content: @Composable (SnackBarCallback)-> Unit,
                )
{
    var snackBarHostState by remember { mutableStateOf(SnackbarHostState()) }
    var showOverflowMenu by rememberSaveable { mutableStateOf(false) }
    val snackBarEventChannel = remember { Channel<SnackBarEvent>(Channel.BUFFERED) }
    val coroutineScope = rememberCoroutineScope()
    val iconActions: List<TopBarAction.IconAction> = state.actions.filter { it is TopBarAction.IconAction }.map { it as TopBarAction.IconAction }
    val overflowActions: List<TopBarAction.OverflowAction> = state.actions.filter { it is TopBarAction.OverflowAction }.map { it as TopBarAction.OverflowAction }

    LaunchedEffect(snackBarEventChannel) {
        snackBarEventChannel.receiveAsFlow().collect { event ->
            val result = snackBarHostState.showSnackbar(
                message = event.message,
                actionLabel = if (event.hasAction) event.actionLabel else null,
                withDismissAction = true,
                duration = if (event.hasAction) SnackbarDuration.Indefinite else SnackbarDuration.Short
            )
            if (event.hasAction) {
                when (result) {
                    SnackbarResult.ActionPerformed -> { event.onAction() }
                    SnackbarResult.Dismissed -> {}
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(data)
            }
        },
        topBar = {
            TopAppBar(
                colors = ThemeTopBarColors,
                title = {
                    if (state.showTitleShimmer) {
                        Box(modifier = Modifier.size(width = 120.dp, height = 24.dp)
                            .shimmer(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)))
                    }
                    else {
                        Text( state.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    for (action in iconActions) {
                        IconActionComponent(action)
                    }

                    if (overflowActions.isNotEmpty()) {
                        OverflowActionComponent({ showOverflowMenu = true })
                    }
                },
                navigationIcon = {
                    if (state.showNavUpAction) {
                        val iconAction = TopBarAction.IconAction(
                            icon = state.navUpAction.icon,
                            onClick = { navigationCallback(state.navUpAction.onClick()) }
                        )
                        IconActionComponent(iconAction)
                    }
                },
            )
        },
        floatingActionButton = {
            if (state.fab != null) {
                FabComponent(state.fab)
            }
        },
        floatingActionButtonPosition = state.fab?.position ?: FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().widthIn(max = dimensionResource(R.dimen.max_content_width)).imePadding()
            ) {
                content(
                    {
                        coroutineScope.launch { snackBarEventChannel.send(it) }
                    }
                )
            }
        }
    }

    if (showOverflowMenu) {
        OverflowMenuComponent(
            actions = overflowActions,
            onDismiss = { showOverflowMenu = false },
        )
    }
}