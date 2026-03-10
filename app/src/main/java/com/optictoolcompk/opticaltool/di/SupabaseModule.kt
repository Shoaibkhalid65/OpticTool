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
            supabaseUrl = "your supabase url",
            supabaseKey = "your supabase key"
        ) {
            install(Auth)
        }
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth =
        client.auth

}