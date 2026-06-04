package com.example.todolist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.todolist.data.model.Note
import com.example.todolist.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    noteId: Int,
    navController: NavController,
    viewModel: NoteViewModel
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentNote: Note? by remember { mutableStateOf(null) }

    val undoHistory = remember { mutableStateListOf<Pair<String, String>>() }
    val redoHistory = remember { mutableStateListOf<Pair<String, String>>() }

    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = noteId) {
        if (noteId != -1) {
            viewModel.getNoteById(noteId) { note ->
                note?.let {
                    val firstWordOfContent = it.content.trim().split(" ").firstOrNull()
                    if (it.title == firstWordOfContent) {
                        title = ""
                    } else {
                        title = it.title
                    }

                    content = it.content
                    timestamp = it.timestamp
                    currentNote = it
                    undoHistory.add(it.title to it.content)
                }
            }
        }
    }

    val formattedDate = remember(timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        "Last edited: ${sdf.format(Date(timestamp))}"
    }

    val isSaveEnabled = content.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == -1) "Add Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val lastState = undoHistory.removeLastOrNull()
                            if (lastState != null) {
                                redoHistory.add(title to content)
                                title = lastState.first
                                content = lastState.second
                            }
                        },
                        enabled = undoHistory.size > 1
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo")
                    }
                    IconButton(
                        onClick = {
                            val nextState = redoHistory.removeLastOrNull()
                            if (nextState != null) {
                                undoHistory.add(title to content)
                                title = nextState.first
                                content = nextState.second
                            }
                        },
                        enabled = redoHistory.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo")
                    }
                    IconButton(
                        onClick = {
                            if (isSaving) return@IconButton
                            isSaving = true

                            val finalTitle = if (title.isBlank()) {
                                content.trim().split(" ").firstOrNull() ?: "New Note"
                            } else {
                                title
                            }

                            val noteToSave = currentNote?.copy(
                                title = finalTitle,
                                content = content,
                                timestamp = System.currentTimeMillis()
                            ) ?: Note(title = finalTitle, content = content)
                            viewModel.upsertNote(noteToSave)
                            navController.popBackStack()
                        },
                        enabled = isSaveEnabled && !isSaving,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Note")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = { newTitle ->
                    if (newTitle != title) {
                        undoHistory.add(title to content)
                        redoHistory.clear()
                    }
                    title = newTitle
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.headlineSmall,
                            // 2. Placeholder text made more transparent
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(
                color = MaterialTheme.colorScheme.primary,
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(16.dp))


            BasicTextField(
                value = content,
                onValueChange = { newContent ->
                    if (newContent != content) {
                        undoHistory.add(title to content)
                        redoHistory.clear()
                    }
                    content = newContent
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            text = "Start typing...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}