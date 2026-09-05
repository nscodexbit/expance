package com.yourname.expensetracker.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.expensetracker.data.local.entity.Profile
import com.yourname.expensetracker.data.local.entity.ProfileType
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var step by remember { mutableStateOf(0) }
    var selectedType by remember { mutableStateOf<ProfileType?>(null) }
    var profileName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    when (step) {
        0 -> WelcomeScreen(
            onNext = { step = 1 }
        )
        1 -> ChooseTypeScreen(
            selectedType = selectedType,
            onSelectType = { selectedType = it },
            onNext = { step = 2 },
            onBack = { step = 0 }
        )
        2 -> PersonalizationScreen(
            profileName = profileName,
            onProfileNameChange = { profileName = it },
            onFinish = {
                scope.launch {
                    val type = selectedType ?: ProfileType.PERSONAL
                    viewModel.createProfile(
                        name = profileName.ifBlank { "My ${type.name.first()}${type.name.lowercase().drop(1)}" },
                        type = type
                    )
                    onFinished()
                }
            },
            onBack = { step = 1 }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WelcomeScreen(onNext: () -> Unit) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Track every rupee,\nknow where it went.",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Simple expense tracking for personal spending and shop books. 100% on your device.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChooseTypeScreen(
    selectedType: ProfileType?,
    onSelectType: (ProfileType) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What are you here for?") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onNext,
                enabled = selectedType != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TypeOptionCard(
                title = "Personal Spending",
                description = "Track personal expenses, budgets, and savings goals",
                icon = Icons.Default.Wallet,
                selected = selectedType == ProfileType.PERSONAL,
                onClick = { onSelectType(ProfileType.PERSONAL) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TypeOptionCard(
                title = "Running a Shop",
                description = "Cash ledger, customer khata book, and supplier tracking",
                icon = Icons.Default.Store,
                selected = selectedType == ProfileType.SHOP,
                onClick = { onSelectType(ProfileType.SHOP) }
            )
        }
    }
}

@Composable
private fun TypeOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
    val bgColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .background(borderColor, RoundedCornerShape(12.dp)),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
    // Note: border is visually represented by background; kept simple
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalizationScreen(
    profileName: String,
    onProfileNameChange: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Almost there!") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start Tracking", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Give your profile a name",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = profileName,
                onValueChange = onProfileNameChange,
                label = { Text("Profile name (optional)") },
                placeholder = { Text("e.g. My Wallet / My Shop") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You can change this anytime in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
