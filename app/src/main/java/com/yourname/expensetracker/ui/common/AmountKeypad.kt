package com.yourname.expensetracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AmountKeypad(
    currentAmount: String,
    onAmountChanged: (String) -> Unit,
    onDone: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    showQuickPresets: Boolean = true
) {
    val presets = listOf(50, 100, 200, 500, 1000, 2000)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showQuickPresets) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    AssistChip(
                        onClick = {
                            val currentVal = currentAmount.toDoubleOrNull() ?: 0.0
                            val newVal = currentVal + preset
                            onAmountChanged(if (newVal % 1.0 == 0.0) newVal.toInt().toString() else String.format("%.2f", newVal))
                        },
                        label = { Text("+$currencySymbol$preset", fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "DEL")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (key in row) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    handleKeyInput(key, currentAmount, onAmountChanged)
                                },
                            color = when (key) {
                                "DEL" -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surface
                            },
                            tonalElevation = 2.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (key == "DEL") {
                                    Icon(
                                        Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (onDone != null) {
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Amount", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun handleKeyInput(
    key: String,
    current: String,
    onChanged: (String) -> Unit
) {
    when (key) {
        "DEL" -> {
            if (current.isNotEmpty()) {
                onChanged(current.dropLast(1))
            }
        }
        "." -> {
            if (!current.contains(".")) {
                onChanged(if (current.isEmpty()) "0." else "$current.")
            }
        }
        else -> {
            // Digit
            val parts = current.split(".")
            if (parts.size > 1 && parts[1].length >= 2) {
                return // allow max 2 decimal places
            }
            if (current == "0") {
                onChanged(key)
            } else if (current.length < 9) {
                onChanged(current + key)
            }
        }
    }
}
