package com.trekking.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import com.trekking.app.api.LoginRequest
import com.trekking.app.api.RetrofitClient
import com.trekking.app.api.Usuario
import com.trekking.app.ui.theme.TrekkingAppTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (Usuario) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
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
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Icono Superior
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text("T", fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color(0xFF192f6a))
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Trekking App",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                "Explora. Descubre. Vive.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

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
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
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
                                try {
                                    val response = RetrofitClient.instance.login(LoginRequest(email, password))
                                    if (response.isSuccessful) {
                                        val loginRes = response.body()
                                        if (loginRes != null) {
                                            onLoginSuccess(
                                                Usuario(
                                                    idUsuario = loginRes.idUsuario,
                                                    nombre = loginRes.nombre,
                                                    telefono = loginRes.telefono,
                                                    correo = loginRes.correo ?: email,
                                                    foto = loginRes.foto,
                                                    rol = loginRes.rol ?: "usuario",
                                                    fechaCreacion = loginRes.fechaCreacion
                                                )
                                            )
                                        } else {
                                            errorMessage = "Respuesta inválida"
                                        }
                                    } else {
                                        errorMessage = "Credenciales incorrectas"
                                    }
                                } catch (e: Exception) {
                                    Log.e("Login", "Error", e)
                                    errorMessage = "Error de conexión"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF192f6a))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("ENTRAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(it, color = Color.Red, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onRegisterClick) {
                Text(
                    "¿Nuevo aquí? Crea una cuenta",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TrekkingAppTheme {
        LoginScreen(onLoginSuccess = {}, onRegisterClick = {})
    }
}
