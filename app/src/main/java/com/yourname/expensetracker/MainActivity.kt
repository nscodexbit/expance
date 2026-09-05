package com.yourname.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yourname.expensetracker.data.local.SessionManager
import com.yourname.expensetracker.data.repository.BackupRepository
import com.yourname.expensetracker.navigation.ExpenseTrackerNavHost
import com.yourname.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var backupRepository: BackupRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseTrackerNavHost(
                        sessionManager = sessionManager,
                        backupRepository = backupRepository
                    )
                }
            }
        }
    }
}
