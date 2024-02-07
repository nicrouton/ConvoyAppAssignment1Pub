package edu.temple.convoy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    var loginVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var isFirstTime = true
        if(!isFirstTime){
            launchHomePage()
        }

        findViewById<Button>(R.id.createAccountButton).setOnClickListener{
            createAccountSetup()
        }
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            loginSetup()
        }

    }

    fun createAccountSetup(){
        setContentView(R.layout.create_account_screen)
        findViewById<Button>(R.id.submitButton).setOnClickListener {
            if(loginVerified){
                launchHomePage()
            }
        }
    }

    fun loginSetup(){
        setContentView(R.layout.login_screen)
        findViewById<Button>(R.id.submitButton).setOnClickListener {
            if(loginVerified){
                launchHomePage()
            }
        }
    }

    fun launchHomePage(){
        startActivity(Intent(this, HomePage::class.java))
    }
}