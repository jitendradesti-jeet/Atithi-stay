const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validHotelPayload(uid, overrides = {}) {
  const now = new Date();
  return {
    name: "सूर्यगढ़ हेरिटेज पैलेस",
    location: "आमेर रोड, जयपुर, राजस्थान",
    price: 7500,
    description: "शाही आंगन और शांत वॉटर कोर्टयार्ड वाला भव्य हेरिटेज होटल।",
    city: "जयपुर",
    state: "राजस्थान",
    categoryName: "PALACE",
    rating: 4.9,
    reviewCount: 418,
    imageKey: "palace",
    amenities: ["मुफ़्त Wi-Fi", "स्विमिंग पूल"],
    checkInTime: "2:00 PM",
    checkOutTime: "11:00 AM",
    rooms: [],
    featured: true,
    isActive: true,
    createdBy: uid,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function validBookingPayload(uid, overrides = {}) {
  const now = new Date();
  return {
    userId: uid,
    hotelId: "hotel_jaipur_palace",
    hotelName: "सूर्यगढ़ हेरिटेज पैलेस",
    hotelCity: "जयपुर",
    hotelImageKey: "palace",
    roomType: "रॉयल डीलक्स सुइट",
    guestName: "आरव शर्मा",
    guestPhone: "9876543210",
    specialRequests: "अर्ली चेक-इन",
    checkInEpochDay: 20400,
    checkOutEpochDay: 20403,
    nights: 3,
    guestsCount: 2,
    roomsCount: 1,
    pricePerNight: 7500,
    taxesAndFees: 2700,
    totalAmount: 25200,
    bookingCode: "ATH-4821",
    status: "confirmed",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Hotels: unauthenticated user cannot read or create hotels", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("hotels").where("isActive", "==", true).get()
  );
  await assertFails(
    unauthDb
      .collection("hotels")
      .doc("hotel_1")
      .set(validHotelPayload(ALICE_UID))
  );
});

test("Hotels: authenticated user can create hotel, query active hotels, and non-owner cannot modify", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const hotelRef = aliceDb.collection("hotels").doc("hotel_jaipur_palace");

  await assertSucceeds(hotelRef.set(validHotelPayload(ALICE_UID)));

  // Query aligned with rules (where isActive == true) succeeds
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("hotels").where("isActive", "==", true).get()
  );

  // Unfiltered query without isActive == true fails (rules are not filters)
  await assertFails(bobDb.collection("hotels").get());

  // Cross-user update/delete fails
  await assertFails(
    bobDb
      .collection("hotels")
      .doc("hotel_jaipur_palace")
      .update({ price: 100, updatedAt: new Date() })
  );
  await assertFails(
    bobDb.collection("hotels").doc("hotel_jaipur_palace").delete()
  );

  // Ghost field fails
  await assertFails(
    aliceDb
      .collection("hotels")
      .doc("hotel_bad")
      .set(validHotelPayload(ALICE_UID, { shadowFlag: true }))
  );
});

test("Unauthenticated user: cannot list or write bookings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("bookings").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookings")
      .doc("b1")
      .set(validBookingPayload(ALICE_UID))
  );
});

test("Owner can create, list, and cancel their own booking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bookingRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("bookings")
    .doc("booking_1");

  await assertSucceeds(bookingRef.set(validBookingPayload(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookings").get()
  );
  await assertSucceeds(
    bookingRef.update({
      status: "cancelled",
      updatedAt: new Date(),
    })
  );
});

test("Cross-user access: Bob cannot read, create, or update Alice's booking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aliceRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("bookings")
    .doc("booking_1");
  await assertSucceeds(aliceRef.set(validBookingPayload(ALICE_UID)));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const bobTargetsAliceRef = bobDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("bookings")
    .doc("booking_1");

  await assertFails(bobTargetsAliceRef.get());
  await assertFails(
    bobTargetsAliceRef.update({ status: "cancelled", updatedAt: new Date() })
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookings")
      .doc("booking_2")
      .set(validBookingPayload(BOB_UID))
  );
});

test("Shadow field and terminal state lock: rejects ghost fields and post-cancel edits", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("bookings")
    .doc("booking_bad");

  await assertFails(
    badRef.set(validBookingPayload(ALICE_UID, { isVerified: true }))
  );

  const validRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("bookings")
    .doc("booking_lock");
  await assertSucceeds(validRef.set(validBookingPayload(ALICE_UID)));
  await assertSucceeds(
    validRef.update({ status: "cancelled", updatedAt: new Date() })
  );
  await assertFails(
    validRef.update({ status: "confirmed", updatedAt: new Date() })
  );
});

test("Favorites: owner can save and remove favorite hotel; cross-user fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const favRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("favorites")
    .doc("hotel_goa_resort");

  await assertSucceeds(
    favRef.set({
      userId: ALICE_UID,
      hotelId: "hotel_goa_resort",
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("favorites")
      .doc("hotel_goa_resort")
      .delete()
  );

  await assertSucceeds(favRef.delete());
});
