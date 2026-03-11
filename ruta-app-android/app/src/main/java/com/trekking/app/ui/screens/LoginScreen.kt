package com.trekking.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trekking.app.api.LoginRequest
import com.trekking.app.api.RetrofitClient
import com.trekking.app.api.Usuario
import com.trekking.app.ui.theme.TrekkingAppTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (Usuario) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4c669f), Color(0xFF3b5998), Color(0xFF192f6a))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Trekking App", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Bienvenido de nuevo", color = Color.White.copy(alpha = 0.85f))
            Spacer(modifier = Modifier.height(40.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo Electrónico", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
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
                                val loginRes = response.body()!!
                                
                                // LOG DE DEPURACIÓN PARA VER EL ROL QUE LLEGA DEL SERVIDOR
                                Log.d("LOGIN_DEBUG", "Rol recibido: ${loginRes.rol}")
                                
                                onLoginSuccess(
                                    Usuario(
                                        idUsuario = loginRes.idUsuario,
                                        nombre = loginRes.nombre,
                                        telefono = null,
                                        correo = email,
                                        foto = null,
                                        rol = loginRes.rol ?: "usuario", // Fallback a usuario
                                        fechaCreacion = loginRes.fechaCreacion
                                    )
                                )
                            } else {
                                errorMessage = "Error en el login"
                            }
                        } catch (e: Exception) {
                            Log.e("LOGIN_DEBUG", "Error de conexión", e)
                            errorMessage = "Error de conexión"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF192f6a))
            ) {
                if (isLoading) CircularProgressIndicator(color = Color(0xFF192f6a), modifier = Modifier.size(24.dp))
                else Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold)
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = Color.Red)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TrekkingAppTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
