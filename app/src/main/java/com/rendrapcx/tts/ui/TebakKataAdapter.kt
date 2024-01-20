package com.rendrapcx.tts.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.rendrapcx.tts.R
import com.rendrapcx.tts.databinding.TebakKataListItemBinding
import com.rendrapcx.tts.model.Data.*

class TebakKataAdapter(
) : RecyclerView.Adapter<TebakKataAdapter.TebakKataViewHolder>() {
    private var listTebakKata = mutableListOf<TebakKata>()
    private var onClickView: ((TebakKata) -> Unit)? = null
    private var onClickShare: ((TebakKata) -> Unit)? = null
    private var onClickEdit: ((TebakKata) -> Unit)? = null
    private var onClickDelete: ((TebakKata) -> Unit)? = null
    private var onClickUpload: ((TebakKata) -> Unit)? = null



    @SuppressLint("NotifyDataSetChanged")
    fun setListItem(tebakKata: MutableList<TebakKata>) {
        this.listTebakKata = tebakKata
        this.notifyDataSetChanged()
    }

    fun setOnClickView(callback: (TebakKata) -> Unit) {
        this.onClickView = callback
    }

    fun setOnClickShare(callback: (TebakKata) -> Unit) {
        this.onClickShare = callback
    }

    fun setOnClickDelete(callback: (TebakKata) -> Unit) {
        this.onClickDelete = callback
    }

    fun setOnClickEdit(callback: (TebakKata) -> Unit) {
        this.onClickEdit = callback
    }

    fun setOnClickUpload(callback: (TebakKata) -> Unit) {
        this.onClickUpload = callback
    }

    class TebakKataViewHolder(val binding: TebakKataListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TebakKataViewHolder {
        return TebakKataViewHolder(
            TebakKataListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return listTebakKata.size
    }

    override fun onBindViewHolder(holder: TebakKataViewHolder, position: Int) {
        val item = listTebakKata[position]



        val answer = previewAnswer(item.answer)
        holder.binding.tvAnswerTbk.text = answer

        /* TASK: TRY CATCH DULU IMAGEURLNYA KALO ERROR DEFAULT AJA*/
        if (item.imageUrl.isEmpty()) holder.binding.imgItemTbk.setImageResource(R.drawable.terka_box)
        else holder.binding.imgItemTbk.setImageURI(item.imageUrl.toUri())

        holder.binding.root.setOnClickListener {
            onClickView?.invoke(item)
        }

        holder.binding.btnCloudUpTbk.setOnClickListener {
            onClickUpload?.invoke(item)
        }

        holder.binding.btnItemDeleteTbk.setOnClickListener(){
            onClickDelete?.invoke(item)
        }

        holder.binding.btnItemEditTbk.setOnClickListener(){
            onClickEdit?.invoke(item)
        }

        holder.binding.btnShareAsQRTbk.setOnClickListener(){
            onClickShare?.invoke(item)
        }

    }

    private fun previewAnswer(string: String) : String {
        val xLen = 15
        val yLen = 3
        val size = 45
        val rightMargin = arrayListOf<Int>()
        for (i in 0 until yLen) {
            rightMargin.add((i * xLen) - 1)
        }
        var pre = ""
        for (i in 0 until size) {
            if (i in rightMargin) {
                if (i >= string.length) {
                    pre += "_"
                    continue
                } else if (string[i] == ' ') pre += "_" + "\n"
                else pre += string[i] + "\n"
            } else {
                if (i >= string.length) {
                    pre += "_"
                    continue
                } else if (string[i] == ' ') pre += "_"
                else pre += string[i]
            }
        }
        return pre
    }

}