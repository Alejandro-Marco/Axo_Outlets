package com.axolotl.axo.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.IntRange

private lateinit var toast: Toast
private lateinit var toastHandler: android.os.Handler

inline fun <reified T : Any> Context.launchActivity(
    noinline modify: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.modify()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Activity.showToast(message: String?, @IntRange(from = 100, to = 4000) duration: Int = 2000) {
    try {
        toast.cancel()
        try {
            toastHandler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {

        }
    } catch (e: Exception) {
    } finally {
        if (message.isNullOrBlank()) {
            try {
                toast.cancel()
            } catch (e: Exception) {
            }
        } else {
            toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
            toast.show()
            toastHandler = android.os.Handler(Looper.getMainLooper())
            toastHandler.postDelayed({
                try {
                    toast.cancel()
                } catch (e: Exception) {
                }
            }, duration.toLong())
        }
    }
}

fun isValidEmail(email: String): Boolean {
    if (email.isNullOrBlank()) {
        return false
    }
    val requiredSubString = arrayOf("@", ".com")
    if (email.isBlank()) {
        return false
    }
    requiredSubString.forEach {
        if (!email.contains(it)) return false
    }
    return true
}

fun isValidUsername(username: String): Boolean {
    val invalidChar = ".-[]<>/\\".toCharArray()
    if (username.contains("\n")) return false
    if (username.isBlank()) return false
    invalidChar.forEach {
        if (username.contains(it)) return false
    }
    return true
}

fun isValidPassword(password: String): Boolean {
    if (password.isBlank()) {
        return false
    }
    return true
}

fun isValidPinName(inputString: String): Boolean {
    val invalidChar = ".-[]<>/\\".toCharArray()
    if (inputString.length > 15)
        return false
    if (inputString.contains("\n"))
        return false
    if (inputString.isBlank())
        return false
    for (char in invalidChar) {
        if (inputString.contains(char)) {
            return false
        }
    }
    return true
}

fun isValidControllerName(inputString: String): Boolean {
    val invalidChar = ".-[]<>/\\".toCharArray()
    if (inputString.length > 20)
        return false
    if (inputString.contains("\n"))
        return false
    if (inputString.isBlank())
        return false
    for (char in invalidChar) {
        if (inputString.contains(char)) {
            return false
        }
    }
    return true
}

fun sanitizePinName(inputString: String): String {
    // does exactly what it looks like
    return inputString.replace(" ", "_")
}

fun sanitizeControllerName(inputString: String): String {
    // does exactly what it looks like
    return inputString.replace(" ", "_")
}

fun restorePinName(inputString: String): String {
    // does exactly what it looks like
    return inputString.replace("_", " ")
}

fun restoreControllerName(inputString: String): String {
    // does exactly what it looks like
    return inputString.replace("_", " ")
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
