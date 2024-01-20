package com.rendrapcx.tts.helper

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class Check {
    @SuppressLint("ServiceCast")
    fun isConnected(context: Context): Boolean {
        val conMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = conMgr.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

}