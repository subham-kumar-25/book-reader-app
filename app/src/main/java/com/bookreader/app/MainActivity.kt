package com.bookreader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookreader.app.data.repository.BookRepository
import com.bookreader.app.ui.screens.*
import com.bookreader.app.ui.theme.BookReaderTheme
import com.bookreader.app.ui.theme.ReadingTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookReaderApp()
        }
    }
}

@Composable
fun BookReaderApp() {
    val context = LocalContext.current
    val repository = remember { BookRepository(context.applicationContext) }
    var appError by remember { mutableStateOf<String?>(null) }

    val content: @Composable () -> Unit = {
        val navController = rememberNavController()

        // Observe global reading theme for app-level theming
        val themeStr by repository.getReadingPreferences()
            .map { it.readingTheme }
            .collectAsState(initial = "LIGHT")

        val readingTheme = when (themeStr) {
            "DARK" -> ReadingTheme.DARK
            "SEPIA" -> ReadingTheme.SEPIA
            else -> ReadingTheme.LIGHT
        }

        BookReaderTheme(readingTheme = readingTheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = "library"
                ) {
                    composable("library") {
                        val libraryViewModel: LibraryViewModel = viewModel(
                            factory = LibraryViewModelFactory(context)
                        )
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            onBookClick = { bookId ->
                                navController.navigate("reader/$bookId")
                            }
                        )
                    }

                    composable(
                        route = "reader/{bookId}",
                        arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
                        val readerViewModel: ReaderViewModel = viewModel(
                            factory = ReaderViewModelFactory(context, bookId)
                        )
                        ReaderScreen(
                            viewModel = readerViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    if (appError == null) {
        content()
    } else {
        AppFallbackScreen(errorMessage = appError!!, onRetry = { appError = null })
    }
}

@Composable
fun AppFallbackScreen(errorMessage: String, onRetry: () -> Unit) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "Oops! Something went wrong.",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
            )
            androidx.compose.material3.Text(
                text = errorMessage,
                modifier = Modifier.padding(top = 12.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            androidx.compose.material3.Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                androidx.compose.material3.Text("Try again")
            }
        }
    }
}
