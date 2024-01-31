package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.databinding.DialogMenuItemOnlineBinding
import com.rendrapcx.tts.model.Data

class OnlineAdapter:RecyclerView.Adapter<OnlineAdapter.OnlineViewHolder>() {
    private var listOnlineLevel  = mutableListOf<Data.OnlineLevel>()
    private var onClickDownload: ((Data.OnlineLevel) -> Unit)? = null

    fun setOnClickDownload(callback: (Data.OnlineLevel) -> Unit) {
        this.onClickDownload = callback
    }

    fun setListItem(list: MutableList<Data.OnlineLevel>) {
        this.listOnlineLevel = list
        this.notifyItemChanged(list.size)
    }
    class OnlineViewHolder(val binding: DialogMenuItemOnlineBinding):RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineViewHolder {
        return OnlineViewHolder(DialogMenuItemOnlineBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return listOnlineLevel.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OnlineViewHolder, position: Int) {
        val item = listOnlineLevel[position]

        holder.binding.textInfo.text = "Level: ${item.id}\n" +
                "Kategori: ${item.category}"

        //if onlinelevel in offlinelevel textInfo hide

        holder.binding.btnDownload.setOnClickListener(){
            onClickDownload?.invoke(item)
        }

    }
}