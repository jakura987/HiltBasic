package com.example.basichilt.module.di

import com.example.basichilt.module.basicFun.BasicImplement
import com.example.basichilt.module.basicFun.BasicInterface
import com.example.basichilt.module.contract.Contract
import com.example.basichilt.module.presenter.MainPresenter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BasicModule {
    @Binds
    @Singleton
    fun bindBasicInterface(implement: BasicImplement):BasicInterface

    @Binds @Singleton
    fun bindMainPresenter(p: MainPresenter): Contract.Presenter
}