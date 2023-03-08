package eu.tutorials.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import eu.tutorials.projemanag.R
import eu.tutorials.projemanag.firebase.FirestoreClass
import eu.tutorials.projemanag.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private var toolbar_sign_in_activity : Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        toolbar_sign_in_activity = findViewById(R.id.toolbar_sign_in_activity)
        btn_sign_in.setOnClickListener{
            signInRegisteredUser()
        }
        setupActionBar()
    }
    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_in_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_in_activity?.setNavigationOnClickListener{onBackPressed()}

    }

    private fun signInRegisteredUser(){
        val email: String = et_email_signin.text.toString().trim{it <= ' '}
        val password: String = et_password_signin.text.toString().trim{it <= ' '}

        if(validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                       FirestoreClass().loadUserData(this)

                    } else {
                        hideProgressDialog()
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, task.exception!!.message,
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun validateForm(email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }else -> {
                true
            }
        }
    }
}