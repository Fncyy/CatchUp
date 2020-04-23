package com.example.catchup.shared.network

import java.io.IOException

@Throws(InterruptedException::class, IOException::class)
fun isConnected(): Boolean {
    val command = "ping -c 1 google.com"
    return Runtime.getRuntime().exec(command).waitFor() == 0
}