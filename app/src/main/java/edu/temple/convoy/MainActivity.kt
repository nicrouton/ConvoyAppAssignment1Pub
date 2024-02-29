package edu.temple.convoy

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONObject
import java.io.File

val USER_NAME = "USERNAME"
val sessionIDFileName = "SessionID"
val usernameFileName = "Username"
val convoyIDFileName = "ConvoyID"

class MainActivity : AppCompatActivity() {

    val urlString = "https://kamorris.com/lab/convoy/account.php"
    lateinit var sessionKey: String
    private lateinit var file: File
    var user = ""

    lateinit var usernameEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var firstnameEditText: EditText
    lateinit var lastnameEditText: EditText
    //Usernames and passwords
    //Hydro 12345
    //NR 12345
    //HydroLink 12345
    //API key is in local.properties
    //MAPS_API_KEY=

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Load the session ID from the file
        file = File(filesDir, sessionIDFileName)
        //If we can retrieve the session ID this is true. Use the fact that there is a sessionID to say the user is logged in
        var isFirstTime = Utils().loadPropertyFromFile(this, sessionIDFileName).first
        val loadUser = Utils().loadPropertyFromFile(this, usernameFileName)

        requestUserPermission()

        //Setup the notification channel for the Location Service
        val channel = NotificationChannel(
            "Convoy",
            "Convoy Notifications",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        if(loadUser.first){
            user = loadUser.second
        }
        if(isFirstTime){
            launchHomePage()
        }
        loginSetup()
    }

    private fun requestUserPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ),
            0
        )
    }

    private fun createAccountSetup(){
        setContentView(R.layout.create_account_screen)
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            setContentView(R.layout.activity_main)
            loginSetup()
        }
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText= findViewById(R.id.passwordEditText)
        firstnameEditText = findViewById(R.id.firstnameEditText)
        lastnameEditText = findViewById(R.id.lastnameEditText)

        findViewById<Button>(R.id.createAccountButton).setOnClickListener {
            if(usernameEditText.text.isEmpty() || passwordEditText.text.isEmpty() ||
                firstnameEditText.text.isEmpty() || lastnameEditText.text.isEmpty()){
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()

                    Log.d("create account button listener ERROR", "one field was empty.")
            } else {

                try {
                    user = usernameEditText.text.toString()
                    val params = HashMap<String, String>()
                    params.put("action", "REGISTER")
                    params.put("username", user)
                    params.put("firstname", firstnameEditText.text.toString())
                    params.put("lastname", lastnameEditText.text.toString())
                    params.put("password", passwordEditText.text.toString())

                    Log.d("create account button listener", "starting post request")

                    Utils().getDataFromAPI(urlString, this@MainActivity, params) {
                        val jsonData = JSONObject(it)
                        Log.d("", it)
                        if (jsonData.getString("status") == "SUCCESS") {
                            sessionKey = jsonData.getString("session_key")
                            Utils().savePropertyToFile(sessionKey, file)
                            Utils().savePropertyToFile(user, File(filesDir, usernameFileName))

                            Log.d(
                                "POST request Create Account button",
                                "POST request made, Launching convoy..."
                            )

                            launchHomePage()
                        } else {
                            Log.d("API", jsonData.getString("message"))
                        }
                    }
                } catch (e: Exception) {
                    Log.d("POST catch exception account creation", "POST catch exception account creation")
                }
            }

        }
    }

    fun loginSetup() {
        findViewById<Button>(R.id.createAccountButton).setOnClickListener{
            createAccountSetup()
        }
        usernameEditText = findViewById(R.id.usernameEditText)
        val password = findViewById<EditText>(R.id.passwordEditText)
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            if (usernameEditText.text.isEmpty() || password.text.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                user = usernameEditText.text.toString()
                val params = HashMap<String, String>()
                params.put("action", "LOGIN")
                params.put("username",user)
                params.put("password", password.text.toString())
                Utils().getDataFromAPI(urlString,this,params){
                    val jsonData = JSONObject(it)
                    Log.d("", it)
                    if(jsonData.getString("status") == "SUCCESS"){
                        sessionKey = jsonData.getString("session_key")
                        Utils().savePropertyToFile(sessionKey, file)
                        Utils().savePropertyToFile(user, File(filesDir, usernameFileName))
                        launchHomePage()
                    } else{
                        Log.d("API",jsonData.getString("message"))
                    }
                }
            }
        }
    }

    fun launchHomePage(){
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED){
                startActivity(
                    Intent(this, HomePage::class.java)
                        .putExtra(USER_NAME, user)
                )
        } else if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                PermissionAlertDialogFragment(
                    "This app does not work without the requested permissions, Would you like to enable them?",
                    "Yes",
                    "No",
                    { _,_ ->
                        requestUserPermission()
                    },
                    { _,_ ->
                        finishAndRemoveTask()
                    }
                ).show(supportFragmentManager, PermissionAlertDialogFragment.TAG)
        } else{
            requestUserPermission()
        }

    }

}