package com.example.myshoplist.core.session

// NOTA: Esto es un gestor de sesión en memoria temporal.
// Para una aplicación real, deberías usar SharedPreferences o DataStore para persistir el token.
object SessionManager {
    var authToken: String? = null
}