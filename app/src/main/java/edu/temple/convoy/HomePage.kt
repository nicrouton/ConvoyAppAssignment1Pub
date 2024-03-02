package edu.temple.convoy

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import edu.temple.convoy.CustomDialogFragment.Companion.TAG
import org.json.JSONObject
import java.io.File


val LOCATION_UPDATE = "LOCATION_UPDATE"
val FCM_SERVICE_UPDATE = "FCM_SERVICE_UPDATE"

class HomePage : AppCompatActivity(), OnMapReadyCallback {

    val convoyURL = "https://kamorris.com/lab/convoy/convoy.php"
    lateinit var username: String
    var convoyID = ""
    var receiverRegistered = false
    val urlString = "https://kamorris.com/lab/convoy/account.php"
    // boolean to keep track of whether the user has joined a convoy already
    var joinedConvoy = false

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

    // when a new fcm token is received/refreshed sent a UPDATE request to server
    private val tokenRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "TOKEN_REFRESHED") {
                val token = intent.getStringExtra("TOKEN")
                // Handle token refresh in your activity
                val params = HashMap<String, String>()
                params["action"] = "UPDATE"
                params["username"] = username
                params["session_key"] = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
                params["fcm_token"] = token.toString()
                Utils().getDataFromAPI(urlString,this@HomePage, params) {
                    val jsonData = JSONObject(it)
                    Log.d("", it)
                    if (jsonData.getString("status") == "SUCCESS") {



                        Log.d(
                            "POST request update fcm",
                            "Updating server with fcm token"
                        )


                    } else {
                        Log.d("API", jsonData.getString("message"))
                        Toast.makeText(this@HomePage, jsonData.getString("message"), Toast.LENGTH_LONG).show()

                    }

                }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "FIREBASE_PAYLOAD") {
                val jsonString = intent.getStringExtra("JSON_DATA")
                val jsonObject = JSONObject(jsonString)
                // Handle the received JSON object here






                Log.d("HomePage", "Received JSON: $jsonObject")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
        username = intent.getStringExtra(USER_NAME)!!
        convoyID = Utils().loadPropertyFromFile(this, convoyIDFileName).second
        val topbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topBar)
        setSupportActionBar(topbar)
        topbar.showOverflowMenu()
        setIfInConoy()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter("FIREBASE_PAYLOAD")
        )


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

    private fun setIfInConoy(){
        val params = HashMap<String,String>()
        params["action"] = "QUERY"
        params["username"] = username
        params["session_key"] = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
        Utils().getDataFromAPI(
            convoyURL,
            this@HomePage,
            params
        ){
            val jsonData = JSONObject(it)

            if(jsonData.getString("status") == "SUCCESS"){
                supportActionBar?.title = jsonData.getString("convoy_id")
            } else{
                supportActionBar?.title = "Not in a convoy"
            }
        }
    }

    private fun leaveConvoy() {
        var leavePrompt = ""
        if (joinedConvoy) {
            leavePrompt = "Are you sure you want to leave?"
        } else {
            leavePrompt = "This will end the convoy. Are you sure?"
        }
        PermissionAlertDialogFragment(
            leavePrompt,
            "Yes",
            "No",
            { _,_ ->
                val params = HashMap<String, String>()
                if (joinedConvoy) {
                    params["action"] = "LEAVE"
                } else {
                    params["action"] = "END"
                }

                params["username"] = username
                params["session_key"] = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
                params["convoy_id"] = convoyID
                Log.d("ConvoyID",convoyID)
                Utils().getDataFromAPI(convoyURL, this@HomePage, params){
                    val jsonData = JSONObject(it)

                    if(jsonData.getString("status") == "SUCCESS"){
                        supportActionBar?.title = "Not in a convoy"
                        if(isMyServiceRunning(LocationListenerService::class.java)){
                            Intent(this, LocationListenerService::class.java).also {
                                it.action = LocationListenerService.Actions.STOP.toString()
                                startForegroundService(it)
                            }
                        }
                        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver)
                        LocalBroadcastManager.getInstance(this).unregisterReceiver(tokenRefreshReceiver)

                        receiverRegistered = false
                    } else{
                        Log.d("API",jsonData.getString("message"))
                        Toast.makeText(this, "Leave failed: $jsonData", Toast.LENGTH_LONG).show()

                    }
                }
            },
            { _,_ ->

            }
        ).show(supportFragmentManager,PermissionAlertDialogFragment.TAG)
    }

    private fun joinConvoy() {
        CustomDialogFragment("Please Enter a ConvoyID", username, true)
            .show(supportFragmentManager, CustomDialogFragment.TAG)

        // join request to the server: action, username, session_key, convoy_id
        val session = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
        val params = HashMap<String, String>()
        params["action"] = "JOIN"
        params["username"] = username
        params["session_key"] = session
        params["convoy_id"] = Utils().loadPropertyFromFile(this@HomePage, convoyIDFileName).second

        Utils().getDataFromAPI(convoyURL, this@HomePage, params) {
            val jsonData = JSONObject(it)
            if(jsonData.getString("status") == "SUCCESS") {
                convoyID = jsonData.getString("convoy_id")
                Utils().savePropertyToFile(convoyID, File(filesDir, convoyIDFileName))
                supportActionBar?.title = "Joined: $convoyID"

                joinedConvoy = true


                Intent(this, LocationListenerService::class.java).also {
                    it.action = LocationListenerService.Actions.START.toString()
                    startForegroundService(it)
                }
                LocalBroadcastManager.getInstance(this)
                    .registerReceiver(
                        locationUpdateReceiver,
                        IntentFilter(LOCATION_UPDATE)
                    )
                receiverRegistered = true


                // Register the broadcast receiver
                val filter = IntentFilter()
                filter.addAction("TOKEN_REFRESHED")
                LocalBroadcastManager.getInstance(this).registerReceiver(tokenRefreshReceiver, filter)
                Log.d("FCM Broadcast Receiver", "FCM broadcast receiver registered in createconvoy()")


                var fcmToken = ""

                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val fcmToken = task.result

                    // Log and toast

                    //Log.d(TAG, msg)
                    Toast.makeText(baseContext, fcmToken, Toast.LENGTH_SHORT).show()
                })


                val params1 = HashMap<String, String>()
                params1["action"] = "UPDATE"
                params1["username"] = username
                params1["session_key"] = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
                params1["fcm_token"] = fcmToken
                Utils().getDataFromAPI(urlString,this@HomePage, params) {
                    val jsonData = JSONObject(it)
                    Log.d("", it)
                }
            } else {
                Toast.makeText(this, "Join failed.", Toast.LENGTH_LONG).show()
                Log.d("JOIN fail", jsonData.toString())
            }
        }
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
                receiverRegistered = true


                // Register the broadcast receiver
                val filter = IntentFilter()
                filter.addAction("TOKEN_REFRESHED")
                LocalBroadcastManager.getInstance(this).registerReceiver(tokenRefreshReceiver, filter)
                Log.d("FCM Broadcast Receiver", "FCM broadcast receiver registered in createconvoy()")

                var fcmToken = ""

                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val fcmToken = task.result

                    // Log and toast

                    //Log.d(TAG, msg)
                    Toast.makeText(baseContext, fcmToken, Toast.LENGTH_SHORT).show()
                })


                val params1 = HashMap<String, String>()
                params1["action"] = "UPDATE"
                params1["username"] = username
                params1["session_key"] = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
                params1["fcm_token"] = fcmToken
                Utils().getDataFromAPI(urlString,this@HomePage, params1) {
                    val jsonData = JSONObject(it)
                    Log.d("", it)
                }



            } else{
                Log.d("API",jsonData.getString("message"))
                if(jsonData.getString("message") == "A Convoy session is already active for that user. Close active session to start another."){
                    CustomDialogFragment(
                        "You already have a convoy running",
                        "",
                        false
                    ).show(
                        supportFragmentManager,
                        CustomDialogFragment.TAG
                    )

                }

            }
        }
    }

    private fun logout() {
        File(filesDir, usernameFileName).delete()
        File(filesDir, sessionIDFileName).delete()
        File(filesDir, convoyIDFileName).delete()
        if(receiverRegistered){
            unregisterReceiver(locationUpdateReceiver)
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(tokenRefreshReceiver)
        startActivity(Intent(this, MainActivity::class.java))

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun addMembers(lat: Double, long: Double) {
        var latlng = LatLng(lat,long)

        if(map != null){

            if(marker == null){
                marker = map.addMarker(MarkerOptions().position(latlng))!!
            } else{
                marker!!.position = latlng
            }
        }

    }

    private fun updatePos(lat: Double, long: Double){
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

        if (joinedConvoy || receiverRegistered) {
            val params1 = HashMap<String, String>()
            params1["action"] = "UPDATE"
            params1["username"] = username
            params1["session_key"] = Utils().loadPropertyFromFile(this@HomePage, "SessionID").second
            params1["convoy_id"] = convoyID
            params1["latitude"] = lat.toString()
            params1["longitude"] = long.toString()
            Utils().getDataFromAPI(urlString,this@HomePage, params1) {
                val jsonData = JSONObject(it)
                Log.d("", it)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tokenRefreshReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)

        leaveConvoy()
        Utils().savePropertyToFile("",  File(filesDir, convoyIDFileName))

    }

    @Suppress("DEPRECATION")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}