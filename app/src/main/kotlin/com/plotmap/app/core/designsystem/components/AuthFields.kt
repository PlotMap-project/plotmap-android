package com.plotmap.app.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.plotmap.app.R
import com.plotmap.app.core.designsystem.LocalIsDarkTheme
import com.plotmap.app.core.designsystem.MilkChocolateCard
import com.plotmap.app.core.designsystem.TotalWhite

object AuthFields {
    private val fieldModifier = Modifier.fillMaxWidth().padding(top = 12.dp)
    private val fieldShape = RoundedCornerShape(18.dp)

    @Composable
    fun LoginFields(
        userName: String,
        password: String,
        onUserNameChange: (String) -> Unit,
        onPasswordChange: (String) -> Unit,
    ) {
        AuthFieldsContainer {
            AuthField(
                value = userName,
                onValueChange = onUserNameChange,
                labelRes = R.string.auth_login_username_or_email_label,
            )
            AuthField(
                value = password,
                onValueChange = onPasswordChange,
                labelRes = R.string.auth_password_label,
                visualTransformation = PasswordVisualTransformation(),
            )
        }
    }

    @Composable
    fun RegistrationFields(
        email: String,
        userName: String,
        password: String,
        passwordRepeat: String,
        isUsernameTaken: Boolean,
        passwordErrorMessage: String?,
        isPasswordValid: Boolean,
        isPasswordRepeatValid: Boolean,
        onEmailChange: (String) -> Unit,
        onUserNameChange: (String) -> Unit,
        onPasswordChange: (String) -> Unit,
        onPasswordRepeatChange: (String) -> Unit,
    ) {
        AuthFieldsContainer {
            AuthField(
                value = email,
                onValueChange = onEmailChange,
                labelRes = R.string.auth_email_label,
            )
            AuthField(
                value = userName,
                onValueChange = onUserNameChange,
                labelRes = R.string.auth_username_label,
                isError = isUsernameTaken,
            )
            if (isUsernameTaken) {
                AuthErrorText(textRes = R.string.auth_username_taken_error)
            }

            AuthField(
                value = password,
                onValueChange = onPasswordChange,
                labelRes = R.string.auth_password_label,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordErrorMessage != null,
            )
            if (passwordErrorMessage != null) {
                AuthErrorText(text = passwordErrorMessage)
            }

            if (isPasswordValid) {
                AuthField(
                    value = passwordRepeat,
                    onValueChange = onPasswordRepeatChange,
                    labelRes = R.string.auth_password_repeat_label,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passwordRepeat.isNotEmpty() && !isPasswordRepeatValid,
                )
                if (passwordRepeat.isNotEmpty() && !isPasswordRepeatValid) {
                    AuthErrorText(textRes = R.string.auth_password_mismatch_error)
                }
            }
        }
    }

    @Composable
    private fun AuthFieldsContainer(content: @Composable ColumnScope.() -> Unit) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }

    @Composable
    private fun AuthField(
        value: String,
        onValueChange: (String) -> Unit,
        labelRes: Int,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        isError: Boolean = false,
    ) {
        val isDarkTheme = LocalIsDarkTheme.current
        val containerColor = if (isDarkTheme) MilkChocolateCard else TotalWhite
        val fieldColors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
            )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            visualTransformation = visualTransformation,
            modifier = fieldModifier,
            singleLine = true,
            isError = isError,
            shape = fieldShape,
            colors = fieldColors,
        )
    }

    @Composable
    private fun AuthErrorText(textRes: Int) {
        Text(
            text = stringResource(textRes),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }

    @Composable
    private fun AuthErrorText(text: String) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
