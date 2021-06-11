package com.axolotl.axo.utility

import android.os.Looper
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance


fun pseudoTrace(traceName: String, traceDuration: Long) {
    val initialHandler = android.os.Handler(Looper.getMainLooper())
    initialHandler.postDelayed({
        val traceHandler = android.os.Handler(Looper.getMainLooper())
        val trace = Firebase.performance.newTrace(traceName)
        trace.start()
        traceHandler.postDelayed({
            trace.stop()
        }, traceDuration)
    }, 0)
}