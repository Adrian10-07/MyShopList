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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Schedule
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
import com.example.myshoplist.features.add_product.presentation.screens.AddProductScreen
import com.example.myshoplist.features.add_product.presentation.viewmodels.AddProductViewModel
import com.example.myshoplist.features.add_product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.ShoppingListViewModel
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Check


@Composable
fun ShoppingListScreen(
    shoppingListViewModel: ShoppingListViewModel = viewModel(),
    addProductViewModel: AddProductViewModel,
    userName: String = "PapaFeliz",
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPurchases: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by shoppingListViewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Log de cambios de estado
    LaunchedEffect(uiState) {
        val logData = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("label", " SCREEN - Estado cambiado")
            put("data", JSONObject().apply {
                put("newState", uiState.javaClass.simpleName)
            })
        }
        Log.d("ShoppingListScreen", logData.toString(2))
    }

    if (showDeleteConfirmation && productToDelete != null) {
        DeleteConfirmationDialog(
            productName = productToDelete?.name ?: "",
            onConfirm = {
                productToDelete?.id?.let { id ->
                    shoppingListViewModel.deleteProduct(id)
                }
                showDeleteConfirmation = false
                productToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                productToDelete = null
            }
        )
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
            HeaderSection(
                userName = userName,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            AddProductScreen(
                viewModel = addProductViewModel,
                onProductAdded = {
                    // Recargar la lista cuando se agrega un producto
                    shoppingListViewModel.loadProducts()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de productos
            when (uiState) {
                is ShoppingListUiState.Loading -> {
                    LoadingView()
                }

                is ShoppingListUiState.Success -> {
                    val items = (uiState as ShoppingListUiState.Success).items
                    ProductList(
                        items = items,
                        onDeleteProduct = { product ->
                            productToDelete = product
                            showDeleteConfirmation = true
                        },
                        onUpdateProduct = { product ->
                            product.id?.let { shoppingListViewModel.updateProduct(it) }
                        }
                    )
                }

                is ShoppingListUiState.Error -> {
                    ErrorView(
                        message = (uiState as ShoppingListUiState.Error).message,
                        onRetry = { shoppingListViewModel.loadProducts() }
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
private fun DeleteConfirmationDialog(
    productName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar producto",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar \"$productName\" de tu lista?",
                fontSize = 16.sp,
                color = Color(0xFF718096)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7043)
                )
            ) {
                Text("Eliminar", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF718096))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun HeaderSection(
    userName: String,
    onLogout: () -> Unit
) {
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
            modifier = Modifier.clickable { onLogout() }
        )
    }
}

@Composable
private fun TotalEstimatedCard(total: Double) {
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
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFFF7043))
    }
}

@Composable
private fun ProductList(
    items: List<Product>,
    onDeleteProduct: (Product) -> Unit = {},
    onUpdateProduct: (Product) -> Unit = {}
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ProductItemCard(
                item = item,
                onTogglePurchased = { onUpdateProduct(item) },
                onDelete = {
                    onDeleteProduct(item)
                }
            )
        }
    }
}

@Composable
private fun ProductItemCard(
    item: Product,
    onTogglePurchased: () -> Unit,
    onDelete: () -> Unit
) {
    val isMarkedAsPurchased = item.isPurchased == 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    // Usamos la comparación lógica que creamos arriba
                    .background(if (isMarkedAsPurchased) Color(0xFF4CAF50) else Color(0xFFF7FAFC))
                    .clickable { onTogglePurchased() },
                contentAlignment = Alignment.Center
            ) {
                if (isMarkedAsPurchased) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    // Opcional: Atenuar el texto si ya está comprado
                    color = if (isMarkedAsPurchased) Color(0xFFCBD5E0) else Color(0xFF2D3748),
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

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFCBD5E0),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚠️", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, fontSize = 16.sp, color = Color(0xFF718096))
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🛒", fontSize = 64.sp)
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
private fun BottomNavigationBar(
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
                onClick = { }
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
private fun NavigationButton(
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