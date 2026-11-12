package com.helpmom.mother.feature.telemetry.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helpmom.mother.HelpMomApp
import com.helpmom.mother.core.database.MeasurementEntity
import com.helpmom.mother.core.network.formatForPhone
import com.helpmom.mother.core.ui.UiState
import com.helpmom.mother.core.ui.theme.HealthAmber
import com.helpmom.mother.core.ui.theme.HealthAmberContainer
import com.helpmom.mother.core.ui.theme.HealthGreen
import com.helpmom.mother.core.ui.theme.HealthGreenContainer
import com.helpmom.mother.core.ui.theme.HealthRed
import com.helpmom.mother.core.ui.theme.HealthRedContainer
import com.helpmom.mother.feature.alerts.ui.EmergencyDialog
import com.helpmom.mother.feature.pregnancy.data.SymptomDto
import com.helpmom.mother.feature.pregnancy.domain.timeAgoLabel
import kotlin.random.Random

/** Colores del semáforo: 1 verde, 2 ámbar, 3 rojo. */
private fun lightColors(healthLight: Int): Pair<Color, Color> = when (healthLight) {
    3 -> HealthRed to HealthRedContainer
    2 -> HealthAmber to HealthAmberContainer
    else -> HealthGreen to HealthGreenContainer
}

/** Texto del badge de cada valor según el semáforo de la medición. */
private fun rangeLabel(healthLight: Int): String = when (healthLight) {
    3 -> "Fuera de rango"
    2 -> "Revisar"
    else -> "En rango"
}

private fun lightName(healthLight: Int): String = when (healthLight) {
    3 -> "Rojo"
    2 -> "Ámbar"
    else -> "Verde"
}

private fun severityName(severity: Int): String = when (severity) {
    3 -> "Severo"
    2 -> "Moderado"
    else -> "Leve"
}

private fun safeDate(value: String): String = runCatching { formatForPhone(value) }.getOrDefault(value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyScreen(
    onBack: () -> Unit,
    onSymptom: () -> Unit
) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val vm: TelemetryViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TelemetryViewModel(
            app.container.telemetryRepository,
            symptomsLoader = {
                val pregnancyId = app.container.sessionStore.current().pregnancyId
                if (pregnancyId.isNullOrBlank()) emptyList()
                else app.container.pregnancyApi.getPregnancy(pregnancyId).body()?.symptoms.orEmpty()
            }
        ) as T
    })
    val history by vm.history.collectAsState()
    var showMeasurementDialog by remember { mutableStateOf(false) }
    var heartRate by remember { mutableFloatStateOf(140f) }
    var temperature by remember { mutableFloatStateOf(36.8f) }

    // Al volver de "Registrar síntoma" se recarga la lista.
    LaunchedEffect(Unit) { vm.loadSymptoms() }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Mi Cuerpo y Telemetría", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sensor simulado
            item {
                val latest = (vm.state as? UiState.Success)?.data
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Sensor simulado", fontWeight = FontWeight.Bold)
                            Text(
                                if (latest != null) "Última lectura: ${timeAgoLabel(latest.measuredAt).lowercase()}"
                                else "Aún no hay mediciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Pill(
                            text = if (vm.state is UiState.Offline) "Sin conexión" else "Sync OK",
                            color = if (vm.state is UiState.Offline) HealthAmber else HealthGreen,
                            container = if (vm.state is UiState.Offline) HealthAmberContainer else HealthGreenContainer
                        )
                    }
                }
            }

            // Valores de la última medición
            when (val current = vm.state) {
                is UiState.Success -> {
                    item {
                        ValueCard(
                            icon = Icons.Rounded.Favorite,
                            title = "Frecuencia cardíaca fetal",
                            value = "${current.data.fetalHeartRateBpm} lpm",
                            healthLight = current.data.healthLight
                        )
                    }
                    item {
                        ValueCard(
                            icon = Icons.Rounded.Thermostat,
                            title = "Temperatura",
                            value = "${"%.1f".format(current.data.temperatureCelsius)} °C",
                            healthLight = current.data.healthLight
                        )
                    }
                    item {
                        Text(
                            current.data.healthSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> item {
                    Text(
                        when (current) {
                            UiState.Offline -> "Sin conexión. Se muestran las mediciones guardadas."
                            is UiState.Error -> current.message
                            else -> "Aún no hay mediciones. Toca \"Tomar medición (simulada)\"."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            vm.errorMessage?.let { message ->
                item { Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }

            // Historial
            if (history.isNotEmpty()) {
                item { SectionTitle("HISTORIAL DE MEDICIONES") }
                items(history, key = { it.id }) { item -> HistoryCard(item) }
            }

            // Síntomas registrados
            item { SectionTitle("SÍNTOMAS REGISTRADOS") }
            if (vm.symptoms.isEmpty()) {
                item {
                    Text(
                        "Todavía no registraste síntomas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(vm.symptoms, key = { it.id }) { symptom -> SymptomCard(symptom) }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSymptom,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+ Registrar nuevo síntoma", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { showMeasurementDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Tomar medición (simulada)", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal de emergencia: se abre con cada medición roja nueva.
    vm.pendingEmergency?.let { measurement ->
        EmergencyDialog(measurement.healthSummary) { vm.dismissEmergency() }
    }

    if (showMeasurementDialog) {
        AlertDialog(
            onDismissRequest = { showMeasurementDialog = false },
            title = { Text("Medición simulada", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Frecuencia cardíaca fetal: ${heartRate.toInt()} lpm")
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
                TextButton(
                    enabled = !vm.isSaving,
                    onClick = {
                        vm.record(heartRate.toInt(), temperature.toDouble())
                        showMeasurementDialog = false
                    }
                ) { Text("Guardar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showMeasurementDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun Pill(text: String, color: Color, container: Color) {
    Surface(shape = RoundedCornerShape(50), color = container) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ValueCard(icon: ImageVector, title: String, value: String, healthLight: Int) {
    val (color, container) = lightColors(healthLight)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Pill(rangeLabel(healthLight), color, container)
        }
    }
}

@Composable
private fun HistoryCard(item: MeasurementEntity) {
    val (color, _) = lightColors(item.healthLight)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${item.fetalHeartRateBpm} lpm · ${"%.1f".format(item.temperatureCelsius)} °C",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Semáforo: ${lightName(item.healthLight)}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(item.healthSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(safeDate(item.measuredAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun SymptomCard(symptom: SymptomDto) {
    val (color, container) = lightColors(symptom.severity)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(symptom.description, fontWeight = FontWeight.Bold)
                Text(safeDate(symptom.occurredAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Pill(severityName(symptom.severity), color, container)
        }
    }
}
