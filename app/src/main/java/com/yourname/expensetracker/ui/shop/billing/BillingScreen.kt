package com.yourname.expensetracker.ui.shop.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Customer
import com.yourname.expensetracker.data.local.entity.Invoice
import com.yourname.expensetracker.data.local.entity.InvoiceItem
import com.yourname.expensetracker.data.repository.CustomerRepository
import com.yourname.expensetracker.ui.shop.khata.FastAddCustomerSheet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    onNavigateToScanner: () -> Unit,
    onBack: () -> Unit,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val customerRepository = viewModel.customerRepository
    val sessionManager = viewModel.sessionManager
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showFastAddCustomer by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var showInvoiceSuccessDialog by remember { mutableStateOf(false) }
    var showThermalDialog by remember { mutableStateOf(false) }

    var customItemName by remember { mutableStateOf("") }
    var customItemPrice by remember { mutableStateOf("") }

    val activeProfileId by sessionManager.activeProfileId.collectAsState(initial = 1L)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Point of Sale / Billing", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${state.cartItems.size} items in cart",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Barcode Scan")
                    }
                    if (state.cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.resetCart() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Cart")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount Due", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format("₹%.2f", state.grandTotal),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.generateInvoice { invoice, items ->
                                scope.launch {
                                    val shopName = sessionManager.shopName.firstOrNull() ?: "My Store"
                                    val shopPhone = sessionManager.shopPhone.firstOrNull() ?: ""
                                    val shopAddress = sessionManager.shopAddress.firstOrNull() ?: ""
                                    val shopGstin = sessionManager.shopGstin.firstOrNull() ?: ""
                                    val shopHeader = sessionManager.shopHeader.firstOrNull() ?: "Tax Invoice"
                                    val shopFooter = sessionManager.shopFooter.firstOrNull() ?: "Thank you!"

                                    val branding = InvoicePdfHelper.ShopBranding(
                                        name = shopName,
                                        phone = shopPhone,
                                        address = shopAddress,
                                        gstin = shopGstin,
                                        header = shopHeader,
                                        footer = shopFooter
                                    )

                                    val pdf = InvoicePdfHelper.generateInvoicePdf(
                                        context = context,
                                        invoice = invoice,
                                        items = items,
                                        branding = branding
                                    )
                                    generatedPdfFile = pdf
                                    showInvoiceSuccessDialog = true
                                }
                            }
                        },
                        enabled = state.cartItems.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.paymentMode == "UDHAR") "Save Udhar & Invoice" else "Checkout & Generate Bill",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar & Barcode Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search product name or barcode...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                    FilledTonalIconButton(
                        onClick = onNavigateToScanner,
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Barcode Scan")
                    }
                }
            }

            // Search Results Popup/Suggestions
            if (state.matchedProducts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Search Results (Tap to add)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            state.matchedProducts.take(5).forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.addToCart(prod)
                                            viewModel.onSearchQueryChanged("")
                                        }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(prod.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Stock: ${prod.currentStock.toInt()} ${prod.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text("₹${prod.sellingPrice}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Add Custom item button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showCustomItemDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Custom Item (Quick Entry)")
                    }
                }
            }

            // Cart Items Header
            item {
                Text(
                    text = "Items in Cart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.cartItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cart is empty", fontWeight = FontWeight.Medium)
                            Text(
                                "Scan barcode or search products to begin billing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(state.cartItems) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    text = "₹${item.unitPrice} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Qty controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.updateQuantity(index, -1.0) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Minus")
                                }
                                Text(
                                    text = String.format("%.0f", item.quantity),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                IconButton(
                                    onClick = { viewModel.updateQuantity(index, 1.0) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Plus")
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = String.format("₹%.2f", item.total),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            IconButton(
                                onClick = { viewModel.removeCartItem(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Discount & GST Selectors
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Discounts & Taxes", fontWeight = FontWeight.SemiBold)

                        // Discount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Discount (%)", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.0, 5.0, 10.0, 15.0).forEach { disc ->
                                    FilterChip(
                                        selected = state.discountPercent == disc,
                                        onClick = { viewModel.setDiscountPercent(disc) },
                                        label = { Text("${disc.toInt()}%") }
                                    )
                                }
                            }
                        }

                        // GST Rate Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GST / Tax", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.0, 5.0, 12.0, 18.0).forEach { gst ->
                                    FilterChip(
                                        selected = state.gstRate == gst,
                                        onClick = { viewModel.setGstRate(gst) },
                                        label = { Text("${gst.toInt()}%") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Mode & Udhar Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Payment Mode", fontWeight = FontWeight.SemiBold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "CASH" to "Cash",
                                "UPI" to "UPI / Online",
                                "UDHAR" to "Udhar / Credit"
                            ).forEach { (mode, label) ->
                                FilterChip(
                                    selected = state.paymentMode == mode,
                                    onClick = { viewModel.setPaymentMode(mode) },
                                    label = { Text(label, fontWeight = if (state.paymentMode == mode) FontWeight.Bold else FontWeight.Normal) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Udhar customer picker
                        if (state.paymentMode == "UDHAR") {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Select Khata Customer", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                TextButton(onClick = { showFastAddCustomer = true }) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Fast Add")
                                }
                            }

                            if (state.selectedCustomer != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(state.selectedCustomer!!.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "Current Due: ₹${state.customerDue.toInt()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        IconButton(onClick = { viewModel.selectCustomer(null) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Deselect")
                                        }
                                    }
                                }
                            } else {
                                // Dropdown or List of Customers
                                if (state.matchedCustomers.isEmpty()) {
                                    Text(
                                        "No customers yet. Tap '+ Fast Add' to add one in 2 taps!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        state.matchedCustomers.take(4).forEach { cust ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { viewModel.selectCustomer(cust) }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(cust.name, fontWeight = FontWeight.Medium)
                                                    Text(
                                                        cust.phone ?: "Select",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("₹%.2f", state.subtotal))
                        }
                        if (state.discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount (${state.discountPercent.toInt()}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format("-₹%.2f", state.discountAmount), color = MaterialTheme.colorScheme.error)
                            }
                        }
                        if (state.taxAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("GST (${state.gstRate.toInt()}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format("+₹%.2f", state.taxAmount))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(String.format("₹%.2f", state.grandTotal), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Custom Item Entry Dialog
    if (showCustomItemDialog) {
        AlertDialog(
            onDismissRequest = { showCustomItemDialog = false },
            title = { Text("Quick Add Custom Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customItemName,
                        onValueChange = { customItemName = it },
                        label = { Text("Item Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customItemPrice,
                        onValueChange = { customItemPrice = it },
                        label = { Text("Unit Price (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = customItemPrice.toDoubleOrNull() ?: 0.0
                        if (customItemName.isNotBlank() && price > 0) {
                            viewModel.addCustomItemToCart(customItemName.trim(), price)
                            customItemName = ""
                            customItemPrice = ""
                            showCustomItemDialog = false
                        }
                    }
                ) {
                    Text("Add to Cart")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Fast Add Customer Sheet
    if (showFastAddCustomer) {
        FastAddCustomerSheet(
            profileId = activeProfileId ?: 1L,
            customerRepository = customerRepository,
            onDismiss = { showFastAddCustomer = false },
            onCustomerCreated = { newCust ->
                viewModel.selectCustomer(newCust)
                showFastAddCustomer = false
            }
        )
    }

    // Invoice Success Dialog
    if (showInvoiceSuccessDialog && state.lastGeneratedInvoice != null) {
        val invoice = state.lastGeneratedInvoice!!
        AlertDialog(
            onDismissRequest = {
                showInvoiceSuccessDialog = false
                viewModel.resetCart()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) },
            title = { Text("Invoice Generated!", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Invoice No: ${invoice.invoiceNumber}", fontWeight = FontWeight.SemiBold)
                    Text("Amount: ₹${invoice.grandTotal} (${invoice.paymentMode})")
                    if (invoice.paymentMode == "UDHAR") {
                        Text("Added to Khata of: ${invoice.customerName ?: "Customer"}", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Choose how you want to share or print the bill:", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        generatedPdfFile?.let { InvoicePdfHelper.sharePdf(context, it) }
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share PDF")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showThermalDialog = true
                        }
                    ) {
                        Text("Thermal / Print")
                    }
                    TextButton(
                        onClick = {
                            showInvoiceSuccessDialog = false
                            viewModel.resetCart()
                        }
                    ) {
                        Text("New Bill")
                    }
                }
            }
        )
    }

    // Thermal Receipt Preview Dialog
    if (showThermalDialog && state.lastGeneratedInvoice != null) {
        val thermalText = remember(state.lastGeneratedInvoice) {
            val branding = InvoicePdfHelper.ShopBranding()
            InvoicePdfHelper.generateThermalReceipt(
                state.lastGeneratedInvoice!!,
                state.lastGeneratedItems,
                branding
            )
        }

        AlertDialog(
            onDismissRequest = { showThermalDialog = false },
            title = { Text("Thermal Receipt (58mm/80mm ESC/POS)") },
            text = {
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = thermalText,
                        color = Color.Green,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Share raw text to Bluetooth printer or clipboard
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, thermalText)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Send to Printer"))
                        showThermalDialog = false
                    }
                ) {
                    Text("Send to Printer / Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showThermalDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
