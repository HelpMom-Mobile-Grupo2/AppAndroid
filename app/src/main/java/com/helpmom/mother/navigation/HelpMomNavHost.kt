package com.helpmom.mother.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.helpmom.mother.HelpMomApp
import com.helpmom.mother.R
import com.helpmom.mother.feature.alerts.ui.PasaLaVozSheet
import com.helpmom.mother.feature.auth.ui.LoginScreen
import com.helpmom.mother.feature.auth.ui.PregnancySetupScreen
import com.helpmom.mother.feature.auth.ui.TermsScreen
import com.helpmom.mother.feature.family.ui.InvitePartnerScreen
import com.helpmom.mother.feature.family.ui.ProfileScreen
import com.helpmom.mother.feature.learning.ui.LearningScreen
import com.helpmom.mother.feature.pregnancy.domain.findPregnancyId
import com.helpmom.mother.feature.pregnancy.ui.HomeScreen
import com.helpmom.mother.feature.telemetry.ui.BodyScreen
import com.helpmom.mother.feature.telemetry.ui.SymptomScreen
import com.helpmom.mother.feature.triage.ui.ChatScreen
import kotlinx.coroutines.launch

/**
 * Después de iniciar sesión: si la mamá ya tiene un embarazo va a Inicio,
 * si no, a Configurar embarazo. Así no se crea un embarazo duplicado.
 */
private suspend fun routeAfterLogin(app: HelpMomApp): String {
    val store = app.container.sessionStore
    val pregnancyId = findPregnancyId(store.current().pregnancyId) {
        app.container.pregnancyApi.getPregnancies().body()?.firstOrNull()?.id
    }
    return if (pregnancyId == null) {
        Routes.PREGNANCY_SETUP
    } else {
        store.savePregnancyId(pregnancyId)
        Routes.HOME
    }
}

private data class Destination(val route: String, val label: Int, val icon: ImageVector)
private val destinations = listOf(
    Destination(Routes.HOME, R.string.nav_home, Icons.Rounded.Home),
    Destination(Routes.BODY, R.string.nav_body, Icons.Rounded.Favorite),
    Destination(Routes.ASSISTANT, R.string.nav_assistant, Icons.AutoMirrored.Rounded.Chat),
    Destination(Routes.LEARNING, R.string.nav_learning, Icons.AutoMirrored.Rounded.MenuBook)
)

@Composable
fun HelpMomNavHost() {
    val nav = rememberNavController()
    val app = LocalContext.current.applicationContext as HelpMomApp
    SideEffect {
        app.container.setUnauthorizedHandler {
            nav.navigate(Routes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }
    val scope = rememberCoroutineScope()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination
    val rail = !currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass.toString().contains("COMPACT", true)
    val rootRoute = current?.hierarchy?.any { it.route in destinations.map(Destination::route) } == true

    Scaffold(bottomBar = {
        if (rootRoute && !rail) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                destinations.forEach { d ->
                    val selected = current?.hierarchy?.any { it.route == d.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(d.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(d.icon, contentDescription = stringResource(d.label)) },
                        label = { Text(stringResource(d.label)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (rootRoute && rail) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    destinations.forEach { d ->
                        val selected = current?.hierarchy?.any { it.route == d.route } == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = { nav.navigate(d.route) { launchSingleTop = true } },
                            icon = { Icon(d.icon, contentDescription = stringResource(d.label)) },
                            label = { Text(stringResource(d.label)) },
                            alwaysShowLabel = true,
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            NavHost(nav, startDestination = Routes.SPLASH, modifier = Modifier.weight(1f).fillMaxSize()) {
                composable(Routes.SPLASH) {
                    val appCtx = LocalContext.current.applicationContext as HelpMomApp
                    var checked by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        val session = appCtx.container.sessionStore.current()
                        val next = when {
                            session.termsAcceptedAt == null -> Routes.TERMS
                            session.token.isNullOrBlank() -> Routes.LOGIN
                            else -> routeAfterLogin(appCtx)
                        }
                        nav.navigate(next) { popUpTo(Routes.SPLASH) { inclusive = true } }
                        checked = true
                    }
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (checked) "Preparando tu espacio…" else "HelpMom", style = MaterialTheme.typography.headlineLarge)
                    }
                }
                composable(Routes.TERMS) { TermsScreen { nav.navigate(Routes.LOGIN) { popUpTo(Routes.TERMS) { inclusive = true } } } }
                composable(Routes.LOGIN) {
                    LoginScreen {
                        scope.launch {
                            val next = routeAfterLogin(app)
                            nav.navigate(next) { popUpTo(Routes.LOGIN) { inclusive = true } }
                        }
                    }
                }
                composable(Routes.PREGNANCY_SETUP) { PregnancySetupScreen { nav.navigate(Routes.HOME) { popUpTo(Routes.PREGNANCY_SETUP) { inclusive = true } } } }
                composable(Routes.HOME) { HomeScreen(
                    onBody = { nav.navigate(Routes.BODY) }, onSymptom = { nav.navigate(Routes.SYMPTOM) },
                    onLearning = { nav.navigate(Routes.LEARNING) }, onProfile = { nav.navigate(Routes.PROFILE) },
                    onAlert = { nav.navigate(Routes.ALERT) },
                    onInvite = { nav.navigate(Routes.INVITE_PARTNER) },
                    onSetup = { nav.navigate(Routes.PREGNANCY_SETUP) }
                ) }
                composable(Routes.BODY) { BodyScreen(onBack = { nav.popBackStack() }, onSymptom = { nav.navigate(Routes.SYMPTOM) }) }
                composable(Routes.ASSISTANT) { ChatScreen() }
                composable(Routes.LEARNING) { LearningScreen() }
                composable(Routes.SYMPTOM) { SymptomScreen { nav.popBackStack() } }
                composable(Routes.ALERT) {
                    PasaLaVozSheet(
                        onDismiss = { nav.popBackStack() },
                        onInvite = { nav.navigate(Routes.INVITE_PARTNER) { popUpTo(Routes.ALERT) { inclusive = true } } }
                    )
                }
                composable(Routes.PROFILE) { ProfileScreen(onInvite = { nav.navigate(Routes.INVITE_PARTNER) }, onTerms = { nav.navigate(Routes.TERMS) }, onSignedOut = { nav.navigate(Routes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } } }) }
                composable(Routes.INVITE_PARTNER) { InvitePartnerScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}
