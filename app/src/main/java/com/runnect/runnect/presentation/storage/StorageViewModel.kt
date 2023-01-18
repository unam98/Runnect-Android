package com.runnect.runnect.presentation.storage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runnect.runnect.data.api.KApiCourse
import com.runnect.runnect.data.model.ResponseGetCourseDto
import kotlinx.coroutines.launch

class StorageViewModel : ViewModel() {

    val service = KApiCourse.ServicePool.courseService //객체 생성

    val getResult = MutableLiveData<ResponseGetCourseDto>()
    val errorMessage = MutableLiveData<String>()

    fun getCourseList() {

        service.also {
            viewModelScope.launch {
                kotlin.runCatching {
                    service.getCourseList()
                }.onSuccess {
                    getResult.value = it.body()
                }.onFailure {
                    errorMessage.value = it.message
                }
            }
        }

    }
}