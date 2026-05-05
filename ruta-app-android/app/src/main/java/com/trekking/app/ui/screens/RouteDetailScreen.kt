package com.trekking.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
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
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.awaitCancellation
import com.trekking.app.data.local.AppDatabase
import com.trekking.app.data.local.toTrekkingRoute
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RouteDetailScreen(
    route: TrekkingRoute?,
    onBack: () -> Unit,
    onCompanyClick: (Int) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var currentRoute by remember { mutableStateOf(route) }
    var isOfflineMode by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isTracking by remember { mutableStateOf(false) }
    var totalDistanceMeters by remember { mutableFloatStateOf(0f) }
    var previousTrackingLocation by remember { mutableStateOf<LatLng?>(null) }
    var isOffRoute by remember { mutableStateOf(false) }
    var showRecommendations by remember { mutableStateOf(false) }
    var showCompanyPopup by remember { mutableStateOf(false) }

    // Intentar recuperar datos locales si faltan datos críticos (ej: geojson)
    LaunchedEffect(route?.id) {
        if (route != null && route.geoJson == null) {
            val local = withContext(Dispatchers.IO) { db.rutaDao().getRutaById(route.id) }
            if (local != null) {
                currentRoute = local.toTrekkingRoute()
                isOfflineMode = true
            }
        }
    }

    val currentRouteData = currentRoute
    if (currentRouteData == null) {
        onBack()
        return
    }

    // Parsear el GeoJSON a una lista de LatLng para la polilínea
    val polylinePoints = remember(currentRouteData.geoJson) {
        Log.d("RouteDetail", "GeoJSON received: ${currentRouteData.geoJson}")
        val points = parseGeoJsonToLatLng(currentRouteData.geoJson)
        Log.d("RouteDetail", "Parsed points count: ${points.size}")
        points
    }
    
    // Gestión de permisos para mostrar la ubicación del usuario
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Cliente de ubicación
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentUserLocation by remember { mutableStateOf<LatLng?>(null) }

    // --- SEGUIMIENTO DE UBICACIÓN CON ALTA PRECISIÓN ---
    LaunchedEffect(isTracking, locationPermissionsState.allPermissionsGranted) {
        if (isTracking && locationPermissionsState.allPermissionsGranted) {
            // Reiniciar métricas al iniciar el recorrido
            totalDistanceMeters = 0f
            previousTrackingLocation = null
            isOffRoute = false

            // Configuración de máxima precisión (GPS)
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMaxUpdateDelayMillis(3000L)
                .setWaitForAccurateLocation(true)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val newLocation = LatLng(location.latitude, location.longitude)
                        currentUserLocation = newLocation

                        // --- Cálculo de distancia recorrida ---
                        previousTrackingLocation?.let { prev ->
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                prev.latitude, prev.longitude,
                                newLocation.latitude, newLocation.longitude,
                                results
                            )
                            // Filtrar ruido GPS: con alta precisión el umbral puede ser más fino (2m)
                            if (results[0] > 2f) {
                                totalDistanceMeters += results[0]
                                previousTrackingLocation = newLocation
                            }
                        } ?: run {
                            previousTrackingLocation = newLocation
                        }

                        // --- Detección de desviación de la ruta oficial ---
                        if (polylinePoints.isNotEmpty()) {
                            val minDistToRoute = polylinePoints.minOf { point ->
                                val results = FloatArray(1)
                                Location.distanceBetween(
                                    newLocation.latitude, newLocation.longitude,
                                    point.latitude, point.longitude,
                                    results
                                )
                                results[0]
                            }
                            isOffRoute = minDistToRoute > 150f // Umbral: 150 metros
                            Log.d("RouteDetail", "Precise Dist to route: ${minDistToRoute.toInt()}m | Off-route: $isOffRoute")
                        }
                    }
                }
            }

            try {
                @SuppressLint("MissingPermission")
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )
                // Mantener el efecto activo mientras isTracking sea true
                awaitCancellation()
            } catch (e: Exception) {
                Log.e("RouteDetail", "Error en seguimiento de ubicación", e)
            } finally {
                // Limpieza al detener el seguimiento o salir de la pantalla
                fusedLocationClient.removeLocationUpdates(locationCallback)
                previousTrackingLocation = null
                isOffRoute = false
            }
        } else {
            previousTrackingLocation = null
            isOffRoute = false
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionsState.launchMultiplePermissionRequest()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(currentRouteData.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (isOfflineMode) {
                            Text("Modo Offline - Datos guardados", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Hero Image
            AsyncImage(
                model = currentRouteData.imageUrl,
                contentDescription = currentRouteData.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(20.dp)) {
                // Header Info
                Text(
                    text = currentRouteData.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "por ${currentRouteData.companyName}",
                    fontSize = 16.sp,
                    color = Color(0xFF3b5998),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { 
                        currentRouteData.companyId?.let { onCompanyClick(it) } 
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Grid de Estadísticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.Info,
                        label = "Dificultad",
                        value = currentRouteData.difficulty,
                        modifier = Modifier.weight(1f),
                        color = when(currentRouteData.difficulty.lowercase(Locale.ROOT)) {
                            "baja" -> Color(0xFF4CAF50)
                            "media" -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                    )
                    StatCard(
                        icon = Icons.Default.DateRange,
                        label = "Duración",
                        value = currentRouteData.duration,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF3b5998)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de Acción (Recomendaciones y Empresa)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showRecommendations = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recomendaciones", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { showCompanyPopup = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3b5998)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Operadora", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Descripción
                Text(
                    text = "Sobre esta aventura",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentRouteData.description,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(32.dp))

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
                        color = MaterialTheme.colorScheme.onBackground
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
                
                val routeLocation = remember(currentRouteData.latitude, currentRouteData.longitude) {
                    val latLng = LatLng(currentRouteData.latitude, currentRouteData.longitude)
                    Log.d("RouteDetail", "Route coordinates: $latLng")
                    latLng
                }
                
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(routeLocation, 12f)
                }

                // Ajustar cámara cuando los datos estén listos o para seguimiento
                LaunchedEffect(currentRouteData.id, polylinePoints, currentRouteData.latitude, isTracking, currentUserLocation) {
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
                        } else if (currentRouteData.latitude != 0.0) {
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
                            title = currentRouteData.title,
                            snippet = currentRouteData.companyName
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

                    // Indicadores de recorrido activo
                    if (isTracking) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Banner principal: estado + distancia recorrida
                            Surface(
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
                                    Text(
                                        "RECORRIDO ACTIVO",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "·  ${formatDistance(totalDistanceMeters)}",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Alerta de desviación de ruta
                            if (isOffRoute && polylinePoints.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFE65100),
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color.Yellow,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "FUERA DE RUTA — Regresa al sendero",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
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
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Centrar Ruta", tint = Color(0xFF3b5998))
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // --- Ventana Emergente de Recomendaciones ---
    if (showRecommendations) {
        AlertDialog(
            onDismissRequest = { showRecommendations = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFFBC02D))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Recomendaciones", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = currentRouteData.recomendaciones ?: "No hay recomendaciones específicas para esta ruta por ahora.",
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF334155)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecommendations = false }) {
                    Text("Entendido", fontWeight = FontWeight.Bold, color = Color(0xFF3b5998))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    // --- Ventana Emergente de Información de la Empresa ---
    if (showCompanyPopup) {
        AlertDialog(
            onDismissRequest = { showCompanyPopup = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = Color(0xFF3b5998))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Sobre la Operadora", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = currentRouteData.companyLogo ?: "https://via.placeholder.com/150",
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentRouteData.companyName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2b4c),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentRouteData.companyDescription ?: "Esta empresa se dedica a brindar las mejores experiencias de trekking.",
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showCompanyPopup = false
                    currentRouteData.companyId?.let { onCompanyClick(it) } 
                }) {
                    Text("VER PERFIL COMPLETO", fontWeight = FontWeight.Bold, color = Color(0xFF3b5998))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompanyPopup = false }) {
                    Text("CERRAR", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

/**
 * Formatea metros a una cadena amigable (ej: "850 m" o "1.23 km").
 */
fun formatDistance(meters: Float): String {
    return if (meters < 1000f) {
        "${meters.toInt()} m"
    } else {
        String.format(Locale.getDefault(), "%.2f km", meters / 1000f)
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
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2b4c))
        }
    }
}
