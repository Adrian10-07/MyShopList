package com.example.myshoplist.features.shopping_list.presentation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myshoplist.features.add_product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.ShoppingListViewModel
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(),
    userName: String = "PapaFeliz",
    onNavigateToAddProduct: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPurchases: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Log de cambios de estado
    LaunchedEffect(uiState) {
        val logData = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("label", "🔄 SCREEN - Estado cambiado")
            put("data", JSONObject().apply {
                put("newState", uiState.javaClass.simpleName)
            })
        }
        Log.d("ShoppingListScreen", logData.toString(2))
    }

    Scaffold(
        containerColor = Color(0xFFFFF5E6),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = 0,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToPurchases = onNavigateToPurchases
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            HeaderSection(userName = userName)

            Spacer(modifier = Modifier.height(8.dp))

            // Total estimado
            when (uiState) {
                is ShoppingListUiState.Success -> {
                    val items = (uiState as ShoppingListUiState.Success).items
                    val total = items.sumOf { it.estimatedPrice }
                    TotalEstimatedCard(total = total)
                }
                else -> {
                    TotalEstimatedCard(total = 0.0)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón "Agregar Cositas" (solo vista, no funcional)
            Button(
                onClick = { /* No hace nada */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7043),
                    disabledContainerColor = Color(0xFFFF7043)
                ),
                shape = RoundedCornerShape(28.dp),
                enabled = false // Deshabilitado como solicitaste
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Agregar Cositas",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de productos
            when (uiState) {
                is ShoppingListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF7043))
                    }
                }

                is ShoppingListUiState.Success -> {
                    val items = (uiState as ShoppingListUiState.Success).items
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items) { item ->
                            ProductItemCard(item = item)
                        }
                    }
                }

                is ShoppingListUiState.Error -> {
                    ErrorView(
                        message = (uiState as ShoppingListUiState.Error).message,
                        onRetry = { viewModel.refresh() }
                    )
                }

                is ShoppingListUiState.Empty -> {
                    EmptyView()
                }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hola,",
                fontSize = 24.sp,
                color = Color(0xFF2D3748)
            )
            Text(
                text = userName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF7043)
            )
        }

        Text(
            text = "CERRAR SESIÓN",
            fontSize = 12.sp,
            color = Color(0xFF718096),
            modifier = Modifier.clickable { /* Implementar logout */ }
        )
    }
}

@Composable
fun TotalEstimatedCard(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "TOTAL EST.",
                fontSize = 12.sp,
                color = Color(0xFF718096),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(total),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
        }
    }
}

@Composable
fun ProductItemCard(item: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox circular
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF7FAFC))
                    .clickable { /* Toggle purchased */ },
                contentAlignment = Alignment.Center
            ) {
                // Radio button vacío
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.category,
                    fontSize = 14.sp,
                    color = Color(0xFF718096)
                )
            }

            Text(
                text = NumberFormat.getCurrencyInstance(Locale("es", "MX")).format(item.estimatedPrice),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF7043)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { /* Delete product */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFCBD5E0)
                )
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color(0xFF718096)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF7043)
            )
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
fun EmptyView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🛒",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay productos pendientes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Agrega productos a tu lista",
            fontSize = 14.sp,
            color = Color(0xFF718096)
        )
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onNavigateToHistory: () -> Unit,
    onNavigateToPurchases: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF2D3748),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationButton(
                icon = Icons.Outlined.List,
                isSelected = selectedTab == 0,
                onClick = { /* Ya estamos aquí */ }
            )
            NavigationButton(
                icon = Icons.Outlined.History,
                isSelected = selectedTab == 1,
                onClick = onNavigateToHistory
            )
            NavigationButton(
                icon = Icons.Outlined.Schedule,
                isSelected = selectedTab == 2,
                onClick = onNavigateToPurchases
            )
        }
    }
}

@Composable
fun NavigationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (isSelected) Color(0xFFFF7043) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color(0xFF718096),
            modifier = Modifier.size(24.dp)
        )
    }
}