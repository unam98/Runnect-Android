package com.runnect.runnect.presentation.discover.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.runnect.runnect.data.dto.DiscoverPromotionItemDTO
import com.runnect.runnect.databinding.ItemDiscoverPromotionBinding

class DiscoverPromotionAdapter:ListAdapter<DiscoverPromotionItemDTO,DiscoverPromotionAdapter.DiscoverPromotionViewHolder>(DiscoverPromotionDiff) {
    lateinit var binding:ItemDiscoverPromotionBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverPromotionViewHolder {
        binding = ItemDiscoverPromotionBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DiscoverPromotionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscoverPromotionViewHolder, position: Int) {
        holder.onBind(getItem(position% FRAME_NUM))
    }
    override fun getItemCount(): Int = PAGE_NUM
    inner class DiscoverPromotionViewHolder(private val binding:ItemDiscoverPromotionBinding):RecyclerView.ViewHolder(binding.root){
        fun onBind(item:DiscoverPromotionItemDTO){
            binding.ivItemDiscoverPromotionBanner.setImageResource(item.img)
        }
    }
    companion object{
        const val FRAME_NUM = 3
        const val PAGE_NUM = 900
    }
}

object DiscoverPromotionDiff : DiffUtil.ItemCallback<DiscoverPromotionItemDTO>() {
    override fun areItemsTheSame(oldItem: DiscoverPromotionItemDTO, newItem: DiscoverPromotionItemDTO): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DiscoverPromotionItemDTO, newItem: DiscoverPromotionItemDTO): Boolean {
        return oldItem == newItem
    }
}