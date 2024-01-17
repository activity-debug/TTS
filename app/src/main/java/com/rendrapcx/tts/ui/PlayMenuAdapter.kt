package com.rendrapcx.tts.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.databinding.DialogMenuItemBinding
import com.rendrapcx.tts.model.Data

class PlayMenuAdapter : RecyclerView.Adapter<PlayMenuAdapter.PlayViewHolder>() {
    private var listLevel = mutableListOf<Data.Level>()
    private var onClickView: ((Data.Level) -> Unit)? = null

    fun setListItem(level: MutableList<Data.Level>) {
        this.listLevel = level
        this.notifyItemChanged(level.size)
    }

    fun setOnClickView(callback: (Data.Level) -> Unit) {
        this.onClickView = callback
    }

    class PlayViewHolder(val binding: DialogMenuItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayViewHolder {
        return PlayViewHolder(
            DialogMenuItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listLevel.size
    }

    override fun onBindViewHolder(holder: PlayViewHolder, position: Int) {
        val level = listLevel[position]

        holder.binding.tvMenuLevel.text = level.category

        holder.binding.root.setOnClickListener {
            onClickView?.invoke(level)
        }


    }

}