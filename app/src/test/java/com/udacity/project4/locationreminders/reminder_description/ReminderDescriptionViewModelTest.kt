package com.udacity.project4.locationreminders.reminder_description

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(AndroidJUnit4::class)
class ReminderDescriptionViewModelTest {

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var reminderDescriptionViewModel: ReminderDescriptionViewModel

    // Create test data
    private val reminderDataItem1 = ReminderDataItem(
        id = "1000",
        title = "Title 1",
        description = "Description 1",
        location = "Location 1",
        latitude = 38.976560,
        longitude = -77.355760
    )

    @Before
    fun setup() {
        stopKoin()

        reminderDescriptionViewModel =
            ReminderDescriptionViewModel(ApplicationProvider.getApplicationContext())
    }

    @After
    fun teardown() {
        reminderDescriptionViewModel.setReminder(null)
    }

    @Test
    fun setReminderTest() = runBlockingTest {
        // Setup
        reminderDescriptionViewModel.setReminder(reminderDataItem1)

        // Assertions
        assertThat(reminderDescriptionViewModel.title.value, `is`(reminderDataItem1.title))
        assertThat(reminderDescriptionViewModel.description.value, `is`(reminderDataItem1.description))
        assertThat(reminderDescriptionViewModel.location.value, `is`(reminderDataItem1.location))
        assertThat(reminderDescriptionViewModel.latitude.value, `is`(reminderDataItem1.latitude.toString()))
        assertThat(reminderDescriptionViewModel.longitude.value, `is`(reminderDataItem1.longitude.toString()))
    }
}