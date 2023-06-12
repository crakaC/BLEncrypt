package com.crakac.blenc.di

import android.content.Context
import com.crakac.blenc.repo.BleDeviceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideBleDeviceRepository(@ApplicationContext context: Context): BleDeviceRepository {
        return BleDeviceRepository(context)
    }
}