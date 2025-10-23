package com.example.notepad

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ContentCopy // Import untuk ikon Copy
import androidx.compose.material.icons.filled.ContentPaste // Import untuk ikon Paste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
// import androidx.compose.ui.text.input.KeyboardOptions telah dihapus
import androidx.compose.ui.graphics.SolidColor
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalClipboardManager // Import untuk Clipboard Manager
import androidx.compose.foundation.text.KeyboardOptions // Import tunggal untuk OutlinedTextField (jika ada)
import androidx.compose.ui.text.TextStyle // Import untuk styling judul

enum class TextTransform { NONE, UPPERCASE, LOWERCASE, CAPITALIZE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    navController: NavHostController,
    noteViewModel: NoteViewModel
) {
    // Inisialisasi Clipboard Manager
    val clipboardManager = LocalClipboardManager.current

    // Pastikan catatan di index 0 ada saat pertama kali dimuat
    LaunchedEffect(Unit) {
        if (noteViewModel.notes.isEmpty()) {
            noteViewModel.addNote("", "")
        }
    }
    val note = noteViewModel.notes.getOrNull(0) ?: Note("", "")

    var title by remember { mutableStateOf(note.title) }

    // textFieldValue adalah state untuk BasicTextField (menyimpan cursor & selection)
    var textFieldValue by remember { mutableStateOf(TextFieldValue(note.content)) }

    // annotatedText menyimpan teks + span styles (bold/italic)
    var annotatedText by remember { mutableStateOf(AnnotatedString(textFieldValue.text)) }

    // transform mode
    var textTransform by remember { mutableStateOf(TextTransform.NONE) }

    // Auto-save saat keluar dari layar
    DisposableEffect(key1 = noteViewModel) {
        onDispose {
            noteViewModel.updateNote(0, title, annotatedText.text)
        }
    }

    // Sync: ketika user mengetik / hapus, kita sinkronkan annotatedText agar span di luar perubahan tetap ada.
    fun syncAnnotated(oldPlain: String, currentAnnotated: AnnotatedString, newPlain: String): AnnotatedString {
        if (oldPlain == newPlain) return currentAnnotated

        val prefix = run {
            val min = minOf(oldPlain.length, newPlain.length)
            var i = 0
            while (i < min && oldPlain[i] == newPlain[i]) i++
            i
        }

        val suffix = run {
            val oldRem = oldPlain.length - prefix
            val newRem = newPlain.length - prefix
            var i = 0
            while (i < oldRem && i < newRem && oldPlain[oldPlain.length - 1 - i] == newPlain[newPlain.length - 1 - i]) i++
            i
        }

        val inserted = newPlain.substring(prefix, newPlain.length - suffix)

        val builder = AnnotatedString.Builder()

        fun appendRangePreserveSpans(s: Int, e: Int) {
            if (s >= e) return
            val substring = currentAnnotated.text.substring(s, e)
            val beforeLen = builder.length
            builder.append(substring)
            currentAnnotated.spanStyles.forEach { range ->
                val rs = range.start
                val re = range.end
                if (rs < e && re > s) {
                    val rstart = maxOf(rs, s) - s + beforeLen
                    val rend = minOf(re, e) - s + beforeLen
                    builder.addStyle(range.item, rstart, rend)
                }
            }
        }

        appendRangePreserveSpans(0, prefix)
        builder.append(inserted)
        appendRangePreserveSpans(oldPlain.length - suffix, oldPlain.length)

        return builder.toAnnotatedString()
    }

    // Replace range [start,end) with replacement (string), preserving spans outside the replaced range.
    fun replaceRangePreserveSpans(
        currentAnnotated: AnnotatedString,
        start: Int,
        end: Int,
        replacement: String
    ): AnnotatedString {
        val builder = AnnotatedString.Builder()
        fun appendRange(s: Int, e: Int) {
            if (s >= e) return
            val substr = currentAnnotated.text.substring(s, e)
            val before = builder.length
            builder.append(substr)
            currentAnnotated.spanStyles.forEach { range ->
                val rs = range.start
                val re = range.end
                if (rs < e && re > s) {
                    val rstart = maxOf(rs, s) - s + before
                    val rend = minOf(re, e) - s + before
                    builder.addStyle(range.item, rstart, rend)
                }
            }
        }
        appendRange(0, start)
        builder.append(replacement)
        appendRange(end, currentAnnotated.text.length)
        return builder.toAnnotatedString()
    }


    // apply span style (bold/italic) to selection range from textFieldValue
    fun applySpanToSelection(
        tfv: TextFieldValue,
        currentAnnotated: AnnotatedString,
        spanStyle: SpanStyle
    ): AnnotatedString {
        val sel = tfv.selection
        if (sel.collapsed) return currentAnnotated
        val start = sel.start.coerceAtLeast(0)
        val end = sel.end.coerceAtMost(currentAnnotated.text.length)
        val builder = AnnotatedString.Builder()
        fun appendRange(s: Int, e: Int) {
            if (s >= e) return
            val substr = currentAnnotated.text.substring(s, e)
            val before = builder.length
            builder.append(substr)
            currentAnnotated.spanStyles.forEach { range ->
                val rs = range.start
                val re = range.end
                if (rs < e && re > s) {
                    val rstart = maxOf(rs, s) - s + before
                    val rend = minOf(re, e) - s + before
                    builder.addStyle(range.item, rstart, rend)
                }
            }
        }
        appendRange(0, start)
        val selected = currentAnnotated.text.substring(start, end)
        val selBefore = builder.length
        builder.append(selected)
        currentAnnotated.spanStyles.forEach { range ->
            val rs = range.start
            val re = range.end
            if (rs < end && re > start) {
                val rstart = maxOf(rs, start) - start + selBefore
                val rend = minOf(re, end) - start + selBefore
                builder.addStyle(range.item, rstart, rend)
            }
        }
        builder.addStyle(spanStyle, selBefore, selBefore + selected.length)
        appendRange(end, currentAnnotated.text.length)
        return builder.toAnnotatedString()
    }

    // transform selected range with function f (uppercase/lower/capitalize)
    fun transformSelectionAndApply(
        tfv: TextFieldValue,
        currentAnnotated: AnnotatedString,
        transformFn: (String) -> String
    ): Pair<TextFieldValue, AnnotatedString> {
        val sel = tfv.selection
        if (sel.collapsed) return Pair(tfv, currentAnnotated)
        val start = sel.start.coerceAtLeast(0)
        val end = sel.end.coerceAtMost(currentAnnotated.text.length)

        val selected = currentAnnotated.text.substring(start, end)
        val replaced = transformFn(selected)

        // 1. Update annotatedText
        val newAnnotated = replaceRangePreserveSpans(currentAnnotated, start, end, replaced)

        // 2. Update textFieldValue (penting untuk text dan selection)
        val newText = tfv.text.replaceRange(start, end, replaced)
        val newTfV = TextFieldValue(
            text = newText,
            // Pertahankan seleksi yang sama (teks yang ditransformasi tetap terseleksi)
            selection = TextRange(start, start + replaced.length)
        )
        return Pair(newTfV, newAnnotated)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Catatan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Manual Save and Back
                        noteViewModel.updateNote(0, title, annotatedText.text)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(modifier = Modifier.padding(start = 8.dp)) {
                    // Tombol BOLD
                    IconButton(onClick = {
                        annotatedText = applySpanToSelection(textFieldValue, annotatedText, SpanStyle(fontWeight = FontWeight.Bold))
                    }) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                    }
                    // Tombol ITALIC
                    IconButton(onClick = {
                        annotatedText = applySpanToSelection(textFieldValue, annotatedText, SpanStyle(fontStyle = FontStyle.Italic))
                    }) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Tombol COPY
                    IconButton(onClick = {
                        val selectedText = textFieldValue.text.substring(
                            textFieldValue.selection.min,
                            textFieldValue.selection.max
                        )
                        if (selectedText.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(selectedText))
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }

                    // Tombol PASTE
                    IconButton(onClick = {
                        val pastedAnnotated = clipboardManager.getText()
                        val pastedText = pastedAnnotated?.text ?: ""

                        if (pastedText.isNotEmpty()) {
                            val currentSelection = textFieldValue.selection
                            val start = currentSelection.start
                            val end = currentSelection.end

                            // 1. Update annotatedText (preserving styles outside the replaced range)
                            annotatedText = replaceRangePreserveSpans(
                                currentAnnotated = annotatedText,
                                start = start,
                                end = end,
                                replacement = pastedText
                            )

                            // 2. Update textFieldValue (untuk text dan kursor)
                            val newText = textFieldValue.text.replaceRange(start, end, pastedText)
                            val newCursorPosition = start + pastedText.length

                            textFieldValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newCursorPosition)
                            )
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Tombol Transform Cycle & Apply (Digabung)
                    IconButton(onClick = {
                        val (newTfV, newAnnotated) = when (textTransform) {
                            TextTransform.NONE -> {
                                textTransform = TextTransform.UPPERCASE
                                transformSelectionAndApply(textFieldValue, annotatedText) { it.uppercase() }
                            }
                            TextTransform.UPPERCASE -> {
                                textTransform = TextTransform.LOWERCASE
                                transformSelectionAndApply(textFieldValue, annotatedText) { it.lowercase() }
                            }
                            TextTransform.LOWERCASE -> {
                                textTransform = TextTransform.CAPITALIZE
                                transformSelectionAndApply(textFieldValue, annotatedText) {
                                    it.split(" ").joinToString(" ") { w -> w.replaceFirstChar { c -> c.uppercase() } }
                                }
                            }
                            TextTransform.CAPITALIZE -> {
                                textTransform = TextTransform.NONE
                                transformSelectionAndApply(textFieldValue, annotatedText) { it } // No-op transform
                            }
                        }
                        textFieldValue = newTfV
                        annotatedText = newAnnotated

                    }) {
                        val label = when (textTransform) {
                            TextTransform.UPPERCASE -> "UP"
                            TextTransform.LOWERCASE -> "lo"
                            TextTransform.CAPITALIZE -> "Ca"
                            else -> "Aa"
                        }
                        Text(label)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- BAGIAN JUDUL DENGAN BASIC TEXT FIELD (TANPA GARIS KOTAK) ---
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                decorationBox = { innerTextField ->
                    // Placeholder (opsional)
                    if (title.isEmpty()) {
                        Text(
                            "Judul (Tanpa Garis Kotak)",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }
            )
            // ----------------------------------------------------------------

            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // placeholder if empty
                    if (annotatedText.text.isEmpty()) {
                        Text("Tulis catatan di sini...", color = Color.Gray)
                    }

                    // Render teks ber-style
                    Text(
                        text = annotatedText,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )

                    // BasicTextField menangkap input, kursor, dan seleksi
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            // sync annotatedText preserving existing spans outside the change range
                            annotatedText = syncAnnotated(textFieldValue.text, annotatedText, newValue.text)
                            textFieldValue = newValue
                        },
                        modifier = Modifier
                            .fillMaxSize(),
                        textStyle = LocalTextStyle.current.copy(color = Color.Transparent),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        // Parameter keyboardOptions dihilangkan, menggunakan default Compose.
                        decorationBox = { inner ->
                            Box(modifier = Modifier.fillMaxSize()) { inner() }
                        }
                    )
                }
            }
        }
    }
}
