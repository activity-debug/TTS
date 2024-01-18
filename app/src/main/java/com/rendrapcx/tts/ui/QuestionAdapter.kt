package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.databinding.QuestionListItemBinding
import com.rendrapcx.tts.model.Data
import java.util.Locale

class QuestionAdapter(
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {
    private var listLevel = mutableListOf<Data.Level>()
    private var onClickView: ((Data.Level) -> Unit)? = null
    private var onClickEdit: ((Data.Level) -> Unit)? = null
    private var onClickDelete: ((Data.Level) -> Unit)? = null
    private var onClickShare: ((Data.Level) -> Unit)? = null
    private var onClickUpload: ((Data.Level) -> Unit)? = null

    fun setListItem(level: MutableList<Data.Level>) {
        this.listLevel = level
        this.notifyItemChanged(level.size)
    }

    fun setOnClickView(callback: (Data.Level) -> Unit ){
        this.onClickView = callback
    }

    fun setOnClickEdit(callback: (Data.Level) -> Unit ){
        this.onClickEdit = callback
    }

    fun setOnClickDelete(callback: (Data.Level) -> Unit ){
        this.onClickDelete = callback
    }

    fun setOnClickShare(callback: (Data.Level) -> Unit ){
        this.onClickShare = callback
    }

    fun setOnClickUpload(callback: (Data.Level) -> Unit ){
        this.onClickUpload = callback
    }

    class QuestionViewHolder(val binding: QuestionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        return QuestionViewHolder(
            QuestionListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listLevel.size
    }

    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val level = listLevel[position]
        holder.binding.tvItemId.text = level.id
        holder.binding.tvItemCategory.text = level.category
        holder.binding.tvItemTitle.text = level.title
        holder.binding.tvItemCreator.text = level.userId

        holder.binding.root.setOnClickListener{
            onClickView?.invoke(level)
        }

        holder.binding.btnItemEdit.setOnClickListener(){
            onClickEdit?.invoke(level)
        }

        holder.binding.btnItemDelete.setOnClickListener(){
            onClickDelete?.invoke(level)
        }

        holder.binding.btnCloudUp.setOnClickListener(){
            onClickUpload?.invoke(level)
        }

        holder.binding.btnShareAsQR.setOnClickListener(){
            onClickShare?.invoke(level)
        }
    }

}