package com.example.catchup.network

import android.content.Context
import android.net.ConnectivityManager
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException


@Throws(InterruptedException::class, IOException::class)
fun isConnected(): Boolean {
    val command = "ping -c 1 google.com"
    return Runtime.getRuntime().exec(command).waitFor() == 0
}