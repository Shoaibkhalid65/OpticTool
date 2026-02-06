package com.optictoolcompk.opticaltool.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://zqemuggkbmlakqxdosay.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpxZW11Z2drYm1sYWtxeGRvc2F5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAyNzQ2NDMsImV4cCI6MjA4NTg1MDY0M30.WZVdh1UiZ26-7xkqVwLr5dU79SiwIstrkU5-FEGegJM"
        ) {
            install(Auth)
        }
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth =
        client.auth

}