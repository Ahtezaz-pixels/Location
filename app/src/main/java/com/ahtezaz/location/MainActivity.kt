package com.ahtezaz.location

import android.Manifest
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.util.*


class MainActivity : AppCompatActivity() {
    private var locationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null
    private var latitude: String? = null
    private var longitude: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val request = findViewById<Button>(R.id.btnRequest)
        request.setOnClickListener {
            requestPermission()
            if (hasFineLocationPermission() && hasCoarseLocationPermission()) {
                locationRequest = LocationRequest.create()
                if (isGPSEnabled()) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d("TAG", "onCreate: IN GPS GPS")

                        LocationServices.getFusedLocationProviderClient(this@MainActivity)
                            .requestLocationUpdates(locationRequest!!, object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult) {
                                    super.onLocationResult(locationResult)
                                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                                        .removeLocationUpdates(this)
                                    if (locationResult.locations.size > 0) {
                                        val index = locationResult.locations.size - 1
                                        val latitude = locationResult.locations[index].latitude
                                        val longitude = locationResult.locations[index].longitude
                                        Log.d("TAG", "onLocationResult: $latitude , $longitude")
                                         val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                                        val addresses: List<Address> =
                                            geocoder.getFromLocation(latitude, longitude, 1)
                                        val cityName: String = addresses[0].getAddressLine(0)
                                         Toast.makeText(this@MainActivity ,"$cityName ",Toast.LENGTH_LONG).show()
                                    }
                                }
                            }, Looper.getMainLooper())
                    } else {
                        Toast.makeText(this@MainActivity ,"In TURN ON GPS",Toast.LENGTH_LONG).show()
                        Log.d("TAG", "onCreate: IN ELSE")
                        turnOnGPS()
                    }

                }

            }
        }
    }

    private fun turnOnGPS() {
        locationRequest = LocationRequest.create()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 5000
        locationRequest?.fastestInterval = 2000
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
        builder.setAlwaysShow(true)
        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(applicationContext)
                .checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Toast.makeText(this@MainActivity, "GPS is already turned on", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this@MainActivity, 2)
                    } catch (ex: SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    private fun hasReadExternalStoragePermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocationPermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasFineLocationPermission() = ActivityCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        var permissionToRequest = mutableListOf<String>()
        if (!hasCoarseLocationPermission()) {
            permissionToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!hasFineLocationPermission()) {
            permissionToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!hasReadExternalStoragePermission()) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionToRequest.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                Log.d("TAG", "onRequestPermissionsResult: ${permissions[i]} granted")
            }
        }
    }

    private fun isGPSEnabled(): Boolean {
        var isEnabled = false
        if (locationManager == null) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        isEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isEnabled
    }


}