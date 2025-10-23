package com.example.notepad

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import java.text.DecimalFormat
import kotlin.math.*

class CalculatorEngine {

    private val factorial = object : Function("fact", 1) {
        override fun apply(vararg args: Double): Double {
            val n = args[0].toInt()
            if (n < 0 || n != args[0].toInt()) {
                throw IllegalArgumentException("Argument factorial harus integer >= 0")
            }
            return (1..n).fold(1.0) { acc, i -> acc * i }
        }
    }

    fun evaluate(exp: String): String {
        return try {
            var sanitized = exp
                .replace("×", "*")
                .replace("÷", "/")
                .replace("√", "sqrt")
                .replace("xʸ", "^")

            val openCount = sanitized.count { it == '(' }
            val closeCount = sanitized.count { it == ')' }

            if (openCount > closeCount) {
                repeat(openCount - closeCount) {
                    sanitized += ")"
                }
            }

            val sin = object : Function("sin", 1) {
                override fun apply(vararg args: Double): Double = sin(Math.toRadians(args[0]))
            }
            val cos = object : Function("cos", 1) {
                override fun apply(vararg args: Double): Double = cos(Math.toRadians(args[0]))
            }
            val tan = object : Function("tan", 1) {
                override fun apply(vararg args: Double): Double = tan(Math.toRadians(args[0]))
            }
            val log = object : Function("log", 1) {
                override fun apply(vararg args: Double): Double = log10(args[0])
            }
            val ln = object : Function("ln", 1) {
                override fun apply(vararg args: Double): Double = ln(args[0])
            }
            val asin = object : Function("asin", 1) {
                override fun apply(vararg args: Double): Double = Math.toDegrees(asin(args[0]))
            }
            val acos = object : Function("acos", 1) {
                override fun apply(vararg args: Double): Double = Math.toDegrees(acos(args[0]))
            }
            val atan = object : Function("atan", 1) {
                override fun apply(vararg args: Double): Double = Math.toDegrees(atan(args[0]))
            }

            val expression = ExpressionBuilder(sanitized)
                .function(factorial)
                .function(sin)
                .function(cos)
                .function(tan)
                .function(log)
                .function(ln)
                .function(asin)
                .function(acos)
                .function(atan)
                .build()

            val result = expression.evaluate()
            val df = DecimalFormat("#.########")
            df.format(result)
        } catch (e: Exception) {
            "Error"
        }
    }
}
