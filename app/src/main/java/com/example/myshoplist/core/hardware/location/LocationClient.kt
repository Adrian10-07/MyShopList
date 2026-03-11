package com.example.myshoplist.core.hardware.location

import com.example.myshoplist.core.hardware.location.model.LocationData

interface LocationClient {
    suspend fun getCurrentLocation(): LocationData?
}