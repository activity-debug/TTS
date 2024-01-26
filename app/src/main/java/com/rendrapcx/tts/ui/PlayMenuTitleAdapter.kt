package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.databinding.DialogMenuItemTitleBinding
import com.rendrapcx.tts.helper.Helper
import com.rendrapcx.tts.model.Data

class PlayMenuTitleAdapter : RecyclerView.Adapter<PlayMenuTitleAdapter.PlayViewHolder>() {
    private var listLevel = mutableListOf<Data.Level>()
    private var onClickView: ((Data.Level) -> Unit)? = null

    fun setListItem(level: MutableList<Data.Level>) {
        this.listLevel = level
        this.notifyItemChanged(level.size)
    }

    fun setOnClickView(callback: (Data.Level) -> Unit) {
        this.onClickView = callback
    }

    class PlayViewHolder(val binding: DialogMenuItemTitleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayViewHolder {
        return PlayViewHolder(
            DialogMenuItemTitleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listLevel.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlayViewHolder, position: Int) {
        val level = listLevel[position]

        val pos = Helper().format(position+1)
//        holder.binding.tvTitleLevel.text = pos + " " + Const.strRed
        holder.binding.tvTitleLevel.text = pos

        holder.binding.root.setOnClickListener {
            onClickView?.invoke(level)
        }

    }
}