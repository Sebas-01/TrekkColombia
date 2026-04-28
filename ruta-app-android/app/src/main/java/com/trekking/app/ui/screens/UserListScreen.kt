package com.trekking.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trekking.app.api.RetrofitClient
import com.trekking.app.api.Usuario
import com.trekking.app.ui.theme.TrekkingAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    currentUser: Usuario?,
    onEditClick: (Usuario) -> Unit,
    onCreateClick: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var users by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val canManage = true // Everyone can manage if there is only one user type

    fun refreshUsers() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getUsuarios()
                if (response.isSuccessful) {
                    users = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("UserList", "Error fetching users", e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshUsers()
    }

    val filteredUsers = users.filter { 
        it.nombre.contains(searchQuery, ignoreCase = true) || 
        it.correo.contains(searchQuery, ignoreCase = true) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Exploradores", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                }
            )
        },
        floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateClick,
                    containerColor = Color(0xFF3b5998),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Usuario")
                }
            }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8FAFC))) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por nombre o correo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            if (isLoading && users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            isAdmin = canManage,
                            onEditClick = { onEditClick(user) },
                            onDeleteClick = {
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.instance.deleteUsuario(user.idUsuario)
                                        if (response.isSuccessful) {
                                            refreshUsers()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("UserList", "Error deleting user", e)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: Usuario, isAdmin: Boolean, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF3b5998)),
                contentAlignment = Alignment.Center
            ) {
                Text(user.nombre.take(1).uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Text(user.correo, fontSize = 14.sp, color = Color(0xFF64748B))
                

            }
            if (isAdmin) {
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF3b5998))
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }
                }
            } else {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Ver", tint = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserListScreenPreview() {
    TrekkingAppTheme {
        val mockUser = Usuario(
            idUsuario = 1,
            nombre = "Sebastian Admin",
            telefono = "123456",
            correo = "admin@trekking.com",
            foto = null,
            fechaCreacion = "2024-01-01"
        )
        UserListScreen(
            currentUser = mockUser,
            onEditClick = {},
            onCreateClick = {},
            onBack = {},
            onLogout = {}
        )
    }
}
