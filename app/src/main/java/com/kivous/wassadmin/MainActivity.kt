package com.kivous.wassadmin

import android.Manifest
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kivous.wassadmin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // change status bar text color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // ask location permission
        locationPermission()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val dbRef = db.collection("location").document("location")


        // get device location and set to edittext
        binding.vAutoDetect.setOnClickListener {
            @Suppress("MissingPermission")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val lat = location.latitude
                        val long = location.longitude
                        binding.etLatitude.setText(lat.toString())
                        binding.etLongitude.setText(long.toString())
                    } else {
                        Toast.makeText(
                            this,
                            "null location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        // get location from FireStore database and set to edittext
        dbRef.addSnapshotListener { location, _ ->
            binding.etLatitude.setText(location?.get("latitude").toString())
            binding.etLongitude.setText(location?.get("longitude").toString())
            binding.etRadius.setText(location?.get("radius").toString())
        }

        // set location to  FireStore database
        binding.btnSetLocation.setOnClickListener {
            val lat = binding.etLatitude.text.toString()
            val long = binding.etLongitude.text.toString()
            val rad = binding.etRadius.text.toString()

            if (lat.isEmpty() && long.isEmpty() && rad.isEmpty()) {
                Toast.makeText(this, "Empty fields", Toast.LENGTH_SHORT).show()
            } else {
                dbRef.set(LocationModel(lat.toDouble(), long.toDouble(), rad.toDouble()))
                    .addOnSuccessListener {
                        dbRef.addSnapshotListener { location, _ ->
                            binding.tvLatitude.text = location?.get("latitude").toString()
                            binding.tvLongitude.text = location?.get("longitude").toString()
                            binding.tvRadius.text = location?.get("radius").toString() + " meter"
                        }
                    }
            }
        }


    }

    private fun locationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                }

                else -> {
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }


}

