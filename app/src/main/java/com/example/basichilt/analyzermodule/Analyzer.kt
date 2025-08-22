package com.example.basichilt.analyzermodule

interface Analyzer<Host, T> {
    fun onAttach(host: Host)
    fun supply(t1: T, t2: T)
}