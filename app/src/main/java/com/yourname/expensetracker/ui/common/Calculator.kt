package com.yourname.expensetracker.ui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CalculatorState(
    val display: String = "0",
    val expression: List<String> = emptyList(),
    val hasDecimal: Boolean = false
)

class Calculator {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun input(digit: String) {
        val current = _state.value
        if (digit == ".") {
            if (!current.hasDecimal) {
                _state.value = current.copy(
                    display = if (current.display == "0") "0." else current.display + ".",
                    hasDecimal = true
                )
            }
            return
        }
        if (current.display == "0") {
            _state.value = current.copy(display = digit)
        } else {
            _state.value = current.copy(display = current.display + digit)
        }
    }

    fun addOperator(op: String) {
        val current = _state.value
        _state.value = current.copy(
            expression = current.expression + current.display + op,
            display = "0",
            hasDecimal = false
        )
    }

    fun evaluate(): Double {
        val current = _state.value
        val fullExpression = current.expression + current.display
        return try {
            evaluateExpression(fullExpression)
        } catch (e: Exception) {
            0.0
        }
    }

    fun clear() {
        _state.value = CalculatorState()
    }

    fun setDisplay(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }
        val displayVal = if (sanitized.isEmpty()) "0" else sanitized
        _state.value = CalculatorState(
            display = displayVal,
            expression = emptyList(),
            hasDecimal = displayVal.contains(".")
        )
    }

    fun delete() {
        val current = _state.value
        if (current.display.length > 1) {
            val newDisplay = current.display.dropLast(1)
            _state.value = current.copy(
                display = newDisplay,
                hasDecimal = newDisplay.contains(".")
            )
        } else {
            _state.value = current.copy(display = "0", hasDecimal = false)
        }
    }

    private fun evaluateExpression(expression: List<String>): Double {
        val values = mutableListOf<Double>()
        val ops = mutableListOf<String>()
        var currentNum = ""

        for (token in expression) {
            when {
                token in listOf("+", "-", "×", "÷", "%") -> {
                    values.add(currentNum.toDouble())
                    currentNum = ""
                    ops.add(token)
                }
                else -> currentNum += token
            }
        }
        values.add(currentNum.toDouble())

        // Handle multiply/divide/percent first
        var i = 0
        while (i < ops.size) {
            val op = ops[i]
            if (op == "×" || op == "÷" || op == "%") {
                val left = values[i]
                val right = values[i + 1]
                val result = when (op) {
                    "×" -> left * right
                    "÷" -> if (right != 0.0) left / right else left
                    "%" -> left * (right / 100.0)
                    else -> 0.0
                }
                values[i] = result
                values.removeAt(i + 1)
                ops.removeAt(i)
            } else {
                i++
            }
        }

        // Handle add/subtract
        var result = values[0]
        for (j in ops.indices) {
            val op = ops[j]
            val next = values[j + 1]
            result = when (op) {
                "+" -> result + next
                "-" -> result - next
                else -> result
            }
        }
        return result
    }
}
