package com.yourname.expensetracker.ui.shop.khata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.expensetracker.data.local.entity.CreditEntry
import com.yourname.expensetracker.data.local.entity.CreditType
import com.yourname.expensetracker.data.local.entity.Customer
import com.yourname.expensetracker.data.repository.CustomerRepository
import com.yourname.expensetracker.ui.common.AmountKeypad
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastAddCustomerSheet(
    profileId: Long,
    customerRepository: CustomerRepository,
    onDismiss: () -> Unit,
    onCustomerCreated: (Customer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fast Add Khata Customer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text("Customer Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = openingBalance,
                onValueChange = { openingBalance = it },
                label = { Text("Opening Due / Udhar Balance (₹)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            AmountKeypad(
                currentAmount = openingBalance,
                onAmountChanged = { openingBalance = it },
                currencySymbol = "₹",
                showQuickPresets = true
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Customer name is required"
                        return@Button
                    }
                    scope.launch {
                        val newCustomer = Customer(
                            profileId = profileId,
                            name = name.trim(),
                            phone = phone.trim().ifBlank { null }
                        )
                        val customerId = customerRepository.insert(newCustomer)
                        val due = openingBalance.toDoubleOrNull() ?: 0.0
                        if (due > 0) {
                            customerRepository.insertCreditEntry(
                                CreditEntry(
                                    customerId = customerId,
                                    type = CreditType.CREDIT_GIVEN,
                                    amount = due,
                                    date = System.currentTimeMillis(),
                                    note = "Opening Balance"
                                )
                            )
                        }
                        val created = newCustomer.copy(id = customerId)
                        onCustomerCreated(created)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Customer & Proceed", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
