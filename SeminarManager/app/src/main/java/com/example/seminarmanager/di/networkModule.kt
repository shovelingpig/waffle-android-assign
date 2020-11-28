package com.example.seminarmanager.di

import android.util.Log
import com.example.seminarmanager.BuildConfig
import com.example.seminarmanager.SeminarManagerApplication
import com.example.seminarmanager.api.SeminarService
import com.example.seminarmanager.api.UserService
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single { provideOkHttpClient() }
    single { provideRetrofit(get(), BuildConfig.WAFFLE_BACKEND_BASE_URL) }
    single { provideSeminarService(get()) }
    single { provideUserService(get()) }
}

private fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

    val tokenInterceptor = object: Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var authHeader = SeminarManagerApplication.prefs.getString("user_token_key", "none")
            val original = chain.request()
            val builder = original.newBuilder().method(original.method, original.body)
            if (authHeader != "none") {
                builder.header("Authorization", "Token $authHeader")
            }
            Log.d("WAFFLE_DEBUG", "NetModule Token : $authHeader")
            return chain.proceed(builder.build())
        }
    }

    OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(tokenInterceptor)
        .build()
} else OkHttpClient
    .Builder()
    .build()

private fun provideRetrofit(
    okHttpClient: OkHttpClient,
    BASE_URL: String
): Retrofit =
    Retrofit.Builder()
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

private fun provideSeminarService(retrofit: Retrofit): SeminarService =
    retrofit.create(SeminarService::class.java)

private fun provideUserService(retrofit: Retrofit): UserService =
    retrofit.create(UserService::class.java)