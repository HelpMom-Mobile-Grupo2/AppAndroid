package com.helpmom.mother.feature.family.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helpmom.mother.HelpMomApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    onInvite: () -> Unit,
    onTerms: () -> Unit,
    onSignedOut: () -> Unit
) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val vm: ProfileViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(app.container.familyApi, app.container.pregnancyApi, app.container.sessionStore) as T
    })

    val scope = rememberCoroutineScope()
    var confirmUnlink by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.loadProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Mi perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(vm.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(vm.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (vm.weekText.isNotBlank()) {
                    Text(
                        "Embarazo: ${vm.weekText}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                Text("Acompañante vinculado:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    when {
                        vm.isLoading -> "Cargando…"
                        vm.partner != null -> vm.partner!!
                        else -> "Sin acompañante"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onInvite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Vincular / Ver QR de Acompañante", fontWeight = FontWeight.Bold)
                }

                vm.errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                if (vm.partner != null) {
                    OutlinedButton(
                        onClick = { confirmUnlink = true },
                        enabled = !vm.isUnlinking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Desvincular Acompañante", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = onTerms,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Términos y Condiciones", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            app.container.sessionStore.clear()
                            // Room no permite borrar en el hilo principal.
                            withContext(Dispatchers.IO) { app.container.clearLocalData() }
                            onSignedOut()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (confirmUnlink) {
        AlertDialog(
            onDismissRequest = { confirmUnlink = false },
            title = { Text("¿Desvincular acompañante?", fontWeight = FontWeight.Bold) },
            text = { Text("Si desvinculas a tu acompañante, dejará de recibir tus alertas de Pasa la Voz hasta que lo vuelvas a conectar.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmUnlink = false
                    vm.unlinkPartner()
                }) { Text("Desvincular", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmUnlink = false }) { Text("Cancelar") }
            }
        )
    }
}
