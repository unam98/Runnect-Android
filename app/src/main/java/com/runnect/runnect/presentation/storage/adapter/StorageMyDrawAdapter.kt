package com.runnect.runnect.presentation.storage.adapter

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.Selection
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.runnect.runnect.R
import com.runnect.runnect.data.model.ResponseGetCourseDto
import com.runnect.runnect.databinding.ItemStorageMyDrawBinding
import com.runnect.runnect.util.callback.OnMyDrawClick


class StorageMyDrawAdapter(val myDrawClickListener: OnMyDrawClick) :
    ListAdapter<ResponseGetCourseDto.Data.Course, StorageMyDrawAdapter.ItemViewHolder>(Differ()) {


    private lateinit var selectionTracker: SelectionTracker<Long>

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class ItemViewHolder(val binding: ItemStorageMyDrawBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun getSelectionKey(): Long = itemId
            }


        fun bind(courseList: ResponseGetCourseDto.Data.Course) {
            binding.storageItem = courseList
        } //여기는 dto를 구독해서 xml 단에서 바로 ui 바인딩을 할 수 있게끔 하려는 목적인듯

        // 여기는 item이 터치가 됐을 때 동작을 만들어주기 위함.
        // 이 listener를 인터페이스로 만들고 추상메서드 override할 때 뷰모델에 세팅해주고
        // 그 뷰모델에 있는 liveData를 xml에서 구독하게 하면 굳이 이렇게 함수 2개로 안 나눠도 될듯
        fun bindViews(data: ResponseGetCourseDto.Data.Course) {
            binding.root.setOnClickListener {
                myDrawClickListener.selectItem(data)
            }
        }

    }

    fun setSelectionTracker(selectionTracker: SelectionTracker<Long>) {
        this.selectionTracker = selectionTracker //입력받은 걸 멤버변수 값에 할당, 즉 입력을 안 받으면 초기화 안 했다고 앱이 죽음
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemStorageMyDrawBinding.inflate(inflater)

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
        holder.bindViews(currentList[position])

        with(holder) {
            binding.setVariable(BR.storageItem, getItem(position))
            binding.root.setOnClickListener {
                selectionTracker.select(position.toLong()) //이게 select을 실행시킴
            }
            binding.selected = selectionTracker.isSelected(position.toLong())
        }

    }

    //선택된 걸 별도의 list에다 저장하려고 애썼는데 그럴 필요가 없었음. 'selectionTracker.selection' 이 부분이 해당 역할을 이미 수행하고 있었음.
    fun removeItem(selection: Selection<Long>) {
        val itemList = mutableListOf<ResponseGetCourseDto.Data.Course>()
        itemList.addAll(currentList)
        val selectedList = selection.toMutableList()

        for(i in selectedList){
            itemList.removeAt(selectedList[i.toInt()].toInt())
        }
        submitList(itemList)
    }


    class Differ : DiffUtil.ItemCallback<ResponseGetCourseDto.Data.Course>() {
        override fun areItemsTheSame(
            oldItem: ResponseGetCourseDto.Data.Course,
            newItem: ResponseGetCourseDto.Data.Course,
        ): Boolean {
            return oldItem.createdAt == newItem.createdAt
        }

        override fun areContentsTheSame(
            oldItem: ResponseGetCourseDto.Data.Course,
            newItem: ResponseGetCourseDto.Data.Course,
        ): Boolean {
            return oldItem == newItem
        }

    }

}
