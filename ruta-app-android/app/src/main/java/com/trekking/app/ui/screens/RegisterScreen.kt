package com.trekking.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trekking.app.api.RegisterRequest
import com.trekking.app.api.RetrofitClient
import com.trekking.app.ui.theme.TrekkingAppTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
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
                .background(color = Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
            }

            // Logo circular consistente con Login
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(color = Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text("T", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFF192f6a))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Únete a la Aventura",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Crea tu cuenta de explorador",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Contenedor de Registro con efecto Blur sin afectar el texto
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
                        value = correo,
                        onValueChange = { correo = it },
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
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
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
                            if (nombre.isNotBlank() && correo.isNotBlank() && password.isNotBlank()) {
                                val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
                                if (!emailPattern.matches(correo)) {
                                    errorMessage = "Correo electrónico inválido"
                                    return@Button
                                }

                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val request = RegisterRequest(
                                            nombre = nombre,
                                            correo = correo,
                                            telefono = telefono,
                                            password = password,
                                            rol = "usuario"
                                        )
                                        
                                        val response = RetrofitClient.instance.registerUsuario(request)
                                        if (response.isSuccessful) {
                                            onRegisterSuccess()
                                        } else {
                                            val body = response.errorBody()?.string()
                                            errorMessage = try {
                                                JSONObject(body ?: "").optString("error", "Error al registrar")
                                            } catch (e: Exception) {
                                                "Error al registrar (${response.code()})"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("RegisterScreen", "Error", e)
                                        errorMessage = "Error de conexión"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                errorMessage = "Completa los campos obligatorios"
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
                            Text("REGISTRARSE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(it, color = Color.Red, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    TrekkingAppTheme {
        RegisterScreen(onBack = {}, onRegisterSuccess = {})
    }
}
