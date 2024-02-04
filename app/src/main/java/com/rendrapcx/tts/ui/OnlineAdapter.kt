package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.R
import com.rendrapcx.tts.constant.Const.Companion.isEditor
import com.rendrapcx.tts.databinding.DialogMenuItemOnlineBinding
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.model.Data.Companion.listLevel

class OnlineAdapter : RecyclerView.Adapter<OnlineAdapter.OnlineViewHolder>() {
    private var listOnlineLevel = mutableListOf<Data.OnlineLevelList>()
    private var onClickDownload: ((Data.OnlineLevelList) -> Unit)? = null
    private var onClickRemove: ((Data.OnlineLevelList) -> Unit)? = null

    fun setOnClickDownload(callback: (Data.OnlineLevelList) -> Unit) {
        this.onClickDownload = callback
    }

    fun setOnClickRemove(callback: (Data.OnlineLevelList) -> Unit) {
        this.onClickRemove = callback
    }

    fun setListItem(list: MutableList<Data.OnlineLevelList>) {
        this.listOnlineLevel = list
        this.notifyItemChanged(list.size)
    }

    class OnlineViewHolder(val binding: DialogMenuItemOnlineBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineViewHolder {
        return OnlineViewHolder(
            DialogMenuItemOnlineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listOnlineLevel.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OnlineViewHolder, position: Int) {
        val item = listOnlineLevel[position]
        val listId = listLevel.map { it.id }

        val downloaded : Boolean = if (listId.contains(item.id)) {
            holder.binding.btnDownload.setImageResource(R.drawable.square_check_solid)
            true
        } else {
            holder.binding.btnDownload.setImageResource(R.drawable.cloud_arrow_down_solid)
            false
        }

        holder.binding.textInfo.text = "Level: ${item.id}\n" +
                "Kategori: ${item.category}\n" +
                "Editor: ${item.editor}"

        holder.binding.btnDownload.setOnClickListener() {
            if (!downloaded) {
                onClickDownload?.invoke(item)
            }
        }

        if (isEditor) holder.binding.btnHapusItemOnline.visibility = View.VISIBLE
        else holder.binding.btnHapusItemOnline.visibility = View.INVISIBLE

        holder.binding.btnHapusItemOnline.setOnClickListener() {
                onClickRemove?.invoke(item)
        }
    }
}