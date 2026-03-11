plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Hilt Plugin
    alias(libs.plugins.hilt.android) apply false

    // Agregamos KSP aquí para que el módulo app lo reconozca
    alias(libs.plugins.devtools.ksp) apply false

    // Kotlin Serialization (Cambiado a sintaxis de alias si lo tienes en el TOML)
    // Si no lo tienes en el TOML, puedes dejarlo como id(...) pero con la versión 2.0.21
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}