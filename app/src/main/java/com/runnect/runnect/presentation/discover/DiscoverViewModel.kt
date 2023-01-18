package com.runnect.runnect.presentation.discover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runnect.runnect.data.dto.RecommendCourseDTO
import com.runnect.runnect.data.dto.request.RequestCourseScrap
import com.runnect.runnect.domain.CourseRepository
import com.runnect.runnect.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(private val courseRepository: CourseRepository) :
    ViewModel() {
    private var _courseInfoState = MutableLiveData<UiState>(UiState.Loading)
    val courseInfoState: LiveData<UiState>
        get() = _courseInfoState

    private var _recommendCourseList = mutableListOf<RecommendCourseDTO>()
    val recommendCourseList: List<RecommendCourseDTO>
        get() = _recommendCourseList

    fun getRecommendCourse() {
        viewModelScope.launch {
            runCatching {
                _courseInfoState.value = UiState.Loading
                courseRepository.getRecommendCourse()
            }.onSuccess {
                _recommendCourseList = it
                _courseInfoState.value = UiState.Success
            }.onFailure {
                _courseInfoState.value = UiState.Failure
            }
        }
    }

    fun postCourseScrap(id:Int,scrapTF:Boolean) {
        viewModelScope.launch {
            kotlin.runCatching {
                Timber.d("id = $id")
                courseRepository.postCourseScrap(RequestCourseScrap(id,scrapTF.toString()))
            }.onSuccess {
                Timber.d("스크랩 성공")
            }.onFailure {
                Timber.d("스크랩 실패")
            }
        }
    }
}