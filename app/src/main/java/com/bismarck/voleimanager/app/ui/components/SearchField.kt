package com.bismarck.voleimanager.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Rounded, compact search text field built on top of [BasicTextField] +
 * [OutlinedTextFieldDefaults.DecorationBox], allowing a custom [height] (e.g. 48.dp) without
 * clipping the input text. The standard [androidx.compose.material3.OutlinedTextField] reserves
 * a fixed 16.dp vertical content padding that doesn't fit within heights below 56.dp.
 */
@Composable
fun RoundedSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    onFocusChanged: ((FocusState) -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    var fieldModifier = modifier.height(height)
    if (focusRequester != null) {
        fieldModifier = fieldModifier.focusRequester(focusRequester)
    }
    if (onFocusChanged != null) {
        fieldModifier = fieldModifier.onFocusChanged(onFocusChanged)
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = fieldModifier,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                colors = OutlinedTextFieldDefaults.colors(),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(),
                        shape = CircleShape,
                    )
                }
            )
        }
    )
}
