package com.example.myshoplist.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MyShopListRetrofit

/*
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShoppingListRetrofit
 */