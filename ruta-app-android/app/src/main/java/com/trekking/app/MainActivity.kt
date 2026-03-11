package com.trekking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.trekking.app.api.*
import com.trekking.app.ui.screens.*
import com.trekking.app.ui.theme.TrekkingAppTheme

enum class Screen { Login, Feed, UserList, ProfileUpdate, RouteDetail, UserCreation, AdminDashboard }

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
    var currentScreen by remember { mutableStateOf(Screen.Login) }
    var currentUser by remember { mutableStateOf<Usuario?>(null) }
    var editingUser by remember { mutableStateOf<Usuario?>(null) }
    var selectedRoute by remember { mutableStateOf<TrekkingRoute?>(null) }

    when (currentScreen) {
        Screen.Login -> LoginScreen(onLoginSuccess = { user ->
            currentUser = user
            // Redirección basada en el rol
            if (user.isSuperAdmin) {
                currentScreen = Screen.AdminDashboard
            } else {
                currentScreen = Screen.Feed
            }
        })
        Screen.AdminDashboard -> AdminDashboardScreen(
            currentUser = currentUser,
            onManageUsersClick = { currentScreen = Screen.UserList },
            onCreateUserClick = { currentScreen = Screen.UserCreation },
            onLogout = {
                currentUser = null
                currentScreen = Screen.Login
            }
        )
        Screen.Feed -> FeedScreen(
            currentUser = currentUser,
            onProfileClick = {
                editingUser = currentUser
                currentScreen = Screen.ProfileUpdate
            },
            onUserListClick = {
                currentScreen = Screen.UserList
            },
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
                if (currentUser?.isSuperAdmin == true) {
                    currentScreen = Screen.AdminDashboard
                } else {
                    currentScreen = Screen.Feed
                }
            },
            onLogout = {
                currentUser = null
                currentScreen = Screen.Login
            }
        )
        Screen.ProfileUpdate -> ProfileUpdateScreen(
            user = editingUser,
            onBack = { 
                if (currentUser?.isSuperAdmin == true) {
                    currentScreen = Screen.UserList 
                } else {
                    currentScreen = Screen.Feed
                }
            },
            onUpdateSuccess = { updatedUser ->
                if (currentUser?.idUsuario == updatedUser.idUsuario) {
                    currentUser = updatedUser
                }
                if (currentUser?.isSuperAdmin == true) {
                    currentScreen = Screen.UserList 
                } else {
                    currentScreen = Screen.Feed
                }
            }
        )
        Screen.RouteDetail -> RouteDetailScreen(
            route = selectedRoute,
            onBack = { currentScreen = Screen.Feed }
        )
        Screen.UserCreation -> UserCreationScreen(
            onBack = { currentScreen = Screen.UserList },
            onSaveSuccess = {
                currentScreen = Screen.UserList
            }
        )
    }
}
