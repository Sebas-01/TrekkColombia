package com.trekking.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trekking.app.api.RetrofitClient
import com.trekking.app.api.UpdateRequest
import com.trekking.app.api.Usuario
import kotlinx.coroutines.launch

@Composable
fun ProfileUpdateScreen(
    user: Usuario?,
    onBack: () -> Unit,
    onUpdateSuccess: (Usuario) -> Unit
) {
    if (user == null) {
        onBack()
        return
    }

    var nombre by remember { mutableStateOf(user.nombre) }
    var telefono by remember { mutableStateOf(user.telefono ?: "") }
    var email by remember { mutableStateOf(user.correo) }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con Imagen de Naturaleza (Nítido)
        AsyncImage(
            model = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&q=80&w=1000",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Overlay oscuro para legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Inicial del usuario como "logo" personalizado
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.nombre.take(1).uppercase(), 
                    fontSize = 36.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Color(0xFF192f6a)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mi Perfil",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Mantén tus datos actualizados",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Contenedor con efecto Blur sin afectar el texto
            Box(modifier = Modifier.fillMaxWidth()) {
                // Capa de fondo con Blur (el "cristal")
                Surface(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(25.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    shadowElevation = 8.dp
                ) {}

                // Capa de contenido (Nítida)
                Column(modifier = Modifier.padding(28.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF192f6a),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF192f6a),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF192f6a),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Nueva Contraseña (opcional)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF192f6a),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                try {
                                    val request = UpdateRequest(
                                        nombre = nombre,
                                        correo = email,
                                        telefono = telefono.ifEmpty { null },
                                        password = if (password.isEmpty()) null else password,
                                        foto = user.foto
                                    )
                                    val response = RetrofitClient.instance.updateUsuario(user.idUsuario, request)
                                    if (response.isSuccessful) {
                                        successMessage = "Datos actualizados correctamente"
                                        onUpdateSuccess(
                                            Usuario(
                                                user.idUsuario,
                                                nombre,
                                                telefono.ifEmpty { null },
                                                email,
                                                user.foto,
                                                user.rol,
                                                user.fechaCreacion
                                            )
                                        )
                                    } else {
                                        errorMessage = "Error al actualizar: código ${response.code()}"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error de conexión: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF192f6a)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("ACTUALIZAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    successMessage?.let {
                        Text(
                            text = it,
                            color = Color(0xFF388E3C),
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("CANCELAR", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
