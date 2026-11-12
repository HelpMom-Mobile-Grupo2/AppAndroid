package com.helpmom.mother.feature.learning.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
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

/** true si el artículo aplica a la semana de embarazo indicada. */
fun Article.isForWeek(week: Int): Boolean = week in weekStart..weekEnd

private val categories = listOf("Todas", "Nutrición", "Ejercicio", "Parto")

/**
 * Aprendizaje con NavigableListDetailPaneScaffold:
 * en teléfono la lista y el detalle son pantallas separadas (con atrás),
 * en tablet se ven los dos paneles lado a lado.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LearningScreen() {
    val app = LocalContext.current.applicationContext as HelpMomApp
    val scope = rememberCoroutineScope()
    val dao = app.container.database.savedArticleDao()
    val saved by dao.observeSavedArticles().collectAsState(emptyList())
    var articles by remember { mutableStateOf(emptyList<Article>()) }
    var currentWeek by remember { mutableStateOf<Int?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Todas") }
    var onlyMyWeek by rememberSaveable { mutableStateOf(false) }
    var showSaved by rememberSaveable { mutableStateOf(false) }

    // El navegador guarda el id del artículo abierto (un String se puede guardar al rotar).
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

    LaunchedEffect(Unit) {
        val json = app.assets.open("articles.json").bufferedReader().use { it.readText() }
        articles = Gson().fromJson(json, object : TypeToken<List<Article>>() {}.type)
        currentWeek = runCatching {
            app.container.pregnancyApi.getPregnancies().body()?.firstOrNull()?.gestationalWeeks
        }.getOrNull()
    }

    val source = if (showSaved) saved.map {
        Article(it.articleId, it.title, it.category, it.weekStart, it.weekEnd, it.summary, it.body, it.imageUrl)
    } else articles
    val visible = source.filter { article ->
        article.title.contains(query, ignoreCase = true) &&
            (category == "Todas" || article.category == category) &&
            (!onlyMyWeek || currentWeek == null || article.isForWeek(currentWeek!!))
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                ArticleList(
                    articles = visible,
                    selectedId = navigator.currentDestination?.contentKey,
                    query = query,
                    onQueryChange = { query = it },
                    category = category,
                    onCategoryChange = { category = it },
                    currentWeek = currentWeek,
                    onlyMyWeek = onlyMyWeek,
                    onOnlyMyWeekChange = { onlyMyWeek = it },
                    showSaved = showSaved,
                    savedCount = saved.size,
                    onShowSavedChange = { showSaved = it },
                    onOpen = { article ->
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, article.id) }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val articleId = navigator.currentDestination?.contentKey
                val article = articles.firstOrNull { it.id == articleId }
                    ?: saved.firstOrNull { it.articleId == articleId }?.let {
                        Article(it.articleId, it.title, it.category, it.weekStart, it.weekEnd, it.summary, it.body, it.imageUrl)
                    }
                if (article == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Selecciona un artículo para leer más.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val isSaved = saved.any { it.articleId == article.id }
                    ArticleDetail(
                        article = article,
                        isSaved = isSaved,
                        showBack = navigator.canNavigateBack(),
                        onBack = { scope.launch { navigator.navigateBack() } },
                        onToggleSave = {
                            scope.launch {
                                if (isSaved) dao.remove(article.id)
                                else dao.save(
                                    SavedArticleEntity(
                                        article.id, article.title, article.category, article.weekStart,
                                        article.weekEnd, article.summary, article.imageUrl, article.body
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun ArticleList(
    articles: List<Article>,
    selectedId: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    currentWeek: Int?,
    onlyMyWeek: Boolean,
    onOnlyMyWeekChange: (Boolean) -> Unit,
    showSaved: Boolean,
    savedCount: Int,
    onShowSavedChange: (Boolean) -> Unit,
    onOpen: (Article) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Guía y aprendizaje", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Buscar artículos") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (currentWeek != null) {
                FilterChip(
                    selected = onlyMyWeek,
                    onClick = { onOnlyMyWeekChange(!onlyMyWeek) },
                    label = { Text("Mi semana ($currentWeek)") }
                )
            }
            categories.forEach { label ->
                FilterChip(
                    selected = category == label,
                    onClick = { onCategoryChange(label) },
                    label = { Text(label) }
                )
            }
        }

        TextButton(onClick = { onShowSavedChange(!showSaved) }) {
            Text(if (showSaved) "Ver todos los artículos" else "Ver guardados ($savedCount)")
        }

        if (articles.isEmpty()) {
            Text(
                if (showSaved) "Aún no guardaste artículos." else "No hay artículos con ese filtro.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(articles, key = { it.id }) { article ->
                Card(
                    onClick = { onOpen(article) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedId == article.id) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "${article.category} · Semanas ${article.weekStart}–${article.weekEnd}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(article.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleDetail(
    article: Article,
    isSaved: Boolean,
    showBack: Boolean,
    onBack: () -> Unit,
    onToggleSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showBack) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver a la lista")
                }
                Text("Volver a la lista", fontWeight = FontWeight.Bold)
            }
        }
        AsyncImage(
            model = article.imageUrl,
            contentDescription = "Imagen: ${article.title}",
            contentScale = ContentScale.Crop,
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isSaved) "Quitar de guardados" else "Guardar artículo", fontWeight = FontWeight.Bold)
        }
    }
}
