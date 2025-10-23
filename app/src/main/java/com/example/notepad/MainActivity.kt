package com.example.notepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    // Inisialisasi ViewModel untuk Notepad
                    val noteViewModel: NoteViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "home") {

                        composable("home") {
                            MainScreen(navController)
                        }

                        composable("list") {
                            NoteListScreen(navController, noteViewModel)
                        }

                        composable("editor") {
                            EditorScreen(navController, noteViewModel)
                        }

                        composable("calculator") {
                            CalculatorScreen(navController)
                        }
                    }
                }
            }
        }
    }
}