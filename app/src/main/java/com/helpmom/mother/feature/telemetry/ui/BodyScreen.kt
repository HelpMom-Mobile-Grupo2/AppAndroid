package com.helpmom.mother.feature.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helpmom.mother.HelpMomApp
import com.helpmom.mother.core.ui.theme.HealthGreen
import com.helpmom.mother.core.ui.theme.HealthGreenContainer
import com.helpmom.mother.core.ui.theme.HealthRed
import com.helpmom.mother.core.ui.theme.HealthRedContainer
import com.helpmom.mother.feature.alerts.ui.EmergencyDialog
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyScreen(
    onBack: () -> Unit = {},
    onSymptom: () -> Unit
) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val vm: TelemetryViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TelemetryViewModel(app.container.telemetryRepository) as T
    })
    var showMeasurementDialog by remember { mutableStateOf(false) }
    var heartRate by remember { mutableFloatStateOf(140f) }
    var temperature by remember { mutableFloatStateOf(36.8f) }
    var kickCount by remember { mutableIntStateOf(8) }
    var emergencyDismissed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header (Figma Frame 2: Mi Cuerpo y Telemetría)
        TopAppBar(
            title = {
                Text(
                    "Mi Cuerpo y Telemetría",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Sensor Band (Figma Frame 2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "BLE  Sensor HelpMom Band",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Batería 85% · Conectado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HealthGreenContainer
                        ) {
                            Text(
                                "Sync OK",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HealthGreen
                            )
                        }
                    }
                }
            }

            // Card 2: Heart Rate (Figma Frame 2)
            item {
                val latestState = vm.state
                val fhr = if (latestState is com.helpmom.mother.core.ui.UiState.Success) latestState.data.fetalHeartRateBpm else 78

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Frecuencia Cardíaca", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$fhr LPM", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = HealthGreenContainer) {
                                Text("En Rango", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HealthGreen)
                            }
                        }
                    }
                }
            }

            // Card 3: Blood Pressure & Temperature (Figma Frame 2)
            item {
                val latestState = vm.state
                val temp = if (latestState is com.helpmom.mother.core.ui.UiState.Success) "%.1f".format(latestState.data.temperatureCelsius) else "36.8"

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Presión Arterial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("115 / 75 mmHg", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Surface(shape = RoundedCornerShape(10.dp), color = HealthGreenContainer) {
                                Text("Óptima", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HealthGreen)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Temperatura", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$temp °C", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Surface(shape = RoundedCornerShape(10.dp), color = HealthGreenContainer) {
                                Text("Óptima", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = HealthGreen)
                            }
                        }
                    }
                }
            }

            // Card 4: Fetal Kicks (Figma Frame 2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Movimientos Fetales", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$kickCount pataditas hoy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { kickCount++ },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Registrar patadita", tint = Color.White)
                        }
                    }
                }
            }

            // History header
            item {
                Text("Historial de Mediciones", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            // History items
            items(vm.history.value) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FCF ${item.fetalHeartRateBpm} LPM · ${item.temperatureCelsius} °C", fontWeight = FontWeight.Bold)
                            val lightColor = if (item.healthLight == 3) HealthRed else HealthGreen
                            Text("Semáforo: ${if (item.healthLight == 3) "Rojo" else "Verde"}", color = lightColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(item.healthSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.measuredAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Bottom Actions (Figma Frame 2)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSymptom,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("+ Registrar Nuevo Síntoma", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = { showMeasurementDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Tomar Medición (Simulada)", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Emergency check
    val currentMeasurement = vm.state
    if (!emergencyDismissed && currentMeasurement is com.helpmom.mother.core.ui.UiState.Success && currentMeasurement.data.healthLight == 3) {
        EmergencyDialog(currentMeasurement.data.healthSummary) { emergencyDismissed = true }
    }

    // Measurement Dialog
    if (showMeasurementDialog) {
        AlertDialog(
            onDismissRequest = { showMeasurementDialog = false },
            title = { Text("Medición Simulada", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Frecuencia cardíaca fetal: ${heartRate.toInt()} LPM")
                    Slider(heartRate, { heartRate = it }, valueRange = 30f..250f)
                    Text("Temperatura: ${"%.1f".format(temperature)} °C")
                    Slider(temperature, { temperature = it }, valueRange = 30f..45f)
                    TextButton(onClick = {
                        heartRate = Random.nextInt(120, 161).toFloat()
                        temperature = Random.nextDouble(36.2, 37.5).toFloat()
                    }) { Text("Valores aleatorios realistas") }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    emergencyDismissed = false
                    vm.record(heartRate.toInt(), temperature.toDouble())
                    showMeasurementDialog = false
                }) { Text("Guardar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showMeasurementDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
