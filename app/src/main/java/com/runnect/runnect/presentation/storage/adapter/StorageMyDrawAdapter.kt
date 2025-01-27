package com.runnect.runnect.presentation.storage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.runnect.runnect.data.model.MyDrawCourse
import com.runnect.runnect.databinding.ItemStorageMyDrawBinding
import com.runnect.runnect.util.callback.ItemCount
import com.runnect.runnect.util.callback.OnMyDrawClick
import timber.log.Timber


class StorageMyDrawAdapter(
    val myDrawClickListener: OnMyDrawClick, val itemCount: ItemCount
) :
    ListAdapter<MyDrawCourse, StorageMyDrawAdapter.ItemViewHolder>(Differ()) {

    lateinit var binding: ItemStorageMyDrawBinding
    private var selectedItems: MutableList<View>? = mutableListOf()
    private var selectedBoxes: MutableList<View>? = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemStorageMyDrawBinding.inflate(inflater)

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    fun handleCheckBoxVisibility(isEditMode: Boolean) {
        selectedBoxes?.forEach {
            it.isVisible = isEditMode
        }
    }

    fun clearSelection() {
        selectedItems?.forEach {
            it.isSelected = false
        }
        selectedBoxes?.forEach {
            it.isSelected = false
            it.isVisible = false
        }
        selectedItems?.clear()
    }

    fun removeItems(removedIds: List<Int>) {
        if (currentList.isNotEmpty()) {
            val newItems = currentList.filter { !removedIds.contains(it.courseId) }
            submitList(newItems)
            itemCount.calcItemSize(newItems.size)
        }
    }

    inner class ItemViewHolder(val binding: ItemStorageMyDrawBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(courseList: MyDrawCourse) {
            with(binding) {
                selectedBoxes?.add(ivCheckbox)

                storageItem = courseList

                ivMyPageUploadCourseSelectBackground.setOnClickListener {
                    val isEditMode = myDrawClickListener.selectItem(courseList.courseId!!)
                    Timber.d("isEditMode값 : $isEditMode")
                    if (isEditMode) {
                        if (it.isSelected) {
                            ivCheckbox.isSelected = false
                            it.isSelected = false
                            selectedBoxes?.remove(ivCheckbox)
                            selectedItems?.remove(it)
                        } else {
                            selected = true
                            ivCheckbox.isSelected = true
                            it.isSelected = true
                            selectedBoxes?.add(ivCheckbox)
                            selectedItems?.add(it)
                        }
                    }
                }
            }
        }
    }

    class Differ : DiffUtil.ItemCallback<MyDrawCourse>() {
        override fun areItemsTheSame(
            oldItem: MyDrawCourse,
            newItem: MyDrawCourse,
        ): Boolean {
            return oldItem.courseId == newItem.courseId
        }

        override fun areContentsTheSame(
            oldItem: MyDrawCourse,
            newItem: MyDrawCourse,
        ): Boolean {
            return oldItem == newItem
        }

    }

}
