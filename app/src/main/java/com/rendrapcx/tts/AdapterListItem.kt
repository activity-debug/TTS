package com.rendrapcx.tts

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.databinding.ActivityCreatorBinding
import com.rendrapcx.tts.databinding.CreatorPartsBinding
import com.rendrapcx.tts.databinding.CustomDialog1Binding
import com.rendrapcx.tts.databinding.DialogInputSoalBinding
import com.rendrapcx.tts.databinding.ListItemQuestionBinding

class AdapterListItem(
    private val clickListener: ((Data.Questions) -> Unit)? = null
) : RecyclerView.Adapter<AdapterListItem.ViewHolder>() {
    private var list = mutableListOf<Data.Questions>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(list: MutableList<Data.Questions>) {
        this.list = list
        this.notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ListItemQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(questions: Data.Questions, clickListener: ((Data.Questions) -> Unit)? = null) {
            binding.root.setOnClickListener() {
                clickListener?.invoke(questions)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemQuestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val questions = list[position]
        holder.binding.tvListItemLevel.text = questions.levelId
        holder.binding.tvListItemId.text = questions.id
        holder.binding.tvListItemNo.text = questions.no
        holder.binding.tvListItemDirection.text = questions.direction
        holder.binding.tvListItemAsk.text = questions.ask
        holder.binding.tvListItemAnswer.text = questions.answer
        var s = ""
        questions.members?.map { it }!!.forEach(){
            s += "["+it.charAt + ":" + it.char + "],"
        }
        holder.binding.tvListItemParts.text = s
        Data.memberList.filter { it.soalId == questions.id }
            .map { it }.forEach() {
                holder.binding.tvListItemParts.text =
                    holder.binding.tvListItemParts.text.toString() + it.value + ", "
            }
        holder.bind(questions, clickListener)
    }
}