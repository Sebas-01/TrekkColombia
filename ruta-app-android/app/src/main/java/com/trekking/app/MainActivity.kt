package com.trekking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.trekking.app.api.*
import com.trekking.app.ui.screens.*
import com.trekking.app.ui.theme.TrekkingAppTheme

enum class Screen { Login, Feed, UserList, ProfileUpdate, RouteDetail, UserCreation, Register, Favorites, RouteList }

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
            onUserListClick = {
                currentScreen = Screen.UserList
            },
            onRouteClick = { route ->
                selectedRoute = route
                currentScreen = Screen.RouteDetail
            }
        )
        Screen.Favorites -> FavoritesScreen(
            currentUser = currentUser,
            onBack = { currentScreen = Screen.Feed },
            onRouteClick = { route ->
                selectedRoute = route
                currentScreen = Screen.RouteDetail
            }
        )
        Screen.UserList -> UserListScreen(
            currentUser = currentUser,
            onEditClick = { user ->
                editingUser = user
                currentScreen = Screen.ProfileUpdate
            },
            onCreateClick = {
                currentScreen = Screen.UserCreation
            },
            onBack = {
                currentScreen = Screen.Feed
            },
            onLogout = {
                SessionManager.clearSession(context)
                currentUser = null
                currentScreen = Screen.Login
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
            }
        )
        Screen.RouteDetail -> RouteDetailScreen(
            route = selectedRoute,
            onBack = { 
                currentScreen = Screen.Feed
            }
        )
        Screen.RouteList -> RouteListScreen(
            onBack = { currentScreen = Screen.Feed },
            onRouteContentClick = { route ->
                selectedRoute = route
                currentScreen = Screen.RouteDetail
            }
        )
        Screen.UserCreation -> UserCreationScreen(
            onBack = { currentScreen = Screen.UserList },
            onSaveSuccess = {
                currentScreen = Screen.UserList
            }
        )
        Screen.Register -> RegisterScreen(
            onBack = { currentScreen = Screen.Login },
            onRegisterSuccess = {
                currentScreen = Screen.Login
            }
        )
    }
}
