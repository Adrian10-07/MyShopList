package com.example.myshoplist.features.login.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myshoplist.features.login.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("¡Inicio de sesión exitoso!")
                onLoginSuccess()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5DEB3)) // Fondo beige
                .padding(padding)
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
                // Avatar circular (placeholder)
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333333)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Aquí iría tu imagen de avatar
                    Text("👤", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Título
                Text(
                    text = "MyShopList",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                // Subtítulo
                Text(
                    text = "¡Bienvenido de nuevo!",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Label Usuario
                Text(
                    text = "Correo Electronico",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 5.dp)
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Campo de Usuario/Email
                TextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    placeholder = { Text("juan@example.com") },
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
                    text = "Contraseña",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 5.dp)
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Campo de Correo
                TextField(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    placeholder = { Text("password123") },
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

                uiState.error?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 10.dp),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                Button(
                    onClick = { viewModel.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8C00),
                        disabledContainerColor = Color(0xFFFFB347)
                    ),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        Text("Cargando...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Text("Ingresar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Enlace de registro
                Text(
                    text = "¿No tienes cuenta? Regístrate",
                    fontSize = 12.sp,
                    color = Color(0xFFFF8C00)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}