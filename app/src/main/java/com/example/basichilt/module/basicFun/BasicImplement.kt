package com.example.basichilt.module.basicFun

import com.example.basichilt.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BasicImplement @Inject constructor() : BasicInterface {
    override fun sayHello(): String {
        return "Hello from Hilt, version_name:${BuildConfig.VERSION_NAME}"
    }
}