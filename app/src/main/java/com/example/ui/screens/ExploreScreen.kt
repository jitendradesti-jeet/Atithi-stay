package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Hotel
import com.example.data.model.HotelCategory
import com.example.ui.viewmodel.FormatUtils
import com.example.ui.viewmodel.SortOption
import com.example.ui.viewmodel.StaySearchCriteria
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
  userDisplayName: String,
  hotels: List<Hotel>,
  isLoading: Boolean,
  favoriteIds: Set<String>,
  criteria: StaySearchCriteria,
  onQueryChange: (String) -> Unit,
  onCategorySelect: (HotelCategory) -> Unit,
  onSortSelect: (SortOption) -> Unit,
  onDatesUpdate: (Long, Long) -> Unit,
  onGuestsRoomsUpdate: (Int, Int) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onHotelClick: (Hotel) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showStaySheet by remember { mutableStateOf(false) }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWide = maxWidth >= 680.dp

    if (isWide) {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        item(span = { GridItemSpan(2) }) {
          ExploreHeaderSection(
            userDisplayName = userDisplayName,
            criteria = criteria,
            onQueryChange = onQueryChange,
            onOpenStaySheet = { showStaySheet = true },
            onCategorySelect = onCategorySelect,
            onSortSelect = onSortSelect,
            resultsCount = hotels.size,
          )
        }
        if (isLoading && hotels.isEmpty()) {
          item(span = { GridItemSpan(2) }) {
            HotelsLoadingState()
          }
        } else if (hotels.isEmpty()) {
          item(span = { GridItemSpan(2) }) {
            EmptySearchState(onReset = {
              onQueryChange("")
              onCategorySelect(HotelCategory.ALL)
            })
          }
        } else {
          items(hotels, key = { it.id }) { hotel ->
            HotelListingCard(
              hotel = hotel,
              isFavorite = hotel.id in favoriteIds,
              nights = criteria.nights,
              onToggleFavorite = { onToggleFavorite(hotel.id) },
              onClick = { onHotelClick(hotel) },
            )
          }
        }
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        item {
          ExploreHeaderSection(
            userDisplayName = userDisplayName,
            criteria = criteria,
            onQueryChange = onQueryChange,
            onOpenStaySheet = { showStaySheet = true },
            onCategorySelect = onCategorySelect,
            onSortSelect = onSortSelect,
            resultsCount = hotels.size,
          )
        }

        if (isLoading && hotels.isEmpty()) {
          item {
            HotelsLoadingState()
          }
        } else if (hotels.isEmpty()) {
          item {
            EmptySearchState(onReset = {
              onQueryChange("")
              onCategorySelect(HotelCategory.ALL)
            })
          }
        } else {
          items(hotels, key = { it.id }) { hotel ->
            HotelListingCard(
              hotel = hotel,
              isFavorite = hotel.id in favoriteIds,
              nights = criteria.nights,
              onToggleFavorite = { onToggleFavorite(hotel.id) },
              onClick = { onHotelClick(hotel) },
            )
          }
        }
      }
    }
  }

  if (showStaySheet) {
    StayDatesAndGuestsSheet(
      criteria = criteria,
      onDismiss = { showStaySheet = false },
      onApply = { checkIn, checkOut, guests, rooms ->
        onDatesUpdate(checkIn, checkOut)
        onGuestsRoomsUpdate(guests, rooms)
        showStaySheet = false
      },
    )
  }
}

@Composable
private fun ExploreHeaderSection(
  userDisplayName: String,
  criteria: StaySearchCriteria,
  onQueryChange: (String) -> Unit,
  onOpenStaySheet: () -> Unit,
  onCategorySelect: (HotelCategory) -> Unit,
  onSortSelect: (SortOption) -> Unit,
  resultsCount: Int,
) {
  val firstName = userDisplayName.trim().substringBefore(" ").ifBlank { "यात्री" }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Column {
      Text(
        text = "नमस्ते, $firstName",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = "कहाँ ठहरना चाहेंगे?",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    OutlinedTextField(
      value = criteria.query,
      onValueChange = onQueryChange,
      placeholder = { Text("शहर या होटल खोजें (जैसे जयपुर, गोवा, मनाली)") },
      leadingIcon = {
        Icon(
          imageVector = Icons.Filled.Search,
          contentDescription = "खोजें",
          tint = MaterialTheme.colorScheme.primary,
        )
      },
      trailingIcon = {
        if (criteria.query.isNotEmpty()) {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(imageVector = Icons.Filled.Clear, contentDescription = "साफ़ करें")
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
      modifier = Modifier.fillMaxWidth().testTag("search_hotel_input"),
    )

    // Stay Dates & Guests Selector Bar
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable(onClick = onOpenStaySheet)
          .minimumInteractiveComponentSize()
          .testTag("stay_criteria_button"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text =
              "${FormatUtils.formatEpochDay(criteria.checkInEpochDay)} - ${FormatUtils.formatEpochDay(criteria.checkOutEpochDay)} (${criteria.nights} रात)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.Group,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${criteria.guestsCount} मेहमान · ${criteria.roomsCount} कमरा",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
        }
      }
    }

    // Categories
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(HotelCategory.entries) { category ->
        val selected = criteria.selectedCategory == category
        FilterChip(
          selected = selected,
          onClick = { onCategorySelect(category) },
          label = { Text(category.labelHi) },
          colors =
            FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            ),
          modifier = Modifier.testTag("category_chip_${category.name.lowercase()}"),
        )
      }
    }

    // Sort & Count row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "$resultsCount बेहतरीन होटल उपलब्ध",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(SortOption.entries) { option ->
          val active = criteria.sortOption == option
          Surface(
            shape = RoundedCornerShape(20.dp),
            color =
              if (active) MaterialTheme.colorScheme.tertiaryContainer
              else MaterialTheme.colorScheme.surfaceVariant,
            modifier =
              Modifier.clip(RoundedCornerShape(20.dp)).clickable { onSortSelect(option) },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (active) {
                Icon(
                  imageVector = Icons.Filled.Tune,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onTertiaryContainer,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = option.labelHi,
                style = MaterialTheme.typography.labelMedium,
                color =
                  if (active) MaterialTheme.colorScheme.onTertiaryContainer
                  else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun HotelListingCard(
  hotel: Hotel,
  isFavorite: Boolean,
  nights: Int,
  onToggleFavorite: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .clickable(onClick = onClick)
        .testTag("hotel_card_${hotel.id}"),
  ) {
    Column {
      Box(modifier = Modifier.fillMaxWidth().height(205.dp)) {
        Image(
          painter = painterResource(id = hotel.imageResId),
          contentDescription = "${hotel.name} की तस्वीर",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color(0x33000000), Color.Transparent, Color(0x99151210))
                )
              )
        )

        // Category badge top-left
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color.Black.copy(alpha = 0.55f),
          modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        ) {
          Text(
            text = hotel.category.labelHi,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          )
        }

        // Favorite toggle top-right
        Surface(
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.92f),
          modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
        ) {
          IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("favorite_button_${hotel.id}"),
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
              contentDescription = if (isFavorite) "पसंदीदा से हटाएं" else "पसंदीदा में जोड़ें",
              tint = if (isFavorite) Color(0xFFD32F2F) else Color(0xFF423A36),
            )
          }
        }

        // Rating badge bottom-left
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.secondaryContainer,
          modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.Star,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${hotel.rating} (${hotel.reviewCount})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }

      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = hotel.name,
          style = MaterialTheme.typography.titleLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text =
              if (hotel.city.isNotBlank() && hotel.state.isNotBlank()) {
                "${hotel.city}, ${hotel.state}"
              } else {
                hotel.location
              },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(hotel.amenities.take(3)) { amenity ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Text(
                text = amenity,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }
        }

        HorizontalDivider(
          color = MaterialTheme.colorScheme.outlineVariant,
          modifier = Modifier.padding(vertical = 2.dp),
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(
              text = "${FormatUtils.formatInr(hotel.startingPrice)} / रात",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "$nights रात कुल ~ ${FormatUtils.formatInr(hotel.startingPrice * nights)}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
          ) {
            Text(
              text = "कमरे देखें",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StayDatesAndGuestsSheet(
  criteria: StaySearchCriteria,
  onDismiss: () -> Unit,
  onApply: (Long, Long, Int, Int) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val datePickerState =
    rememberDateRangePickerState(
      initialSelectedStartDateMillis = TimeUnit.DAYS.toMillis(criteria.checkInEpochDay),
      initialSelectedEndDateMillis = TimeUnit.DAYS.toMillis(criteria.checkOutEpochDay),
    )
  var guests by remember { mutableIntStateOf(criteria.guestsCount) }
  var rooms by remember { mutableIntStateOf(criteria.roomsCount) }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(
        text = "तारीख़ और मेहमान चुनें",
        style = MaterialTheme.typography.headlineSmall,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        StepperCard(
          label = "मेहमान",
          value = guests,
          min = 1,
          max = 12,
          onValueChange = { guests = it },
          modifier = Modifier.weight(1f),
        )
        StepperCard(
          label = "कमरे",
          value = rooms,
          min = 1,
          max = 6,
          onValueChange = { rooms = it },
          modifier = Modifier.weight(1f),
        )
      }

      DateRangePicker(
        state = datePickerState,
        title = null,
        headline = {
          val startDay =
            datePickerState.selectedStartDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it) }
              ?: criteria.checkInEpochDay
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
        TextButton(onClick = onDismiss) { Text("रद्द करें") }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            val startDay =
              datePickerState.selectedStartDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it) }
                ?: criteria.checkInEpochDay
            val endDay =
              datePickerState.selectedEndDateMillis?.let { TimeUnit.MILLISECONDS.toDays(it) }
                ?: (startDay + 1L)
            onApply(startDay, endDay, guests, rooms)
          },
          shape = RoundedCornerShape(14.dp),
        ) {
          Text("लागू करें")
        }
      }
    }
  }
}

@Composable
private fun StepperCard(
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
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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

@Composable
private fun HotelsLoadingState() {
  Column(
    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    androidx.compose.material3.CircularProgressIndicator()
    Text(
      text = "होटल लोड हो रहे हैं...",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun EmptySearchState(onReset: () -> Unit) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Icon(
      imageVector = Icons.Filled.Search,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(48.dp),
    )
    Text(
      text = "कोई होटल नहीं मिला",
      style = MaterialTheme.typography.titleLarge,
    )
    Text(
      text = "कृपया किसी अन्य शहर या श्रेणी के साथ खोजें",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Button(onClick = onReset, shape = RoundedCornerShape(14.dp)) {
      Text("सभी होटल दिखाएं")
    }
  }
}
