package com.trekking.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.maps.android.compose.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.json.JSONObject
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
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
    val scope = rememberCoroutineScope()
    var isTracking by remember { mutableStateOf(false) }
    
    // Gestión de permisos para mostrar la ubicación del usuario
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Cliente de ubicación
    val context = androidx.compose.ui.platform.LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentUserLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(isTracking) {
        if (isTracking && locationPermissionsState.allPermissionsGranted) {
            while (isTracking) {
                try {
                    @SuppressLint("MissingPermission")
                    val locationResult = fusedLocationClient.lastLocation
                    locationResult.addOnSuccessListener { location ->
                        if (location != null) {
                            currentUserLocation = LatLng(location.latitude, location.longitude)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RouteDetail", "Error getting location", e)
                }
                delay(2000) // Actualizar cada 2 segundos
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionsState.launchMultiplePermissionRequest()
    }

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
                .background(Color(0xFFF9FBFC))
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

                // Stats Grid
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

                // Mapa Interactivo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ruta en el Mapa",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2b4c)
                    )
                    if (locationPermissionsState.allPermissionsGranted) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                "Ubicación activa",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val routeLocation = remember(route.latitude, route.longitude) {
                    val latLng = LatLng(route.latitude, route.longitude)
                    Log.d("RouteDetail", "Route coordinates: $latLng")
                    latLng
                }
                
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(routeLocation, 12f)
                }

                // Parsear el GeoJSON a una lista de LatLng para la polilínea
                val polylinePoints = remember(route.geoJson) {
                    Log.d("RouteDetail", "GeoJSON received: ${route.geoJson}")
                    val points = parseGeoJsonToLatLng(route.geoJson)
                    Log.d("RouteDetail", "Parsed points count: ${points.size}")
                    points
                }

                // Ajustar cámara cuando los datos estén listos o para seguimiento
                LaunchedEffect(route.id, polylinePoints, route.latitude, isTracking, currentUserLocation) {
                    if (isTracking && currentUserLocation != null) {
                        Log.d("RouteDetail", "Following user: $currentUserLocation")
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(currentUserLocation!!, 17f)
                        )
                    } else if (!isTracking) {
                        if (polylinePoints.isNotEmpty()) {
                            Log.d("RouteDetail", "Centering on polyline start")
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(polylinePoints[0], 15f)
                            )
                        } else if (route.latitude != 0.0) {
                            Log.d("RouteDetail", "Centering on marker location")
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(routeLocation, 15f)
                            )
                        }
                    }
                }

                // Configuración del Mapa
                val mapProperties = remember(locationPermissionsState.allPermissionsGranted) {
                    MapProperties(
                        isMyLocationEnabled = locationPermissionsState.allPermissionsGranted,
                        mapType = MapType.TERRAIN
                    )
                }
                
                val uiSettings = remember {
                    MapUiSettings(
                        myLocationButtonEnabled = true,
                        zoomControlsEnabled = false,
                        compassEnabled = true
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties,
                        uiSettings = uiSettings
                    ) {
                        Marker(
                            state = rememberMarkerState(position = routeLocation),
                            title = route.title,
                            snippet = route.companyName
                        )

                        if (polylinePoints.isNotEmpty()) {
                            Polyline(
                                points = polylinePoints,
                                color = Color(0xFF3b5998),
                                width = 12f,
                                jointType = JointType.ROUND,
                                startCap = RoundCap(),
                                endCap = RoundCap()
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            if (locationPermissionsState.allPermissionsGranted) {
                                isTracking = !isTracking
                            } else {
                                locationPermissionsState.launchMultiplePermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .size(56.dp)
                            .background(if (isTracking) Color.Red else Color(0xFF3b5998), CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = if (isTracking) Icons.Default.Close else Icons.Default.PlayArrow, 
                            contentDescription = if (isTracking) "Detener Recorrido" else "Iniciar Recorrido",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Etiqueta de estado de seguimiento
                    if (isTracking) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Red,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("RECORRIDO ACTIVO", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            if (polylinePoints.isNotEmpty()) {
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(polylinePoints[0], 15f))
                                }
                            } else {
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(routeLocation, 14f))
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(Color.White, CircleShape)
                            .size(40.dp)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Centrar Ruta", tint = Color(0xFF3b5998))
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

/**
 * Parsea un string GeoJSON simple (LineString) a una lista de puntos LatLng.
 */
fun parseGeoJsonToLatLng(geoJsonStr: String?): List<LatLng> {
    if (geoJsonStr.isNullOrBlank()) return emptyList()
    
    return try {
        val jsonObject = JSONObject(geoJsonStr)
        val type = jsonObject.optString("type")
        val coordinates = jsonObject.optJSONArray("coordinates")
        
        if (type == "LineString" && coordinates != null) {
            val points = mutableListOf<LatLng>()
            for (i in 0 until coordinates.length()) {
                val point = coordinates.getJSONArray(i)
                val lon = point.getDouble(0)
                val lat = point.getDouble(1)
                points.add(LatLng(lat, lon))
            }
            points
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("RouteDetail", "Error parsing GeoJSON", e)
        emptyList()
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
