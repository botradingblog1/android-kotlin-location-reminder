package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Unit tests for RemindersLocalRepository
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Create test data
    private val reminder1 = ReminderDTO(
        id = "1000",
        title = "Title 1",
        description = "Description 1",
        location = "Location 1",
        latitude = 38.976560,
        longitude = -77.355760

    )
    private val reminder2 = ReminderDTO(
        id = "10001",
        title = "Title 2",
        description = "Description 2",
        location = "Location 2",
        latitude = 38.976569,
        longitude = -77.355769
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun teardown() {
        database.close()
    }

    /*
    * Test saveReminder: Given a reminder, verify that it is inserted into the db and retrieved correctly
    */
    @Test
    fun testSaveReminder() = runBlocking {
        // Save one reminder
        repository.saveReminder(reminder1)

        val result = repository.getReminders()
        MatcherAssert.assertThat(result, CoreMatchers.notNullValue())
        Truth.assertThat(result).isInstanceOf(Result.Success::class.java)

        val resultSuccess = result as Result.Success
        val reminderList = resultSuccess.data

        Truth.assertThat(reminderList).hasSize(1)
        Truth.assertThat(reminderList).contains(reminder1)
    }

    /*
    * Test getReminderById. Given a reminder, verify that it can be retrieved by id
    */
    @Test
    fun testGetReminderById() = runBlocking {
        // Save one reminder
        repository.saveReminder(reminder1)

        // Verify reminder data from db
        val result = repository.getReminder(reminder1.id)
        MatcherAssert.assertThat(result, CoreMatchers.notNullValue())
        Truth.assertThat(result).isInstanceOf(Result.Success::class.java)

        val resultSuccess = result as Result.Success
        val checkReminder = resultSuccess.data

        MatcherAssert.assertThat(checkReminder.id, CoreMatchers.`is`(reminder1.id))
        MatcherAssert.assertThat(checkReminder.title, CoreMatchers.`is`(reminder1.title))
        MatcherAssert.assertThat(checkReminder.description, CoreMatchers.`is`(reminder1.description))
        MatcherAssert.assertThat(checkReminder.location, CoreMatchers.`is`(reminder1.location))
        MatcherAssert.assertThat(checkReminder.latitude, CoreMatchers.`is`(reminder1.latitude))
        MatcherAssert.assertThat(checkReminder.longitude, CoreMatchers.`is`(reminder1.longitude))
    }

    /*
    * Test getReminders: Given multiple reminders, verify that they are inserted into the db and retrieved correctly
    */
    @Test
    fun testGetReminders() = runBlocking {
        // Save multiple reminders
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val result = repository.getReminders()
        MatcherAssert.assertThat(result, CoreMatchers.notNullValue())
        Truth.assertThat(result).isInstanceOf(Result.Success::class.java)

        val resultSuccess = result as Result.Success
        val reminderList = resultSuccess.data

        Truth.assertThat(reminderList).hasSize(2)
        Truth.assertThat(reminderList).contains(reminder1)
        Truth.assertThat(reminderList).contains(reminder2)
    }

    /*
    * Given multiple reminders saved into the db, verify that they can be deleted
    */
    @Test
    fun testDeleteReminders() = runBlocking {
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        // Delete all reminders
        repository.deleteAllReminders()

        // Retrieve reminders
        val result = repository.getReminders()
        MatcherAssert.assertThat(result, CoreMatchers.notNullValue())
        Truth.assertThat(result).isInstanceOf(Result.Success::class.java)

        val resultSuccess = result as Result.Success
        val reminderList = resultSuccess.data

        // Assertions
        MatcherAssert.assertThat(reminderList.isEmpty(), CoreMatchers.`is`(true))
    }
}