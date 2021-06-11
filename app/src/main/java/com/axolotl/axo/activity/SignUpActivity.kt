package com.axolotl.axo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.axolotl.axo.R
import com.axolotl.axo.model.User
import com.axolotl.axo.utility.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    // firebase
    private lateinit var firebaseAuth: FirebaseAuth             // authentication
    private val firestoreDB = Firebase.firestore

    companion object {
        const val TAG = "SignUpActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // initialize firebase variables
        firebaseAuth = Firebase.auth

        layoutGotoSignIn.setOnClickListener {
            launchActivity<SignInActivity> {

            }
        }

        btnSignUp.setOnClickListener {
            hideKeyboard()
            val email = etSignUpEmail.text.toString().toLowerCase()
            val name = etSignUpUsername.text.toString()
            val password = etSignUpPassword.text.toString()
            val confirmPassword = etSignUpConfirmPassword.text.toString()
            btnSignUp.isEnabled = false
            layoutGotoSignIn.isEnabled = false
            if (signUpUser(email, name, password, confirmPassword)) {
                showToast("Creating Account")
            } else {
                btnSignUp.isEnabled = true
                layoutGotoSignIn.isEnabled = true
            }
        }
    }

    private fun signUpUser(
        email: String,
        name: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (!isValidEmail(email)) {
            with(etSignUpEmail) {
                this.error = "Invalid Email"
                this.requestFocus()
            }
            return false
        }
        if (!isValidUsername(name)) {
            with(etSignUpUsername) {
                this.error = "Invalid Username"
                this.requestFocus()
            }
            return false
        }
        if (!isValidPassword(password)) {
            with(etSignUpPassword) {
                this.error = "Invalid Password"
                this.requestFocus()
            }
            return false
        }
        if (password != confirmPassword) {
            with(etSignUpConfirmPassword) {
                this.error = "Passwords do not match"
                this.requestFocus()
            }
            return false
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(SignInActivity.TAG, "createUserWithEmail:success")
                    // create a user in firestore
                    val user = User(email, password, name)
                    firestoreDB.collection("Users")
                        .document(email)
                        .set(user)
                        .addOnSuccessListener {
                            launchActivity<MainActivity> {
                                putExtra("USERNAME", name)
                                putExtra("EMAIL", email)
                            }
                        }
                        .addOnFailureListener {
                            showToast("Error")
                            Log.w(TAG, "Error adding document")
                            btnSignUp.isEnabled = true
                            layoutGotoSignIn.isEnabled = true
                        }

                } else {
                    showToast("Account Creation Failed")
                    btnSignUp.isEnabled = true
                    layoutGotoSignIn.isEnabled = true
                }
            }
        return true
    }
}