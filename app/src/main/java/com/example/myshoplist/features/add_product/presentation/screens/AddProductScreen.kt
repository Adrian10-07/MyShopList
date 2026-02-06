package com.example.myshoplist.features.add_product.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myshoplist.features.add_product.domain.use_case.AddProductUseCase
import com.example.myshoplist.features.add_product.presentation.viewmodels.AddProductViewModel
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onProductAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var estimatedPrice by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Log para cambios de estado
    LaunchedEffect(uiState) {
        val logData = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("label", "🔄 SCREEN - Estado cambiado")
            put("data", JSONObject().apply {
                put("newState", uiState.javaClass.simpleName)
            })
        }
        Log.d("AddProductScreen", logData.toString(2))
    }

    // Manejar éxito
    LaunchedEffect(uiState) {
        if (uiState is AddProductUiState.Success) {
            val product = (uiState as AddProductUiState.Success).product

            val logData = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("label", "✅ SCREEN - Producto agregado exitosamente")
                put("data", JSONObject().apply {
                    put("productId", product.id)
                    put("name", product.name)
                    put("category", product.category)
                })
            }
            Log.d("AddProductScreen", logData.toString(2))

            onProductAdded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo de nombre
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    Log.d("AddProductScreen", "Nombre actualizado: $it")
                },
                label = { Text("Nombre del producto") },
                placeholder = { Text("Ej: Leche entera") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is AddProductUiState.Loading
            )

            // Dropdown de categorías
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    placeholder = { Text("Selecciona una categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = uiState !is AddProductUiState.Loading
                )

                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    AddProductUseCase.VALID_CATEGORIES.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                showCategoryDropdown = false

                                val logData = JSONObject().apply {
                                    put("timestamp", System.currentTimeMillis())
                                    put("label", "📝 SCREEN - Categoría seleccionada")
                                    put("data", JSONObject().apply {
                                        put("category", category)
                                    })
                                }
                                Log.d("AddProductScreen", logData.toString(2))
                            }
                        )
                    }
                }
            }

            // Campo de precio
            OutlinedTextField(
                value = estimatedPrice,
                onValueChange = {
                    estimatedPrice = it
                    Log.d("AddProductScreen", "Precio actualizado: $it")
                },
                label = { Text("Precio estimado") },
                placeholder = { Text("Ej: 45.50") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") },
                enabled = uiState !is AddProductUiState.Loading
            )

            // Mostrar error
            if (uiState is AddProductUiState.Error) {
                val errorMessage = (uiState as AddProductUiState.Error).message

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón de guardar
            Button(
                onClick = {
                    val logData = JSONObject().apply {
                        put("timestamp", System.currentTimeMillis())
                        put("label", "🔵 SCREEN - Botón guardar presionado")
                        put("data", JSONObject().apply {
                            put("name", name)
                            put("category", selectedCategory)
                            put("estimatedPrice", estimatedPrice)
                        })
                    }
                    Log.d("AddProductScreen", logData.toString(2))

                    viewModel.addProduct(name, selectedCategory, estimatedPrice)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddProductUiState.Loading
            ) {
                if (uiState is AddProductUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Producto")
                }
            }
        }
    }
}