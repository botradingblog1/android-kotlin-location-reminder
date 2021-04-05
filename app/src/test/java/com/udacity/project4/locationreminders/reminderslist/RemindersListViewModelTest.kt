package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
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

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {
    private val fakeDataSource = FakeDataSource()
    private lateinit var viewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Create  test data
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
        stopKoin()

        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

        runBlockingTest {
            fakeDataSource.deleteAllReminders()
        }
    }

    @After
    fun tearDown() {}

    /*
     * Given that the repository has no reminders, the 'No data' indicator should show
     */
    @Test
    fun loadReminders_showNoData() = runBlockingTest {
        viewModel.loadReminders()

        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    /*
     * Given that the repository has reminders, the 'No data' indicator should not show
     */
    @Test
    fun loadReminders_notShowNoData() = runBlockingTest {
        fakeDataSource.saveReminder(reminder1)

        viewModel.loadReminders()

        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    /*
     * Given that reminders are being loaded, the 'show loading' indicator should first show and then hide when loading is done
     */
    @Test
    fun loadReminders_showLoading() = runBlockingTest {
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)

        // Pause dispatcher to verify loading
        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()

        // Check that loading indicator is visible
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Resume
        mainCoroutineRule.resumeDispatcher()

        // Check that loading indicator is hidden
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    /*
     * Given that a reminder exists in the data source, verify that it is loaded into reminder list
     */
    @Test
    fun loadReminders_loadSingleReminder() = runBlockingTest {
        fakeDataSource.saveReminder(reminder1)

        viewModel.loadReminders()

        val checkRemindersList = viewModel.remindersList.value!!
        val checkReminder = checkRemindersList[0]

        assertThat(checkReminder.id, `is`(reminder1.id))
        assertThat(checkReminder.title, `is`(reminder1.title))
        assertThat(checkReminder.description, `is`(reminder1.description))
        assertThat(checkReminder.location, `is`(reminder1.location))
        assertThat(checkReminder.latitude, `is`(reminder1.latitude))
        assertThat(checkReminder.longitude, `is`(reminder1.longitude))
    }

    /*
     * Given that multiple reminder exists in the data source, verify that they are loaded into reminder list
    */
    @Test
    fun loadReminders_loadMultipleReminders() = runBlockingTest {
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)

        viewModel.loadReminders()

        val checkRemindersList = viewModel.remindersList.value!!
        val checkReminder1 = checkRemindersList[0]

        assertThat(checkReminder1.id, `is`(reminder1.id))
        assertThat(checkReminder1.title, `is`(reminder1.title))
        assertThat(checkReminder1.description, `is`(reminder1.description))
        assertThat(checkReminder1.location, `is`(reminder1.location))
        assertThat(checkReminder1.latitude, `is`(reminder1.latitude))
        assertThat(checkReminder1.longitude, `is`(reminder1.longitude))

        val checkReminder2 = checkRemindersList[1]
        assertThat(checkReminder2.id, `is`(reminder2.id))
        assertThat(checkReminder2.title, `is`(reminder2.title))
        assertThat(checkReminder2.description, `is`(reminder2.description))
        assertThat(checkReminder2.location, `is`(reminder2.location))
        assertThat(checkReminder2.latitude, `is`(reminder2.latitude))
        assertThat(checkReminder2.longitude, `is`(reminder2.longitude))
    }
}