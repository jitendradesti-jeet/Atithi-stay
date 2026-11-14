package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Booking
import com.example.data.model.BookingRequest
import com.example.data.model.Hotel
import com.example.data.model.RoomOption
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenContainer
import com.example.ui.viewmodel.BookingActionState
import com.example.ui.viewmodel.FormatUtils
import com.example.ui.viewmodel.StaySearchCriteria

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HotelDetailScreen(
  hotel: Hotel,
  isFavorite: Boolean,
  criteria: StaySearchCriteria,
  defaultGuestName: String,
  bookingActionState: BookingActionState,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onConfirmBooking: (RoomOption, String, String?, String?) -> Unit,
  onDismissBookingResult: () -> Unit,
  onViewBookings: () -> Unit,
) {
  var selectedRoom by remember(hotel.id) { mutableStateOf(hotel.rooms.first()) }
  var guestName by remember(defaultGuestName) { mutableStateOf(defaultGuestName) }
  var guestPhone by remember { mutableStateOf("") }
  var specialRequests by remember { mutableStateOf("") }

  val previewRequest =
    remember(hotel, selectedRoom, criteria, guestName) {
      BookingRequest(
        hotel = hotel,
        room = selectedRoom,
        guestName = guestName,
        guestPhone = guestPhone,
        specialRequests = specialRequests,
        checkInEpochDay = criteria.checkInEpochDay,
        checkOutEpochDay = criteria.checkOutEpochDay,
        nights = criteria.nights,
        guestsCount = criteria.guestsCount,
        roomsCount = criteria.roomsCount,
      )
    }

  Scaffold(
    bottomBar = {
      Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 20.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text(
              text = "कुल देय राशि (${criteria.nights} रात)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = FormatUtils.formatInr(previewRequest.totalAmount),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          Button(
            onClick = {
              onConfirmBooking(selectedRoom, guestName, guestPhone, specialRequests)
            },
            enabled = bookingActionState !is BookingActionState.Submitting,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(52.dp).testTag("confirm_booking_button"),
          ) {
            if (bookingActionState is BookingActionState.Submitting) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("प्रोसेस हो रहा है...")
            } else {
              Text(
                text = "अभी बुक करें",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
      LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier.fillMaxSize().imePadding(),
      ) {
        item {
          Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            Image(
              painter = painterResource(id = hotel.imageResId),
              contentDescription = hotel.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
            Box(
              modifier =
                Modifier.fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(Color(0x88151210), Color.Transparent, Color(0xAA151210))
                    )
                  )
            )
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .statusBarsPadding()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.92f)) {
                IconButton(
                  onClick = onBack,
                  modifier = Modifier.minimumInteractiveComponentSize().testTag("detail_back_button"),
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "वापस जाएं",
                    tint = Color(0xFF201A17),
                  )
                }
              }

              Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.92f)) {
                IconButton(
                  onClick = onToggleFavorite,
                  modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                  Icon(
                    imageVector =
                      if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "पसंदीदा",
                    tint = if (isFavorite) Color(0xFFD32F2F) else Color(0xFF201A17),
                  )
                }
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Filled.Star,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${hotel.rating} · ${hotel.reviewCount} समीक्षाएँ",
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
          }
        }

        item {
          Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(text = hotel.name, style = MaterialTheme.typography.headlineMedium)
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.LocationOn,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = hotel.location,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Text(text = hotel.description, style = MaterialTheme.typography.bodyLarge)

            // Check-in / Check-out info
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "चेक-इन: ${hotel.checkInTime}  |  चेक-आउट: ${hotel.checkOutTime}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Medium,
                    )
                    Text(
                      text =
                        "${FormatUtils.formatEpochDay(criteria.checkInEpochDay)} से ${FormatUtils.formatEpochDay(criteria.checkOutEpochDay)} (${criteria.guestsCount} मेहमान · ${criteria.roomsCount} कमरा)",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            }

            // Amenities
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(text = "प्रमुख सुविधाएं", style = MaterialTheme.typography.titleMedium)
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                hotel.amenities.forEach { amenity ->
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                  ) {
                    Text(
                      text = amenity,
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                  }
                }
              }
            }

            // Room Selection
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(text = "कमरे का विकल्प चुनें", style = MaterialTheme.typography.titleMedium)
              hotel.rooms.forEach { room ->
                val isSelected = selectedRoom.id == room.id
                Card(
                  shape = RoundedCornerShape(18.dp),
                  border =
                    BorderStroke(
                      width = if (isSelected) 2.dp else 1.dp,
                      color =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
                  colors =
                    CardDefaults.cardColors(
                      containerColor =
                        if (isSelected)
                          MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                  modifier =
                    Modifier.fillMaxWidth()
                      .clip(RoundedCornerShape(18.dp))
                      .clickable { selectedRoom = room }
                      .testTag("room_option_${room.id}"),
                ) {
                  Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = room.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                      )
                      Text(
                        text = "${FormatUtils.formatInr(room.pricePerNight)}/रात",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                      )
                    }

                    Row(
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                          imageVector = Icons.Filled.Bed,
                          contentDescription = null,
                          modifier = Modifier.size(16.dp),
                          tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = room.bedType,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                      if (room.breakfastIncluded) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                            imageVector = Icons.Filled.FreeBreakfast,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = VerifiedGreen,
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = "नाश्ता शामिल",
                            style = MaterialTheme.typography.bodySmall,
                            color = VerifiedGreen,
                            fontWeight = FontWeight.Medium,
                          )
                        }
                      }
                    }

                    FlowRow(
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      room.highlights.forEach { tag ->
                        Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                          Text(
                            text = tag,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                          )
                        }
                      }
                    }
                  }
                }
              }
            }

            // Guest Details Form
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(text = "मेहमान का विवरण", style = MaterialTheme.typography.titleMedium)
              OutlinedTextField(
                value = guestName,
                onValueChange = { guestName = it },
                label = { Text("मुख्य मेहमान का पूरा नाम *") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("guest_name_input"),
              )
              OutlinedTextField(
                value = guestPhone,
                onValueChange = { guestPhone = it },
                label = { Text("मोबाइल नंबर (वैकल्पिक)") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("guest_phone_input"),
              )
              OutlinedTextField(
                value = specialRequests,
                onValueChange = { specialRequests = it },
                label = { Text("विशेष अनुरोध (जैसे अर्ली चेक-इन, शांत कमरा)") },
                maxLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("special_requests_input"),
              )
            }

            // Price Breakdown
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(text = "किराया विवरण", style = MaterialTheme.typography.titleMedium)
                PriceRow(
                  label =
                    "${FormatUtils.formatInr(selectedRoom.pricePerNight)} × ${criteria.nights} रात × ${criteria.roomsCount} कमरा",
                  amount = FormatUtils.formatInr(previewRequest.roomSubtotal),
                )
                PriceRow(
                  label = "कर एवं सेवा शुल्क (12% GST)",
                  amount = FormatUtils.formatInr(previewRequest.taxesAndFees),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                PriceRow(
                  label = "कुल देय राशि",
                  amount = FormatUtils.formatInr(previewRequest.totalAmount),
                  highlight = true,
                )
              }
            }

            AnimatedVisibility(visible = bookingActionState is BookingActionState.Error) {
              val msg = (bookingActionState as? BookingActionState.Error)?.message.orEmpty()
              Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text = msg,
                  color = MaterialTheme.colorScheme.onErrorContainer,
                  modifier = Modifier.padding(12.dp),
                )
              }
            }
          }
        }
      }
    }
  }

  if (bookingActionState is BookingActionState.Confirmed) {
    BookingSuccessDialog(
      booking = bookingActionState.booking,
      onDismiss = onDismissBookingResult,
      onViewBookings = onViewBookings,
    )
  }
}

@Composable
private fun PriceRow(label: String, amount: String, highlight: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style =
        if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
      fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
    )
    Text(
      text = amount,
      style =
        if (highlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
      fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
      color =
        if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun BookingSuccessDialog(
  booking: Booking,
  onDismiss: () -> Unit,
  onViewBookings: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Surface(shape = CircleShape, color = VerifiedGreenContainer, modifier = Modifier.size(56.dp)) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = VerifiedGreen,
            modifier = Modifier.size(34.dp),
          )
        }
      }
    },
    title = {
      Text(
        text = "बुकिंग कन्फ़र्म हो गई!",
        style = MaterialTheme.typography.headlineSmall,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "${booking.hotelName} में आपका कमरा सफलतापूर्वक बुक हो गया है।",
          style = MaterialTheme.typography.bodyMedium,
        )
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "बुकिंग कोड: ${booking.bookingCode}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text =
                "${FormatUtils.formatEpochDay(booking.checkInEpochDay)} - ${FormatUtils.formatEpochDay(booking.checkOutEpochDay)} · ${booking.roomType}",
              style = MaterialTheme.typography.bodySmall,
            )
            Text(
              text = "कुल राशि: ${FormatUtils.formatInr(booking.totalAmount)}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onViewBookings,
        modifier = Modifier.testTag("go_to_bookings_button"),
      ) {
        Text("मेरी बुकिंग देखें")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("ठीक है") }
    },
  )
}
