package edu.temple.convoy

import android.app.ActionBar
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toolbar
import com.android.volley.BuildConfig
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomePage : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
        val topbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.topBar)
        setSupportActionBar(topbar)
        topbar.showOverflowMenu()
        topbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.logout -> {
                    CustomDialogFragment().show(supportFragmentManager, CustomDialogFragment.TAG)
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
                    CustomDialogFragment().show(supportFragmentManager, CustomDialogFragment.TAG)
                    true
                }
                R.id.joinConvoy -> {

                    true
                }
                R.id.leaveConvoy -> {

                    true
                }

                else -> {false}
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}