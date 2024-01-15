package com.rendrapcx.tts.ui.dlg

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import com.rendrapcx.tts.constant.Const
import com.rendrapcx.tts.databinding.DialogMenuPlayBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
import com.rendrapcx.tts.ui.BoardActivity
import com.rendrapcx.tts.ui.PlayMenuAdapter
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.R)
fun Context.playMenu(
    context: Context,
    lifecycle: Lifecycle
) {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val binding = DialogMenuPlayBinding.inflate(inflater)
    val builder = AlertDialog.Builder(context).setView(binding.root)
    val dialog = builder.create()

    extracted(dialog)

    dialog.window?.attributes?.gravity = Gravity.BOTTOM
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(true)

    var state = 0

    fun listByCategory(): MutableList<Data.Level> {
        val new = mutableListOf<Data.Level>()
        val arr = arrayListOf<String>()
        Data.listLevel.forEach() {
            for (i in 0 until Data.listLevel.size) {
                if (!arr.contains(it.category)) arr.add(it.category)
            }
        }

        for (i in 0 until arr.size) {
            new.add(Data.listLevel.first() { it.category == arr[i] })
        }
        return new
    }

    fun changeListFiltered(category: String) {
        binding.apply {

            val filteredList = Data.listLevel.filter { it.category == category }.toMutableList()
            val adapter = PlayMenuAdapter()
            myRecView.layoutManager = GridLayoutManager(context, 2)
            myRecView.adapter = adapter
            adapter.setListItem(filteredList)

            adapter.setOnClickView {
                lifecycle.coroutineScope.launch {
                    Const.boardSet = Const.BoardSet.PLAY_USER
                    Const.currentLevel = it.id

                    Data.listLevel =
                        DB.getInstance(applicationContext).level().getLevel(Const.currentLevel)
                    Data.listQuestion =
                        DB.getInstance(applicationContext).question()
                            .getQuestion(Const.currentLevel)
                    Data.listPartial = DB.getInstance(applicationContext).partial().getPartial(
                        Const.currentLevel
                    )

                    val i = Intent(this@playMenu, BoardActivity::class.java)
                    startActivity(i)
                    dialog.dismiss()
                }

            }
        }

    }


    fun showListByCategory() {
        binding.apply {
            val adapter = PlayMenuAdapter()
            myRecView.layoutManager = GridLayoutManager(context, 2)
            myRecView.adapter = adapter
            adapter.setListItem(listByCategory())


            adapter.setOnClickView {
                changeListFiltered(it.category)
            }
        }
    }

    showListByCategory()

    dialog.show()
}

@RequiresApi(Build.VERSION_CODES.R)
private fun extracted(dialog: AlertDialog) {
    val window = dialog.window

    val windowInsetsController =
        WindowCompat.getInsetsController(window!!, window.decorView)
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
        if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
            || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
        ) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        view.onApplyWindowInsets(windowInsets)
    }
}