package com.helpmom.mother.feature.pregnancy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helpmom.mother.HelpMomApp
import com.helpmom.mother.core.ui.theme.*
import com.helpmom.mother.feature.pregnancy.domain.babyFruitInfo
import com.helpmom.mother.feature.pregnancy.domain.daysRemaining
import com.helpmom.mother.feature.pregnancy.domain.healthBadgeLabel
import com.helpmom.mother.feature.pregnancy.domain.healthStatusLabel
import com.helpmom.mother.feature.pregnancy.domain.timeAgoLabel
import com.helpmom.mother.feature.pregnancy.domain.responseLabel
import com.helpmom.mother.feature.pregnancy.domain.trimester
import java.time.LocalDate

@Composable
fun HomeScreen(
    onBody: () -> Unit,
    onSymptom: () -> Unit,
    onLearning: () -> Unit,
    onProfile: () -> Unit,
    onAlert: () -> Unit,
    onInvite: () -> Unit = {},
    onSetup: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val vm: HomeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(
                app.container.pregnancyApi,
                app.container.familyApi,
                app.container.telemetryApi,
                app.container.alertsApi,
                app.container.sessionStore
            ) as T
    })

    LaunchedEffect(Unit) {
        vm.loadData()
    }

    LaunchedEffect(Unit) {
        while (true) {
            vm.pollAlerts()
            kotlinx.coroutines.delay(15_000)
        }
    }

    val pregnancy = vm.pregnancy
    if (pregnancy == null) {
        HomeWithoutPregnancy(
            isLoading = vm.isLoading,
            pregnancyMissing = vm.pregnancyMissing,
            errorMessage = vm.errorMessage,
            onSetup = onSetup,
            onRetry = { vm.loadData() }
        )
        return
    }

    val weeks = pregnancy.gestationalWeeks
    val days = pregnancy.gestationalDays
    val dueDate = runCatching { LocalDate.parse(pregnancy.dueDate.take(10)) }.getOrNull() ?: LocalDate.now()
    val remainingDays = daysRemaining(dueDate)
    val currentTrimester = trimester(weeks)
    val trimesterText = when (currentTrimester) {
        1 -> "PRIMER TRIMESTRE"
        2 -> "SEGUNDO TRIMESTRE"
        else -> "TERCER TRIMESTRE"
    }
    val fruitInfo = babyFruitInfo(weeks)
    val partnerName = vm.partnerName
    val partnerDisplayName = partnerName ?: "tu acompañante"

    val healthLight = vm.healthStatus?.healthLight
    val badgeText = healthBadgeLabel(healthLight)
    val (healthColor, healthContainerColor, healthIcon) = when (healthLight) {
        3 -> Triple(HealthRed, HealthRedContainer, Icons.Rounded.Warning)
        2 -> Triple(HealthAmber, HealthAmberContainer, Icons.Rounded.Warning)
        1 -> Triple(HealthGreen, HealthGreenContainer, Icons.Rounded.CheckCircle)
        else -> Triple(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant, Icons.Rounded.CheckCircle)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row (Figma Frame 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "H",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            "HelpMom",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Gestación segura",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "Semana $weeks · Día $days",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { onProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            vm.motherInitials,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Milestone Card (Figma Frame 1)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            trimesterText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Tu bebé tiene el tamaño de una ${fruitInfo.fruitName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Peso aprox: ${fruitInfo.weightText} · Longitud: ${fruitInfo.lengthText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HealthGreenContainer
                        ) {
                            Text(
                                "Faltan $remainingDays días",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HealthGreen
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF9C4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(fruitInfo.emoji, fontSize = 36.sp)
                    }
                }
            }

            // Health Status Card (Item 1 requirement: NO NUMBERS)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBody() }
                    .semantics(mergeDescendants = true) {},
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(healthIcon, contentDescription = null, tint = healthColor, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Estado de salud: ${healthStatusLabel(healthLight)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                val measuredAgo = timeAgoLabel(vm.healthStatus?.measuredAt)
                                if (measuredAgo.isNotBlank()) {
                                    Text(measuredAgo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = healthContainerColor) {
                            Text(badgeText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = healthColor)
                        }
                    }
                    Text(
                        vm.healthStatus?.healthSummary ?: "Aún no hay mediciones. Toma una en Mi Cuerpo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Acompañante Response Card
            vm.latestAlert?.let { alert ->
                if (alert.status == 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Column {
                                Text(
                                    "Esperando a $partnerDisplayName…",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Enviada ${timeAgoLabel(alert.createdAt).lowercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (alert.status == 2) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Column {
                                Text(
                                    "$partnerDisplayName: ${responseLabel(alert.response)}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    timeAgoLabel(alert.respondedAt ?: alert.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Wellness Actions Section
            Text(
                "ACCIONES DE BIENESTAR",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSymptom() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Registrar síntoma", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLearning() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Guía semana $weeks", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // Floating pill button (Item 2 requirement: "Vincular ahora" if no partner)
        ExtendedFloatingActionButton(
            onClick = {
                if (partnerName != null) onAlert() else onInvite()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                if (partnerName != null) Icons.Rounded.Notifications else Icons.Filled.PersonAdd,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (partnerName != null) "Pasa la Voz ($partnerName)" else "Vincular ahora",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeWithoutPregnancy(
    isLoading: Boolean,
    pregnancyMissing: Boolean,
    errorMessage: String?,
    onSetup: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
                Text("Cargando tu embarazo…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            pregnancyMissing -> {
                Text("Aún no configuraste tu embarazo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Ingresa tu FUM para calcular tus semanas de gestación.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onSetup, shape = RoundedCornerShape(14.dp)) { Text("Configurar embarazo") }
            }
            else -> {
                Text(errorMessage ?: "No pudimos cargar tus datos.", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(14.dp)) { Text("Reintentar") }
            }
        }
    }
}
