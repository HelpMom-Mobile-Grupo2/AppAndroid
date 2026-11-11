package com.helpmom.mother.feature.learning.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.helpmom.mother.HelpMomApp
import com.helpmom.mother.core.database.SavedArticleEntity
import kotlinx.coroutines.launch

data class Article(
    val id: String,
    val title: String,
    val category: String,
    val weekStart: Int,
    val weekEnd: Int,
    val summary: String,
    val body: String,
    val imageUrl: String
)

@Composable
fun LearningScreen() {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val scope = rememberCoroutineScope()
    val saved by app.container.database.savedArticleDao().observeSavedArticles().collectAsState(emptyList())
    var articles by remember { mutableStateOf(emptyList<Article>()) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todas") }
    var showSaved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val json = app.assets.open("articles.json").bufferedReader().use { it.readText() }
        articles = Gson().fromJson(json, object : TypeToken<List<Article>>() {}.type)
    }

    val visible = (if (showSaved) saved.map {
        Article(it.articleId, it.title, it.category, it.weekStart, it.weekEnd, it.summary, it.body, it.imageUrl)
    } else articles)
        .filter { it.title.contains(query, true) && (category == "Todas" || it.category == category) }

    val windowInfo = currentWindowAdaptiveInfo()
    val isExpanded = !windowInfo.windowSizeClass.windowWidthSizeClass.toString().contains("COMPACT", true)

    if (isExpanded) {
        // Two-pane Adaptive Layout (List + Detail)
        Row(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Guía y Aprendizaje", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar artículos") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todas", "Nutrición", "Ejercicio", "Parto").forEach { label ->
                        FilterChip(
                            selected = category == label,
                            onClick = { category = label },
                            label = { Text(label) }
                        )
                    }
                }

                TextButton(onClick = { showSaved = !showSaved }) {
                    Text(if (showSaved) "Ver todos" else "Ver guardados (${saved.size})")
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visible) { article ->
                        Card(
                            onClick = { selectedArticle = article },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedArticle?.id == article.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(article.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(article.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            VerticalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                selectedArticle?.let { article ->
                    ArticleDetailPane(
                        article = article,
                        isSaved = saved.any { s -> s.articleId == article.id },
                        onToggleSave = {
                            scope.launch {
                                val dao = app.container.database.savedArticleDao()
                                if (saved.any { s -> s.articleId == article.id }) dao.remove(article.id)
                                else dao.save(SavedArticleEntity(article.id, article.title, article.category, article.weekStart, article.weekEnd, article.summary, article.imageUrl, article.body))
                            }
                        }
                    )
                } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Selecciona un artículo para leer más.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    } else {
        // Single-pane Compact Layout
        if (selectedArticle != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedArticle = null }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                    Text("Volver a la lista", fontWeight = FontWeight.Bold)
                }
                ArticleDetailPane(
                    article = selectedArticle!!,
                    isSaved = saved.any { s -> s.articleId == selectedArticle!!.id },
                    onToggleSave = {
                        scope.launch {
                            val dao = app.container.database.savedArticleDao()
                            if (saved.any { s -> s.articleId == selectedArticle!!.id }) dao.remove(selectedArticle!!.id)
                            else dao.save(SavedArticleEntity(selectedArticle!!.id, selectedArticle!!.title, selectedArticle!!.category, selectedArticle!!.weekStart, selectedArticle!!.weekEnd, selectedArticle!!.summary, selectedArticle!!.imageUrl, selectedArticle!!.body))
                        }
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Guía y Aprendizaje", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar artículos") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todas", "Nutrición", "Ejercicio", "Parto").forEach { label ->
                        FilterChip(
                            selected = category == label,
                            onClick = { category = label },
                            label = { Text(label) }
                        )
                    }
                }

                TextButton(onClick = { showSaved = !showSaved }) {
                    Text(if (showSaved) "Ver todos los artículos" else "Ver guardados (${saved.size})")
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visible) { article ->
                        Card(
                            onClick = { selectedArticle = article },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(article.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(article.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleDetailPane(
    article: Article,
    isSaved: Boolean,
    onToggleSave: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AsyncImage(
            model = article.imageUrl,
            contentDescription = "Imagen: ${article.title}",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = ColorPainter(Color.LightGray)
        )
        Text(article.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(article.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(article.body, style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = onToggleSave,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (isSaved) "Quitar de Guardados" else "Guardar Artículo", fontWeight = FontWeight.Bold)
        }
    }
}
