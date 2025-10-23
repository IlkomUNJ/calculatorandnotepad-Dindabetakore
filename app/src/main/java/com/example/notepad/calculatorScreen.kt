package com.example.notepad // Note: Using the project's original package name

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController // NEW: Required for the back button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // NEW: Required for back button
import androidx.compose.material.icons.filled.Menu // NEW: Required for scientific menu icon
import kotlin.text.Regex // NEW: Required for safe RegEx usage

val DarkGray = Color(0xFF2E2E2E)
val LightGray = Color(0xFFA2BBCF)
val Blue = Color(0xFF205B7A)
val White = Color.White
val Black = Color.Black

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// RENAMED: from CalculatorApp to CalculatorScreen, and accepts navController
fun CalculatorScreen(navController: NavHostController) {
    val engine = remember { CalculatorEngine() }
    val state = remember { CalculatorState() }
    var showScientific by remember { mutableStateOf(false) }
    var isInverse by remember { mutableStateOf(false) }

    fun handleInput(button: String) {
        when (button) {
            "AC" -> state.clear()
            "⌫" -> state.backspace()
            "=" -> {
                val result = engine.evaluate(state.expression)
                state.display = result
                state.expression = if (result != "Error") result else ""
            }
            "inv" -> isInverse = !isInverse
            "1/x" -> {
                if (state.expression.isNotEmpty() && state.expression != "0" && state.display != "Error") {
                    val currentResult = engine.evaluate(state.expression)
                    if (currentResult != "Error") {
                        val finalResult = engine.evaluate("1/(${currentResult})")
                        state.display = finalResult
                        state.expression = finalResult
                    }
                }
            }
            "." -> {
                val isDigitBefore =
                    state.expression.isNotEmpty() && state.expression.last().isDigit()

                // pastikan hasil akhir ini boolean
                val lastNumber = state.expression.substringAfterLast('+')
                    .substringAfterLast('-')
                    .substringAfterLast('×')
                    .substringAfterLast('÷')
                val hasDecimalInCurrentNumber = lastNumber.contains('.')

                if (isDigitBefore && !hasDecimalInCurrentNumber) {
                    state.expression += button
                    state.display = state.expression
                }
            }

            else -> {
                when (button) {
                    "sin", "cos", "tan" -> {
                        val func = if (isInverse) "a$button(" else "$button("
                        val disp = if (isInverse) "$button⁻¹(" else "$button("
                        state.expression += func
                        state.display += disp
                    }
                    "xʸ" -> {
                        state.expression += "^"
                        state.display += "^"
                    }
                    "x!" -> {
                        if (state.expression.isNotEmpty() && state.expression.last().isDigit()) {
                            val parts = state.expression.split(Regex("[+÷×-]"))
                            val lastPart = parts.lastOrNull() ?: ""
                            val indexToReplace = state.expression.length - lastPart.length

                            if (lastPart.all { it.isDigit() || it == '.' } && lastPart.isNotEmpty()) {
                                state.expression = state.expression.substring(0, indexToReplace) + "fact($lastPart)"
                                state.display += "!"
                            }
                        }
                    }
                    "ln", "log" -> { // ADDED: Logics for ln and log (missing in original)
                        state.expression += "$button("
                        state.display += "$button("
                    }
                    "%", "+", "-", "×", "÷" -> { // ADDED: Handle operators when expression is not empty
                        if (state.expression.isNotEmpty() && state.expression.last().isDigit()) {
                            state.expression += button
                            state.display = state.expression
                        }
                    }
                    else -> {
                        if (state.display == "0" || state.display == "Error") {
                            state.expression = button
                            state.display = button
                        } else {
                            state.expression += button
                            state.display += button
                        }
                    }
                }
            }
        }
    }

    // START: Scaffold for TopBar and Navigation
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalkulator") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke Menu Utama")
                    }
                },
                actions = {
                    IconButton(onClick = { showScientific = !showScientific }) {
                        Icon(
                            Icons.Filled.Menu, // FIX: Unresolved reference 'Menu'
                            contentDescription = if (showScientific) "Tutup Mode Ilmiah" else "Buka Mode Ilmiah",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Black, titleContentColor = White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply Scaffold padding
                .background(Black)
                .padding(8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display Ekspresi (Added for better tracking)
            Text(
                text = state.expression,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = White.copy(alpha = 0.6f),
                textAlign = TextAlign.End,
                maxLines = 1
            )

            // Display Hasil
            Text(
                text = state.display,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                fontSize = if (state.display.length > 9) 48.sp else 72.sp,
                fontWeight = FontWeight.Light,
                color = White,
                textAlign = TextAlign.End,
                maxLines = 2
            )

            Spacer(Modifier.height(16.dp))

            // Animated Scientific Buttons
            AnimatedVisibility(visible = showScientific) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sciButtons = listOf(
                        listOf("inv", "sin", "cos", "tan", "."), // Moved "." to handle its logic centrally
                        listOf("ln", "log", "x!", "xʸ", "1/x")
                    )
                    sciButtons.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { label ->
                                val displayLabel = if (isInverse) {
                                    when (label) {
                                        "sin" -> "sin⁻¹"
                                        "cos" -> "cos⁻¹"
                                        "tan" -> "tan⁻¹"
                                        else -> label
                                    }
                                } else label

                                CalculatorButton(
                                    text = displayLabel,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    onClick = {
                                        if (label == "inv") {
                                            isInverse = !isInverse
                                        } else {
                                            handleInput(label)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Basic Buttons
            val basicButtons = listOf(
                listOf("7", "8", "9", "AC", "⌫"),
                listOf("4", "5", "6", "×"),
                listOf("1", "2", "3", "÷"),
                listOf("0", "=", "+", "-")
            )

            basicButtons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { label ->
                        CalculatorButton(
                            text = label,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            onClick = { handleInput(label) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// CalculatorButton remains the same
@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (bgColor, textColor) = when (text) {
        "AC", "⌫", "%" -> LightGray to Black
        "÷", "×", "-", "+", "=", "inv" -> Blue to White
        else -> DarkGray to White
    }

    ElevatedButton(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Medium)
    }
}