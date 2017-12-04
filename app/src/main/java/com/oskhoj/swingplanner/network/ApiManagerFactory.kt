package com.oskhoj.swingplanner.network

import com.oskhoj.swingplanner.BuildConfig
import com.oskhoj.swingplanner.network.service.EventService
import com.oskhoj.swingplanner.network.service.TeacherService
import com.oskhoj.swingplanner.util.create
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiManagerFactory {
    private val serverUrl: String = "http://newtest-env.tvsxkf4uuc.eu-central-1.elasticbeanstalk.com/api/"

    private val retrofit: Retrofit by lazy { initializeRetrofit() }

    val eventService: EventService by lazy { retrofit.create<EventService>() }

    val teacherService: TeacherService by lazy { retrofit.create<TeacherService>() }

    private fun initializeRetrofit() = Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(client())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private fun client() =
            OkHttpClient.Builder().apply {
                addInterceptor(loggingInterceptor(BuildConfig.DEBUG))
            }.build()

    private fun loggingInterceptor(isDebug: Boolean): HttpLoggingInterceptor =
            HttpLoggingInterceptor().apply {
                level = if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }
}