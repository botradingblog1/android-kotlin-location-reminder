package com.udacity.project4.locationreminders.geofence

object GeofenceConstants {
    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    const val GEOFENCE_RADIUS_IN_METERS = 500f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
    const val ACTION_GEOFENCE_EVENT = "com.udacity.project4.action.ACTION_GEOFENCE_EVENT"


}