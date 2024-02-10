package edu.temple.convoy

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException

class Utils {

    public fun getDataFromAPI(urlString: String, context: Context, params: Map<String, String>, response: Response.Listener<String>){
        var sessionKey = ""
        var queue = Volley.newRequestQueue(context)
        var stringRequest = object : StringRequest(
            Method.POST,
            urlString,
            response,
            Response.ErrorListener { error ->
                Toast.makeText(context, "There was an error making the request", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }
        queue.add(stringRequest)

    }

    public fun savePropertyToFile(sessionID: String, file: File){
        try{
            val outputStream = FileOutputStream(file)
            outputStream.write(sessionID.toByteArray())
            outputStream.close()
            Log.d("Saving", "File saved completed")
        } catch(e: Exception){
            e.printStackTrace()
        }
    }

    public fun loadPropertyFromFile(context: Context, propertyName: String): Pair<Boolean, String>{
        val file = File(context.filesDir, propertyName)
        var sessionKey = ""
        if(file.exists()){
            try {
                val br = BufferedReader(FileReader(file))
                val text = StringBuilder()
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    text.append(line)
                }
                br.close()
                Log.d("Loaded Session ID",text.toString())
                sessionKey = text.toString()
                if(sessionKey.isEmpty()){
                    return Pair(false, sessionKey)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return Pair(true, sessionKey)
        } else {
            return Pair(false, sessionKey)
        }
    }

//    fun createAccountSetup(){
//        setContentView(R.layout.create_account_screen)
//        findViewById<Button>(R.id.loginButton).setOnClickListener {
//            setContentView(R.layout.activity_main)
//            loginSetup()
//        }
//        val username = findViewById<EditText>(R.id.usernameEditText)
//        val password = findViewById<EditText>(R.id.passwordEditText)
//        val firstname = findViewById<EditText>(R.id.firstnameEditText)
//        val lastname = findViewById<EditText>(R.id.lastnameEditText)
//
//        findViewById<Button>(R.id.createAccountButton).setOnClickListener {
//            if(username.text.isEmpty() || password.text.isEmpty() ||
//                firstname.text.isEmpty() || lastname.text.isEmpty()){
//                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
//            } else {
//                var queue = Volley.newRequestQueue(this@MainActivity)
//                var stringRequest = object : StringRequest(
//                    Method.POST,
//                    urlString,
//                    Response.Listener { response ->
//                        val jsonData = JSONObject(response)
//                        Log.d("", response)
//                        if(jsonData.getString("status") == "SUCCESS"){
//                            sessionKey = jsonData.getString("session_key")
//                            saveSessionID(sessionKey)
//                            launchHomePage()
//                        } else{
//                            Log.d("API",jsonData.getString("message"))
//                        }
//                    },
//                    Response.ErrorListener { error ->
//                        Toast.makeText(this, "There was an error making the login request", Toast.LENGTH_LONG).show()
//                    }) {
//                    override fun getParams(): Map<String, String> {
//                        val params = HashMap<String, String>()
//                        params.put("action", "REGISTER")
//                        params.put("username",username.text.toString())
//                        params.put("firstname",firstname.text.toString())
//                        params.put("lastname",lastname.text.toString())
//                        params.put("password", password.text.toString())
//                        return params
//                    }
//                }
//                queue.add(stringRequest)
//            }
//        }
//    }
//
//    fun loginSetup() {
//        findViewById<Button>(R.id.createAccountButton).setOnClickListener{
//            createAccountSetup()
//        }
//        val username = findViewById<EditText>(R.id.usernameEditText)
//        val password = findViewById<EditText>(R.id.passwordEditText)
//        findViewById<Button>(R.id.loginButton).setOnClickListener {
//            if (username.text.isEmpty() || password.text.isEmpty()) {
//                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
//            } else {
//                var queue = Volley.newRequestQueue(this@MainActivity)
//                var stringRequest = object : StringRequest(
//                    Method.POST,
//                    urlString,
//                    Response.Listener { response ->
//                        val jsonData = JSONObject(response)
//                        Log.d("", response)
//                        if(jsonData.getString("status") == "SUCCESS"){
//                            sessionKey = jsonData.getString("session_key")
//                            saveSessionID(sessionKey)
//                            launchHomePage()
//                        } else{
//                            Log.d("API",jsonData.getString("message"))
//                        }
//                    },
//                    Response.ErrorListener { error ->
//                        Toast.makeText(
//                            this,
//                            "There was an error making the login request",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }) {
//                    override fun getParams(): Map<String, String> {
//                        val params = HashMap<String, String>()
//                        params.put("action", "LOGIN")
//                        params.put("username", username.text.toString())
//                        params.put("password", password.text.toString())
//                        return params
//                    }
//                }
//                queue.add(stringRequest)
//            }
//        }
//    }
}