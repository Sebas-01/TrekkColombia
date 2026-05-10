package com.trekking.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trekking.app.api.ForgotPasswordRequest
import com.trekking.app.api.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF192f6a), Color(0xFF3b5998), Color(0xFF192f6a))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Recuperar Contraseña",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Ingresa tu correo electrónico y te enviaremos las instrucciones para restablecer tu contraseña.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Correo Electrónico",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("ejemplo@correo.com", color = Color.Gray.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF192f6a)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            successMessage = null
                            try {
                                val response = RetrofitClient.instance.recuperarPassword(ForgotPasswordRequest(email))
                                if (response.isSuccessful) {
                                    successMessage = response.body()?.message ?: "Solicitud enviada correctamente"
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    errorMessage = try {
                                        JSONObject(errorBody ?: "").optString("error", "Error al procesar la solicitud")
                                    } catch (e: Exception) {
                                        "Error: ${response.code()}"
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de conexión"
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Por favor ingresa tu correo electrónico"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF192f6a)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(size = 24.dp, color = Color(0xFF192f6a))
                } else {
                    Text("ENVIAR INSTRUCCIONES", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VOLVER AL LOGIN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Diálogos de Alerta ---
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Error") },
                text = { Text(errorMessage!!) },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("Aceptar")
                    }
                }
            )
        }

        if (successMessage != null) {
            AlertDialog(
                onDismissRequest = { 
                    successMessage = null
                    onBack()
                },
                title = { Text("¡Éxito!") },
                text = { Text(successMessage!!) },
                confirmButton = {
                    TextButton(onClick = { 
                        successMessage = null
                        onBack()
                    }) {
                        Text("Ir al Login")
                    }
                }
            )
        }
    }
}
