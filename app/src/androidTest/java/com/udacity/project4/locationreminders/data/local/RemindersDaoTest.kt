package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Unit tests for RemindersDAO
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Create reminder test data
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

        dao = database.reminderDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    /*
     * Test saveReminder. Given a reminder, verify that it is inserted into the db and retrieved correctly
     */
    @Test
    fun testSaveReminder() = runBlockingTest {
        dao.saveReminder(reminder1)

        Truth.assertThat(dao.getReminders()).hasSize(1)
        Truth.assertThat(dao.getReminders()).contains(reminder1)
    }

    /*
     * Test getReminderById. Given a reminder, verify that it can be retrieved by id
     */
    @Test
    fun testGetReminderById() = runBlockingTest {
        dao.saveReminder(reminder1)

        // Verify reminder data from db
        val checkReminder = dao.getReminderById(reminder1.id)
        assertThat(checkReminder as ReminderDTO, notNullValue())

        assertThat(checkReminder.id, `is`(reminder1.id))
        assertThat(checkReminder.title, `is`(reminder1.title))
        assertThat(checkReminder.description, `is`(reminder1.description))
        assertThat(checkReminder.location, `is`(reminder1.location))
        assertThat(checkReminder.latitude, `is`(reminder1.latitude))
        assertThat(checkReminder.longitude, `is`(reminder1.longitude))
    }

    /*
     * Test getReminders: Given multiple reminders, verify that they are inserted into the db and retrieved correctly
     */
    @Test
    fun testGetReminders() = runBlockingTest {
        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)

        Truth.assertThat(dao.getReminders()).hasSize(2)
        Truth.assertThat(dao.getReminders()).contains(reminder1)
        Truth.assertThat(dao.getReminders()).contains(reminder2)

        // Verify reminder 1
        val checkReminder1 = dao.getReminderById(reminder1.id)
        assertThat(checkReminder1 as ReminderDTO, notNullValue())
        assertThat(checkReminder1.id, `is`(reminder1.id))
        assertThat(checkReminder1.title, `is`(reminder1.title))
        assertThat(checkReminder1.description, `is`(reminder1.description))
        assertThat(checkReminder1.location, `is`(reminder1.location))
        assertThat(checkReminder1.latitude, `is`(reminder1.latitude))
        assertThat(checkReminder1.longitude, `is`(reminder1.longitude))

        // Verify reminder 2
        val checkReminder2 = dao.getReminderById(reminder2.id)
        assertThat(checkReminder2 as ReminderDTO, notNullValue())
        assertThat(checkReminder2.id, `is`(reminder2.id))
        assertThat(checkReminder2.title, `is`(reminder2.title))
        assertThat(checkReminder2.description, `is`(reminder2.description))
        assertThat(checkReminder2.location, `is`(reminder2.location))
        assertThat(checkReminder2.latitude, `is`(reminder2.latitude))
        assertThat(checkReminder2.longitude, `is`(reminder2.longitude))
    }

    /*
     * Given that multiple reminders were into the db, verify that they can be deleted
     */
    @Test
    fun testDeleteReminders() = runBlockingTest {
        // Save reminders
        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)

        // Delete reminders
        dao.deleteAllReminders()

        // Retrieve reminders
        val checkReminderList = dao.getReminders()

        // Assertions
        assertThat(checkReminderList.isEmpty(), `is`(true))
    }
}