package com.helpmom.mother.feature.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helpmom.mother.HelpMomApp
import com.helpmom.mother.feature.pregnancy.data.SymptomRequest
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomScreen(onSaved: () -> Unit) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val scope = rememberCoroutineScope()
    var description by rememberSaveable { mutableStateOf("") }
    var severity by rememberSaveable { mutableIntStateOf(1) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Registrar Síntoma", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onSaved) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("¿Qué síntoma o molestia sientes?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Ej. Pequeños tirones en el bajo vientre al ponerme de pie rápido") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Text("Nivel de Intensidad", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Leve", "Moderado", "Severo").forEachIndexed { index, label ->
                            val level = index + 1
                            FilterChip(
                                selected = severity == level,
                                onClick = { severity = level },
                                label = { Text(label, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (errorMessage.isNotBlank()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.weight(1f))

            Button(
                enabled = description.isNotBlank() && !isSaving,
                onClick = {
                    scope.launch {
                        isSaving = true
                        val pregnancyId = app.container.sessionStore.current().pregnancyId
                        if (pregnancyId == null) {
                            errorMessage = "Primero configura tu embarazo."
                            isSaving = false
                        } else {
                            val response = runCatching {
                                app.container.pregnancyApi.addSymptom(
                                    pregnancyId,
                                    SymptomRequest(description, severity, OffsetDateTime.now().toString())
                                )
                            }.getOrNull()

                            isSaving = false
                            if (response?.isSuccessful == true) {
                                onSaved()
                            } else {
                                errorMessage = "No se pudo guardar el síntoma."
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isSaving) "Guardando…" else "Guardar Síntoma", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
