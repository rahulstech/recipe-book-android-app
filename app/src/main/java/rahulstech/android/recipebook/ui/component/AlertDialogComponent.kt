package rahulstech.android.recipebook.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import rahulstech.android.recipebook.R

@Composable
fun YesNoDialog(onDismiss: ()-> Unit,
                title: String,
                onYes: ()-> Unit,
                onNo: ()-> Unit = {},
                message: String = "",
                actionYesLabel: String = stringResource(R.string.label_yes),
                actionNoLabel: String = stringResource(R.string.label_no),
                onClose: ()-> Unit = onDismiss
                )
{
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = {
                onClose()
                onNo()
            }) {
                Text(actionNoLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onClose()
                onYes()
            }) {
                Text(actionYesLabel)
            }
        }
    )
}