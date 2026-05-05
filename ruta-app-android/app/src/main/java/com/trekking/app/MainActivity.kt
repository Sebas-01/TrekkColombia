package com.trekking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.trekking.app.api.*
import com.trekking.app.ui.screens.*
import com.trekking.app.ui.theme.TrekkingAppTheme

enum class Screen { Login, Feed, ProfileUpdate, RouteDetail, Register, Favorites, RouteList, CompanyDetail }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrekkingAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Intentar recuperar sesión al iniciar
    val savedUser = remember { SessionManager.getUser(context) }
    
    var currentScreen by remember { 
        mutableStateOf(if (savedUser != null) Screen.Feed else Screen.Login) 
    }
    var currentUser by remember { mutableStateOf<Usuario?>(savedUser) }
    var editingUser by remember { mutableStateOf<Usuario?>(null) }
    var selectedRoute by remember { mutableStateOf<TrekkingRoute?>(null) }
    var selectedCompanyId by remember { mutableStateOf<Int?>(null) }

    // Estado del modo oscuro
    val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
    var isDarkMode by remember { 
        mutableStateOf(SessionManager.isDarkMode(context) ?: systemInDark) 
    }

    val toggleDarkMode: () -> Unit = {
        isDarkMode = !isDarkMode
        SessionManager.setDarkMode(context, isDarkMode)
    }

    TrekkingAppTheme(darkTheme = isDarkMode) {
        when (currentScreen) {
            Screen.Login -> LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    currentScreen = Screen.Feed
                },
                onRegisterClick = { currentScreen = Screen.Register }
            )

            Screen.Feed -> FeedScreen(
                currentUser = currentUser,
                onProfileClick = {
                    editingUser = currentUser
                    currentScreen = Screen.ProfileUpdate
                },
                onLogout = {
                    SessionManager.clearSession(context)
                    currentUser = null
                    currentScreen = Screen.Login
                },
                onFavoritesClick = {
                    currentScreen = Screen.Favorites
                },
                onRouteClick = { route ->
                    selectedRoute = route
                    currentScreen = Screen.RouteDetail
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = toggleDarkMode
            )
            Screen.Favorites -> FavoritesScreen(
                currentUser = currentUser,
                onBack = { currentScreen = Screen.Feed },
                onRouteClick = { route ->
                    selectedRoute = route
                    currentScreen = Screen.RouteDetail
                }
            )

            Screen.ProfileUpdate -> ProfileUpdateScreen(
                user = editingUser,
                onBack = { 
                    currentScreen = Screen.Feed
                },
                onUpdateSuccess = { updatedUser ->
                    if (currentUser?.idUsuario == updatedUser.idUsuario) {
                        currentUser = updatedUser
                    }
                    currentScreen = Screen.Feed
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = toggleDarkMode
            )
            Screen.RouteDetail -> RouteDetailScreen(
                route = selectedRoute,
                onBack = { 
                    currentScreen = Screen.Feed
                },
                onCompanyClick = { id ->
                    selectedCompanyId = id
                    currentScreen = Screen.CompanyDetail
                }
            )
            Screen.RouteList -> RouteListScreen(
                onBack = { currentScreen = Screen.Feed },
                onRouteContentClick = { route ->
                    selectedRoute = route
                    currentScreen = Screen.RouteDetail
                }
            )

            Screen.Register -> RegisterScreen(
                onBack = { currentScreen = Screen.Login },
                onRegisterSuccess = {
                    currentScreen = Screen.Login
                }
            )

            Screen.CompanyDetail -> CompanyDetailScreen(
                companyId = selectedCompanyId ?: 0,
                onBack = { currentScreen = Screen.RouteDetail }
            )
        }
    }
}
