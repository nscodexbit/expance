package com.yourname.expensetracker.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.expensetracker.data.local.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopBrandingSettingsScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var shopName by remember { mutableStateOf("") }
    var shopPhone by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var shopGstin by remember { mutableStateOf("") }
    var shopHeader by remember { mutableStateOf("Tax Invoice / Cash Bill") }
    var shopFooter by remember { mutableStateOf("Thank you for your business!") }

    LaunchedEffect(Unit) {
        shopName = sessionManager.shopName.firstOrNull() ?: "My Store"
        shopPhone = sessionManager.shopPhone.firstOrNull() ?: ""
        shopAddress = sessionManager.shopAddress.firstOrNull() ?: ""
        shopGstin = sessionManager.shopGstin.firstOrNull() ?: ""
        shopHeader = sessionManager.shopHeader.firstOrNull() ?: "Tax Invoice / Cash Bill"
        shopFooter = sessionManager.shopFooter.firstOrNull() ?: "Thank you for your business!"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Shop & Invoice Branding", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Store Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("These details appear on your printed & PDF invoices", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Store / Business Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = shopPhone,
                onValueChange = { shopPhone = it },
                label = { Text("Contact Phone") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = shopAddress,
                onValueChange = { shopAddress = it },
                label = { Text("Store Address / City") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = shopGstin,
                onValueChange = { shopGstin = it },
                label = { Text("GSTIN / Tax ID (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text("Receipt Customization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = shopHeader,
                onValueChange = { shopHeader = it },
                label = { Text("Invoice Header Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = shopFooter,
                onValueChange = { shopFooter = it },
                label = { Text("Footer Thank-You Message") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    scope.launch {
                        sessionManager.setShopBranding(
                            name = shopName.trim().ifBlank { "My Store" },
                            phone = shopPhone.trim(),
                            address = shopAddress.trim(),
                            gstin = shopGstin.trim(),
                            header = shopHeader.trim().ifBlank { "Tax Invoice" },
                            footer = shopFooter.trim().ifBlank { "Thank you!" }
                        )
                        snackbarHostState.showSnackbar("Branding settings saved successfully!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Branding Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
