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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
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
import com.trekking.app.data.local.AppDatabase
import com.trekking.app.data.local.RutaEntity
import com.trekking.app.data.local.toTrekkingRoute
import com.trekking.app.ui.theme.TrekkingAppTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    currentUser: Usuario?,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onFavoritesClick: () -> Unit,
    onRouteClick: (TrekkingRoute) -> Unit,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var routes by remember { mutableStateOf<List<TrekkingRoute>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    LaunchedEffect(currentUser) {
        try {
            val response = RetrofitClient.instance.getRutas(currentUser?.idUsuario)
            if (response.isSuccessful) {
                val remoteRoutes = response.body() ?: emptyList()
                routes = remoteRoutes
                
                // Guardar en caché local
                withContext(Dispatchers.IO) {
                    val entities = remoteRoutes.map { 
                        RutaEntity(
                            id = it.id,
                            title = it.title,
                            imageUrl = it.imageUrl,
                            description = it.description,
                            height = it.height,
                            companyId = it.companyId,
                            companyName = it.companyName,
                            companyIdentification = it.companyIdentification,
                            difficulty = it.difficulty,
                            duration = it.duration,
                            guideName = it.guideName,
                            latitude = it.latitude,
                            longitude = it.longitude,
                            geoJson = it.geoJson,
                            isFavorite = it.isFavorite,
                            recomendaciones = it.recomendaciones,
                            companyLogo = it.companyLogo,
                            companyDescription = it.companyDescription
                        )
                    }
                    db.rutaDao().deleteAllRutas()
                    db.rutaDao().insertRutas(entities)
                }
            } else {
                // Si la respuesta no es exitosa, intentar cargar de la DB local
                val localRutas = withContext(Dispatchers.IO) { db.rutaDao().getAllRutas() }
                if (localRutas.isNotEmpty()) {
                    routes = localRutas.map { it.toTrekkingRoute() }
                }
            }
        } catch (e: Exception) {
            Log.e("FeedScreen", "Error fetching routes, loading from local DB", e)
            // Cargar de la DB local en caso de error de red
            val localRutas = withContext(Dispatchers.IO) { db.rutaDao().getAllRutas() }
            if (localRutas.isNotEmpty()) {
                routes = localRutas.map { it.toTrekkingRoute() }
            }
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
        containerColor = MaterialTheme.colorScheme.background, 
        topBar = {
            Surface(
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface
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
                                focusedContainerColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE9E9E9),
                                unfocusedContainerColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE9E9E9),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true
                        )

                        // Iconos de acción
                        IconButton(onClick = onFavoritesClick) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favoritos", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        
                        IconButton(onClick = onToggleDarkMode) {
                            Icon(
                                if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Cambiar Tema",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        

                        
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión", tint = MaterialTheme.colorScheme.onSurface)
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (route.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (route.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = route.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = route.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
