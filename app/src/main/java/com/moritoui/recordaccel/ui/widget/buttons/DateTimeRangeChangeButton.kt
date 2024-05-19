package com.moritoui.recordaccel.ui.widget.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.moritoui.recordaccel.R
import com.moritoui.recordaccel.model.TimeTerm

@Composable
fun DateTimeRangeChangeButton(
    selectTimeTerm: TimeTerm,
    onClickTerm: (TimeTerm) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TimeTerm.values().forEach {
            OutlinedButton(
                onClick = {
                    onClickTerm(it)
                },
                colors = when (selectTimeTerm) {
                    it -> ButtonDefaults.buttonColors()
                    else -> ButtonDefaults.outlinedButtonColors()
                },
            ) {
                Text(
                    stringResource(R.string.date_change_buttontext, it.text),
                )
            }
        }
    }
}
