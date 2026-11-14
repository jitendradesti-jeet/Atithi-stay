package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.HotelCatalog
import com.example.ui.viewmodel.FormatUtils
import com.example.ui.viewmodel.StaySearchCriteria
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private data class DestinationHighlight(
  val city: String,
  val subtitle: String,
  val imageResId: Int,
)

private val popularDestinations =
  listOf(
    DestinationHighlight("जयपुर", "शाही पैलेस", R.drawable.img_hotel_palace_1794674450122),
    DestinationHighlight("गोवा", "बीच रिसॉर्ट", R.drawable.img_hotel_beach_1794674456340),
    DestinationHighlight("मनाली", "हिमालय शैले", R.drawable.img_hotel_mountain_1794674461108),
    DestinationHighlight("मुंबई", "अर्बन सुइट्स", R.drawable.img_hotel_suite_1794674449793),
    DestinationHighlight("उदयपुर", "झील हवेली", R.drawable.img_hotel_palace_1794674450122),
    DestinationHighlight("ऋषिकेश", "रिवर रिट्रीट", R.drawable.img_hotel_mountain_1794674461108),
  )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
  userDisplayName: String,
  criteria: StaySearchCriteria,
  onStartSearch: (city: String, checkInEpochDay: Long, checkOutEpochDay: Long, guests: Int, rooms: Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  var cityInput by remember(criteria.query) { mutableStateOf(criteria.query) }
  var checkInEpochDay by remember(criteria.checkInEpochDay) { mutableStateOf(criteria.checkInEpochDay) }
  var checkOutEpochDay by remember(criteria.checkOutEpochDay) { mutableStateOf(criteria.checkOutEpochDay) }
  var checkInText by remember(criteria.checkInEpochDay) {
    mutableStateOf(epochDayToIsoString(criteria.checkInEpochDay))
  }
  var checkOutText by remember(criteria.checkOutEpochDay) {
    mutableStateOf(epochDayToIsoString(criteria.checkOutEpochDay))
  }
  var guestsCount by remember(criteria.guestsCount) { mutableIntStateOf(criteria.guestsCount) }
  var roomsCount by remember(criteria.roomsCount) { mutableIntStateOf(criteria.roomsCount) }
  var validationError by remember { mutableStateOf<String?>(null) }
  var showDateRangeSheet by remember { mutableStateOf(false) }

  val firstName = userDisplayName.trim().substringBefore(" ").ifBlank { "यात्री" }

  fun submitSearch() {
    val parsedStart = parseIsoDateToEpochDay(checkInText)
    val parsedEnd = parseIsoDateToEpochDay(checkOutText)
    if (parsedStart == null || parsedEnd == null) {
      validationError = "कृपया सही तारीख़ लिखें (YYYY-MM-DD) या कैलेंडर आइकन से चुनें"
      return
    }
    if (parsedEnd <= parsedStart) {
      validationError = "चेक-आउट तारीख़, चेक-इन तारीख़ के बाद की होनी चाहिए"
      return
    }
    validationError = null
    onStartSearch(cityInput.trim(), parsedStart, parsedEnd, guestsCount, roomsCount)
  }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .statusBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 14.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().widthIn(max = 640.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      // Hero Banner Card
      Box(
        modifier =
          Modifier.fillMaxWidth().height(168.dp).clip(RoundedCornerShape(24.dp)),
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_hotel_palace_1794674450122),
          contentDescription = "Atithi Stay हेरिटेज बैनर",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color(0x33151210), Color(0xCC151210))
                )
              )
        )
        Column(
          modifier =
            Modifier.align(Alignment.BottomStart).padding(horizontal = 18.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "नमस्ते, $firstName",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFFF6BE6D),
          )
          Text(
            text = "अपनी अगली ठहरने की जगह खोजें",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      // Search Form Card
      Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Text(
            text = "होटल बुकिंग शुरू करें",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )

          // City Text Field
          OutlinedTextField(
            value = cityInput,
            onValueChange = {
              cityInput = it
              validationError = null
            },
            label = { Text("शहर या गंतव्य (City)") },
            placeholder = { Text("जैसे जयपुर, गोवा, मनाली, उदयपुर...") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "शहर",
                tint = MaterialTheme.colorScheme.primary,
              )
            },
            trailingIcon = {
              if (cityInput.isNotEmpty()) {
                IconButton(
                  onClick = { cityInput = "" },
                  modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                  Icon(imageVector = Icons.Filled.Clear, contentDescription = "शहर साफ़ करें")
                }
              }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(14.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
              ),
            modifier = Modifier.fillMaxWidth().testTag("search_city_input"),
          )

          // Quick City Suggestions
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            listOf("जयपुर", "गोवा", "मनाली", "उदयपुर", "ऋषिकेश").forEach { city ->
              SuggestionChip(
                onClick = {
                  cityInput = city
                  validationError = null
                },
                label = { Text(city) },
                modifier = Modifier.testTag("city_chip_$city"),
              )
            }
          }

          // Date Range Text Fields (Check-in and Check-out)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedTextField(
              value = checkInText,
              onValueChange = { value ->
                checkInText = value
                validationError = null
                parseIsoDateToEpochDay(value)?.let { checkInEpochDay = it }
              },
              label = { Text("चेक-इन तारीख़") },
              placeholder = { Text("YYYY-MM-DD") },
              trailingIcon = {
                IconButton(
                  onClick = { showDateRangeSheet = true },
                  modifier =
                    Modifier.minimumInteractiveComponentSize()
                      .testTag("open_date_range_picker_button"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "चेक-इन तारीख़ चुनें",
                    tint = MaterialTheme.colorScheme.primary,
                  )
                }
              },
              singleLine = true,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f).testTag("search_checkin_input"),
            )

            OutlinedTextField(
              value = checkOutText,
              onValueChange = { value ->
                checkOutText = value
                validationError = null
                parseIsoDateToEpochDay(value)?.let { checkOutEpochDay = it }
              },
              label = { Text("चेक-आउट तारीख़") },
              placeholder = { Text("YYYY-MM-DD") },
              trailingIcon = {
                IconButton(
                  onClick = { showDateRangeSheet = true },
                  modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                  Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "चेक-आउट तारीख़ चुनें",
                    tint = MaterialTheme.colorScheme.primary,
                  )
                }
              },
              singleLine = true,
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(onDone = { submitSearch() }),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f).testTag("search_checkout_input"),
            )
          }

          // Date Range Calendar Shortcut Summary Pill
          val parsedIn = parseIsoDateToEpochDay(checkInText) ?: checkInEpochDay
          val parsedOut = parseIsoDateToEpochDay(checkOutText) ?: checkOutEpochDay
          val nightsCount = (parsedOut - parsedIn).coerceAtLeast(1L)
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showDateRangeSheet = true }
                .minimumInteractiveComponentSize()
                .testTag("date_range_summary_button"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.CalendarMonth,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text =
                    "${FormatUtils.formatEpochDay(parsedIn)} → ${FormatUtils.formatEpochDay(parsedOut)} ($nightsCount रात)",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                )
              }
              Text(
                text = "कैलेंडर से चुनें",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
          }

          // Guests & Rooms Steppers
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            SearchStepperBox(
              label = "मेहमान",
              value = guestsCount,
              min = 1,
              max = 12,
              onValueChange = { guestsCount = it },
              modifier = Modifier.weight(1f),
            )
            SearchStepperBox(
              label = "कमरे",
              value = roomsCount,
              min = 1,
              max = 6,
              onValueChange = { roomsCount = it },
              modifier = Modifier.weight(1f),
            )
          }

          AnimatedVisibility(visible = validationError != null) {
            Surface(
              color = MaterialTheme.colorScheme.errorContainer,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = validationError.orEmpty(),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(10.dp),
              )
            }
          }

          Button(
            onClick = { submitSearch() },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("search_hotels_button"),
          ) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "होटल खोजें",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }

      // Popular Destinations Showcase
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "लोकप्रिय गंतव्य",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        LazyRow(
          contentPadding = PaddingValues(end = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(popularDestinations, key = { it.city }) { destination ->
            Card(
              shape = RoundedCornerShape(18.dp),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier =
                Modifier.width(146.dp)
                  .height(150.dp)
                  .clip(RoundedCornerShape(18.dp))
                  .clickable {
                    cityInput = destination.city
                    val parsedStart = parseIsoDateToEpochDay(checkInText) ?: checkInEpochDay
                    val parsedEnd =
                      (parseIsoDateToEpochDay(checkOutText) ?: checkOutEpochDay).let {
                        if (it > parsedStart) it else parsedStart + 1L
                      }
                    onStartSearch(
                      destination.city,
                      parsedStart,
                      parsedEnd,
                      guestsCount,
                      roomsCount,
                    )
                  }
                  .testTag("destination_card_${destination.city}"),
            ) {
              Box(modifier = Modifier.fillMaxSize()) {
                Image(
                  painter = painterResource(id = destination.imageResId),
                  contentDescription = destination.city,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize(),
                )
                Box(
                  modifier =
                    Modifier.fillMaxSize()
                      .background(
                        Brush.verticalGradient(
                          colors = listOf(Color.Transparent, Color(0xCC151210))
                        )
                      )
                )
                Column(
                  modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                ) {
                  Text(
                    text = destination.city,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = destination.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                  )
                }
              }
            }
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Hotel,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text(
            text =
              "${HotelCatalog.hotels.size} सत्यापित प्रॉपर्टी उपलब्ध — तुरंत कन्फ़र्मेशन और मुफ़्त रद्दीकरण विकल्प के साथ।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }

  if (showDateRangeSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialStart = parseIsoDateToEpochDay(checkInText) ?: checkInEpochDay
    val initialEnd = parseIsoDateToEpochDay(checkOutText) ?: checkOutEpochDay
    val datePickerState =
      rememberDateRangePickerState(
        initialSelectedStartDateMillis = TimeUnit.DAYS.toMillis(initialStart),
        initialSelectedEndDateMillis = TimeUnit.DAYS.toMillis(initialEnd),
      )

    ModalBottomSheet(
      onDismissRequest = { showDateRangeSheet = false },
      sheetState = sheetState,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "चेक-इन और चेक-आउट तारीख़ चुनें",
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(horizontal = 4.dp),
        )
        DateRangePicker(
          state = datePickerState,
          title = null,
          headline = {
            val startDay =
              datePickerState.selectedStartDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it) }
                ?: initialStart
            val endDay =
              datePickerState.selectedEndDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it) }
                ?: (startDay + 1L)
            Text(
              text = "${FormatUtils.formatEpochDay(startDay)} → ${FormatUtils.formatEpochDay(endDay)}",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
          },
          showModeToggle = false,
          modifier = Modifier.fillMaxWidth().height(360.dp),
        )
        Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
          horizontalArrangement = Arrangement.End,
        ) {
          TextButton(onClick = { showDateRangeSheet = false }) { Text("रद्द करें") }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              val startDay =
                datePickerState.selectedStartDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it) }
                  ?: initialStart
              val endDay =
                (datePickerState.selectedEndDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it) }
                  ?: (startDay + 1L)).let { if (it > startDay) it else startDay + 1L }
              checkInEpochDay = startDay
              checkOutEpochDay = endDay
              checkInText = epochDayToIsoString(startDay)
              checkOutText = epochDayToIsoString(endDay)
              validationError = null
              showDateRangeSheet = false
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("apply_date_range_button"),
          ) {
            Text("चुनें")
          }
        }
      }
    }
  }
}

@Composable
private fun SearchStepperBox(
  label: String,
  value: Int,
  min: Int,
  max: Int,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(
          text = label,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "$value",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      Row {
        IconButton(
          onClick = { if (value > min) onValueChange(value - 1) },
          enabled = value > min,
          modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
          Icon(imageVector = Icons.Filled.Remove, contentDescription = "$label घटाएं")
        }
        IconButton(
          onClick = { if (value < max) onValueChange(value + 1) },
          enabled = value < max,
          modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
          Icon(imageVector = Icons.Filled.Add, contentDescription = "$label बढ़ाएं")
        }
      }
    }
  }
}

internal fun epochDayToIsoString(epochDay: Long): String {
  val formatter =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
  return formatter.format(Date(TimeUnit.DAYS.toMillis(epochDay)))
}

internal fun parseIsoDateToEpochDay(input: String): Long? {
  val trimmed = input.trim()
  if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(trimmed)) return null
  val formatter =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
      isLenient = false
      timeZone = TimeZone.getTimeZone("UTC")
    }
  val date = runCatching { formatter.parse(trimmed) }.getOrNull() ?: return null
  return TimeUnit.MILLISECONDS.toDays(date.time)
}
