package com.example.todolist.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.todolist.data.model.Todo
import com.example.todolist.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    val todos by viewModel.todos.collectAsState()
    val isDialogShown by viewModel.isDialogShown.collectAsState()
    val dialogTitle by viewModel.dialogTitle.collectAsState()
    val currentTodo by viewModel.currentTodo.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDatePickerShown by viewModel.isDatePickerShown.collectAsState()
    val tempDueDate by viewModel.tempDueDate.collectAsState()

    val totalTasks = todos.size
    val completedTasks = todos.count { it.isCompleted }
    val completionPercentage = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isDialogShown,
            exit = scaleOut() + fadeOut()
        ) {
            AddEditTodoDialog(
                dialogTitle = dialogTitle,
                todo = currentTodo,
                dueDate = tempDueDate,
                onDueDateClicked = { viewModel.onDueDateClicked() },
                onConfirm = { title -> viewModel.upsertTodo(title) },
                onDismiss = { viewModel.onDialogDismiss() }
            )
        }

        if (isDatePickerShown) {
            val datePickerState =
                rememberDatePickerState(initialSelectedDateMillis = tempDueDate ?: System.currentTimeMillis())
            DatePickerDialog(
                onDismissRequest = { viewModel.onDatePickerDismiss() },
                confirmButton = {
                    TextButton(onClick = { viewModel.onDateSelected(datePickerState.selectedDateMillis) }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDatePickerDismiss() }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (todos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No results found." else "No tasks yet!",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(todos, key = { it.id }) { todo ->
                    var isVisible by remember { mutableStateOf(true) }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = EnterTransition.None,
                        exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)),
                        modifier = Modifier.animateItemPlacement()
                    ) {
                        Column {
                            TodoItem(
                                todo = todo,
                                onCheckedChange = { isChecked -> viewModel.onTodoCheckedChange(todo, isChecked) },
                                onEdit = { viewModel.onEditTodoClicked(todo) },
                                onDelete = {
                                    coroutineScope.launch {
                                        isVisible = false
                                        delay(300)
                                        viewModel.deleteTodo(todo)
                                    }
                                },
                                onPinToggle = { viewModel.onPinToggle(todo) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        TaskCompletionCircle(
            percentage = completionPercentage,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(60.dp)
        )

        // 🎉 Confetti effect once when all tasks complete
        if (completionPercentage == 1f) {
            ConfettiEffect()
        }
    }
}

@Composable
fun TodoItem(
    todo: Todo,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPinToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = todo.title,
                    style = TextStyle(
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 18.sp,
                        color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                todo.dueDate?.let {
                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(Date(it))
                    Text(
                        text = "Due: $formattedDate",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            IconButton(onClick = onPinToggle) {
                Icon(
                    imageVector = if (todo.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = "Pin Task",
                    tint = if (todo.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.6f
                    )
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit To-Do", tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete To-Do", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddEditTodoDialog(
    dialogTitle: String,
    todo: Todo?,
    dueDate: Long?,
    onDueDateClicked: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(todo) { mutableStateOf(todo?.title ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dialogTitle, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDueDateClicked() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Due Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = dueDate?.let {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date(it))
                        } ?: "Set due date",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            onConfirm(text)
                        },
                        enabled = text.isNotBlank() && !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCompletionCircle(
    percentage: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 6.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    val checkmarkColor = MaterialTheme.colorScheme.onSurface

    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            percentage,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = size.minDimension - strokeWidthPx
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            drawArc(
                color = progressColor,
                startAngle = 270f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            if (percentage == 1f) {
                val checkmarkPath = Path().apply {
                    val checkSize = diameter * 0.35f
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    moveTo(centerX - checkSize / 2f, centerY)
                    lineTo(centerX - checkSize / 6f, centerY + checkSize / 3f)
                    lineTo(centerX + checkSize / 2f, centerY - checkSize / 3f)
                }
                drawPath(
                    checkmarkPath,
                    color = checkmarkColor,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// 🎉 Confetti effect with fade-out
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    confettiCount: Int = 15,
    durationMillis: Int = 2500
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.surfaceVariant
    )

    val confetti = remember {
        List(confettiCount) {
            ConfettiPiece(
                x = (0..1000).random().toFloat(),
                y = 0f,
                color = colors.random(),
                size = (6..12).random().toFloat(),
                speed = (100..250).random().toFloat()
            )
        }
    }

    var time by remember { mutableStateOf(0L) }
    var running by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < durationMillis) {
            time = System.currentTimeMillis() - start
            delay(16)
        }
        running = false
    }

    if (running) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val progress = time.toFloat() / durationMillis.toFloat()
            val alpha = 1f - progress

            confetti.forEach { c ->
                val yOffset = (time / 1000f) * c.speed
                drawCircle(
                    color = c.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = c.size,
                    center = Offset(
                        (c.x % size.width),
                        c.y + yOffset
                    )
                )
            }
        }
    }
}

data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val speed: Float
)
