package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito
import org.mockito.Mockito.verify

// done TODO: test the navigation of the fragments.
// done TODO: test the displayed data on the UI.
// done TODO: add testing for the error messages.

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()
        appContext = getApplicationContext()
        startKoin {
            androidContext(appContext)
            modules(listOf(setupTestModules))
        }

        repository = get()

        // Perform cleanup before running in case the repository has data in it
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            repository.deleteAllReminders()
        }
        stopKoin()
    }

    // Set up test modules
    private val setupTestModules = module {
        viewModel {
            RemindersListViewModel(
                appContext,
                get()
            )
        }

        single {
            SaveReminderViewModel(
                appContext,
                get()
            )
        }

        single { LocalDB.createRemindersDao(appContext) }

        single<ReminderDataSource> { RemindersLocalRepository(get()) }
    }

    // Create reminder test data
    private val reminder1 = ReminderDTO(
        id = "1000",
        title = "Title 1",
        description = "Description 1",
        location = "Location 1",
        latitude = 38.976560,
        longitude = -77.355760
    )

    // Given first launch and empty repository, verify 'no data' indicator
    @Test
    fun testVerifyNoDataIndicator() {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // Verify 'no data' is displayed
        onView(ViewMatchers.withText(getApplicationContext<Context>().getString(R.string.no_data))).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }

    // Given click on fab button, verify navigation to SaveReminder fragment
    @Test
    fun testNavigationToSaveReminder() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // Perform fab click
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Assertions
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    // Given a reminder in repository, verify that it is loaded into ReminderListFragment
    @Test
    fun testLoadReminder() {
        runBlocking {
            repository.saveReminder(reminder1)
        }

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // Assertions
        onView(ViewMatchers.withText(reminder1.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText(reminder1.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText(reminder1.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}