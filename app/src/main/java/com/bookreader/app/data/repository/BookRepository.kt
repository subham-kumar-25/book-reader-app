package com.bookreader.app.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bookreader.app.data.model.Book
import com.bookreader.app.data.model.ReadingPreferences
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reading_preferences")

class BookRepository(private val context: Context) {

    private val bookDao = BookDatabase.getDatabase(context).bookDao()

    // Preference keys
    private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
    private val THEME_KEY = stringPreferencesKey("reading_theme")
    private val LINE_SPACING_KEY = floatPreferencesKey("line_spacing")

    // --- Book Library ---

    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()

    suspend fun getBookById(id: Long): Book? = bookDao.getBookById(id)

    suspend fun addBook(uri: Uri, title: String): Long = withContext(Dispatchers.IO) {
        val fileName = "${System.currentTimeMillis()}_${title.replace(" ", "_")}.pdf"
        val internalFile = java.io.File(context.filesDir, fileName)

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to access selected PDF file.")

        inputStream.use { input ->
            internalFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val pageCount = getPdfPageCount(internalFile.absolutePath)

        val book = Book(
            title = title,
            filePath = internalFile.absolutePath,
            totalPages = pageCount
        )
        bookDao.insertBook(book)
    }

    suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
        val file = java.io.File(book.filePath)
        if (file.exists()) {
            try {
                file.delete()
            } catch (_: Throwable) {
                // ignore delete failures; preserve app stability
            }
        }
        bookDao.deleteBook(book)
    }

    suspend fun updateReadingProgress(bookId: Long, page: Int) {
        bookDao.updateProgress(bookId, page)
    }

    // --- PDF Parsing ---

    private suspend fun getPdfPageCount(filePath: String): Int = withContext(Dispatchers.IO) {
        val file = java.io.File(filePath)
        if (!file.exists()) return@withContext 0

        return@withContext try {
            PDFBoxResourceLoader.init(context)
            PDDocument.load(file).use { document ->
                document.numberOfPages
            }
        } catch (t: Throwable) {
            0
        }
    }

    suspend fun extractTextFromPage(filePath: String, pageIndex: Int): String =
        withContext(Dispatchers.IO) {
            val file = java.io.File(filePath)
            if (!file.exists()) return@withContext "Error reading page: file not found"

            try {
                PDFBoxResourceLoader.init(context)
                PDDocument.load(file).use { document ->
                    val stripper = PDFTextStripper().apply {
                        startPage = pageIndex + 1
                        endPage = pageIndex + 1
                    }
                    stripper.getText(document).trim()
                }
            } catch (t: Throwable) {
                "Error reading page: ${t.message}"
            }
        }

    suspend fun extractAllText(filePath: String, onProgress: (Int, Int) -> Unit): List<String> =
        withContext(Dispatchers.IO) {
            val file = java.io.File(filePath)
            if (!file.exists()) return@withContext listOf("Error loading book: file not found")

            try {
                PDFBoxResourceLoader.init(context)
                PDDocument.load(file).use { document ->
                    val totalPages = document.numberOfPages
                    val pages = mutableListOf<String>()

                    for (i in 0 until totalPages) {
                        val stripper = PDFTextStripper().apply {
                            startPage = i + 1
                            endPage = i + 1
                        }
                        pages.add(stripper.getText(document).trim())
                        onProgress(i + 1, totalPages)
                    }

                    pages
                }
            } catch (t: Throwable) {
                listOf("Error loading book: ${t.message}")
            }
        }

    // --- Reading Preferences ---

    fun getReadingPreferences(): Flow<ReadingPreferences> =
        context.dataStore.data.map { prefs ->
            ReadingPreferences(
                fontSize = prefs[FONT_SIZE_KEY] ?: 16f,
                readingTheme = prefs[THEME_KEY] ?: "LIGHT",
                lineSpacing = prefs[LINE_SPACING_KEY] ?: 1.6f
            )
        }

    suspend fun saveFontSize(size: Float) {
        context.dataStore.edit { it[FONT_SIZE_KEY] = size }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun saveLineSpacing(spacing: Float) {
        context.dataStore.edit { it[LINE_SPACING_KEY] = spacing }
    }
}
