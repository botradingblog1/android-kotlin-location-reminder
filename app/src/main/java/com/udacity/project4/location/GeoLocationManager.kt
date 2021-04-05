package com.udacity.project4.location


import android.content.Context
import android.os.Looper
import android.util.Log
import android.view.View
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng

object GeoLocationManager {
    private val TAG = "GeoLocationManager"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest= LocationRequest()
    private var setupComplete = false
    private var started = false
    private val UPDATE_INTERVAL_MILLISECONDS: Long = 0
    private val FASTEST_UPDATE_INTERVAL_MILLISECONDS = UPDATE_INTERVAL_MILLISECONDS / 2
    private var lastLocation: LatLng? = null

    private fun performSetup(context: Context) {
        configureLocationRequest()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        setupComplete = true
    }

    private fun configureLocationRequest() {
        locationRequest.interval = UPDATE_INTERVAL_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations){
                val latitude = location.latitude
                val longitude = location.longitude
                lastLocation = LatLng(latitude, longitude)
                //Log.d(TAG, "location: lat: $latitude, lon: $longitude")
            }
        }
    }

    fun startLocationTracking(context: Context) {
        if (!setupComplete) {
            performSetup(context)
        }

        if (!started) {
            //noinspection MissingPermission
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())

            started = true
        }
    }

    fun stopLocationTracking() {
        if (started) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            started = false
        }
    }

    fun getLastLocation(): LatLng? {
        return lastLocation
    }
}
