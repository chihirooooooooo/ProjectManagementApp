package eu.tutorials.projemanag.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.TextView
import eu.tutorials.projemanag.R
import eu.tutorials.projemanag.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    var tv_app_name : TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        tv_app_name = findViewById(R.id.tv_app_name)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val typeFace : Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        tv_app_name?.typeface = typeFace
        Handler().postDelayed({
            var currentUserID = FirestoreClass().getCurrentUserId()
            if(currentUserID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }

            finish()
        },2500)
    }
}