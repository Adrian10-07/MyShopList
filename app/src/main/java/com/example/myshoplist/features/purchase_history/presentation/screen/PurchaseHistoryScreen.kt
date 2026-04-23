package com.tuspaquetes.features.purchase_history.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myshoplist.features.purchase_history.domain.entities.Purchase
import com.example.myshoplist.features.purchase_history.domain.entities.PurchaseProduct
import com.tuspaquetes.features.purchase_history.presentation.viewmodels.PurchaseHistoryViewModel

@Composable
fun PurchaseHistoryScreen(
    viewModel: PurchaseHistoryViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5E6))
            .systemBarsPadding()
    ) {
        // ── Header ──────────────────────────────────────────────────── //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateToBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color(0xFF2D3748),
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = "Historial",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748),
            )
            IconButton(onClick = { viewModel.loadPurchaseHistory() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar",
                    tint = Color(0xFF718096),
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        // ── Estados ──────────────────────────────────────────────────── //
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF7043))
                }
            }

            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            }

            uiState.purchases.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = "🛒", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay compras en el historial",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(uiState.purchases, key = { it.id }) { purchase ->
                        PurchaseCard(
                            purchase   = purchase,
                            isExpanded = uiState.expandedPurchaseId == purchase.id,
                            onToggle   = { viewModel.toggleExpand(purchase.id) },
                        )
                    }
                }
            }
        }
    }
}

// ── Tarjeta de compra con menú desplegable ───────────────────────── //

@Composable
private fun PurchaseCard(
    purchase: Purchase,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrowRotation",
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Fila superior: fecha + cantidad ──────────────────── //
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically,
            ) {
                Text(
                    text       = purchase.purchaseDate.take(10),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF2D3748),
                )
                Surface(
                    color = Color(0xFFF7FAFC),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text     = "${purchase.itemCount} items",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color    = Color(0xFF718096),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // ── Ubicación GPS ────────────────────────────────────── //
            if (purchase.latitude != null && purchase.longitude != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(bottom = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint     = Color(0xFF718096),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text     = "Lat: ${"%.4f".format(purchase.latitude)}, Lon: ${"%.4f".format(purchase.longitude)}",
                        fontSize = 12.sp,
                        color    = Color(0xFF718096),
                    )
                }
            }

            // ── Fila inferior: total + botón de expansión ────────── //
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically,
            ) {
                Text(
                    text       = "Total: $${"%.2f".format(purchase.totalAmount)}",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFFFF7043),
                )

                // Mostrar flecha sólo si hay productos que mostrar
                if (purchase.products.isNotEmpty()) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Colapsar" else "Ver productos",
                            tint     = Color(0xFF718096),
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(arrowRotation),
                        )
                    }
                }
            }

            // ── Menú desplegable de productos ────────────────────── //
            AnimatedVisibility(
                visible = isExpanded && purchase.products.isNotEmpty(),
                enter   = expandVertically(),
                exit    = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text       = "Productos comprados",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF718096),
                        modifier   = Modifier.padding(bottom = 4.dp),
                    )

                    purchase.products.forEach { product ->
                        PurchaseProductRow(product = product)
                    }
                }
            }
        }
    }
}

// ── Fila de un producto dentro del menú desplegable ─────────────── //

@Composable
private fun PurchaseProductRow(product: PurchaseProduct) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment   = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = product.name,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = Color(0xFF2D3748),
            )
            Text(
                text     = product.category,
                fontSize = 12.sp,
                color    = Color(0xFF718096),
            )
        }
        Text(
            text       = "$${"%.2f".format(product.price)}",
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFFFF7043),
        )
    }
}
