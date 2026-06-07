package com.bookreader.app.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bookreader.app.data.model.Book
import com.bookreader.app.data.model.ReadingPreferences
import com.bookreader.app.data.repository.BookRepository
import com.bookreader.app.ui.theme.ReadingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReaderUiState(
    val book: Book? = null,
    val currentPageText: String = "",
    val currentPage: Int = 0,
    val isLoading: Boolean = true,
    val loadingProgress: Float = 0f,
    val preferences: ReadingPreferences = ReadingPreferences(),
    val showSettings: Boolean = false,
    val errorMessage: String? = null
)

class ReaderViewModel(
    private val repository: BookRepository,
    private val bookId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadBook()
        observePreferences()
    }

    private fun loadBook() {
        viewModelScope.launch {
            try {
                val book = repository.getBookById(bookId)
                if (book == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Book not found",
                        currentPageText = ""
                    )
                    return@launch
                }

                val safePage = book.currentPage.coerceIn(0, (book.totalPages - 1).coerceAtLeast(0))
                _uiState.value = _uiState.value.copy(
                    book = book,
                    currentPage = safePage,
                    isLoading = true,
                    loadingProgress = 0f,
                    currentPageText = "",
                    errorMessage = null
                )

                val pageText = repository.extractTextFromPage(book.filePath, safePage)
                val isError = pageText.startsWith("Error")

                _uiState.value = _uiState.value.copy(
                    currentPageText = pageText,
                    isLoading = false,
                    errorMessage = if (isError) pageText else null
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load book: ${t.message ?: "unknown error"}",
                    currentPageText = ""
                )
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            repository.getReadingPreferences().collect { prefs ->
                _uiState.value = _uiState.value.copy(preferences = prefs)
            }
        }
    }

    fun goToPage(page: Int) {
        val total = _uiState.value.book?.totalPages ?: 0
        val clamped = page.coerceIn(0, (total - 1).coerceAtLeast(0))
        _uiState.value = _uiState.value.copy(
            currentPage = clamped,
            isLoading = true,
            loadingProgress = 0f,
            currentPageText = "",
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                repository.updateReadingProgress(bookId, clamped)
                val bookPath = _uiState.value.book?.filePath
                if (bookPath != null) {
                    val pageText = repository.extractTextFromPage(bookPath, clamped)
                    val isError = pageText.startsWith("Error")
                    _uiState.value = _uiState.value.copy(
                        currentPageText = pageText,
                        isLoading = false,
                        errorMessage = if (isError) pageText else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Book file path is unavailable"
                    )
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load page: ${t.message ?: "unknown error"}",
                    currentPageText = ""
                )
            }
        }
    }

    fun nextPage() = goToPage(_uiState.value.currentPage + 1)
    fun previousPage() = goToPage(_uiState.value.currentPage - 1)

    fun toggleSettings() {
        _uiState.value = _uiState.value.copy(showSettings = !_uiState.value.showSettings)
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch { repository.saveFontSize(size) }
    }

    fun setTheme(theme: ReadingTheme) {
        viewModelScope.launch { repository.saveTheme(theme.name) }
    }

    fun setLineSpacing(spacing: Float) {
        viewModelScope.launch { repository.saveLineSpacing(spacing) }
    }
}
class ReaderViewModelFactory(
    private val context: Context,
    private val bookId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReaderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReaderViewModel(BookRepository(context), bookId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
