package com.example.notepad


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CalculatorState {
    var display by mutableStateOf("0")
    var expression by mutableStateOf("")
    var isInverse by mutableStateOf(false)

    fun clear() {
        display = "0"
        expression = ""
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            display = if (expression.isEmpty()) "0" else expression
        }
    }
}
