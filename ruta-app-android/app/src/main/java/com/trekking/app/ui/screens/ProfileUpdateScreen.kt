package com.trekking.app.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Text("Editar Perfil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = telefono,
                onValueChange = { telefono = it },
                placeholder = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Nueva Contraseña (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.White.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val request = UpdateRequest(nombre, email, telefono, if (password.isEmpty()) null else password)
                            val response = RetrofitClient.instance.updateUsuario(user.idUsuario, request)
                            if (response.isSuccessful) {
                                onUpdateSuccess(
                                    Usuario(
                                        user.idUsuario,
                                        nombre,
                                        telefono,
                                        email,
                                        user.foto,
                                        user.rol,
                                        user.fechaCreacion
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            // handle error
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF192f6a))
            ) {
                Text("ACTUALIZAR", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBack) {
                Text("Cancelar", color = Color.White)
            }
        }
    }
}
