package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.locationreminders.geofence.GeofenceConstants.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem


class GeofenceManager(act: Activity) {
    private val TAG = "GeofenceManager"
    private lateinit var geofencingClient: GeofencingClient
    private var activity: Activity = act

    init {
        buildClient(activity)
    }

    private fun buildClient(activity: Activity): GeofencingClient {
        geofencingClient = LocationServices.getGeofencingClient(activity)

        return geofencingClient
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        val geofencePendingIntent = PendingIntent.getBroadcast(
                activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return geofencePendingIntent
    }

    /*
      * Adds a Geofence for a reminder
      */
    @SuppressLint("MissingPermission")
    fun addGeofenceForReminder(reminder: ReminderDataItem) {

        // Create geofence
        val geofence = Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(
                        reminder.latitude!!,
                        reminder.longitude!!,
                        GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

        val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

        geofencingClient.addGeofences(geofencingRequest, buildPendingIntent())?.run {
            addOnSuccessListener {
                Toast.makeText(activity, R.string.geofence_added,
                        Toast.LENGTH_SHORT)
                        .show()
                Log.e(TAG, geofence.requestId)
            }
            addOnFailureListener {
                Toast.makeText(activity, R.string.geofences_not_added,
                        Toast.LENGTH_SHORT).show()
                if ((it.message != null)) {
                    Log.w(TAG, it.message!!)
                }
            }
        }
    }
}