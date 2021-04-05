package com.udacity.project4

import android.app.Application
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

// End-to-end test for RemindersActivity
@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()

        // Set up test modules
        val testModules = module {
            single {
                SaveReminderViewModel(
                        appContext,
                        get() as ReminderDataSource
                )
            }
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(appContext)
            }
        }

        // Create Koin modules
        startKoin {
            modules(listOf(testModules))
        }

        // Get repository
        repository = get()

        // Delete all reminders in case the repository isn't empty
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun tearDown() {
        // Delete all reminders
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    // Create test data
    private val reminder1 = ReminderDTO(
        id = "1000",
        title = "Title 1",
        description = "Description 1",
        location = "Location 1",
        latitude = 38.976560,
        longitude = -77.355760
    )

    // done TODO: add End to End testing to the app
    @Test
    fun testEndToEnd() = runBlocking {
        // Launch Reminders Activity
        val remindersActivity = ActivityScenario.launch(RemindersActivity::class.java)

        // Click fab to add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Verify navigation to Save Reminder fragment
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))

        // Enter data in reminder form
        onView(withId(R.id.reminderTitle)).perform(replaceText(reminder1.title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(reminder1.description))

        // Click select location
        onView(withId(R.id.selectLocation)).perform(click())

        // Verify that we are on Select Location fragment
        onView(withId(R.id.mapview_select_location)).check(matches(isDisplayed()))

        // Select location
        onView(withId(R.id.mapview_select_location)).perform(click())

        // Wait for select location fragment to close
        SystemClock.sleep(1000);

        // Verify that we are on Save Reminder fragment
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))

        // Save reminder
        onView(withId(R.id.saveReminder)).perform(click())

        // Close activity
        remindersActivity.close()
    }
}
