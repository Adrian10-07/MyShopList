package com.example.myshoplist.core.network

import com.example.myshoplist.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.url.encodedPath.endsWith("auth/login", ignoreCase = true)) {
            return chain.proceed(originalRequest)
        }
        val token = SessionManager.authToken
        if (token == null) {
            return chain.proceed(originalRequest)
        }
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }
}
