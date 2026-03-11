package com.trekking.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trekking.app.api.TrekkingRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    route: TrekkingRoute?,
    onBack: () -> Unit
) {
    if (route == null) {
        onBack()
        return
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(route.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color(0xFFF0F4F8))
        ) {
            // Hero Image
            AsyncImage(
                model = route.imageUrl,
                contentDescription = route.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(20.dp)) {
                // Header Info
                Text(
                    text = route.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A2b4c)
                )
                Text(
                    text = "por ${route.companyName}",
                    fontSize = 16.sp,
                    color = Color(0xFF3b5998),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Grid (Difficulty, Duration, Guide)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.Info,
                        label = "Dificultad",
                        value = route.difficulty,
                        modifier = Modifier.weight(1f),
                        color = when(route.difficulty.lowercase()) {
                            "baja" -> Color(0xFF4CAF50)
                            "media" -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                    )
                    StatCard(
                        icon = Icons.Default.DateRange,
                        label = "Duración",
                        value = route.duration,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF3b5998)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.AccountCircle,
                        label = "Guía",
                        value = route.guideName,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF673AB7)
                    )
                    Spacer(modifier = Modifier.weight(1f)) // Placeholder for symmetry
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Description
                Text(
                    text = "Sobre esta aventura",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A2b4c)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = route.description,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Interactive Map Placeholder (Graphical for now)
                Text(
                    text = "Ruta en el Mapa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A2b4c)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Map Placeholder",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF94A3B8)
                        )
                        Text(
                            "Mapa Interactivo (Próximamente)",
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Stylized "path" lines for graphics
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Drawing some decorative lines to look like a map
                        // (simplified representation)
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2b4c))
        }
    }
}
