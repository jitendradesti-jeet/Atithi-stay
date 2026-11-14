package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.catalog.HotelCatalog
import com.example.data.repository.HotelBookingRepository
import com.example.data.repository.HotelRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookingsScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HotelDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.theme.AtithiStayTheme
import com.example.ui.viewmodel.HotelBookingViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object NavRoutes {
  const val EXPLORE = "explore"
  const val BOOKINGS = "bookings"
  const val SAVED = "saved"
  const val PROFILE = "profile"
  const val HOTEL_DETAIL = "hotel_detail/{hotelId}"

  fun hotelDetail(hotelId: String): String = "hotel_detail/$hotelId"
}

private enum class BottomNavDestination(
  val route: String,
  val labelHi: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
) {
  EXPLORE(NavRoutes.EXPLORE, "खोजें", Icons.Filled.Explore, Icons.Outlined.Explore),
  BOOKINGS(
    NavRoutes.BOOKINGS,
    "बुकिंग",
    Icons.Filled.ConfirmationNumber,
    Icons.Outlined.ConfirmationNumber,
  ),
  SAVED(NavRoutes.SAVED, "पसंदीदा", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
  PROFILE(NavRoutes.PROFILE, "प्रोफ़ाइल", Icons.Filled.Person, Icons.Outlined.Person),
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { AtithiStayTheme { AppContent() } }
  }
}

@Composable
fun AppContent(auth: FirebaseAuth = Firebase.auth) {
  val context = LocalContext.current
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val currentUser by
    auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

  LaunchedEffect(Unit) {
    if (auth.currentUser == null) {
      attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = {},
        onUnauthenticated = {},
        scope = this,
      )
    }
  }

  val user = currentUser
  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    key(user.uid) { MainAtithiStayApp(user = user, credentialManager = credentialManager) }
  }
}

@Composable
private fun MainAtithiStayApp(user: FirebaseUser, credentialManager: CredentialManager) {
  val viewModel: HotelBookingViewModel =
    viewModel(
      key = user.uid,
      factory =
        viewModelFactory {
          initializer {
            val app =
              checkNotNull(this[APPLICATION_KEY]) {
                "APPLICATION_KEY missing from CreationExtras"
              }
            val databaseId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            HotelBookingViewModel(
              hotelRepository = HotelRepository(db),
              repository = HotelBookingRepository(db),
              currentUserId = user.uid,
            )
          }
        },
    )

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val showTopLevelNav =
    BottomNavDestination.entries.any { it.route == currentRoute }

  val hotelsState by viewModel.hotelsState.collectAsStateWithLifecycle()
  val filteredHotels by viewModel.filteredHotels.collectAsStateWithLifecycle()
  val favoriteIds by viewModel.favoriteHotelIds.collectAsStateWithLifecycle()
  val favoriteHotels by viewModel.favoriteHotels.collectAsStateWithLifecycle()
  val searchCriteria by viewModel.searchCriteria.collectAsStateWithLifecycle()
  val bookingsState by viewModel.bookingsState.collectAsStateWithLifecycle()
  val bookingActionState by viewModel.bookingActionState.collectAsStateWithLifecycle()
  val bookingStats by viewModel.bookingStats.collectAsStateWithLifecycle()
  val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(actionMessage) {
    actionMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearActionMessage()
    }
  }

  val navigateToTopLevel: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 680.dp

    Scaffold(
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      bottomBar = {
        if (showTopLevelNav && !useNavigationRail) {
          NavigationBar {
            BottomNavDestination.entries.forEach { dest ->
              val selected = currentRoute == dest.route
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTopLevel(dest.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = dest.labelHi,
                  )
                },
                label = { Text(dest.labelHi) },
                modifier = Modifier.testTag("nav_${dest.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (showTopLevelNav && useNavigationRail) {
          NavigationRail {
            BottomNavDestination.entries.forEach { dest ->
              val selected = currentRoute == dest.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTopLevel(dest.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = dest.labelHi,
                  )
                },
                label = { Text(dest.labelHi) },
                modifier = Modifier.testTag("nav_${dest.route}"),
              )
            }
          }
        }

        NavHost(
          navController = navController,
          startDestination = NavRoutes.EXPLORE,
          modifier = Modifier.weight(1f).fillMaxSize(),
        ) {
          composable(NavRoutes.EXPLORE) {
            ExploreScreen(
              userDisplayName = user.displayName ?: "यात्री",
              hotels = filteredHotels,
              isLoading = hotelsState is UiState.Loading,
              favoriteIds = favoriteIds,
              criteria = searchCriteria,
              onQueryChange = viewModel::updateQuery,
              onCategorySelect = viewModel::selectCategory,
              onSortSelect = viewModel::selectSortOption,
              onDatesUpdate = viewModel::updateDates,
              onGuestsRoomsUpdate = viewModel::updateGuestsAndRooms,
              onToggleFavorite = viewModel::toggleFavorite,
              onHotelClick = { hotel ->
                viewModel.resetBookingActionState()
                navController.navigate(NavRoutes.hotelDetail(hotel.id))
              },
              modifier = Modifier.padding(top = 8.dp),
            )
          }

          composable(NavRoutes.BOOKINGS) {
            BookingsScreen(
              bookingsState = bookingsState,
              onCancelBooking = viewModel::cancelBooking,
              onUpdateNotes = viewModel::updateBookingNotes,
              onExploreHotels = { navigateToTopLevel(NavRoutes.EXPLORE) },
              modifier = Modifier.padding(top = 8.dp),
            )
          }

          composable(NavRoutes.SAVED) {
            SavedScreen(
              favoriteHotels = favoriteHotels,
              nights = searchCriteria.nights,
              onToggleFavorite = viewModel::toggleFavorite,
              onHotelClick = { hotel ->
                viewModel.resetBookingActionState()
                navController.navigate(NavRoutes.hotelDetail(hotel.id))
              },
              onExploreHotels = { navigateToTopLevel(NavRoutes.EXPLORE) },
              modifier = Modifier.padding(top = 8.dp),
            )
          }

          composable(NavRoutes.PROFILE) {
            ProfileScreen(
              displayName = user.displayName.orEmpty(),
              email = user.email.orEmpty(),
              activeBookingsCount = bookingStats.first,
              savedHotelsCount = bookingStats.second,
              onSignOut = {
                signOut(credentialManager = credentialManager, scope = coroutineScope)
              },
              modifier = Modifier.padding(top = 8.dp),
            )
          }

          composable(
            route = NavRoutes.HOTEL_DETAIL,
            arguments = listOf(navArgument("hotelId") { type = NavType.StringType }),
          ) { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId").orEmpty()
            val hotel = viewModel.findHotelById(hotelId) ?: HotelCatalog.hotels.first()
            HotelDetailScreen(
              hotel = hotel,
              isFavorite = hotel.id in favoriteIds,
              criteria = searchCriteria,
              defaultGuestName = user.displayName.orEmpty(),
              bookingActionState = bookingActionState,
              onBack = {
                viewModel.resetBookingActionState()
                navController.popBackStack()
              },
              onToggleFavorite = { viewModel.toggleFavorite(hotel.id) },
              onConfirmBooking = { room, guestName, phone, requests ->
                viewModel.bookHotel(hotel, room, guestName, phone, requests)
              },
              onDismissBookingResult = { viewModel.resetBookingActionState() },
              onViewBookings = {
                viewModel.resetBookingActionState()
                navController.popBackStack(NavRoutes.EXPLORE, inclusive = false)
                navigateToTopLevel(NavRoutes.BOOKINGS)
              },
            )
          }
        }
      }
    }
  }
}
