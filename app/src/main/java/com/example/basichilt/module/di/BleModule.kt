package com.example.basichilt.module.di

import com.example.basichilt.module.ble.BtManager
import com.example.basichilt.module.ble.DefaultBtManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)  // 安装到最顶层的应用组件
interface BleModule {

    @Binds
    @Singleton
    fun bindBtManager(
        impl: DefaultBtManager
    ): BtManager


}