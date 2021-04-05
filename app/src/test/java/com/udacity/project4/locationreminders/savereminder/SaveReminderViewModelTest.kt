package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    // done TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private val fakeDataSource = FakeDataSource()

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val reminderDataItem1 = ReminderDataItem(
        id = "1000",
        title = "Title 1",
        description = "Description 1",
        location = "Location 1",
        latitude = 38.976560,
        longitude = -77.355760
    )

    @Before
    fun setupViewModel() {
        stopKoin()

        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun saveReminderResultSuccess() = runBlockingTest {
        // Setup
        saveReminderViewModel.saveReminder(reminderDataItem1)

        // Verify reminder item 1
        val resultReminderDataItem1 =
            fakeDataSource.getReminder(reminderDataItem1.id) as Result.Success

        // Assertions
        assertThat(resultReminderDataItem1.data.id, `is`(reminderDataItem1.id))
        assertThat(resultReminderDataItem1.data.title, `is`(reminderDataItem1.title))
        assertThat(resultReminderDataItem1.data.description, `is`(reminderDataItem1.description))
        assertThat(resultReminderDataItem1.data.location, `is`(reminderDataItem1.location))
        assertThat(resultReminderDataItem1.data.latitude, `is`(reminderDataItem1.latitude))
        assertThat(resultReminderDataItem1.data.longitude, `is`(reminderDataItem1.longitude))
    }

    @Test
    fun testSetLocation() = runBlockingTest {
        // Setup
        val latLong = LatLng(reminderDataItem1.latitude!!, reminderDataItem1.longitude!!)

        saveReminderViewModel.setLocation(latLong)

        // Assertions
        val lat = saveReminderViewModel.latitude.value
        val lon = saveReminderViewModel.longitude.value
        assertThat(lat, `is`(reminderDataItem1.latitude!!))
        assertThat(lon, `is`(reminderDataItem1.longitude!!))
    }

    @Test
    fun testSetSelectedLocationString() = runBlockingTest {
        // Setup
        val testStr = "this is my string"
        saveReminderViewModel.setSelectedLocationString(testStr)

        // Assertions
        val verifyStr = saveReminderViewModel.reminderSelectedLocationStr.value
        assertThat(testStr, `is`(verifyStr))
    }

    @Test
    fun testValidateNoTitle() = runBlockingTest {
        // Setup
        reminderDataItem1.title = ""
        val res = saveReminderViewModel.validateEnteredData(reminderDataItem1)

        // Assertions
        assertThat(res, `is`(false))
    }

    @Test
    fun testValidateNoLocationData() = runBlockingTest {
        // Setup
        reminderDataItem1.location = ""
        val res = saveReminderViewModel.validateEnteredData(reminderDataItem1)

        // Assertions
        assertThat(res, `is`(false))
    }
}