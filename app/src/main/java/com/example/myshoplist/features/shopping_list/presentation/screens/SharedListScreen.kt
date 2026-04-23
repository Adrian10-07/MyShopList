package com.example.myshoplist.features.shopping_list.presentation.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.SharedListViewModel
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.SharedProduct
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedListScreen(
    viewModel: SharedListViewModel,
    listId: String,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Nuevas variables para el portapapeles y el teclado
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var linkInput by remember { mutableStateOf("") }

    // Carga Room → Firestore → escucha en tiempo real
    LaunchedEffect(listId) {
        viewModel.initSharedList(listId)
    }

    // Mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorShown()
        }
    }

    // Abrir Share Sheet cuando se genera el link
    LaunchedEffect(uiState.shareLink) {
        uiState.shareLink?.let { link ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, "¡Únete a mi lista de compras!\n$link")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(intent, "Compartir lista"))
            viewModel.onShareLinkShown()
        }
    }

    // Snackbar de confirmación al finalizar
    LaunchedEffect(uiState.isFinalized) {
        if (uiState.isFinalized) {
            snackbarHostState.showSnackbar("¡Compra finalizada! La lista fue limpiada.")
            viewModel.onFinalizedShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFFFF5E6),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Lista Compartida", style = MaterialTheme.typography.titleLarge)
                        if (uiState.isSyncing) {
                            Text(
                                "Sincronizando...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onShareList(listId) }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFF5E6)
                )
            )
        }
    ) { paddingValues ->
        // Se cambió el Box principal por un Column para acomodar el input arriba
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // ── Sección para pegar link de otra lista ────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            LinkInputSection(
                value = linkInput,
                onValueChange = { linkInput = it },
                onPasteFromClipboard = {
                    val pasted = clipboardManager.getText()?.text ?: ""
                    linkInput = pasted
                },
                onLoad = {
                    keyboardController?.hide()
                    viewModel.loadFromLink(linkInput)
                    linkInput = ""
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ── Contenido principal ──────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when {
                    // Cargando por primera vez
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFFFF7043)
                        )
                    }

                    // Lista vacía
                    uiState.products.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🛒", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay productos en esta lista",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Pega el link de una lista compartida arriba\no agrega productos desde la lista principal",
                                fontSize = 14.sp,
                                color = Color(0xFF718096),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        val pendientes   = uiState.products.filter { !it.isPurchased }
                        val comprados    = uiState.products.filter { it.isPurchased }
                        val total        = uiState.products.sumOf { it.estimatedPrice }
                        val totalComprado = comprados.sumOf { it.estimatedPrice }

                        Column(modifier = Modifier.fillMaxSize()) {

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                // Resumen de totales
                                item {
                                    SharedListSummaryCard(
                                        total = total,
                                        totalComprado = totalComprado,
                                        comprados = comprados.size,
                                        pendientes = pendientes.size
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Pendientes
                                if (pendientes.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Pendientes (${pendientes.size})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF718096)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    items(pendientes, key = { it.id }) { product ->
                                        SharedProductCard(
                                            product = product,
                                            onToggle = { viewModel.toggleProduct(listId, product.id) }
                                        )
                                    }
                                }

                                // Comprados
                                if (comprados.isNotEmpty()) {
                                    item {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Comprados (${comprados.size})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFF718096)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    items(comprados, key = { it.id }) { product ->
                                        SharedProductCard(
                                            product = product,
                                            onToggle = { viewModel.toggleProduct(listId, product.id) }
                                        )
                                    }
                                }
                            }

                            // Botón Finalizar — visible cuando hay al menos un comprado
                            if (comprados.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.finalizeSharedPurchase(listId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Finalizar Compra",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            } // Cierra Box weight(1f)
        } // Cierra Column principal
    }
}

// ─── Card de resumen ─────────────────────────────────────────────────────────
@Composable
private fun SharedListSummaryCard(
    total: Double,
    totalComprado: Double,
    comprados: Int,
    pendientes: Int
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total estimado", fontSize = 12.sp, color = Color(0xFF718096))
                Text(
                    currencyFormat.format(total),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Comprado: ${currencyFormat.format(totalComprado)}",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$pendientes pendientes", fontSize = 13.sp, color = Color(0xFFFF7043))
                Text("$comprados comprados", fontSize = 13.sp, color = Color(0xFF4CAF50))
            }
        }
    }
}

// ─── Card de producto ────────────────────────────────────────────────────────
@Composable
private fun SharedProductCard(
    product: SharedProduct,
    onToggle: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

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
                    .background(
                        if (product.isPurchased) Color(0xFF4CAF50) else Color(0xFFF7FAFC)
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (product.isPurchased) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.isPurchased) Color(0xFFCBD5E0) else Color(0xFF2D3748),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    fontSize = 14.sp,
                    color = Color(0xFF718096)
                )
            }

            Text(
                text = currencyFormat.format(product.estimatedPrice),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (product.isPurchased) Color(0xFFCBD5E0) else Color(0xFFFF7043)
            )
        }
    }
}

// ─── Input para pegar link ───────────────────────────────────────────────────
@Composable
private fun LinkInputSection(
    value: String,
    onValueChange: (String) -> Unit,
    onPasteFromClipboard: () -> Unit,
    onLoad: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Ver lista de otro usuario",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Pega el link aquí", fontSize = 13.sp, color = Color(0xFFCBD5E0))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onLoad() }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF7043),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
                // Botón pegar desde portapapeles
                IconButton(
                    onClick = onPasteFromClipboard,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF7FAFC))
                ) {
                    Icon(
                        Icons.Outlined.ContentPaste,
                        contentDescription = "Pegar",
                        tint = Color(0xFF718096)
                    )
                }
                // Botón cargar
                Button(
                    onClick = onLoad,
                    enabled = value.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF7043),
                        disabledContainerColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("Ver", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}