package com.helpmom.mother.feature.alerts.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helpmom.mother.HelpMomApp
import com.helpmom.mother.core.ui.theme.HealthRed
import com.helpmom.mother.core.ui.theme.HealthRedContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasaLaVozSheet(onDismiss: () -> Unit) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val vm: PasaLaVozViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PasaLaVozViewModel(app.container.alertsApi, app.container.familyApi) as T
    })

    var selectedType by remember { mutableIntStateOf(2) }

    LaunchedEffect(Unit) {
        vm.loadPartnerName()
    }

    val partnerDisplayName = vm.partnerName ?: "tu acompañante"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row (Item 2: dynamic title)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pasa la Voz a $partnerDisplayName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cerrar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                "Selecciona una necesidad rápida con un toque:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Options List
            val options = listOf(
                1 to ("🍪" to "Tengo un antojo específico"),
                2 to ("🛌" to "Siento mucho cansancio / dolor lumbar"),
                3 to ("🚶" to "¿Puedes venir un momento?")
            )

            options.forEach { (typeId, pair) ->
                val (emoji, text) = pair
                val isSelected = selectedType == typeId
                val borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                val containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                val textColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                        .clickable { selectedType = typeId },
                    color = containerColor
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(emoji, fontSize = 20.sp)
                            Text(
                                text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            if (vm.resultMessage.isNotBlank()) {
                Text(
                    vm.resultMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    vm.sendAlert(selectedType)
                },
                enabled = !vm.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (vm.isLoading) "Enviando…" else "Confirmar y Enviar Notificación",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmergencyDialog(
    guidance: String,
    onSafe: () -> Unit
) {
    BackHandler(enabled = true) {} // Prevent dismiss on back button (Item 6 requirement)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC2A0808)), // Dark red 80% backdrop (Figma Frame 7)
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Red Alert Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(HealthRedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = "Alerta",
                        tint = HealthRed,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    "PROTOCOLO DE SEGURIDAD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HealthRed,
                    letterSpacing = 1.sp
                )

                Text(
                    "Atención Requerida: Posible Emergencia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    guidance.ifBlank { "Esta simulación detectó una posible señal de alarma. Se requiere evaluación clínica inmediata." },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val context = LocalContext.current

                // Red Emergency Call Button (Figma Frame 7)
                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:106")))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthRed)
                ) {
                    Text(
                        "Llamar a Urgencias (106 / SAMU)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    "Tu acompañante ya fue avisado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Item 6 requirement: Link text MUST say "Ya estoy a salvo"
                TextButton(onClick = onSafe) {
                    Text(
                        "Ya estoy a salvo",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
