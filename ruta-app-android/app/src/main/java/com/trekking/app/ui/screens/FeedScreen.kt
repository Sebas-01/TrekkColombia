package com.trekking.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trekking.app.api.FavoriteRequest
import com.trekking.app.api.RetrofitClient
import com.trekking.app.api.TrekkingRoute
import com.trekking.app.api.Usuario
import com.trekking.app.ui.theme.TrekkingAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    currentUser: Usuario?,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onFavoritesClick: () -> Unit,
    onUserListClick: () -> Unit,
    onRouteClick: (TrekkingRoute) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var routes by remember { mutableStateOf<List<TrekkingRoute>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        try {
            val response = RetrofitClient.instance.getRutas(currentUser?.idUsuario)
            if (response.isSuccessful) {
                routes = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("FeedScreen", "Error fetching routes", e)
        } finally {
            isLoading = false
        }
    }

    val toggleFavorite: (TrekkingRoute) -> Unit = { route ->
        scope.launch {
            if (currentUser != null) {
                try {
                    if (route.isFavorite) {
                        RetrofitClient.instance.removeFavorito(currentUser.idUsuario, route.id)
                    } else {
                        RetrofitClient.instance.addFavorito(FavoriteRequest(currentUser.idUsuario, route.id))
                    }
                    // Actualizar estado local
                    routes = routes.map {
                        if (it.id == route.id) it.copy(isFavorite = !it.isFavorite) else it
                    }
                } catch (e: Exception) {
                    Log.e("FeedScreen", "Error toggling favorite", e)
                }
            }
        }
    }

    val filteredRoutes = remember(searchQuery, routes) {
        if (searchQuery.isBlank()) routes
        else routes.filter { it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color(0xFFF9FBFC), // Fondo Pinterest-like
        topBar = {
            Surface(
                shadowElevation = 0.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Perfil / Logo
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE9E9E9))
                                .clickable { onProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                currentUser?.nombre?.take(1)?.uppercase() ?: "U",
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                        
                        // Barra de búsqueda centralizada
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                                .heightIn(max = 52.dp),
                            placeholder = { Text("Buscar rutas...", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            shape = RoundedCornerShape(26.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFE9E9E9),
                                unfocusedContainerColor = Color(0xFFE9E9E9),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )

                        // Iconos de acción
                        IconButton(onClick = onFavoritesClick) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favoritos", tint = Color.Black)
                        }
                        
                        if (currentUser?.isSuperAdmin == true) {
                            IconButton(onClick = onUserListClick) {
                                Icon(Icons.Default.Person, contentDescription = "Usuarios", tint = Color.Black)
                            }
                        }
                        
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(filteredRoutes) { route ->
                    FeedItem(
                        route = route, 
                        onFavoriteClick = { toggleFavorite(route) }, 
                        onClick = { onRouteClick(route) }
                    )
                }
            }
        }
    }
}

@Composable
fun FeedItem(route: TrekkingRoute, onFavoriteClick: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = route.imageUrl,
                    contentDescription = route.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(route.height.dp) // Altura variable (Pinterest style)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Botón Favorito Flotante
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (route.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (route.isFavorite) Color.Red else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = route.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = route.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = route.companyName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF192f6a)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    TrekkingAppTheme {
        val mockUser = Usuario(
            idUsuario = 1,
            nombre = "Sebastian",
            telefono = "123456",
            correo = "sebas@gmail.com",
            foto = null,
            rol = "usuario",
            fechaCreacion = null
        )
        FeedScreen(
            currentUser = mockUser,
            onProfileClick = {},
            onLogout = {},
            onFavoritesClick = {},
            onUserListClick = {},
            onRouteClick = {}
        )
    }
}
