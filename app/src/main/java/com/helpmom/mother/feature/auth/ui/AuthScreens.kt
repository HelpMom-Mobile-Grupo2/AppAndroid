package com.helpmom.mother.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helpmom.mother.HelpMomApp
import com.helpmom.mother.core.ui.UiState
import com.helpmom.mother.feature.pregnancy.data.PregnancyRequest
import com.helpmom.mother.feature.pregnancy.domain.calculateDueDate
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun LoginScreen(onAuthenticated: () -> Unit) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(app.container.authRepository, app.container.pregnancyApi, app.container.sessionStore) as T
    })
    var registering by rememberSaveable { mutableStateOf(false) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Logo "H" header
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "H",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "HelpMom",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Acompañamiento seguro en tu gestación",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TabRow(
                    selectedTabIndex = if (registering) 1 else 0
                ) {
                    Tab(
                        selected = !registering,
                        onClick = { registering = false },
                        text = { Text("Ingresar", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = registering,
                        onClick = { registering = true },
                        text = { Text("Crear cuenta", fontWeight = FontWeight.Bold) }
                    )
                }

                if (registering) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña (10 a 128 caracteres)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp)
                )

                vm.fieldErrors.values.forEach { err ->
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                when (val state = vm.state) {
                    is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    UiState.Offline -> Text("Sin conexión. Revisa tu internet", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    else -> Unit
                }

                Button(
                    onClick = {
                        if (registering) vm.register(fullName, email, password)
                        else vm.login(email, password)
                    },
                    enabled = password.length in 10..128 && email.isNotBlank() && (!registering || fullName.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        if (registering) "Crear cuenta" else "Ingresar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (vm.state is UiState.Success) {
                    LaunchedEffect(vm.state) { onAuthenticated() }
                }
                if (vm.state is UiState.Loading) {
                    Text(
                        "Conectando con el servidor…",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun TermsScreen(onAccepted: () -> Unit) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    var accepted by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Términos y Condiciones", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = "Bienvenida a HelpMom. La aplicación proporciona información de seguimiento gestacional y herramientas de apoyo prenatal. Esta información no reemplaza el criterio médico profesional ni constituye diagnóstico clínico.",
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = accepted, onCheckedChange = { accepted = it })
            Text("Leí y acepto los términos y condiciones de uso", modifier = Modifier.padding(start = 8.dp))
        }
        Button(
            enabled = accepted,
            onClick = {
                scope.launch {
                    app.container.sessionStore.acceptTerms(System.currentTimeMillis())
                    onAccepted()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Aceptar y Continuar", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancySetupScreen(onComplete: () -> Unit) {
    val app = LocalContext.current.applicationContext as HelpMomApp
    var lmp by rememberSaveable { mutableStateOf(LocalDate.now().minusDays(70).toString()) }
    var due by rememberSaveable { mutableStateOf(calculateDueDate(LocalDate.parse(lmp)).toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configurar Embarazo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Ingresa la fecha del primer día de tu última menstruación (FUM) para calcular tus semanas de gestación.")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Fecha de Última Menstruación (FUM)", fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("FUM: $lmp")
                }
                Text(
                    "Fecha probable de parto (FPP): $due",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            enabled = !isSubmitting,
            onClick = {
                scope.launch {
                    isSubmitting = true
                    errorMessage = ""
                    val response = runCatching {
                        app.container.pregnancyApi.createPregnancy(PregnancyRequest(lmp, due))
                    }.getOrNull()

                    isSubmitting = false
                    if (response?.isSuccessful == true && response.body() != null) {
                        app.container.sessionStore.savePregnancyId(response.body()!!.id)
                        onComplete()
                    } else {
                        val apiErr = response?.let { com.helpmom.mother.core.network.ApiError.from(it) }
                        errorMessage = apiErr?.detail ?: "No se pudo guardar la configuración del embarazo."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (isSubmitting) "Guardando…" else "Comenzar", fontWeight = FontWeight.Bold)
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(lmp).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        lmp = date.toString()
                        due = calculateDueDate(date).toString()
                    }
                    showDatePicker = false
                }) { Text("Listo") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state) }
    }
}
