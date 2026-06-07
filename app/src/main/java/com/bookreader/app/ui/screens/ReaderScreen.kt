package com.bookreader.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookreader.app.ui.theme.ReadingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showControls by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Reset scroll when page changes
    LaunchedEffect(state.currentPage) {
        scrollState.scrollTo(0)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val readingTheme = when (state.preferences.readingTheme) {
        "DARK" -> ReadingTheme.DARK
        "SEPIA" -> ReadingTheme.SEPIA
        else -> ReadingTheme.LIGHT
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading) {
                // Loading state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading book... ${(state.loadingProgress * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = state.loadingProgress,
                        modifier = Modifier.width(200.dp)
                    )
                }
            } else if (state.book == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            "Unable to open book",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            state.errorMessage ?: "The selected book could not be loaded.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = onBack) {
                            Text("Go back")
                        }
                    }
                }
            } else {
                // Reading content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (dragAmount < -50) viewModel.nextPage()
                                else if (dragAmount > 50) viewModel.previousPage()
                            }
                        }
                ) {
                    // Top bar (hide/show on tap)
                    AnimatedVisibility(
                        visible = showControls,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        TopAppBar(
                            title = {
                                Text(
                                    state.book?.title ?: "",
                                    maxLines = 1,
                                    fontSize = 16.sp
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.toggleSettings() }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        )
                    }

                    // Page content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable { showControls = !showControls }
                    ) {
                        val pageText = state.currentPageText

                        if (pageText.isBlank()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "(This page appears to be empty or contains only images)",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        } else {
                            Text(
                                text = pageText,
                                fontSize = state.preferences.fontSize.sp,
                                lineHeight = (state.preferences.fontSize * state.preferences.lineSpacing).sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            )
                        }
                    }

                    // Bottom navigation bar
                    AnimatedVisibility(
                        visible = showControls,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        BottomPageBar(
                            currentPage = state.currentPage,
                            totalPages = state.book?.totalPages ?: 0,
                            onPrevious = { viewModel.previousPage() },
                            onNext = { viewModel.nextPage() },
                            onPageSlide = { viewModel.goToPage(it) }
                        )
                    }
                }
            }

            // Settings panel overlay
            if (state.showSettings) {
                SettingsPanel(
                    preferences = state.preferences,
                    currentTheme = readingTheme,
                    onFontSizeChange = { viewModel.setFontSize(it) },
                    onThemeChange = { viewModel.setTheme(it) },
                    onLineSpacingChange = { viewModel.setLineSpacing(it) },
                    onDismiss = { viewModel.toggleSettings() }
                )
            }
        }
    }
}

@Composable
fun BottomPageBar(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPageSlide: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Page slider
            if (totalPages > 1) {
                Slider(
                    value = currentPage.toFloat(),
                    onValueChange = { onPageSlide(it.toInt()) },
                    valueRange = 0f..(totalPages - 1).toFloat().coerceAtLeast(0f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = currentPage > 0
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous page")
                }

                Text(
                    "${currentPage + 1} / $totalPages",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                IconButton(
                    onClick = onNext,
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next page")
                }
            }
        }
    }
}

@Composable
fun SettingsPanel(
    preferences: com.bookreader.app.data.model.ReadingPreferences,
    currentTheme: ReadingTheme,
    onFontSizeChange: (Float) -> Unit,
    onThemeChange: (ReadingTheme) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clickable { /* prevent dismiss */ },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Reading Settings", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(20.dp))

                // Font size
                Text("Font Size: ${preferences.fontSize.toInt()}sp",
                    style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = preferences.fontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 12f..28f,
                    steps = 7
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Line spacing
                Text("Line Spacing: ${String.format("%.1f", preferences.lineSpacing)}x",
                    style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = preferences.lineSpacing,
                    onValueChange = onLineSpacingChange,
                    valueRange = 1.2f..2.4f,
                    steps = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Theme picker
                Text("Theme", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeButton(
                        label = "Light",
                        bg = Color(0xFFFFFFFF),
                        textColor = Color(0xFF1A1A1A),
                        selected = currentTheme == ReadingTheme.LIGHT,
                        onClick = { onThemeChange(ReadingTheme.LIGHT) }
                    )
                    ThemeButton(
                        label = "Sepia",
                        bg = Color(0xFFF5E6C8),
                        textColor = Color(0xFF3E2723),
                        selected = currentTheme == ReadingTheme.SEPIA,
                        onClick = { onThemeChange(ReadingTheme.SEPIA) }
                    )
                    ThemeButton(
                        label = "Dark",
                        bg = Color(0xFF121212),
                        textColor = Color(0xFFE8E8E8),
                        selected = currentTheme == ReadingTheme.DARK,
                        onClick = { onThemeChange(ReadingTheme.DARK) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ThemeButton(
    label: String,
    bg: Color,
    textColor: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
    val borderWidth = if (selected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = textColor, fontSize = 13.sp)
    }
}
