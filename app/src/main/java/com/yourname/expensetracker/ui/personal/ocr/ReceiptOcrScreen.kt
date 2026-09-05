package com.yourname.expensetracker.ui.personal.ocr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptOcrScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReceiptOcrViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Zero-Cloud Receipt Extraction",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Take a photo or pick a receipt from your gallery. Extracted amounts and merchants can be reviewed and edited before saving.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Image Picker Button / Preview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.imageUri == null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Tap to Pick or Capture Receipt",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Private photo picker (zero permissions needed)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (state.isScanning) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Analyzing receipt with on-device OCR...", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Receipt Scanned Successfully", fontWeight = FontWeight.Bold)
                                    Text("Tap to pick a different receipt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Editable Review Form
            if (state.merchant.isNotBlank() || state.amount.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Review & Edit Extracted Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = state.merchant,
                                onValueChange = viewModel::updateMerchant,
                                label = { Text("Merchant / Store Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = state.amount,
                                onValueChange = viewModel::updateAmount,
                                label = { Text("Extracted Amount") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Category Selector
                            if (state.categories.isNotEmpty()) {
                                Text("Category", style = MaterialTheme.typography.labelMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.categories.take(4).forEach { cat ->
                                        FilterChip(
                                            selected = state.selectedCategoryId == cat.id,
                                            onClick = { viewModel.updateCategory(cat.id) },
                                            label = { Text(cat.name) }
                                        )
                                    }
                                }
                            }

                            // Account Selector
                            if (state.accounts.isNotEmpty()) {
                                Text("Paid From Account", style = MaterialTheme.typography.labelMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.accounts.forEach { acc ->
                                        FilterChip(
                                            selected = state.selectedAccountId == acc.id,
                                            onClick = { viewModel.updateAccount(acc.id) },
                                            label = { Text(acc.name) }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = { viewModel.saveTransaction(onDone) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Expense to Ledger")
                            }
                        }
                    }
                }
            }
        }
    }
}
