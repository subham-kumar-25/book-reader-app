package com.bookreader.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bookreader.app.data.model.Book
import com.bookreader.app.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: BookRepository) : ViewModel() {

    val books = repository.getAllBooks()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun addBook(uri: Uri, title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addBook(uri, title)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add book: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                repository.deleteBook(book)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove book: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class LibraryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(BookRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
