package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.location.GeoLocationManager
import com.udacity.project4.locationreminders.geofence.GeofenceManager
import com.udacity.project4.locationreminders.geofence.GeofenceUtils
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    private val TAG = "SaveReminderFragment"
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    // Failure from checkLocationSettings -> start resolution
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    val onFailureListener =  OnFailureListener {
        val exception = it
        if (exception is ResolvableApiException) {
            try {
                exception.startResolutionForResult(activity, REQUEST_TURN_DEVICE_LOCATION_ON)
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
            }
        } else {
            Snackbar.make(
                    binding.mainLayoutSaveReminder, R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
            }.show()
        }
    }

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    // Check if both foreground and background permissions are granted
    private fun locationPermissionsGranted(): Boolean {
        val foregroundLocationApproved = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundPermissionApproved =
                if (runningQOrLater) {
                    ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

        return foregroundLocationApproved && backgroundPermissionApproved
    }

    /*
     *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
     */
    private fun requestLocationPermissions() {
        if (locationPermissionsGranted())
            return
        Log.d(AuthenticationActivity.TAG, "Requesting location permission")

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        requestPermissions(
                permissionsArray,
                resultCode)
    }

    private fun showLocationPermissionsError() {
        val message = getString(R.string.permission_denied_explanation)
        val snackbar = Snackbar.make(this.view!!, message, LENGTH_INDEFINITE)

        snackbar.setAction(getString(R.string.snackbar_try_again)) {
            snackbar.dismiss()

            // Check for permissions again
            startLocationPermissionFlow()
        }
        snackbar.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode === REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE ||
                requestCode === REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE) {
            if (
                    grantResults.isEmpty() ||
                    grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                    (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                            grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                            PackageManager.PERMISSION_DENIED))
            {
                // Show snackbar to inform user that permissions are needed and to restart permission flow
                showLocationPermissionsError()
            } else {
                // Verify that location settings are enabled
                checkDeviceLocationSettings()
            }
        }
    }

    private fun startLocationPermissionFlow() {
        // Check and request location permissions
        if (locationPermissionsGranted()) {
            // Verify that location settings are enabled
            checkDeviceLocationSettings()
        }
        else {
            requestLocationPermissions()
        }
    }

    private fun showLocationSettingsError() {
        val message = getString(R.string.location_settings_error)
        val snackbar = Snackbar.make(this.view!!, message, LENGTH_INDEFINITE)

        snackbar.setAction(getString(R.string.snackbar_try_again)) {
            snackbar.dismiss()

            // Verify device settings again
            checkDeviceLocationSettings()
        }
        snackbar.show()
    }

    /*
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(requireActivity(),
                            REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                showLocationSettingsError()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                // Start location services
                GeoLocationManager.startLocationTracking(requireContext().applicationContext)

                // Navigate to 'Select Location' fragment
                _viewModel.navigationCommand.value =
                        NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Here we are requesting user permission to access location
            startLocationPermissionFlow()
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val reminder = ReminderDataItem(title, description, location, latitude, longitude)

            // Validate form
            if (_viewModel.validateEnteredData(reminder)) {
                // Check location settings enabled
                GeofenceUtils.checkDeviceLocationSettings(activity as Activity, OnCompleteListener<LocationSettingsResponse>() {
                    if (it.isSuccessful) {
                        // Create geo fencing request
                    }
                }, onFailureListener)

                // TODO: use the user entered reminder details to:
                // 1) done: add a geofencing request
                // 2) done: save the reminder to the local db - done

                // Add geofence request
                val geofenceManager = GeofenceManager(requireActivity())
                geofenceManager.addGeofenceForReminder(reminder)

                // Save reminder
                _viewModel.saveReminder(reminder)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    }
}
