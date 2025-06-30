package com.example.p2p.auth

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.p2p.data.AppDatabase
import com.example.p2p.data.User
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "user_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        application,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun hashPassword(password: String): String {
        return password.hashCode().toString()
    }

    fun registerUser(username: String, password: String, photoUri: Uri?) {
        viewModelScope.launch {
            val passwordHash = hashPassword(password)
            val user = User(username = username, passwordHash = passwordHash, photoUri = photoUri?.toString())
            userDao.insertUser(user)
            Log.d("AuthViewModel", "User registered: $username")
        }
    }

    suspend fun loginUser(username: String, password: String, rememberMe: Boolean): Boolean {
        val user = userDao.getUserByUsername(username)
        val hashedPassword = hashPassword(password)
        val success = user != null && user.passwordHash == hashedPassword

        if (success && rememberMe) {
            sharedPreferences.edit()
                .putString("remembered_username", username)
                .apply()
        } else if (!rememberMe) {
            clearRememberedUser()
        }
        return success
    }

    fun getRememberedUser(): String? {
        return sharedPreferences.getString("remembered_username", null)
    }

    fun clearRememberedUser() {
        sharedPreferences.edit()
            .remove("remembered_username")
            .apply()
    }

    fun authenticateWithBiometrics(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
        Log.d("AuthViewModel", "Biometric authentication initiated (placeholder)")
        onSuccess()
    }
}
