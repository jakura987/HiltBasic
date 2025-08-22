package com.example.basichilt.analyzermodule

data class DemoViewState(
    val pageMode: Int,
    val demoVO: DemoVO
){
    fun inModifiable(): Boolean = true

    companion object{
        const val MODE_EDIT = 1
        fun initial() : DemoViewState{
            return DemoViewState(MODE_EDIT, DemoVO(savedName = "初始名字"))
        }
    }

}
