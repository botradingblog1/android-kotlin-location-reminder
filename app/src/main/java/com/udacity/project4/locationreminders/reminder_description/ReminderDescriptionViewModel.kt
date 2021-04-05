package com.udacity.project4.locationreminders.reminder_description

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

class ReminderDescriptionViewModel(val app: Application) :
        BaseViewModel(app) {

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val location = MutableLiveData<String>()
    val latitude = MutableLiveData<String>()
    val longitude = MutableLiveData<String>()

    fun setReminder(reminder: ReminderDataItem?) {
        if (reminder == null) {
            return
        }
        title.value = reminder.title
        description.value = reminder.description
        location.value = reminder.location
        latitude.value = reminder.latitude.toString()
        longitude.value = reminder.longitude.toString()
    }
}