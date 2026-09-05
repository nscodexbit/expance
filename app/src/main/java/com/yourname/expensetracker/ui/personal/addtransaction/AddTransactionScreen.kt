package com.yourname.expensetracker.ui.personal.addtransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.Account
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.TransactionType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onDone: () -> Unit,
    onScanReceipt: (() -> Unit)? = null,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val calculatorState by viewModel.calculator.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showVoiceDialog by remember { mutableStateOf(false) }

    val handleSave = {
        viewModel.save(
            onSuccess = onDone,
            onError = { msg ->
                scope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showVoiceDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Voice Input")
                    }
                    if (onScanReceipt != null) {
                        IconButton(onClick = onScanReceipt) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Scan Receipt")
                        }
                    }
                    IconButton(
                        onClick = { handleSave() },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.testTag("save_transaction_appbar")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type tabs (Expense / Income)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeTab(
                    label = "Expense",
                    selected = uiState.type == TransactionType.EXPENSE || uiState.type == TransactionType.CASH_OUT,
                    modifier = Modifier.weight(1f).testTag("type_expense_tab"),
                    onClick = { viewModel.selectType(TransactionType.EXPENSE) }
                )
                TypeTab(
                    label = "Income",
                    selected = uiState.type == TransactionType.INCOME || uiState.type == TransactionType.CASH_IN,
                    modifier = Modifier.weight(1f).testTag("type_income_tab"),
                    onClick = { viewModel.selectType(TransactionType.INCOME) }
                )
            }

            // Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.currencySymbol} ${calculatorState.display}",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.type == TransactionType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("amount_display_text")
                    )
                    if (calculatorState.expression.isNotEmpty()) {
                        Text(
                            text = calculatorState.expression.joinToString(" "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (uiState.duplicateWarning) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Similar transaction recorded recently. Double check amount.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Category selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                val categories = if (uiState.type == TransactionType.EXPENSE || uiState.type == TransactionType.CASH_OUT) {
                    uiState.expenseCategories
                } else {
                    uiState.incomeCategories
                }
                if (categories.isEmpty()) {
                    Text(
                        text = "Loading categories...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            CategoryChip(
                                category = category,
                                selected = category.id == uiState.selectedCategoryId,
                                onClick = { viewModel.selectCategory(category.id) }
                            )
                        }
                    }
                }
            }

            // Account selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Account / Payment Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (uiState.accounts.isEmpty()) {
                    Text(
                        text = "Setting up accounts...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.accounts) { account ->
                            AccountChip(
                                account = account,
                                selected = account.id == uiState.selectedAccountId,
                                onClick = { viewModel.selectAccount(account.id) }
                            )
                        }
                    }
                }
            }

            // Note input
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text("Note / Description (optional)") },
                modifier = Modifier.fillMaxWidth().testTag("transaction_note_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Calculator keypad
            CalculatorPad(
                calculator = viewModel.calculator,
                display = calculatorState.display
            )

            // Prominent Save Button
            Button(
                onClick = { handleSave() },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.type == TransactionType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.type == TransactionType.INCOME) "Save Income" else "Save Expense",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showVoiceDialog) {
        var voiceText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Voice / Quick Speech Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Speak or type naturally (e.g., 'Chai 20 rupees', 'Grocery 850', 'Petrol 500')",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = voiceText,
                        onValueChange = { voiceText = it },
                        placeholder = { Text("e.g. Dinner 450") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.parseVoiceInput(voiceText)
                    showVoiceDialog = false
                }) {
                    Text("Parse & Fill")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoiceDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TypeTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AccountChip(account: Account, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(
            text = account.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CalculatorPad(calculator: com.yourname.expensetracker.ui.common.Calculator, display: String) {
    val buttons = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf(".", "0", "C", "+")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    CalculatorButton(
                        label = key,
                        modifier = Modifier.weight(1f),
                        isOperator = key in listOf("÷", "×", "-", "+")
                    ) {
                        when (key) {
                            "C" -> calculator.clear()
                            "÷", "×", "-", "+" -> calculator.addOperator(key)
                            else -> calculator.input(key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorButton(label: String, modifier: Modifier = Modifier, isOperator: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isOperator) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.height(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = if (isOperator) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
