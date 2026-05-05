package com.trekking.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trekking.app.api.RetrofitClient
import com.trekking.app.api.TrekkingRoute
import com.trekking.app.api.Usuario
import com.trekking.app.ui.theme.TrekkingAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    currentUser: Usuario?,
    onBack: () -> Unit,
    onRouteClick: (TrekkingRoute) -> Unit
) {
    var favorites by remember { mutableStateOf<List<TrekkingRoute>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            try {
                val response = RetrofitClient.instance.getFavoritos(currentUser.idUsuario)
                if (response.isSuccessful) {
                    favorites = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("FavoritesScreen", "Error fetching favorites", e)
            } finally {
                isLoading = false
            }
        }
    }

    val removeFavorite: (TrekkingRoute) -> Unit = { route ->
        scope.launch {
            if (currentUser != null) {
                try {
                    val response = RetrofitClient.instance.removeFavorito(currentUser.idUsuario, route.id)
                    if (response.isSuccessful) {
                        favorites = favorites.filter { it.id != route.id }
                    }
                } catch (e: Exception) {
                    Log.e("FavoritesScreen", "Error removing favorite", e)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mis Favoritos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aún no tienes rutas favoritas", color = Color.Gray)
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
                items(favorites) { route ->
                    FeedItem(
                        route = route.copy(isFavorite = true), // Forzamos que se vea como favorito en esta pantalla
                        onFavoriteClick = { removeFavorite(route) },
                        onClick = { onRouteClick(route) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    TrekkingAppTheme {
        FavoritesScreen(currentUser = null, onBack = {}, onRouteClick = {})
    }
}
