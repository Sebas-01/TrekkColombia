package com.trekking.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trekking.app.api.TrekkingRoute
import com.trekking.app.api.Usuario
import com.trekking.app.ui.theme.TrekkingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    currentUser: Usuario?,
    onProfileClick: () -> Unit,
    onUserListClick: () -> Unit,
    onRouteClick: (TrekkingRoute) -> Unit
) {
    val routes = remember {
        listOf(
            TrekkingRoute(1, "Camino del Inca", "https://picsum.photos/seed/1/400/600", "Una ruta milenaria que atraviesa los Andes hasta llegar a Machu Picchu.", 300, "Inca Trails Ltd.", "Alta", "4 días", "Juan Pérez"),
            TrekkingRoute(2, "Nevado del Cocuy", "https://picsum.photos/seed/2/400/400", "Nieve en el trópico colombiano. Siente la magia de los glaciares.", 200, "Andes Adventures", "Muy Alta", "2 días", "María García"),
            TrekkingRoute(3, "Ciudad Perdida", "https://picsum.photos/seed/3/400/700", "Tesoro arqueológico en la Sierra Nevada de Santa Marta.", 350, "Sierra Treks", "Media", "5 días", "Carlos Ruiz"),
            TrekkingRoute(4, "Páramo de Santurbán", "https://picsum.photos/seed/4/400/500", "Tierra de frailejones y nacimientos de agua cristalina.", 250, "Páramo Tours", "Media", "1 día", "Elena Blanco"),
            TrekkingRoute(5, "Desierto de la Tatacoa", "https://picsum.photos/seed/5/400/600", "Un laberinto de tierra roja bajo cielos estrellados.", 280, "Desert Guides", "Baja", "1 día", "Felipe Mora"),
            TrekkingRoute(6, "Valle del Cocora", "https://picsum.photos/seed/6/400/450", "Hogar de la palma de cera, árbol nacional de Colombia.", 220, "Quindío Nature", "Media", "6 horas", "Sofía Vargas")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Descubre Rutas", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onUserListClick) {
                        Icon(Icons.Default.Person, contentDescription = "Usuarios")
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3b5998))
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currentUser?.nombre?.take(1)?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF0F4F8)),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(routes) { route ->
                Box(modifier = Modifier.clickable { onRouteClick(route) }) {
                    FeedItem(route)
                }
            }
        }
    }
}

@Composable
fun FeedItem(route: TrekkingRoute) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = route.imageUrl,
                contentDescription = route.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(route.height.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = route.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A2b4c)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = route.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2
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
            onUserListClick = {},
            onRouteClick = {}
        )
    }
}
