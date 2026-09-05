package com.yourname.expensetracker.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.local.entity.Category
import com.yourname.expensetracker.data.local.entity.Profile
import com.yourname.expensetracker.data.local.entity.Transaction
import com.yourname.expensetracker.data.local.entity.TransactionType
import com.yourname.expensetracker.data.repository.CategoryRepository
import com.yourname.expensetracker.data.repository.ProfileRepository
import com.yourname.expensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategorySpendItem(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float
)

data class AnomalyItem(
    val transaction: Transaction,
    val categoryName: String,
    val ratio: Double,
    val categoryAverage: Double
)

data class InsightsUiState(
    val profile: Profile? = null,
    val currencySymbol: String = "₹",
    val selectedPeriod: String = "This Month", // This Month, Last 30 Days, Year
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val avgDailySpend: Double = 0.0,
    val projectedMonthEndSpend: Double = 0.0,
    val daysRemaining: Int = 0,
    val categoryBreakdown: List<CategorySpendItem> = emptyList(),
    val explainInsights: List<String> = emptyList(),
    val anomalies: List<AnomalyItem> = emptyList(),
    val monthlyTrend: List<Pair<String, Double>> = emptyList()
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val profileRepository: ProfileRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private var activeProfileId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.activeProfileId.collect { profileId ->
                if (profileId != null) {
                    activeProfileId = profileId
                    val profile = profileRepository.getProfileById(profileId)
                    val symbol = if (profile?.currency == "USD") "$" else if (profile?.currency == "EUR") "€" else "₹"
                    _uiState.update { it.copy(profile = profile, currencySymbol = symbol) }
                    loadInsights(profileId)
                }
            }
        }
    }

    fun setPeriod(period: String) {
        _uiState.update { it.copy(selectedPeriod = period) }
        activeProfileId?.let { loadInsights(it) }
    }

    private fun loadInsights(profileId: Long) {
        viewModelScope.launch {
            val (startDate, endDate) = getPeriodBounds(_uiState.value.selectedPeriod)

            combine(
                transactionRepository.getTransactionsByProfile(profileId),
                categoryRepository.getCategoriesByProfile(profileId)
            ) { allTxns, categories ->
                val catMap = categories.associateBy { it.id }

                val periodTxns = allTxns.filter { it.date in startDate..endDate }
                val expenseTxns = periodTxns.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.CASH_OUT }
                val incomeTxns = periodTxns.filter { it.type == TransactionType.INCOME || it.type == TransactionType.CASH_IN }

                val totalSpent = expenseTxns.sumOf { it.amount }
                val totalIncome = incomeTxns.sumOf { it.amount }

                // Category breakdown
                val catSpendMap = mutableMapOf<Long, Double>()
                expenseTxns.forEach { t ->
                    t.categoryId?.let { catId ->
                        catSpendMap[catId] = (catSpendMap[catId] ?: 0.0) + t.amount
                    }
                }

                val breakdown = catSpendMap.mapNotNull { (catId, amount) ->
                    val cat = catMap[catId]
                    if (cat != null && totalSpent > 0) {
                        CategorySpendItem(
                            category = cat,
                            totalAmount = amount,
                            percentage = ((amount / totalSpent) * 100).toFloat()
                        )
                    } else null
                }.sortedByDescending { it.totalAmount }

                // Trend projection (Current Month)
                val cal = Calendar.getInstance()
                val currentDay = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
                val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val daysLeft = (maxDays - currentDay).coerceAtLeast(0)

                val avgDaily = if (currentDay > 0) totalSpent / currentDay else 0.0
                val projected = totalSpent + (avgDaily * daysLeft)

                // Anomaly Detection: compare each transaction against its category's average
                val anomalies = mutableListOf<AnomalyItem>()
                val catAllExpenses = allTxns.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.CASH_OUT }
                val catAverages = catAllExpenses.groupBy { it.categoryId }.mapValues { entry ->
                    if (entry.value.isNotEmpty()) entry.value.sumOf { it.amount } / entry.value.size else 0.0
                }

                expenseTxns.forEach { t ->
                    val catId = t.categoryId
                    val avg = catAverages[catId] ?: 0.0
                    if (avg > 0 && t.amount >= 2.0 * avg && t.amount > 20.0) {
                        val ratio = t.amount / avg
                        val catName = catMap[catId]?.name ?: "Uncategorized"
                        anomalies.add(AnomalyItem(t, catName, ratio, avg))
                    }
                }

                // 5+ Rule-Based "Explain" Insights
                val insights = mutableListOf<String>()
                if (breakdown.isNotEmpty()) {
                    val top = breakdown.first()
                    insights.add("${top.category.name} is your largest expense (${String.format("%.1f", top.percentage)}% of total spending).")
                }

                if (avgDaily > 0) {
                    insights.add("Daily pace: spending ~${_uiState.value.currencySymbol}${String.format("%.0f", avgDaily)}/day. On track for ~${_uiState.value.currencySymbol}${String.format("%.0f", projected)} by month-end.")
                }

                if (totalIncome > totalSpent && totalSpent > 0) {
                    val savingsRate = ((totalIncome - totalSpent) / totalIncome) * 100
                    insights.add("Healthy cash margin: net savings rate is ${String.format("%.1f", savingsRate)}% of inflows.")
                } else if (totalSpent > totalIncome && totalIncome > 0) {
                    insights.add("Alert: Spending exceeds income by ${_uiState.value.currencySymbol}${String.format("%.2f", totalSpent - totalIncome)} this period.")
                }

                if (anomalies.isNotEmpty()) {
                    insights.add("Detected ${anomalies.size} unusual transaction(s) exceeding 2x category averages.")
                } else {
                    insights.add("Spending patterns are stable with no irregular spikes detected.")
                }

                insights.add("Regularly updating recurring templates keeps month-end forecast accurate.")

                _uiState.update {
                    it.copy(
                        totalSpent = totalSpent,
                        totalIncome = totalIncome,
                        avgDailySpend = avgDaily,
                        projectedMonthEndSpend = projected,
                        daysRemaining = daysLeft,
                        categoryBreakdown = breakdown,
                        explainInsights = insights,
                        anomalies = anomalies
                    )
                }
            }.collect()
        }
    }

    private fun getPeriodBounds(period: String): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        return when (period) {
            "Last 30 Days" -> (now - 30L * 24 * 60 * 60 * 1000) to now
            "Year" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis to now
            }
            else -> { // This Month
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                start to cal.timeInMillis
            }
        }
    }
}
