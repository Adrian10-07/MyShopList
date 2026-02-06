package com.example.myshoplist.features.add_product.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myshoplist.features.add_product.domain.use_case.AddProductUseCase
import com.example.myshoplist.features.add_product.presentation.viewmodels.AddProductViewModel

/**
 * Componente modal flotante para agregar productos
 */
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    onProductAdded: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showModal by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var estimatedPrice by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    // Manejar éxito
    LaunchedEffect(uiState) {
        if (uiState is AddProductUiState.Success) {
            // Limpiar campos
            name = ""
            selectedCategory = "General"
            estimatedPrice = ""

            // Cerrar modal
            showModal = false

            // Resetear estado
            viewModel.resetState()

            // Notificar que se agregó el producto
            onProductAdded()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Botón "Agregar Cositas"
        AnimatedVisibility(
            visible = !showModal,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { -it }
            ),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { -it }
            )
        ) {
            AddProductButton(
                onClick = { showModal = true }
            )
        }

        // Formulario flotante
        AnimatedVisibility(
            visible = showModal,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { -it }
            ),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { -it }
            )
        ) {
            AddProductForm(
                name = name,
                onNameChange = { name = it },
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it },
                estimatedPrice = estimatedPrice,
                onPriceChange = { estimatedPrice = it },
                showCategoryDropdown = showCategoryDropdown,
                onToggleCategoryDropdown = { showCategoryDropdown = !showCategoryDropdown },
                onDismissCategoryDropdown = { showCategoryDropdown = false },
                uiState = uiState,
                onCancel = {
                    showModal = false
                    name = ""
                    selectedCategory = "General"
                    estimatedPrice = ""
                    viewModel.resetState()
                },
                onSave = {
                    viewModel.addProduct(name, selectedCategory, estimatedPrice)
                }
            )
        }
    }
}

/**
 * Botón naranja "Agregar Cositas"
 */
@Composable
private fun AddProductButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF7043)
        ),
        shape = RoundedCornerShape(28.dp)
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
}

/**
 * Formulario de agregar producto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductForm(
    name: String,
    onNameChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    estimatedPrice: String,
    onPriceChange: (String) -> Unit,
    showCategoryDropdown: Boolean,
    onToggleCategoryDropdown: () -> Unit,
    onDismissCategoryDropdown: () -> Unit,
    uiState: AddProductUiState,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¿QUÉ NECESITAS?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF718096),
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = {
                    Text(
                        "Ej. Zanahorias Mágicas",
                        color = Color(0xFFCBD5E0)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is AddProductUiState.Loading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF7043),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF7FAFC),
                    unfocusedContainerColor = Color(0xFFF7FAFC)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRECIO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF718096),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = estimatedPrice,
                        onValueChange = onPriceChange,
                        placeholder = {
                            Text("0.00", color = Color(0xFFCBD5E0))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = uiState !is AddProductUiState.Loading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF7043),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF7FAFC),
                            unfocusedContainerColor = Color(0xFFF7FAFC)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TIPO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF718096),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = showCategoryDropdown,
                        onExpandedChange = { onToggleCategoryDropdown() }
                    ) {
                        OutlinedTextField(
                            value = getCategoryShortName(selectedCategory),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            enabled = uiState !is AddProductUiState.Loading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF7043),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF7FAFC),
                                unfocusedContainerColor = Color(0xFFF7FAFC),
                                disabledBorderColor = Color(0xFFE2E8F0),
                                disabledContainerColor = Color(0xFFF7FAFC)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = onDismissCategoryDropdown
                        ) {
                            AddProductUseCase.VALID_CATEGORIES.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        onCategoryChange(category)
                                        onDismissCategoryDropdown()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (uiState is AddProductUiState.Error) {
                ErrorMessage(message = (uiState as AddProductUiState.Error).message)
            }

            FormButtons(
                uiState = uiState,
                onCancel = onCancel,
                onSave = onSave
            )
        }
    }
}

/**
 * Mensaje de error
 */
@Composable
private fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFED7D7)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFC53030),
            fontSize = 14.sp
        )
    }
}

/**
 * Botones Cancelar y Guardar
 */
@Composable
private fun FormButtons(
    uiState: AddProductUiState,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            enabled = uiState !is AddProductUiState.Loading
        ) {
            Text(
                text = "Cancelar",
                color = Color(0xFF718096),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            enabled = uiState !is AddProductUiState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF7043)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState is AddProductUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Guardar",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Helper para nombres cortos de categorías
 */
private fun getCategoryShortName(category: String): String {
    return when (category) {
        "Frutas y Verduras" -> "Frutas/Verd."
        "Carnes y Pescados" -> "Carnes/Pesc."
        "Higiene Personal" -> "Higiene"
        else -> category
    }
}