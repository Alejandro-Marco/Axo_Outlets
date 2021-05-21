package com.axolotl.axo.activity

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.axolotl.axo.R
import com.axolotl.axo.model.Controller
import com.axolotl.axo.model.EmptyController
import com.axolotl.axo.model.Pin
import com.axolotl.axo.model.User
import com.axolotl.axo.utility.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_account_info.*
import kotlinx.android.synthetic.main.fragment_controller_info.*
import kotlinx.android.synthetic.main.fragment_register_controller.*
import java.io.ByteArrayOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    // firebase
    private lateinit var firebaseAuth: FirebaseAuth             // authentication
    private val firestoreDB = Firebase.firestore
    private val firebaseRTDB = Firebase.database
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val firebaseStorageImage = firebaseStorage.getReference("images")

    // local variables
    private lateinit var accountImageBitmap: Bitmap
    private lateinit var email: String
    private lateinit var username: String
    private var tabViewMap = mutableMapOf<LinearLayout, LinearLayout>()
    private var activeControllerID: String = ""
    private lateinit var eventListener: ValueEventListener
    private lateinit var pinTextViewMap: Map<String, Map<String, TextView>>
    private lateinit var layoutPinMap: Map<String, LinearLayout>
    private lateinit var pinStateMap: Map<String, TextView>
    private lateinit var pinSwitchMap: Map<String, ImageView>
    private var loadingControllers = true
    private var userData: User? = null
    private lateinit var layoutActiveControllerPins: Map<String, LinearLayout>
//    private lateinit var controllerNameChangeListener: Array<ValueEventListener>


    // performance tracing
//    private val controllerSwitchTrace = Firebase.performance.newTrace("switch_controller")

    // -> Testing new implementations
    private var switchControllerTimeCounter: Long = 0
//    private lateinit var controllerListenerMap: Map<String, ValueEventListener>

    // design
    private val layoutIdleTabBackground = R.color.black_obsidian
    private val layoutActiveTabBackground = R.color.transparent
//    private val layoutActiveTabBackground = R.color.gray_dark
    private val switchOnImage = R.drawable.img_on
    private val switchOffImage = R.drawable.img_off

    companion object {
        const val TAG = "SignInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // firebase
        firebaseAuth = Firebase.auth                // authentication
        // initiate local values
        pinSwitchMap = mapOf(
                "pin_1" to imgSwitchPin1,
                "pin_2" to imgSwitchPin2,
                "pin_3" to imgSwitchPin3,
                "pin_4" to imgSwitchPin4,
                "pin_5" to imgSwitchPin5
        )
        pinStateMap = mapOf(
                "pin_1" to tvStatePin1,
                "pin_2" to tvStatePin2,
                "pin_3" to tvStatePin3,
                "pin_4" to tvStatePin4,
                "pin_5" to tvStatePin5
        )
        // used for updating pin layout
        pinTextViewMap = mapOf(
                "pin_1" to mapOf(
                        "name" to tvNamePin1,
                        "state" to tvStatePin1
                ),
                "pin_2" to mapOf(
                        "name" to tvNamePin2,
                        "state" to tvStatePin2
                ),
                "pin_3" to mapOf(
                        "name" to tvNamePin3,
                        "state" to tvStatePin3
                ),
                "pin_4" to mapOf(
                        "name" to tvNamePin4,
                        "state" to tvStatePin4
                ),
                "pin_5" to mapOf(
                        "name" to tvNamePin5,
                        "state" to tvStatePin5
                )
        )
        // used for creating pin layout
        layoutPinMap = mapOf(
                "pin_1" to layoutControllerPin1,
                "pin_2" to layoutControllerPin2,
                "pin_3" to layoutControllerPin3,
                "pin_4" to layoutControllerPin4,
                "pin_5" to layoutControllerPin5
        )
        tabViewMap = mutableMapOf(
                layoutControllerTabButton to layoutControllerInfoFragment,
                layoutRegisterTabButton to layoutRegisterControllerFragment
        )
        layoutActiveControllerPins = mapOf(
                "pin_1" to layoutActiveControllerPin1,
                "pin_2" to layoutActiveControllerPin2,
                "pin_3" to layoutActiveControllerPin3,
                "pin_4" to layoutActiveControllerPin4,
                "pin_5" to layoutActiveControllerPin5
        )
        // get extra values from previous activity
        email = intent.getStringExtra("EMAIL").toString()
        username = intent.getStringExtra("USERNAME").toString()
        // initialize
        tvUsername.text = username
//        for ((pinID, layout) in layoutPinMap){
//            layout.isVisible = false;
//        }
//        layoutActiveControllerInfo.isVisible = false
        hideKeyboard()
//        setVisibleTab(layoutControllerTabButton)


        loadInitialData()
        showToast(null)

        layoutSignOut.setOnClickListener {
            signOut()
        }

        layoutControllerTabButton.setOnClickListener {
            hideKeyboard()
            setVisibleTab(layoutControllerTabButton)
        }

        layoutRegisterTabButton.setOnClickListener {
            setVisibleTab(layoutRegisterTabButton)
        }

        btnRegisterController.setOnClickListener {
            val controllerID = etRegisterControllerID.text.toString()
            val controllerPassword = etRegisterControllerPassword.text.toString()
            registerController(controllerID, controllerPassword)
        }

        for ((pinID, pinSwitch) in pinSwitchMap) {
            pinSwitch.setOnClickListener {
                val pinState = pinStateMap[pinID]!!.text.toString().toLowerCase(Locale.ROOT)
                if (pinState == "off") {
                    changeState(pinID, "on")
                } else {
                    changeState(pinID, "off")
                }
            }
        }

        layoutRegisterControllerDropdown.setOnClickListener {
            toggleControllerListVisibility()
        }

        layoutActiveControllerName.setOnClickListener {
            renameControllerDialog(activeControllerID)
        }

        imgRemoveController.setOnClickListener {
            removeControllerDialog(activeControllerID)
        }

        for ((id, layout) in layoutActiveControllerPins) {
            layout.setOnClickListener {
                renameControllerPinDialog(id)
            }
        }
        layoutGotoAccountInfo.setOnClickListener {
            layoutControllerMain.isVisible = false
            layoutAccountInfo.isVisible = true
        }

        // account side
        layoutAccountReturn.setOnClickListener {
            layoutControllerMain.isVisible = true
            layoutAccountInfo.isVisible = false
        }

        layoutAccountEditEmail.setOnClickListener {
            changeAccountEmailDialog()
        }

        layoutAccountEditUsername.setOnClickListener {
            changeAccountUsernameDialog()
        }

        btnAccountChangePass.setOnClickListener {
            changeAccountPasswordDialog()
        }
        imgAccountImage.setOnClickListener {
            changeAccountImage()
        }
//            ImageViewCompat.setImageTintList(imgAccountImage, null)

        tvAccountRemoveImage.setOnClickListener {
            removeAccountImageDialog()
        }
    }


    // -> Testing new implementations
    private fun loadControllerListeners() {

    }

    private fun removeAccountImageDialog(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Remove Image?")
        val dialogLayout = inflater.inflate(R.layout.prompt_remove_controller, null)
        builder.setView(dialogLayout)
        builder.setIcon(R.drawable.img_account)
        builder.setPositiveButton("Remove") { dialog, which ->
            removeAccountImage()
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
//            showToast("cancelled")
        }
        builder.setOnCancelListener {

//            showToast("cancelled")
        }
        builder.show()
    }

    private fun removeAccountImage(){
        val imageRef = firebaseStorageImage.child("${userData!!.email}.jpg")
        imageRef.delete().addOnSuccessListener {
            // File deleted successfully
            ImageViewCompat.setImageTintList(imgAccountImage, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_radiant)))
            imgAccountImage.setImageResource(R.drawable.img_account)
            ImageViewCompat.setImageTintList(imgUserImage, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange_radiant)))
            imgUserImage.setImageResource(R.drawable.img_account)

        }.addOnFailureListener {
            Log.d("ERROR", "There was an error")
            // Uh-oh, an error occurred!
        }
    }

    private fun changeAccountImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            val accountImageURI = data.data!!
            accountImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, accountImageURI)
//            val bitmapDrawable = BitmapDrawable(bitmap)
            // remove the tint and the image
            ImageViewCompat.setImageTintList(imgAccountImage, null)
            imgAccountImage.setImageDrawable(null)
            ImageViewCompat.setImageTintList(imgUserImage, null)
            imgUserImage.setImageDrawable(null)
//            imgAccountImage.setImageDrawable(bitmapDrawable)
//            imgUserImage.setImageDrawable(bitmapDrawable)
            imgAccountImage.setImageBitmap(Bitmap.createScaledBitmap(accountImageBitmap, 128, 128, false))
            imgUserImage.setImageBitmap(Bitmap.createScaledBitmap(accountImageBitmap, 40, 40, false))
            Log.d("TAG", accountImageURI.toString())

            val fileRef = firebaseStorageImage.child(userData!!.email + ".jpg")
            // Upload the file
            val uploadTask = fileRef.putFile(accountImageURI!!)
            Log.d("UPLOAD", "DOING")
            val urlTask = uploadTask.continueWithTask { task ->
                if (task.isSuccessful) {
                    fileRef.downloadUrl
                } else {
                    task.exception?.let {
                        throw it
                    }
                }
            }
        }
    }

    private fun toggleControllerListVisibility() {
        if (layoutControllerList.isVisible) {
            layoutControllerList.isVisible = false
            val image = getDrawable(R.drawable.img_expand)
            tvControllerDropdown
                    .setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            image,
                            null
                    )
        } else {
            layoutControllerList.isVisible = true
            val image = getDrawable(R.drawable.img_collapse)
            tvControllerDropdown
                    .setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            image,
                            null
                    )
        }
    }

    private fun loadInitialData() {
        layoutMainBodyContainer.isVisible = false
        layoutLoadingController.isVisible = true
        // Load Image
        firebaseStorageImage.child("${email}.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener {imageData ->
            // Use the bytes to display the image
            Log.d("IMAGE", "Image was received")
            // remove the tint and the image
            ImageViewCompat.setImageTintList(imgAccountImage, null)
            imgAccountImage.setImageDrawable(null)
            ImageViewCompat.setImageTintList(imgUserImage, null)
            imgUserImage.setImageDrawable(null)
            accountImageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            val bitmapDrawable = BitmapDrawable(accountImageBitmap)
//            imgAccountImage.setImageDrawable(bitmapDrawable)
//            imgUserImage.setImageDrawable(bitmapDrawable)
            imgAccountImage.setImageBitmap(Bitmap.createScaledBitmap(accountImageBitmap, 128, 128, false))
            imgUserImage.setImageBitmap(Bitmap.createScaledBitmap(accountImageBitmap, 40, 40, false))

        }.addOnFailureListener {
            // Handle any errors
        }
        firestoreDB.collection("Users")
                .document(email)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
                    userData = user
                    // update the EmailDisplayed in Account info
                    email = userData!!.email
                    tvAccountInfoEmail.text = userData!!.email
                    tvAccountInfoUsername.text = userData!!.name
                    try {
                        registerController(user!!.activeController, user.controllers[user.activeController].toString(), booting = true)
                    } catch (e: Exception) {
                        layoutMainBodyContainer.isVisible = true
                        setVisibleTab(layoutControllerTabButton)
                        Log.d(TAG, "No active controllers")
                    }
                }

    }

    private fun changeAccountEmailDialog(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Change Email")
        val dialogLayout = inflater.inflate(R.layout.prompt_edit_email, null)
        val newEmailView = dialogLayout.findViewById<EditText>(R.id.etChangeAccountEmail)
        val passwordView = dialogLayout.findViewById<EditText>(R.id.etChangeAccountEmailPassword)
        builder.setView(dialogLayout)
        builder.setIcon(R.drawable.img_email)
        builder.setPositiveButton("Confirm"){ dialog, which ->
            // update the email in Firebase Auth
            val _user = Firebase.auth.currentUser
            val _userNewEmail = newEmailView.text.toString().toLowerCase()
            val password = passwordView.text.toString()
            if (_user.email.toString() == _userNewEmail){
                showToast("Same email")
                return@setPositiveButton
            }
            if (isValidEmail(_userNewEmail) && isValidPassword(password)){
                updatingAccount.isVisible = true
                firebaseAuth.signInWithEmailAndPassword(_user!!.email.toString(), password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(SignInActivity.TAG, "signInWithEmail:success")
                            _user!!.updateEmail(_userNewEmail)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // update the firestore
                                        // READ THIS
                                        // docs states it's impossible to rename the document

                                        // remove the account image

                                        val oldEmail = userData!!.email
                                        userData!!.email = _user.email.toString()
                                        firestoreDB.collection("Users")
                                            .document(_user.email.toString())
                                            .set(userData!!)
                                            .addOnSuccessListener {
                                                // update the account image
                                                firebaseStorageImage.child("${email}.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener {imageData ->
                                                    // Use the bytes to display the image
                                                    Log.d("IMAGE", "Image was received")

                                                    val fileRef = firebaseStorageImage.child(userData!!.email + ".jpg")
                                                    val uploadTask = fileRef.putBytes(imageData)
                                                    Log.d("UPLOAD", "DOING")
                                                    val urlTask = uploadTask.continueWithTask { task ->
                                                        if (task.isSuccessful) {
                                                            fileRef.downloadUrl
                                                            val imageRef = firebaseStorageImage.child("${oldEmail}.jpg")
                                                            imageRef.delete().addOnSuccessListener {

                                                            }
                                                        } else {
                                                            task.exception?.let {
                                                                throw it
                                                            }
                                                        }
                                                    }
                                                }
                                                    .addOnFailureListener{

                                                    }
                                            }
                                        firestoreDB.collection("Users")
                                            .document(oldEmail)
                                            .delete()
                                        Log.d(TAG, "User email address updated.")
                                        showToast("Email updated", 1000)
                                        tvAccountInfoEmail.text = userData!!.email
                                        updatingAccount.isVisible = false

                                    }
                                }
                                .addOnFailureListener {
//                                    showToast(it.toString())
                                    if (it.toString() == "com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account.")
                                        showToast("Email taken", 1000)
                                    Log.d("EMAIL CHANGE", it.toString())
                                    updatingAccount.isVisible = false
                                }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(SignInActivity.TAG, "signInWithEmail:failure", task.exception)
                            updatingAccount.isVisible = false
//                            showToast("Invalid Email/Password")
                        }
                    }
                    .addOnFailureListener {
                        showToast("Error please try again", 1000)
                        updatingAccount.isVisible = false
                    }
            }
            else{
                if (!isValidEmail(_userNewEmail))
                    showToast("Invalid Email", 1000)
                else
                    showToast("Invalid password", 2000)
            }
            hideKeyboard()
        }
        builder.setNegativeButton("Cancel"){ dialog, which ->
//            showToast("Cancelled", 1000)
            hideKeyboard()
        }
        builder.setOnCancelListener {
//            showToast("Alt", 1000)
            hideKeyboard()
        }
        builder.show()
    }

    private fun changeAccountUsernameDialog(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.prompt_edit_username, null)
        builder.setIcon(R.drawable.img_user)
        builder.setTitle("Change Username")
        builder.setView(dialogLayout)
        builder.setPositiveButton("Confirm"){ dialog, which ->
            updatingAccount.isVisible = true
            val _nameView = dialogLayout.findViewById<EditText>(R.id.etChangeAccountUsername)
            val _newName = _nameView.text.toString()
            showToast("Rename", 1000)
            firestoreDB.collection("Users")
                .document(userData!!.email)
                .update("name", _newName)
                .addOnSuccessListener {
                    tvAccountInfoUsername.text = _newName
                    tvUsername.text = _newName
                    updatingAccount.isVisible = false
                }
                .addOnFailureListener {
                    updatingAccount.isVisible = false
                }
        }
        builder.setNegativeButton("Cancel"){ dialog, which ->
//            showToast("Cancelled", 1000)
        }
        builder.setOnCancelListener {
//            showToast("Clicked out", 1000)
        }
        builder.show()
    }

    private fun changeAccountPasswordDialog(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Change Password")
        val dialogLayout = inflater.inflate(R.layout.prompt_edit_password, null)
        val newPasswordView = dialogLayout.findViewById<EditText>(R.id.etChangeAccountPassword)
        val passwordView = dialogLayout.findViewById<EditText>(R.id.etChangeAccountPasswordPassInput)
        builder.setView(dialogLayout)
        builder.setIcon(R.drawable.img_password)
        builder.setPositiveButton("Confirm"){ dialog, which ->
            // update the email in Firebase Auth
            val _currentPass = passwordView.text.toString()
            val _newPass = newPasswordView.text.toString()
            if (isValidPassword(_newPass) && isValidPassword(_currentPass)){
                updatingAccount.isVisible = true
                firebaseAuth.signInWithEmailAndPassword(userData!!.email.toString(), _currentPass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val _user = Firebase.auth.currentUser
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(SignInActivity.TAG, "signInWithEmail:success")
                            _user!!.updatePassword(_newPass)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // update the firestore
                                        // READ THIS
                                        // docs states it's impossible to rename the document
                                        userData!!.email = _user.email.toString()
                                        firestoreDB.collection("Users")
                                            .document(_user.email.toString())
                                            .update("password", _newPass)
                                        Log.d(TAG, "User password updated.")
                                        showToast("Password updated", 1000)
                                        updatingAccount.isVisible = false
                                    }
                                }
                                .addOnFailureListener {
//                                    showToast(it.toString())
                                    showToast("Error please try again")
                                    updatingAccount.isVisible = false
                                }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(SignInActivity.TAG, "signInWithEmail:failure", task.exception)
                            updatingAccount.isVisible = false
                            showToast("Invalid Email/Password")
                        }
                    }
                    .addOnFailureListener {
                        showToast("Error please try again", 1000)
                        updatingAccount.isVisible = false
                    }
            }
            else{
                showToast("Invalid password", 2000)
            }
            hideKeyboard()
        }
        builder.setNegativeButton("Cancel"){ dialog, which ->
//            showToast("Cancelled", 1000)
            hideKeyboard()
        }
        builder.setOnCancelListener {
//            showToast("Alt", 1000)
            hideKeyboard()
        }
        builder.show()
    }

    private fun removeControllerDialog(controllerID: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Remove Controller")
        val dialogLayout = inflater.inflate(R.layout.prompt_remove_controller, null)
        builder.setView(dialogLayout)
        builder.setIcon(R.drawable.img_plug)
        builder.setPositiveButton("Remove") { dialog, which ->
            var currentKeyIndex = userData!!.controllers.keys.toSortedSet().indexOf(controllerID)
//            showToast(userData!!.controllers.keys.toSortedSet().toString())
//            Log.d("Tag", currentKeyIndex.toString())
            if (currentKeyIndex == userData!!.controllers.keys.toSortedSet().size - 1) {
                currentKeyIndex -= 1
            }
            userData!!.controllers.remove(controllerID)
            if (userData!!.controllers.isEmpty()) {
//                showToast("BREAK")
                disconnectController(controllerID)
                firestoreDB.collection("Users")
                        .document(email)
                        .update("controllers", null)
                firestoreDB.collection("Users")
                        .document(email)
                        .update("activeController", null)
                activeControllerID = ""
                setVisibleTab(layoutControllerTabButton)
            } else {
                // Note that after removing the controller calling the registerController will automatically update firestore
                // Note to self, THIS IS FUCKING BULLSHIT. FIX THIS BITCH ASS CODE
                val _controllerID = userData!!.controllers.keys.toSortedSet().elementAt(currentKeyIndex)
                val password = userData!!.controllers[_controllerID].toString()
                registerController(_controllerID, password, booting = true)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
//            showToast("cancelled")
        }
        builder.setOnCancelListener {

//            showToast("cancelled")
        }
        builder.show()
    }

    private fun renameControllerPinDialog(pinDefaultName: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.prompt_edit_single_rename, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.tvChangeName)
        builder.setTitle("Rename Pin")
        builder.setView(dialogLayout)
        builder.setIcon(R.drawable.img_outlet)
        builder.setPositiveButton("OK") { dialogInterface, i ->
            Log.d(TAG, dialogInterface.toString() + i.toString())
            var newPinName = editText.text.toString()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            if (isValidPinName(newPinName)) {
                newPinName = sanitizePinName(newPinName)
                firebaseRTDB.getReference("Controllers")
                        .child(activeControllerID.toString()).child("pins").child(pinDefaultName).child("name").setValue(newPinName)
            } else {
                showToast("Invalid Name")
            }
            hideKeyboard()
        }
        builder.setNegativeButton("Cancel") { dialogInterface, i ->
            Log.d(TAG, dialogInterface.toString() + i.toString())
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
//            showToast("Rename Cancelled")
            hideKeyboard()
        }
        builder.setOnCancelListener {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
//            showToast("Rename Cancelled")
            hideKeyboard()
        }
        builder.show()
    }

    private fun renameControllerDialog(id: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Rename Controller")
        val dialogLayout = inflater.inflate(R.layout.prompt_edit_single_rename, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.tvChangeName)
        builder.setView(dialogLayout)
        builder.setIcon(R.drawable.img_plug)
        builder.setPositiveButton("OK") { dialogInterface, i ->
            Log.d(TAG, dialogInterface.toString() + i.toString())
            var newName = editText.text.toString()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            if (isValidControllerName(newName)) {
                newName = sanitizePinName(newName)
                firebaseRTDB.getReference("Controllers")
                        .child(id).child("name").setValue(newName)
                        .addOnSuccessListener {
                            for ((_id, password) in userData!!.controllers) {
                                if (_id == activeControllerID) {
                                    updateUserControllers(_id, password)
                                }
                            }
                        }
            } else {
                showToast("Invalid name")
            }
            hideKeyboard()
        }
        builder.setNegativeButton("Cancel") { dialogInterface, i ->
            Log.d(TAG, dialogInterface.toString() + i.toString())
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
//            showToast("Rename Cancelled")
            hideKeyboard()
        }
        builder.setOnCancelListener {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
//            showToast("Rename Cancelled")
            hideKeyboard()
        }
        builder.show()
    }

    private fun registerController(controllerID: String, password: String, booting: Boolean = false) {
        if (controllerID.isBlank()) {
            etRegisterControllerID.error = "Invalid ID"
            return
        }
        if (controllerID.isBlank()) {
            etRegisterControllerID.error = "Invalid password"
            return
        }
        firebaseRTDB.getReference("Controllers")
                .child(controllerID)
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.value != null) {
                        if (dataSnapshot.child("password").value == password) {
                            disconnectController(activeControllerID)
                            setActiveController(controllerID)
                            updateUserControllers(controllerID, password, booting)
                            // create views layout
                            createViews(dataSnapshot)
                            readControllerData(controllerID, password)
                            etRegisterControllerID.text?.clear()
                            etRegisterControllerPassword.text?.clear()
                            hideKeyboard()
//                        layoutActiveControllerInfo.isVisible = true
                            if (booting) {
                                setVisibleTab(layoutControllerTabButton)
                            } else {
                                showToast("Controller was registered")
                            }
                        } else {
                            showToast("Passwords do not match")
                            return@addOnSuccessListener
                        }
                    } else {
                        showToast("Controller does not exist")
                        return@addOnSuccessListener
                    }

                }
                .addOnFailureListener {
                    showToast("Error")
                }
    }

    private fun readControllerData(controllerID: String, password: String) {
        eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("summary")) {
                    updateViews(snapshot)
                } else {
//                    Log.d(TAG, "Populate Controller")
                    val controllerData = snapshot.getValue(EmptyController::class.java) as EmptyController
                    populateNewController(controllerID, password, controllerData)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        firebaseRTDB.getReference("Controllers").child(controllerID).addValueEventListener(eventListener)
    }

    private fun createViews(dataSnapshot: DataSnapshot) {
        val _dataSnapshot = dataSnapshot.child("pins")
        val activePins = mutableListOf<String>()
        Log.d(TAG, "CREATE VIEWS")
        Log.d(TAG, _dataSnapshot.toString())
        Log.d(TAG, _dataSnapshot.value.toString())
        Log.d(TAG, _dataSnapshot.children.toString())
        for (child in _dataSnapshot.children) {
            Log.d(TAG, child.key.toString())
            val pinID = child.key.toString()
            activePins.add(pinID)
        }
        for ((_pin, _layout) in layoutPinMap) {
            _layout.isVisible = activePins.contains(_pin)
        }

        Log.d("TIME MEASUREMENT-PLIST", System.currentTimeMillis().toString())

    }

    private fun updateViews(dataSnapshot: DataSnapshot) {
//        Log.d(TAG, "UPDATE VIEWS")
//        Log.d(TAG, dataSnapshot.toString())
        val controllerData = dataSnapshot.getValue(Controller::class.java)
        if (controllerData != null) {
            tvActiveControllerID.text = controllerData.id
            tvActiveControllerName.text = restoreControllerName(controllerData.name)
            for ((pinID, pinData) in controllerData.pins) {
                Log.d(TAG, pinTextViewMap[pinID]?.get("name").toString())
                pinTextViewMap[pinID]?.get("name")?.text = restorePinName(pinData.name)
                pinTextViewMap[pinID]?.get("state")?.text = pinData.state
                if (pinData.state == "on") {
                    pinSwitchMap[pinID]?.setImageResource(switchOnImage)
                } else {
                    pinSwitchMap[pinID]?.setImageResource(switchOffImage)
                }
            }
        }
    }

    private fun populateNewController(controllerID: String, password: String, controllerData: EmptyController) {
        // used only when the controller has no data
        val pinMap = mutableMapOf<String, Pin>()
        for (pin in controllerData.pins) {
//            Log.d(TAG, pin.key.toString())
            val pinData = Pin(pin.key)
//            Log.d(TAG, pinData.name)
            pinMap[pin.key] = pinData
        }
        val controllerPopulated = Controller(controllerID, password, pinMap)
//        Log.d(TAG, controllerPopulated.summary)
        firebaseRTDB.getReference("Controllers")
                .child(controllerID)
                .setValue(controllerPopulated)

    }

    private fun setActiveController(id: String) {
//        tvActiveControllerID.text = id
        activeControllerID = id
        firestoreDB.collection("Users")
                .document(email)
                .update("activeController", id)
    }

    private fun disconnectController(id: String) {
        try {
            firebaseRTDB.getReference("Controllers")
                    .child(id)
                    .removeEventListener(eventListener)
            Log.d(TAG, "EVENT LISTENER WAS REMOVED")
        } catch (e: Exception) {
            Log.d(TAG, "THERE WAS NO EVENT LISTENER")
        }
    }

    private fun updateUserControllers(id: String, password: String, booting: Boolean = false) {
        val controllerMap = mutableMapOf<String, String>()
//        firestoreDB.collection("Users")
//                .document(email)
//                .get()
//                .addOnSuccessListener { documentSnapshot ->
//                    val user = documentSnapshot.toObject(User::class.java)
//                    try {
//                        user?.controllers?.forEach {
//                            controllerMap[it.key] = it.value
//                        }
//                    } catch (e: Exception) {
//                    }
//                    controllerMap[id] = password
//                    firestoreDB.collection("Users")
//                            .document(email)
//                            .update("controllers", controllerMap)
//
//                    // update controller list
//                    updateRegisteredControllerList(controllerMap)
//                }
        try {
            userData?.controllers?.forEach {
                controllerMap[it.key] = it.value
            }
        } catch (e: Exception) {
        }
        updateRegisteredControllerList(controllerMap, booting = booting)
        controllerMap[id] = password
        firestoreDB.collection("Users")
                .document(email)
                .update("controllers", controllerMap)
        userData?.controllers = controllerMap

        // update controller list
    }

    private fun updateRegisteredControllerList(controllerMap: Map<String, String>, booting: Boolean = false) {
        // update controller list
        firebaseRTDB.getReference("Controllers")
                .get()
                .addOnSuccessListener { controllerList ->
                    layoutControllerList.removeAllViews()
                    controllerList.children.forEach { controller ->
                        if (controllerMap.containsKey(controller.key)) {
                            val controllerData = controller.getValue(Controller::class.java)
                            try {
                                if (controllerData != null) {
                                    Log.d(TAG, "ID: ${controllerData.id}, Name: ${controllerData.name}")
                                    val textView: TextView = TextView(this)
                                    textView.id = ViewCompat.generateViewId()
//                                    textView.text = "ID: ${controllerData.id}, Name: ${restoreControllerName(controllerData.name)}"
                                    textView.text = restoreControllerName(controllerData.name)
                                    textView.textSize = 20f
                                    textView.setTextColor(resources.getColor(R.color.white_cream))
                                    textView.typeface = ResourcesCompat.getFont(this, R.font.roboto_black)
                                    textView.setPaddingRelative(0, 20, 0, 20)
                                    val layoutParams: ActionBar.LayoutParams = ActionBar.LayoutParams(
                                            ActionBar.LayoutParams.MATCH_PARENT,
                                            ActionBar.LayoutParams.WRAP_CONTENT
                                    )
                                    layoutParams.setMargins(5, 5, 5, 5)
                                    textView.gravity = Gravity.CENTER
                                    textView.layoutParams = layoutParams
                                    if (controllerData.id == activeControllerID) {
                                        textView.setBackgroundResource(R.drawable.btn_background_round_orange)
                                    }
                                    textView.setOnClickListener {
//                                        showToast(textView.text.toString())
                                        registerController(controllerData.id, controllerData.password, booting = true)
                                        switchControllerTimeCounter = System.currentTimeMillis()
                                        Log.d("TIME MEASUREMENT-CLICK", System.currentTimeMillis().toString())
                                    }
                                    layoutControllerList.addView(textView)

                                }
                            } catch (e: Exception) {
                            } finally {
                                if (booting) {
                                    layoutMainBodyContainer.isVisible = true
                                    layoutLoadingController.isVisible = false
                                }
                            }
                        }
                    }
                    Log.d("TIME MEASUREMENT-CLIST", System.currentTimeMillis().toString())
                    if (switchControllerTimeCounter > 0) {
                        Log.d("TIME TAKEN", (System.currentTimeMillis() - switchControllerTimeCounter).toString())
                        pseudoTrace("switch_controller", (System.currentTimeMillis() - switchControllerTimeCounter))
                    }
                }
    }

    private fun changeState(pinID: String, newState: String) {
        if (activeControllerID.isBlank()) {
            return
        }
        firebaseRTDB.getReference("Controllers")
                .child(activeControllerID)
                .child("pins")
                .child(pinID)
                .child("state")
                .setValue(newState)
                .addOnSuccessListener {
                    updateControllerSummary(pinID, newState)
                }
                .addOnFailureListener {

                }
    }

    private fun updateControllerSummary(pinID: String, newState: String) {
        if (activeControllerID.isBlank()) {
            return
        }
        var summary = ""
        for ((_pinID, layout) in layoutPinMap) {
            if (layout.isVisible) {
                summary += if (pinID == _pinID) {
                    if (newState == "on") {
                        _pinID.replace("pin_", "P") + "S1"
                    } else {
                        _pinID.replace("pin_", "P") + "S0"
                    }
                } else if (pinStateMap[_pinID]?.text.toString().toLowerCase(Locale.ROOT) == "on") {
                    _pinID.replace("pin_", "P") + "S1"
                } else {
                    _pinID.replace("pin_", "P") + "S0"
                }
            }
        }

        firebaseRTDB.getReference("Controllers")
                .child(activeControllerID)
                .child("summary")
                .setValue(summary)
    }

    private fun setVisibleTab(linearLayout: LinearLayout) {
        for ((tab, layout) in tabViewMap) {
            if (tab == linearLayout) {
                tab.setBackgroundResource(layoutActiveTabBackground)
                if (linearLayout == layoutControllerTabButton) {
                    if (activeControllerID.isEmpty()) {
                        layoutControllerInfoFragment.isVisible = false
                        layoutNoControllerFragment.isVisible = true
                    } else {
                        layoutControllerInfoFragment.isVisible = true
                        layoutNoControllerFragment.isVisible = false
                    }
                } else {
                    layout.isVisible = true
                }
            } else {
                tab.setBackgroundResource(layoutIdleTabBackground)
                layout.isVisible = false
            }
        }
    }

    private fun signOut() {
        // DO NOT FUCKING TOUCH
        disconnectController(activeControllerID)
        firebaseAuth.signOut()
        launchActivity<SignInActivity> {
            putExtra("EMAIL", email)
        }
    }
}
