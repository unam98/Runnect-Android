package com.runnect.runnect.presentation.discover.load.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.runnect.runnect.data.model.CourseLoadInfoDTO
import com.runnect.runnect.databinding.ItemDiscoverLoadSelectBinding
import com.runnect.runnect.util.DiffUtilItemCallback
import com.runnect.runnect.util.callback.OnItemClick
import timber.log.Timber

class DiscoverLoadAdapter(context: Context, listener: OnItemClick) :
    ListAdapter<CourseLoadInfoDTO, DiscoverLoadAdapter.DiscoverLoadViewHolder>(DiffUtilItemCallback()) {
    private val inflater by lazy { LayoutInflater.from(context) }
    private val mCallback = listener
    private var selectedCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverLoadViewHolder {
        return DiscoverLoadViewHolder(
            ItemDiscoverLoadSelectBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DiscoverLoadViewHolder, position: Int) {
        holder.onBind(currentList[position])

    }

    inner class DiscoverLoadViewHolder(private val binding: ItemDiscoverLoadSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: CourseLoadInfoDTO) {
            binding.ivItemDiscoverLoadSelectMap.load(data.img)
            binding.ivItemDiscoverLoadSelectMap.isSelected = false
            binding.tvItemDiscoverLoadSelectLocation.text = data.location
            binding.ivItemDiscoverLoadSelectMap.setOnClickListener {
                Timber.d("1. Adapter에서 Activity에 정의된 콜백함수 호출")
                //오직 하나의 코스만 선택되도록 함
                if (it.isSelected) {
                    mCallback.selectItem(0)
                    it.isSelected = false
                    selectedCount -= 1
                } else if (!it.isSelected && selectedCount < 1) {
                    mCallback.selectItem(data.id)
                    it.isSelected = true
                    selectedCount += 1
                }
            }
        }

    }

}