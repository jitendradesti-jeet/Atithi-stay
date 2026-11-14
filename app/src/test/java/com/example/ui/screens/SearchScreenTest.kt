package com.example.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.ui.theme.AtithiStayTheme
import com.example.ui.viewmodel.StaySearchCriteria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun searchScreen_validCityAndDates_triggersStartSearch() {
    var capturedCity: String? = null
    var capturedCheckIn: Long? = null
    var capturedCheckOut: Long? = null

    composeTestRule.setContent {
      AtithiStayTheme {
        SearchScreen(
          userDisplayName = "आरव",
          criteria = StaySearchCriteria(checkInEpochDay = 20400L, checkOutEpochDay = 20402L),
          onStartSearch = { city, checkIn, checkOut, _, _ ->
            capturedCity = city
            capturedCheckIn = checkIn
            capturedCheckOut = checkOut
          },
        )
      }
    }

    composeTestRule.onNodeWithTag("search_city_input").performTextInput("जयपुर")
    composeTestRule.onNodeWithTag("search_checkin_input").performTextClearance()
    composeTestRule.onNodeWithTag("search_checkin_input").performTextInput("2026-12-01")
    composeTestRule.onNodeWithTag("search_checkout_input").performTextClearance()
    composeTestRule.onNodeWithTag("search_checkout_input").performTextInput("2026-12-04")
    composeTestRule.onNodeWithTag("search_hotels_button").performScrollTo().performClick()

    assertEquals("जयपुर", capturedCity)
    assertEquals(parseIsoDateToEpochDay("2026-12-01"), capturedCheckIn)
    assertEquals(parseIsoDateToEpochDay("2026-12-04"), capturedCheckOut)
  }

  @Test
  fun searchScreen_invalidDateRange_showsErrorAndDoesNotSubmit() {
    var submitted = false

    composeTestRule.setContent {
      AtithiStayTheme {
        SearchScreen(
          userDisplayName = "आरव",
          criteria = StaySearchCriteria(checkInEpochDay = 20400L, checkOutEpochDay = 20402L),
          onStartSearch = { _, _, _, _, _ -> submitted = true },
        )
      }
    }

    composeTestRule.onNodeWithTag("search_checkin_input").performTextClearance()
    composeTestRule.onNodeWithTag("search_checkin_input").performTextInput("2026-12-05")
    composeTestRule.onNodeWithTag("search_checkout_input").performTextClearance()
    composeTestRule.onNodeWithTag("search_checkout_input").performTextInput("2026-12-03")
    composeTestRule.onNodeWithTag("search_hotels_button").performScrollTo().performClick()

    assertEquals(false, submitted)
    assertNull(parseIsoDateToEpochDay("invalid"))
    composeTestRule
      .onNodeWithText("चेक-आउट तारीख़, चेक-इन तारीख़ के बाद की होनी चाहिए")
      .assertIsDisplayed()
  }
}
