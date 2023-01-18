package com.runnect.runnect.presentation.discover.upload

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import coil.load
import com.runnect.runnect.R
import com.runnect.runnect.binding.BindingActivity
import com.runnect.runnect.databinding.ActivityDiscoverUploadBinding
import com.runnect.runnect.presentation.MainActivity
import com.runnect.runnect.presentation.state.UiState
import com.runnect.runnect.util.extension.clearFocus
import com.runnect.runnect.util.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class DiscoverUploadActivity :
    BindingActivity<ActivityDiscoverUploadBinding>(R.layout.activity_discover_upload) {
    private val viewModel: DiscoverUploadViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
        binding.lifecycleOwner = this


        initLayout()
        addListener()
        addObserver()
    }

    private fun initLayout() {
        binding.etDiscoverUploadDesc.movementMethod = null //내용 초과시 스크롤 되지 않도록 함
        with(binding) {
            ivDiscoverUploadMap.load(intent.getStringExtra("img"))
            tvDiscoverUploadDistance.text = intent.getStringExtra("distance")
            tvDiscoverUploadDeparture.text = intent.getStringExtra("departure")
            viewModel.id = intent.getIntExtra("courseId", 0) //선택한 코스의 id로 API 호출 예정
        }

    }

    private fun addListener() {
        binding.ivDiscoverUploadBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        binding.ivDiscoverUploadFinish.setOnClickListener {
            Timber.d("버튼 활성화 여부 - ${it.isActivated}")
            if (it.isActivated) {
                viewModel.postUploadMyCourse()
            }
        }
        //키보드 이벤트에 따른 동작 정의
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            val heightDiff: Int = binding.root.rootView.height - (r.bottom - r.top)
            if (heightDiff > 200) { //root view의 높이가 200이상일 때에는 keyboard up
                //ok now we know the keyboard is up...
                binding.ivDiscoverUploadFinish.visibility = View.GONE
                binding.tvDiscoverUploadFinish.visibility = View.GONE
            } else {
                //그 이하일 때에는 keyboard down
                binding.ivDiscoverUploadFinish.visibility = View.VISIBLE
                binding.tvDiscoverUploadFinish.visibility = View.VISIBLE
            }
        }
    }

    private fun addObserver() {
        viewModel.isUploadEnable.observe(this) {
            binding.ivDiscoverUploadFinish.isActivated = it
        }
        viewModel.courseUpLoadState.observe(this){ state ->
            if(state == UiState.Success){
                showToast("업로드 완료!")
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }

    //키보드 밖 터치 시, 키보드 내림
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val focusView = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev!!.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                clearFocus(focusView)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}