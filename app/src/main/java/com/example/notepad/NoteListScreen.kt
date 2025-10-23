package com.example.notepad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color // Import untuk warna kustom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(navController: NavHostController, noteViewModel: NoteViewModel) {
    // Pastikan selalu ada catatan di indeks 0 untuk ditampilkan
    LaunchedEffect(Unit) {
        if (noteViewModel.notes.isEmpty()) {
            noteViewModel.addNote("Catatan Saya", "Ini adalah catatan tunggal Anda.")
        }
    }

    // Asumsi catatan tunggal ada di indeks 0
    val note = noteViewModel.notes.getOrNull(0) ?: Note("Catatan Saya", "(Ketuk untuk mulai menulis)")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Notes") },
                // --- PERUBAHAN DI SINI: Menambahkan warna latar belakang kustom ---
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF616161) // Contoh warna Biru Muda (Light Cyan)
                    // Anda juga bisa menggunakan warna tema: MaterialTheme.colorScheme.primaryContainer
                )
                // ----------------------------------------------------------------
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top // Untuk memastikan kartu ada di atas
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    // Navigasi ke EditorScreen ketika diklik
                    .clickable { navController.navigate("editor") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = note.title.ifEmpty { "(Tanpa Judul)" },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = note.content.take(100) + if (note.content.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
