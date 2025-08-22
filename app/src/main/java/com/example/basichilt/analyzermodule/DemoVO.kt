package com.example.basichilt.analyzermodule

import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableField

import timber.log.Timber

data class DemoVO constructor(
    val savedName: String,
    val name: ObservableField<String> = ObservableField(savedName)
) {

    init {
        val callback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                Timber.v("DemoVO set trigger...")
            }

        }
        name.addOnPropertyChangedCallback(callback)
    }

    // 叶子分析器（名字差异）
    internal var nameAnalyzer: Analyzer<DemoVO, String>? = null
        set(value) {
            field = value
            field?.onAttach(this)
            field?.supply(savedName, name.get().orEmpty())
        }

    // 顶层分析器（只负责把 nameAnalyzer 插进去，保持你原项目的形态）
    var analyzer: Analyzer<DemoVO, Unit>? = null
        set(value) {
            Timber.v("test combineLatest TherapyDetailVO 里的 analyzer 触发detailvo里的set方法")
            field = value
            field?.onAttach(this)
//            field?.supply(savedDto, dto)
        }





}
