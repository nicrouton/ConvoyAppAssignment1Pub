package edu.temple.convoy

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import android.widget.Toolbar
import com.android.volley.BuildConfig
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import java.io.File

class HomePage : AppCompatActivity(), OnMapReadyCallback {

    val convoyURL = "https://kamorris.com/lab/convoy/convoy.php"
    lateinit var username: String
    var convoyID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
        username = intent.getStringExtra(USER_NAME)!!
        convoyID = Utils().loadPropertyFromFile(this, convoyIDFileName).second
        val topbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topBar)
        setSupportActionBar(topbar)
        topbar.showOverflowMenu()
        if(convoyID.isEmpty()){
            supportActionBar?.title = "Not in a convoy"
        } else {
            supportActionBar?.title = convoyID
        }
        topbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.logout -> {
                    logout()
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNavView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.createConvoy -> {
                    createConvoy()
                    true
                }
                R.id.joinConvoy -> {
                    joinConvoy()
                    true
                }
                R.id.leaveConvoy -> {
                    leaveConvoy()
                    true
                }

                else -> {false}
            }
        }

    }

    private fun leaveConvoy() {
        val params = HashMap<String, String>()
        params["action"] = "END"
        params["username"] = username
        params["session_key"] = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
        // Might need to check if this is null and show error
        params["convoy_id"] = convoyID
        Log.d("ConvoyID",convoyID)
        Utils().getDataFromAPI(convoyURL, this@HomePage, params){
            val jsonData = JSONObject(it)

            if(jsonData.getString("status") == "SUCCESS"){
                supportActionBar?.title = "Not in a convoy"
            } else{
                Log.d("API",jsonData.getString("message"))
            }
        }
    }

    private fun joinConvoy() {
        CustomDialogFragment("Please Enter a ConvoyID", this@HomePage)
            .show(supportFragmentManager, CustomDialogFragment.TAG)
    }

    private fun createConvoy() {
        val session = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
        val params = HashMap<String, String>()
        params["action"] = "CREATE"
        params["username"] = username
        params["session_key"] = session
        Utils().getDataFromAPI(convoyURL, this@HomePage, params){
            val jsonData = JSONObject(it)
            if(jsonData.getString("status") == "SUCCESS"){
                convoyID = jsonData.getString("convoy_id")
                Utils().savePropertyToFile(convoyID, File(filesDir, convoyIDFileName))
                supportActionBar?.title = convoyID
            } else{
                Log.d("API",jsonData.getString("message"))
                Log.d("SessionKey", session)
            }
        }
    }

    private fun logout() {
        File(filesDir, usernameFileName).delete()
        File(filesDir, sessionIDFileName).delete()
        startActivity(Intent(this, MainActivity::class.java))

    }

    override fun onMapReady(googleMap: GoogleMap) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}