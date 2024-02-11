package edu.temple.convoy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import java.io.File

val LOCATION_UPDATE = "LOCATION_UPDATE"

class HomePage : AppCompatActivity(), OnMapReadyCallback {

    val convoyURL = "https://kamorris.com/lab/convoy/convoy.php"
    lateinit var username: String
    var convoyID = ""

    private lateinit var mapView: MapView
    private var marker: Marker? = null
    private lateinit var map: GoogleMap

    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("", intent.toString())
            if (intent?.action == LOCATION_UPDATE) {
                Log.d("", "GOT SOMETHING")
                val lat = intent?.getDoubleExtra("LATITUDE", 0.0)
                val long = intent?.getDoubleExtra("LONGITUDE", 0.0)
                if (lat != null && long != null) {
                    updatePos(lat, long)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
        username = intent.getStringExtra(USER_NAME)!!
        convoyID = Utils().loadPropertyFromFile(this, convoyIDFileName).second
        val topbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topBar)
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//            locationUpdateReceiver,
//            IntentFilter(LOCATION_UPDATE)
//        )
        setSupportActionBar(topbar)
        topbar.showOverflowMenu()
        supportActionBar?.title = "Not in a convoy"

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
                Intent(this, LocationListenerService::class.java).also {
                    it.action = LocationListenerService.Actions.STOP.toString()
                    //can crash if service is not running
                    startForegroundService(it)
                }
                LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver)
            } else{
                Log.d("API",jsonData.getString("message"))
            }
        }
    }

    private fun joinConvoy() {
        CustomDialogFragment("Please Enter a ConvoyID", username)
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

                Intent(this, LocationListenerService::class.java).also {
                    it.action = LocationListenerService.Actions.START.toString()
                    startForegroundService(it)
                }
                LocalBroadcastManager.getInstance(this)
                    .registerReceiver(
                        locationUpdateReceiver,
                        IntentFilter(LOCATION_UPDATE)
                    )

            } else{
                Log.d("API",jsonData.getString("message"))
                Log.d("SessionKey", session)
            }
        }
    }

    private fun logout() {
        File(filesDir, usernameFileName).delete()
        File(filesDir, sessionIDFileName).delete()
        unregisterReceiver(locationUpdateReceiver)
        startActivity(Intent(this, MainActivity::class.java))

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    public fun updatePos(lat: Double, long: Double){
        var latlng = LatLng(lat,long)

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 15f)

        if(map != null){
            map.animateCamera(cameraUpdate)
            if(marker == null){
                marker = map.addMarker(MarkerOptions().position(latlng))!!
            } else{
                marker!!.position = latlng
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver)
        leaveConvoy()
    }
}