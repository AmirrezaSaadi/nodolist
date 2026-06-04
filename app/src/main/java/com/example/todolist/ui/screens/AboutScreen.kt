package com.example.todolist.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val versionName = remember { getAppVersion(context) }

    var isNavigatingBack by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About NoDoList") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isNavigatingBack) return@IconButton
                        isNavigatingBack = true
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .animatedBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "NoDoList",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                AboutSection(title = "Description") {
                    Text(
                        text = "This is a minimalistic notes & to-do app designed to help you stay organized without distractions. Write down your thoughts, keep track of tasks, and manage your day with a clean, easy-to-use interface.",
                        // 1. Use a more standard text size
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AboutSection(title = "Developed By") {
                    Text(
                        text = "Amirreza Saadi",
                        // 1. Use a more standard text size
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AboutSection(title = "Contact & Support") {
                    ContactSection()
                }

                // 2. Add a spacer here for better separation
                Spacer(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Built with Jetpack Compose & Kotlin\n${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ContactSection() {
    val context = LocalContext.current

    val annotatedText = buildAnnotatedString {
        append("For support or inquiries, please contact us at:\n")

        // Email
        pushStringAnnotation(tag = "EMAIL", annotation = "mailto:AmirrezaSaadi0509@gmail.com")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("AmirrezaSaadi0509@gmail.com")
        }
        pop()
        append("\n")

        // GitHub
        pushStringAnnotation(tag = "GITHUB", annotation = "https://github.com/AmirrezaSaadi")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("github.com/AmirrezaSaadi")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        // 1. Use a more standard text size
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        onClick = { offset ->
            annotatedText.getStringAnnotations(start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    when (annotation.tag) {
                        "EMAIL" -> {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse(annotation.item)
                            }
                            context.startActivity(intent)
                        }

                        "GITHUB" -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                    }
                }
        }
    )
}

private fun Modifier.animatedBorder(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "borderAnimation")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing)
        ),
        label = "borderProgress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val pathMeasure = remember { PathMeasure() }
    val path = remember { Path() }
    val segmentPath = remember { Path() }

    this.drawBehind {
        val margin = 12.dp.toPx()
        val cornerRadiusValue = 16.dp.toPx()

        val roundRect = RoundRect(
            rect = Rect(
                offset = Offset(margin, margin),
                size = size.copy(
                    width = size.width - 2 * margin,
                    height = size.height - 2 * margin
                )
            ),
            cornerRadius = CornerRadius(cornerRadiusValue)
        )
        path.reset()
        path.addRoundRect(roundRect)

        pathMeasure.setPath(path, false)

        val length = pathMeasure.length
        val segmentLength = length / 4f
        val start = length * progress
        val stop = start + segmentLength

        segmentPath.reset()
        pathMeasure.getSegment(start, stop, segmentPath, startWithMoveTo = true)

        if (stop > length) {
            pathMeasure.getSegment(0f, stop - length, segmentPath, startWithMoveTo = true)
        }

        drawPath(
            path = segmentPath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
fun AboutSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

private fun getAppVersion(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "N/A"
    } catch (e: PackageManager.NameNotFoundException) {
        "N/A"
    }
}