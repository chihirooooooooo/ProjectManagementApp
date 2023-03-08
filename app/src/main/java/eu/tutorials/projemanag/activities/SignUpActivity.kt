package eu.tutorials.projemanag.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import eu.tutorials.projemanag.R
import eu.tutorials.projemanag.firebase.FirestoreClass
import eu.tutorials.projemanag.models.User
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    var toolbar_sign_up_activity : Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        toolbar_sign_up_activity = findViewById(R.id.toolbar_sign_up_activity)
        setupActionBar()
        btn_sign_up.setOnClickListener{
            registerUser()
        }


    }
    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_up_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_up_activity?.setNavigationOnClickListener{onBackPressed()}

    }

    private fun registerUser(){
        val name: String = et_name.text.toString().trim{it <= ' '}
        val email: String = et_email.text.toString().trim{it <= ' '}
        val password: String = et_password.text.toString().trim{it <= ' '}
        if(validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    task ->
                    if(task.isSuccessful){
                        val firebaseUser : FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    }else{
                        Toast.makeText(
                            this,
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this, "you have successfully registered", Toast.LENGTH_LONG)
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()

    }






    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
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