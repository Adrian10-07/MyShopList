package com.example.myshoplist.features.register.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myshoplist.features.register.presentation.viewmodels.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC0E8DB)) // Fondo verde mint
    ) {
        // Botón atrás en la esquina superior izquierda
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = Color.DarkGray
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Contenedor blanco redondeado con el formulario
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White, shape = RoundedCornerShape(20.dp))
                        .padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Avatar circular con icono
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🥦", fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Título
                    Text(
                        text = "Únete",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    // Subtítulo
                    Text(
                        text = "¡Comienza a comprar inteligentemente!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 5.dp)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Mostrar error si existe
                    uiState.error?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth(),
                            fontSize = 12.sp
                        )
                    }

                    // Label Nombre
                    Text(
                        text = "NOMBRE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Campo de Nombre
                    TextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        placeholder = { Text("Ej. BroccoliCool") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Label Correo
                    Text(
                        text = "CORREO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Campo de Email
                    TextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        placeholder = { Text("hola@ejemplo.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Label Contraseña
                    Text(
                        text = "CONTRASEÑA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Campo de Contraseña
                    TextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        placeholder = { Text("Contraseña segura") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    // Botón de Registro
                    Button(
                        onClick = { viewModel.register() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853), // Verde
                            disabledContainerColor = Color(0xFF66BB6A)
                        )
                    ) {
                        Text("Crear Cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // Enlace de inicio de sesión
                    Text(
                        text = "¿Ya tienes cuenta? Inicia Sesión",
                        fontSize = 12.sp,
                        color = Color(0xFF00C853)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}