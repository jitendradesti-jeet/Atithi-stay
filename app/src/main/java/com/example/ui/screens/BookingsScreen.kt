package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.catalog.HotelCatalog
import com.example.data.model.Booking
import com.example.ui.theme.CancelledRed
import com.example.ui.theme.CancelledRedContainer
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenContainer
import com.example.ui.viewmodel.FormatUtils
import com.example.ui.viewmodel.UiState

@Composable
fun BookingsScreen(
  bookingsState: UiState<List<Booking>>,
  onCancelBooking: (String) -> Unit,
  onUpdateNotes: (String, String?, String?) -> Unit,
  onExploreHotels: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
  var bookingToViewPass by remember { mutableStateOf<Booking?>(null) }
  var bookingToEdit by remember { mutableStateOf<Booking?>(null) }

  Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
    Text(
      text = "मेरी बुकिंग",
      style = MaterialTheme.typography.headlineMedium,
    )
    Spacer(modifier = Modifier.height(10.dp))

    TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.background) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("आगामी / कन्फ़र्म") },
        modifier = Modifier.testTag("bookings_tab_confirmed"),
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("रद्द बुकिंग") },
        modifier = Modifier.testTag("bookings_tab_cancelled"),
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    when (bookingsState) {
      is UiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is UiState.Error -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = bookingsState.message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
      is UiState.Success -> {
        val targetStatus =
          if (selectedTab == 0) Booking.STATUS_CONFIRMED else Booking.STATUS_CANCELLED
        val filtered = bookingsState.data.filter { it.status == targetStatus }

        if (filtered.isEmpty()) {
          EmptyBookingsState(
            isCancelledTab = selectedTab == 1,
            onExploreHotels = onExploreHotels,
          )
        } else {
          LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
          ) {
            items(filtered, key = { it.id }) { booking ->
              BookingCard(
                booking = booking,
                onViewPass = { bookingToViewPass = booking },
                onEditNotes = { bookingToEdit = booking },
                onCancelClick = { bookingToCancel = booking },
              )
            }
          }
        }
      }
    }
  }

  bookingToCancel?.let { booking ->
    AlertDialog(
      onDismissRequest = { bookingToCancel = null },
      title = { Text("बुकिंग रद्द करें?") },
      text = {
        Text(
          "क्या आप वाकई ${booking.hotelName} (${booking.bookingCode}) की बुकिंग रद्द करना चाहते हैं?"
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onCancelBooking(booking.id)
            bookingToCancel = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CancelledRed),
          modifier = Modifier.testTag("confirm_cancel_booking_button"),
        ) {
          Text("हाँ, रद्द करें")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { bookingToCancel = null }) { Text("नहीं") }
      },
    )
  }

  bookingToViewPass?.let { booking ->
    BookingPassDialog(booking = booking, onDismiss = { bookingToViewPass = null })
  }

  bookingToEdit?.let { booking ->
    EditBookingNotesDialog(
      booking = booking,
      onDismiss = { bookingToEdit = null },
      onSave = { phone, notes ->
        onUpdateNotes(booking.id, phone, notes)
        bookingToEdit = null
      },
    )
  }
}

@Composable
private fun BookingCard(
  booking: Booking,
  onViewPass: () -> Unit,
  onEditNotes: () -> Unit,
  onCancelClick: () -> Unit,
) {
  val isConfirmed = booking.status == Booking.STATUS_CONFIRMED
  Card(
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp).testTag("booking_card_${booking.id}"),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(id = HotelCatalog.drawableForKey(booking.hotelImageKey)),
          contentDescription = booking.hotelName,
          contentScale = ContentScale.Crop,
          modifier = Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isConfirmed) VerifiedGreenContainer else CancelledRedContainer,
          ) {
            Text(
              text = if (isConfirmed) "कन्फ़र्म · ${booking.bookingCode}" else "रद्द · ${booking.bookingCode}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (isConfirmed) VerifiedGreen else CancelledRed,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
          Text(
            text = booking.hotelName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.LocationOn,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "${booking.hotelCity} · ${booking.roomType}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text =
              "${FormatUtils.formatEpochDay(booking.checkInEpochDay)} → ${FormatUtils.formatEpochDay(booking.checkOutEpochDay)} (${booking.nights} रात)",
            style = MaterialTheme.typography.bodyMedium,
          )
        }
        Text(
          text = FormatUtils.formatInr(booking.totalAmount),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = onViewPass,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).testTag("view_pass_button_${booking.id}"),
        ) {
          Icon(
            imageVector = Icons.Filled.ConfirmationNumber,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("बुकिंग पास")
        }

        if (isConfirmed) {
          OutlinedButton(
            onClick = onEditNotes,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("edit_booking_button_${booking.id}"),
          ) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = "अनुरोध बदलें",
              modifier = Modifier.size(16.dp),
            )
          }
          Button(
            onClick = onCancelClick,
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = CancelledRedContainer,
                contentColor = CancelledRed,
              ),
            modifier = Modifier.testTag("cancel_booking_button_${booking.id}"),
          ) {
            Text("रद्द करें")
          }
        }
      }
    }
  }
}

@Composable
private fun BookingPassDialog(booking: Booking, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("ई-बुकिंग पास (${booking.bookingCode})") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = booking.hotelName, style = MaterialTheme.typography.titleMedium)
        Text(
          text = "${booking.hotelCity} · ${booking.roomType}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider()
        Text("मेहमान: ${booking.guestName}")
        if (!booking.guestPhone.isNullOrBlank()) {
          Text("मोबाइल: ${booking.guestPhone}")
        }
        Text(
          "चेक-इन: ${FormatUtils.formatEpochDayFull(booking.checkInEpochDay)}\nचेक-आउट: ${FormatUtils.formatEpochDayFull(booking.checkOutEpochDay)}"
        )
        Text("मेहमान एवं कमरे: ${booking.guestsCount} मेहमान · ${booking.roomsCount} कमरा")
        if (!booking.specialRequests.isNullOrBlank()) {
          Text("विशेष अनुरोध: ${booking.specialRequests}")
        }
        HorizontalDivider()
        Text(
          text = "कुल भुगतान: ${FormatUtils.formatInr(booking.totalAmount)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) { Text("बंद करें") }
    },
  )
}

@Composable
private fun EditBookingNotesDialog(
  booking: Booking,
  onDismiss: () -> Unit,
  onSave: (String?, String?) -> Unit,
) {
  var phone by remember(booking.id) { mutableStateOf(booking.guestPhone.orEmpty()) }
  var notes by remember(booking.id) { mutableStateOf(booking.specialRequests.orEmpty()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("मेहमान अनुरोध अपडेट करें") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("मोबाइल नंबर") },
          singleLine = true,
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("विशेष अनुरोध") },
          maxLines = 3,
        )
      }
    },
    confirmButton = {
      Button(onClick = { onSave(phone, notes) }) { Text("सेव करें") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("रद्द करें") }
    },
  )
}

@Composable
private fun EmptyBookingsState(isCancelledTab: Boolean, onExploreHotels: () -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Filled.Hotel,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(56.dp),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = if (isCancelledTab) "कोई रद्द बुकिंग नहीं है" else "अभी तक कोई बुकिंग नहीं है",
      style = MaterialTheme.typography.titleLarge,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text =
        if (isCancelledTab) "आपके द्वारा रद्द की गई बुकिंग यहाँ दिखाई देंगी।"
        else "अपना पसंदीदा होटल चुनें और कुछ ही सेकंड में कमरा बुक करें।",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (!isCancelledTab) {
      Spacer(modifier = Modifier.height(18.dp))
      Button(onClick = onExploreHotels, shape = RoundedCornerShape(14.dp)) {
        Text("होटल खोजें")
      }
    }
  }
}
