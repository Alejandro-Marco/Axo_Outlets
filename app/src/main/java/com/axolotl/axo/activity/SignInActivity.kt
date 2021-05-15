package com.axolotl.axo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.axolotl.axo.R
import com.axolotl.axo.model.User
import com.axolotl.axo.utility.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    // firebase
    private lateinit var firebaseAuth: FirebaseAuth             // authentication
    private val firestoreDB = Firebase.firestore

    companion object{
        const val TAG = "SignInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // initialize firebase variables
        firebaseAuth = Firebase.auth

        layoutGotoSignUp.setOnClickListener {
            launchActivity<SignUpActivity> {

            }
        }

        btnSignIn.setOnClickListener {
            hideKeyboard()
            val email = etSignInEmail.text.toString()
            val password = etSignInPassword.text.toString()
            signInUser(email, password)
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if(currentUser != null){
            showLoading()
            etSignInEmail.setText(currentUser.email.toString())
            etSignInPassword.setText("**********")
            launchMain(currentUser.email.toString())
        }
        else{
            hideLoading()
        }
    }

    private fun signInUser(email: String, password: String){
        showLoading()
        if (!isValidEmail(email)){
            with(etSignInEmail){
                this.error = "Invalid Email"
                this.requestFocus()
            }
            hideLoading()
            return
        }
        if (!isValidPassword(password)){
            with(etSignInPassword){
                this.error = "Invalid Password"
                this.requestFocus()
            }
            hideLoading()
            return
        }
        showToast("Signing In")
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    showToast("Sign In Successful")
                    launchMain(email)
//                    val user = firebaseAuth.currentUser
//                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    showToast("Invalid Email/Password")
                    hideLoading()
                }
            }
    }

    private fun launchMain(email: String){
        firestoreDB.collection("Users")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    launchActivity<MainActivity> {
                        putExtra("EMAIL", user?.email)
                        putExtra("USERNAME", user?.name)
                    }
                } else {
                    Log.d(TAG, "No such document")
                    hideLoading()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
                hideLoading()
            }
    }

    private fun showLoading(){
        loadingSignIn.isVisible = true
        btnSignIn.isEnabled = false
        layoutGotoSignUp.isEnabled = false
    }

    private fun hideLoading(){
        loadingSignIn.isVisible = false
        btnSignIn.isEnabled = true
        layoutGotoSignUp.isEnabled = true
    }
}