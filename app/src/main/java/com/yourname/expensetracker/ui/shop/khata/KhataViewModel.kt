package com.yourname.expensetracker.ui.shop.khata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.DataInitializer
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.CreditEntry
import com.yourname.expensetracker.data.local.entity.CreditType
import com.yourname.expensetracker.data.local.entity.Customer
import com.yourname.expensetracker.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerListItem(
    val customer: Customer,
    val outstanding: Double
)

data class CustomerDetailState(
    val customer: Customer? = null,
    val entries: List<CreditEntry> = emptyList(),
    val outstanding: Double = 0.0
)

@HiltViewModel
class KhataViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val dataInitializer: DataInitializer,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerListItem>>(emptyList())
    val customers: StateFlow<List<CustomerListItem>> = _customers.asStateFlow()

    private val _detailState = MutableStateFlow(CustomerDetailState())
    val detailState: StateFlow<CustomerDetailState> = _detailState.asStateFlow()

    val currencySymbol: StateFlow<String> = sessionManager.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            val initialProfileId = dataInitializer.ensureInitialized()
            activeProfileId = initialProfileId
            observeCustomers(initialProfileId)

            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null && profileId != activeProfileId) {
                    activeProfileId = profileId
                    observeCustomers(profileId)
                }
            }
        }
    }

    private fun observeCustomers(profileId: Long) {
        customerRepository.getCustomersByProfile(profileId)
            .flatMapLatest { list ->
                val flows = list.map { customer ->
                    customerRepository.getOutstandingBalance(customer.id)
                        .map { balance -> CustomerListItem(customer, balance) }
                }
                if (flows.isEmpty()) flowOf(emptyList()) else combine(flows) { it.toList() }
            }
            .onEach { _customers.value = it.sortedByDescending { item -> item.outstanding } }
            .launchIn(viewModelScope)
    }

    fun loadCustomerDetail(customerId: Long) {
        viewModelScope.launch {
            val customer = customerRepository.getCustomerById(customerId)
            _detailState.value = _detailState.value.copy(customer = customer)
        }
        customerRepository.getEntriesByCustomer(customerId)
            .onEach { entries ->
                _detailState.value = _detailState.value.copy(entries = entries)
            }
            .launchIn(viewModelScope)
        customerRepository.getOutstandingBalance(customerId)
            .onEach { balance ->
                _detailState.value = _detailState.value.copy(outstanding = balance)
            }
            .launchIn(viewModelScope)
    }

    fun addCustomer(name: String, phone: String?, creditLimit: Double = 0.0, address: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val profileId = activeProfileId ?: dataInitializer.ensureInitialized()
            customerRepository.insert(
                Customer(
                    profileId = profileId,
                    name = name.trim(),
                    phone = phone?.takeIf { it.isNotBlank() },
                    creditLimit = creditLimit,
                    address = address?.takeIf { it.isNotBlank() }
                )
            )
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.update(customer)
            _detailState.value = _detailState.value.copy(customer = customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.delete(customer)
        }
    }

    fun addCreditEntry(customerId: Long, type: CreditType, amount: Double, note: String? = null) {
        if (amount <= 0) return
        viewModelScope.launch {
            customerRepository.insertCreditEntry(
                CreditEntry(
                    customerId = customerId,
                    type = type,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    note = note
                )
            )
        }
    }

    fun deleteCreditEntry(entry: CreditEntry) {
        viewModelScope.launch {
            customerRepository.deleteCreditEntry(entry)
        }
    }

    fun clearDetail() {
        _detailState.value = CustomerDetailState()
    }
}
