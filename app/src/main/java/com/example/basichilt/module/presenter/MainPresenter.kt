package com.example.basichilt.module.presenter

import com.example.basichilt.module.basicFun.BasicInterface
import com.example.basichilt.module.ble.BtManager
import com.example.basichilt.module.contract.Contract
import javax.inject.Inject

class MainPresenter @Inject constructor(
    private val basic: BasicInterface
) : Contract.Presenter{

    private var view: Contract.View? = null

    override fun attach(view: Contract.View) {
        this.view = view
    }

    override fun onButtonClicked() {
        val text = basic.sayHello()
        view?.showGreeting(text)
    }

    override fun detach() {
        view = null
    }
}