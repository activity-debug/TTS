package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.net.http.UrlRequest.Status
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.databinding.QuestionListItemBinding
import com.rendrapcx.tts.model.Data
import java.util.Locale

class QuestionAdapter(
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {
    private var listLevel = mutableListOf<Data.Level>()
    private var onClickView: ((Data.Level) -> Unit)? = null
    private var onClickEdit: ((Data.Level) -> Unit)? = null
    private var onClickDelete: ((Data.Level) -> Unit)? = null
    private var onClickStatus: ((Data.Level) -> Unit)? = null
    private var onClickUpload: ((Data.Level) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setListItem(level: MutableList<Data.Level>) {
        this.listLevel = level
        this.notifyDataSetChanged()
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

    fun setOnClickStatus(callback: (Data.Level) -> Unit ){
        this.onClickStatus = callback
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
        holder.binding.tvItemId.text = "${position + 1} - ${level.id}"
        holder.binding.tvItemCategory.text = level.category
        holder.binding.tvItemTitle.text = level.title
        holder.binding.tvItemCreator.text = level.userId
        holder.binding.swPublish.text = level.status.name

        holder.binding.swPublish.isChecked = holder.binding.swPublish.text == Const.FilterStatus.POST.name

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

        holder.binding.swPublish.setOnClickListener(){
            onClickStatus?.invoke(level)
        }

    }

}