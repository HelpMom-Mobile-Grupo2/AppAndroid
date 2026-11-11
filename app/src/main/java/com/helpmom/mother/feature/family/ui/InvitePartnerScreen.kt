package com.helpmom.mother.feature.family.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helpmom.mother.HelpMomApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitePartnerScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val app = context.applicationContext as HelpMomApp
    val vm: InvitePartnerViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InvitePartnerViewModel(app.container.familyApi) as T
    })

    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.loadOrGenerateInvitation()
    }

    val partnerDisplayName = vm.partnerName ?: "Tu pareja"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Vincular Acompañante", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Conecta a tu pareja en el Modo Compañero",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                "$partnerDisplayName recibirá tus alertas de apoyo sin invadir tus datos clínicos confidenciales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // QR Card (Item 4: QR from API)
            Card(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val bitmap = vm.qrBitmap
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Código QR para vincular acompañante",
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    if (vm.expiresCountdownText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                "• ${vm.expiresCountdownText}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Manual Code Section (Item 4: Code from API)
            Text("Clave de acceso manual:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        vm.code.ifBlank { "..." },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    TextButton(
                        onClick = {
                            if (vm.code.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Código HelpMom", vm.code))
                                isCopied = true
                            }
                        },
                        enabled = vm.code.isNotBlank()
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isCopied) "¡Copiado!" else "Copiar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Steps Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("1. Tu pareja instala la app y escoge \"Soy Acompañante\".", style = MaterialTheme.typography.bodySmall)
                    Text("2. Escanea este código o ingresa la clave manual.", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Share Button
            OutlinedButton(
                onClick = {
                    if (vm.code.isNotBlank()) {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Únete a HelpMom en el Modo Compañero usando el código: ${vm.code}")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir Enlace de Invitación"))
                    }
                },
                enabled = vm.code.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Compartir Enlace de Invitación", fontWeight = FontWeight.Bold)
            }
        }
    }
}
