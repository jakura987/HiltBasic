package com.example.basichilt.analyzermodule

import androidx.lifecycle.ViewModel
import com.example.basichilt.module.contract.Contract
import com.jakewharton.rxrelay2.BehaviorRelay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DemoViewModel @Inject constructor() : ViewModel() {
    val nameChanged: BehaviorRelay<Boolean> = BehaviorRelay.createDefault(true)
}