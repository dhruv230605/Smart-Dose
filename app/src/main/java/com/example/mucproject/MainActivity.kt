package com.example.mucproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mucproject.ui.*
import com.example.mucproject.ui.theme.MUCProjectTheme
import com.example.mucproject.data.UserRole

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MUCProjectTheme {
                val viewModel: SmartDoseViewModel = viewModel()
                val session by viewModel.userSession.collectAsState(null)
                val navController = rememberNavController()

                LaunchedEffect(session) {
                    if (session?.isLoggedIn == true) {
                        val route = when (session?.role) {
                            UserRole.ELDER -> "elder"
                            UserRole.CAREGIVER -> "caregiver"
                            else -> "login"
                        }
                        navController.navigate(route) {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                // Use Scaffold to handle system bars padding globally
                Scaffold(
                    contentWindowInsets = WindowInsets.statusBars
                ) { innerPadding ->
                    NavHost(
                        navController = navController, 
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") { LoginScreen(viewModel) }
                        composable("elder") { ElderScreen(viewModel, onNavigateToSettings = { navController.navigate("settings") }) }
                        composable("caregiver") { CaregiverScreen(viewModel) }
                        composable("settings") { SettingsScreen(viewModel, onBack = { navController.popBackStack() }) }
                    }
                }
            }
        }
    }
}
