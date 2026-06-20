package com.marketplace.app.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.marketplace.app.BuildConfig
import com.marketplace.app.data.local.TokenStore
import com.marketplace.app.data.remote.AuthInterceptor
import com.marketplace.app.data.remote.MarketplaceApi
import com.marketplace.app.data.remote.TokenAuthenticator
import com.marketplace.app.data.repository.MarketplaceRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val tokenStore = TokenStore(appContext)

    private val gson: Gson = GsonBuilder().create()

    private val baseUrl: String = BuildConfig.API_BASE_URL.let { if (it.endsWith("/")) it else "$it/" }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .addInterceptor(AuthInterceptor(tokenStore))
        .authenticator(TokenAuthenticator(baseUrl, gson, tokenStore))
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val api: MarketplaceApi = retrofit.create(MarketplaceApi::class.java)

    val repository: MarketplaceRepository = MarketplaceRepository(api, tokenStore, gson)
}
