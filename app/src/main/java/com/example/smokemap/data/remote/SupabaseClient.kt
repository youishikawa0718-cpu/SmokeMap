package com.example.smokemap.data.remote

import com.example.smokemap.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object SupabaseClient {

    val supabaseUrl: String = BuildConfig.SUPABASE_URL
    val supabaseKey: String = BuildConfig.SUPABASE_ANON_KEY

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(supabaseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SupabaseApi::class.java)
    }
}