package com.example.p2p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.p2p.auth.AuthViewModel
import com.example.p2p.auth.LoginScreen
import com.example.p2p.auth.RegistrationScreen
import com.example.p2p.ui.home.HomeScreen
import com.example.p2p.ui.theme.P2PTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            P2PTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val scope = rememberCoroutineScope()

                    val startDestination = if (authViewModel.getRememberedUser() != null) "home" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                onLoginClick = { username, password, rememberMe ->
                                    scope.launch {
                                        if (authViewModel.loginUser(username, password, rememberMe)) {
                                            navController.navigate("home")
                                        } else {
                                            // TODO: Show error message to user (e.g., Snackbar)
                                            println("Login failed: Invalid username or password")
                                        }
                                    }
                                },
                                onRegisterClick = { navController.navigate("register") }
                            )
                        }
                        composable("register") {
                            RegistrationScreen(
                                onRegisterClick = { username, password, photoUri ->
                                    authViewModel.registerUser(username, password, photoUri)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(onLogoutClick = {
                                scope.launch {
                                    authViewModel.clearRememberedUser()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
