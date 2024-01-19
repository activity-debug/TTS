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

    @SuppressLint("NotifyDataSetChanged")
    fun setListItem(tebakKata: MutableList<TebakKata>) {
        this.listTebakKata = tebakKata
        this.notifyDataSetChanged()
    }

    fun setOnClickView(callback: (TebakKata) -> Unit) {
        this.onClickView = callback
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

        val answer = item.answer.trimStart().trimEnd().trim()
        holder.binding.tvAnswerTbk.text = answer

        /* TASK: TRY CATCH DULU IMAGEURLNYA KALO ERROR DEFAULT AJA*/
        if (item.imageUrl.isEmpty()) holder.binding.imgItemTbk.setImageResource(R.drawable.terka_box)
        else holder.binding.imgItemTbk.setImageURI(item.imageUrl.toUri())

//        holder.binding.root.setOnClickListener {
//            onClickView?.invoke(item)
//        }

    }

}