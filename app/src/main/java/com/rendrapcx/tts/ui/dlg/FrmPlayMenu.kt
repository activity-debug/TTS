package com.rendrapcx.tts.ui.dlg

import android.content.Context
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
import com.rendrapcx.tts.databinding.DialogMenuPlayBinding
import com.rendrapcx.tts.model.DB
import com.rendrapcx.tts.model.Data
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

    dialog.window?.attributes?.gravity = Gravity.BOTTOM
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(true)

    lifecycle.coroutineScope.launch {
        Data.listLevel =
            DB.getInstance(applicationContext).level().getAllLevel().ifEmpty { return@launch }
    }

    val adapter = PlayMenuAdapter()
    binding.apply {
        myRecView.layoutManager = GridLayoutManager(context, 2)
        myRecView.adapter = adapter

        adapter.setListItem(Data.listLevel)
    }

    dialog.show()
}