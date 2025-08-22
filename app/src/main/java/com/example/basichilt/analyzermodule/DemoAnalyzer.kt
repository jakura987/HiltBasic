package com.example.basichilt.analyzermodule

import com.jakewharton.rxrelay2.Relay
import timber.log.Timber

class DemoAnalyzer constructor(
    val nameChangedRelay: Relay<Boolean>
) : Analyzer<DemoVO, Unit> {

    private val nameDifferAnalyzer = object : Analyzer<DemoVO, String> {
        override fun onAttach(host: DemoVO) {
            Timber.v("nameDifferAnalyzer.onAttach host=%s", host)
        }

        override fun supply(t1: String, t2: String) {
            val changed = (t1 != t2)
            Timber.v("nameDifferAnalyzer.supply saved=%s current=%s -> %s", t1, t2, changed)
            nameChangedRelay.accept(changed)
        }

    }


    override fun onAttach(host: DemoVO) {
        host.nameAnalyzer = nameDifferAnalyzer
    }

    override fun supply(t1: Unit, t2: Unit) {
        /* 本 demo 不用 */
    }
}