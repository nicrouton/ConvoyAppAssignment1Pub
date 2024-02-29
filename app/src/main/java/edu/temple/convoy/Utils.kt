package edu.temple.convoy

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException

class Utils {

    /*
        A function to simplify the volley requests
     */
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



    /*
        Allows saving simple text to a file to be accessed by loadPropertyFromFile()
     */
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

    /*
        Loads a string from a file if it is available. The first value in the pair indicates if the
        file is there and there is something in it and the second one is the string from the file
     */
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
}