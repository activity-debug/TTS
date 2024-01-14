package com.rendrapcx.tts.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.constant.GameState
import com.rendrapcx.tts.databinding.ActivityBoardBinding
import com.rendrapcx.tts.databinding.QuestionListItemBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.viewmodel.BoardViewModel

class QuestionAdapter(
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {
    private var listLevel = mutableListOf<Data.Level>()
    private var onClickView: ((Data.Level) -> Unit)? = null
    private var onClickEdit: ((Data.Level) -> Unit)? = null
    private var onClickDelete: ((Data.Level) -> Unit)? = null

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


    class QuestionViewHolder(val binding: QuestionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
//        private var onClickView: ((Data.Level) -> Unit)? = null
//        fun setOnClickView(callback: (Data.Level) -> Unit ){
//            this.onClickView = callback
//        }
    }

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

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val level = listLevel[position]
        holder.binding.tvItemId.text = level.id
        holder.binding.tvItemCategory.text = level.category
        holder.binding.tvItemDimension.text = level.dimension

        holder.binding.root.setOnClickListener{
            onClickView?.invoke(level)
        }

        holder.binding.btnItemEdit.setOnClickListener(){
            onClickEdit?.invoke(level)
        }

        holder.binding.btnItemDelete.setOnClickListener(){
            onClickDelete?.invoke(level)
        }
    }

}