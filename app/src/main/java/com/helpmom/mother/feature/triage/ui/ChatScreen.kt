package com.helpmom.mother.feature.triage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
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
import com.helpmom.mother.core.network.formatForPhone
import com.helpmom.mother.core.ui.theme.*
import com.helpmom.mother.feature.alerts.ui.EmergencyDialog
import com.helpmom.mother.feature.pregnancy.domain.urgencyLabel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign
import com.helpmom.mother.feature.triage.data.TriageSummaryDto

@Composable
fun ChatScreen() {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val vm: ChatViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatViewModel(app.container.triageApi) as T
    })

    val listState = rememberLazyListState()
    var showHistory by rememberSaveable { mutableStateOf(false) }
    val messages = vm.conversation?.messages.orEmpty()
    // Índice del último mensaje del asistente: solo ahí va el chip de urgencia.
    val lastAssistantIndex = messages.indexOfLast { it.author == 2 }

    // Baja al último mensaje cuando llega uno nuevo.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // Header (Item 5: Asistente simulado, no reemplaza a tu médico)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "IA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "Asistente prenatal HelpMom",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "● En línea 24/7",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HealthGreen
                    )
                }
                IconButton(onClick = {
                    showHistory = !showHistory
                    if (showHistory) vm.loadHistory()
                }) {
                    Icon(Icons.Rounded.History, contentDescription = "Conversaciones anteriores")
                }
            }
        }

        Text(
            "Asistente simulado, no reemplaza a tu médico.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center
        )

        if (showHistory) {
            ConversationHistory(
                vm = vm,
                modifier = Modifier.weight(1f),
                onOpen = { id ->
                    vm.openConversation(id)
                    showHistory = false
                },
                onNew = {
                    vm.newConversation()
                    showHistory = false
                }
            )
        } else if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Cuéntame cómo te sientes. Por ejemplo: \"tengo náuseas leves\" o \"me duele la cabeza\".",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages, key = { _, msg -> msg.id }) { index, msg ->
                val isMother = msg.author == 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMother) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMother) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                msg.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isMother) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Show urgency chip for assistant responses
                            if (!isMother && index == lastAssistantIndex) {
                                val urgency = vm.conversation?.urgency
                                val chipText = urgencyLabel(urgency)
                                if (chipText.isNotBlank()) {
                                    val (bgColor, textColor) = when (urgency) {
                                        1 -> HealthGreenContainer to HealthGreen
                                        2 -> HealthAmberContainer to HealthAmber
                                        else -> HealthRedContainer to HealthRed
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = bgColor,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            chipText,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (vm.errorMessage.isNotBlank()) {
            Text(
                vm.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Bottom text field row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = vm.inputMessage,
                    enabled = vm.emergencyGuidance == null,
                    onValueChange = { vm.inputMessage = it },
                    placeholder = { Text("Escribe tu consulta aquí…") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )

                IconButton(
                    onClick = { vm.sendMessage() },
                    enabled = !vm.isSending && vm.emergencyGuidance == null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Modal de Emergencia (Item 6)
    vm.emergencyGuidance?.let { guidance ->
        EmergencyDialog(guidance = guidance) {
            vm.dismissEmergency()
        }
    }
}

@Composable
private fun ConversationHistory(
    vm: ChatViewModel,
    modifier: Modifier,
    onOpen: (String) -> Unit,
    onNew: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Conversaciones anteriores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onNew) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Nueva")
            }
        }
        when {
            vm.isLoadingHistory -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            vm.history.isEmpty() -> Text(
                "Todavía no tienes conversaciones.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vm.history, key = { it.id }) { item -> ConversationRow(item) { onOpen(item.id) } }
            }
        }
    }
}

@Composable
private fun ConversationRow(item: TriageSummaryDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    runCatching { formatForPhone(item.createdAt) }.getOrDefault(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                UrgencyChip(item.urgency)
            }
            Text(item.lastMessage, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun UrgencyChip(urgency: Int?) {
    val text = urgencyLabel(urgency)
    if (text.isBlank()) return
    val (bgColor, textColor) = when (urgency) {
        1 -> HealthGreenContainer to HealthGreen
        2 -> HealthAmberContainer to HealthAmber
        else -> HealthRedContainer to HealthRed
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
