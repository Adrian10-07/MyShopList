package com.example.myshoplist.core.session

// Esto es un gestor de sesión en memoria temporal.
object SessionManager {
    var authToken: String? = null
    /** ID del usuario autenticado actualmente. Null antes del primer login. */
    var userId: String? = null
}