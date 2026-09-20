package com.example.cpen321application.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

data class SignedInUser(val displayName: String, val email: String)

/**
 * Opens the Google sign-in sheet and returns the signed-in user.
 * serverClientId must be the WEB OAuth client ID from the Google Auth Platform.
 */
suspend fun signInWithGoogle(context: Context, serverClientId: String): SignedInUser {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(serverClientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val response = CredentialManager.create(context).getCredential(context, request)
    val credential = response.credential

    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val name = googleCredential.displayName
            ?: listOfNotNull(googleCredential.givenName, googleCredential.familyName)
                .joinToString(" ")
        return SignedInUser(displayName = name, email = googleCredential.id)
    }
    error("Unexpected credential type: " + credential.type)
}
