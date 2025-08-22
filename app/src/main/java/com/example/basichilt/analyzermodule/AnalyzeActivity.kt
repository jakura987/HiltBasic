package com.example.basichilt.analyzermodule

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.basichilt.R
import com.example.basichilt.databinding.ActivityAnalyzeBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

@AndroidEntryPoint
class AnalyzeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyzeBinding
    private val vm: DemoViewModel by viewModels()
    private val bag = CompositeDisposable()

    // 返回键拦截：根据 isEnabled 决定是否弹窗
    private val backGuard = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            AlertDialog.Builder(this@AnalyzeActivity)
                .setTitle("提示")
                .setMessage("有未保存的更改，是否离开？")
                .setNegativeButton("取消", null)
                .setPositiveButton("离开") { _, _ ->
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(backGuard)

        // 初始状态（名字走 ObservableField）
        val state = DemoViewState.initial()
        binding.vo = state
        binding.executePendingBindings()

        // 接上线：总分析器里会把 nameDifferAnalyzer 插到 VO 上
        val analyzer = DemoAnalyzer(vm.nameChanged)
        state.demoVO.analyzer = analyzer

        // “保存”：把当前文本作为新的 savedName（简化处理）
        binding.btnSave.setOnClickListener {
            val cur = state.demoVO.name.get().orEmpty()
            val newVo = DemoVO(savedName = cur) // name 初始就是当前
            // 重新绑定到视图 & 重新接上 analyzer
            val newState = state.copy(demoVO = newVo)
            binding.vo = newState
            binding.executePendingBindings()
            newVo.analyzer = analyzer
            Timber.v("saved. reset dirty=false")
        }
    }

    @SuppressLint("AutoDispose")
    override fun onStart() {
        super.onStart()
        // 只有“名字改动”一条流：首发 false + 去重
        val disposable : Disposable = vm.nameChanged
            .distinctUntilChanged()
            .startWith(false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { changed ->
                binding.btnSave.isEnabled = changed
                backGuard.isEnabled = changed
                Timber.v("nameChanged = $changed")
            }

        bag.add(disposable)

    }

    override fun onStop() {
        super.onStop()
        bag.clear()
    }
}