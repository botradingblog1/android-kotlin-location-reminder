package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//    TODO: Create a fake data source to act as a double to the real data source
class FakeDataSource(private val reminderList: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {
    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // done TODO("Return the reminders")

        if (shouldReturnError) {
            return Result.Error("Error occurred in getReminders")
        }
        else {
            return Result.Success(reminderList)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        // done TODO("save the reminder")

        reminderList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        // done TODO("return the reminder with the id")
        if (shouldReturnError) {
            return Result.Error("Error occurred in getReminder")
        }
        else {
            // Get reminder from list
            val reminder = reminderList?.find {
                it.id == id
            }

            if (reminder != null) {
                return Result.Success(reminder)
            }
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        // done TODO("delete all the reminders")

        reminderList?.clear()
    }
}