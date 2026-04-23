package com.example.myshoplist.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE products ADD COLUMN pendingSync   INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE products ADD COLUMN pendingDelete INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE products ADD COLUMN pendingToggle INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `purchases` (
                `id`            TEXT    NOT NULL,
                `totalAmount`   REAL    NOT NULL,
                `purchaseDate`  TEXT    NOT NULL,
                `itemCount`     INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `purchase_items` (
                `localId`     INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `purchaseId`  TEXT    NOT NULL,
                `productName` TEXT    NOT NULL,
                `category`    TEXT    NOT NULL,
                `price`       REAL    NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Soporte de compras offline pendientes de sincronización
        db.execSQL("ALTER TABLE purchases ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
        // ID del producto del servidor para reconstruir la petición durante sync
        db.execSQL("ALTER TABLE purchase_items ADD COLUMN productId TEXT NOT NULL DEFAULT ''")
    }
}