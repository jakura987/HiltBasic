package com.example.basichilt.module.contract

interface Contract {
    interface View {
        fun showGreeting(text: String)
    }
    interface Presenter {
        fun attach(view: View)
        fun onButtonClicked()
        fun detach()
    }
}