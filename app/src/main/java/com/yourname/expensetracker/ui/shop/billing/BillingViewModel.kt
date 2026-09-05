package com.yourname.expensetracker.ui.shop.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.*
import com.yourname.expensetracker.data.repository.CustomerRepository
import com.yourname.expensetracker.data.repository.InventoryRepository
import com.yourname.expensetracker.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class CartItem(
    val inventoryItemId: Long? = null,
    val name: String,
    val unitPrice: Double,
    val quantity: Double = 1.0,
    val total: Double = unitPrice * quantity
)

data class BillingUiState(
    val cartItems: List<CartItem> = emptyList(),
    val searchQuery: String = "",
    val matchedProducts: List<InventoryItem> = emptyList(),
    val matchedCustomers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val customerDue: Double = 0.0,
    val paymentMode: String = "CASH", // CASH, UPI, UDHAR
    val discountPercent: Double = 0.0,
    val gstRate: Double = 0.0,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val lastGeneratedInvoice: Invoice? = null,
    val lastGeneratedItems: List<InvoiceItem> = emptyList(),
    val isInvoiceGenerated: Boolean = false
)

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val inventoryRepository: InventoryRepository,
    val customerRepository: CustomerRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private var activeProfileId: Long = 1L

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { id ->
                activeProfileId = id ?: 1L
                loadProductsAndCustomers()
            }
        }
    }

    private fun loadProductsAndCustomers() {
        viewModelScope.launch {
            inventoryRepository.getItems(activeProfileId).collect { items ->
                val q = _uiState.value.searchQuery
                if (q.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(
                        matchedProducts = items.filter {
                            it.name.contains(q, ignoreCase = true) || (it.barcode?.contains(q) == true)
                        }
                    )
                }
            }
        }
        viewModelScope.launch {
            customerRepository.getCustomersByProfile(activeProfileId).collect { customers ->
                _uiState.value = _uiState.value.copy(matchedCustomers = customers)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            inventoryRepository.getItems(activeProfileId).firstOrNull()?.let { items ->
                _uiState.value = _uiState.value.copy(
                    matchedProducts = if (query.isBlank()) emptyList() else items.filter {
                        it.name.contains(query, ignoreCase = true) || (it.barcode?.contains(query) == true)
                    }
                )
            }
        }
    }

    fun addToCart(product: InventoryItem) {
        val existingIndex = _uiState.value.cartItems.indexOfFirst { it.inventoryItemId == product.id }
        val updatedList = _uiState.value.cartItems.toMutableList()
        if (existingIndex >= 0) {
            val old = updatedList[existingIndex]
            val newQty = old.quantity + 1.0
            updatedList[existingIndex] = old.copy(quantity = newQty, total = old.unitPrice * newQty)
        } else {
            updatedList.add(
                CartItem(
                    inventoryItemId = product.id,
                    name = product.name,
                    unitPrice = product.sellingPrice,
                    quantity = 1.0,
                    total = product.sellingPrice
                )
            )
        }
        updateCartTotals(updatedList)
    }

    fun addCustomItemToCart(name: String, price: Double, qty: Double = 1.0) {
        val updatedList = _uiState.value.cartItems.toMutableList()
        updatedList.add(
            CartItem(
                inventoryItemId = null,
                name = name,
                unitPrice = price,
                quantity = qty,
                total = price * qty
            )
        )
        updateCartTotals(updatedList)
    }

    fun updateQuantity(index: Int, delta: Double) {
        val list = _uiState.value.cartItems.toMutableList()
        if (index in list.indices) {
            val item = list[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                list.removeAt(index)
            } else {
                list[index] = item.copy(quantity = newQty, total = item.unitPrice * newQty)
            }
            updateCartTotals(list)
        }
    }

    fun removeCartItem(index: Int) {
        val list = _uiState.value.cartItems.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            updateCartTotals(list)
        }
    }

    fun setDiscountPercent(percent: Double) {
        _uiState.value = _uiState.value.copy(discountPercent = percent)
        recalculateTotals()
    }

    fun setGstRate(rate: Double) {
        _uiState.value = _uiState.value.copy(gstRate = rate)
        recalculateTotals()
    }

    fun setPaymentMode(mode: String) {
        _uiState.value = _uiState.value.copy(paymentMode = mode)
    }

    fun selectCustomer(customer: Customer?) {
        _uiState.value = _uiState.value.copy(selectedCustomer = customer)
        if (customer != null) {
            viewModelScope.launch {
                val balance = customerRepository.getOutstandingBalance(customer.id).firstOrNull() ?: 0.0
                _uiState.value = _uiState.value.copy(customerDue = balance)
            }
        } else {
            _uiState.value = _uiState.value.copy(customerDue = 0.0)
        }
    }

    private fun updateCartTotals(items: List<CartItem>) {
        _uiState.value = _uiState.value.copy(cartItems = items)
        recalculateTotals()
    }

    private fun recalculateTotals() {
        val s = _uiState.value
        val sub = s.cartItems.sumOf { it.total }
        val discAmount = sub * (s.discountPercent / 100.0)
        val taxable = sub - discAmount
        val tax = taxable * (s.gstRate / 100.0)
        val grand = taxable + tax

        _uiState.value = s.copy(
            subtotal = sub,
            discountAmount = discAmount,
            taxAmount = tax,
            grandTotal = grand
        )
    }

    fun generateInvoice(onSuccess: (Invoice, List<InvoiceItem>) -> Unit) {
        val s = _uiState.value
        if (s.cartItems.isEmpty()) return

        viewModelScope.launch {
            val invNum = "INV-" + SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val invoice = Invoice(
                profileId = activeProfileId,
                invoiceNumber = invNum,
                customerName = s.selectedCustomer?.name,
                customerPhone = s.selectedCustomer?.phone,
                subtotal = s.subtotal,
                discountPercent = s.discountPercent,
                discountAmount = s.discountAmount,
                gstRate = s.gstRate,
                taxAmount = s.taxAmount,
                grandTotal = s.grandTotal,
                paymentMode = s.paymentMode,
                date = System.currentTimeMillis()
            )

            val invoiceItems = s.cartItems.map {
                InvoiceItem(
                    invoiceId = 0L,
                    itemName = it.name,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    total = it.total
                )
            }

            // Save to database
            val invoiceId = invoiceRepository.createInvoice(invoice, invoiceItems)

            // Adjust inventory stock for items from inventory
            s.cartItems.forEach { item ->
                item.inventoryItemId?.let { id ->
                    inventoryRepository.adjustStock(id, -item.quantity)
                }
            }

            // If payment mode is UDHAR / CREDIT, create a CreditEntry in Khata ledger!
            if (s.paymentMode == "UDHAR" && s.selectedCustomer != null) {
                customerRepository.insertCreditEntry(
                    CreditEntry(
                        customerId = s.selectedCustomer.id,
                        type = CreditType.CREDIT_GIVEN,
                        amount = s.grandTotal,
                        date = System.currentTimeMillis(),
                        note = "Invoice #$invNum"
                    )
                )
            }

            val finalInvoice = invoice.copy(id = invoiceId)
            _uiState.value = _uiState.value.copy(
                lastGeneratedInvoice = finalInvoice,
                lastGeneratedItems = invoiceItems,
                isInvoiceGenerated = true
            )

            onSuccess(finalInvoice, invoiceItems)
        }
    }

    fun resetCart() {
        _uiState.value = BillingUiState(
            matchedCustomers = _uiState.value.matchedCustomers
        )
    }
}
