package com.example.data.catalog

import androidx.annotation.DrawableRes
import com.example.R
import com.example.data.model.Hotel
import com.example.data.model.HotelCategory
import com.example.data.model.RoomOption

object HotelCatalog {
  const val KEY_PALACE = "palace"
  const val KEY_BEACH = "beach"
  const val KEY_MOUNTAIN = "mountain"
  const val KEY_SUITE = "suite"

  @DrawableRes
  fun drawableForKey(imageKey: String): Int =
    when (imageKey) {
      KEY_PALACE -> R.drawable.img_hotel_palace_1794674450122
      KEY_BEACH -> R.drawable.img_hotel_beach_1794674456340
      KEY_MOUNTAIN -> R.drawable.img_hotel_mountain_1794674461108
      else -> R.drawable.img_hotel_suite_1794674449793
    }

  val hotels: List<Hotel> =
    listOf(
      Hotel(
        id = "hotel_jaipur_palace",
        name = "सूर्यगढ़ हेरिटेज पैलेस",
        location = "आमेर रोड, जल महल के पास, जयपुर, राजस्थान",
        price = 7500L,
        description =
          "गुलाबी नगरी जयपुर के शाही आंगन, बलुआ पत्थर के मेहराब और शांत वॉटर कोर्टयार्ड के बीच स्थित एक भव्य हेरिटेज अनुभव।",
        city = "जयपुर",
        state = "राजस्थान",
        categoryName = HotelCategory.PALACE.name,
        rating = 4.9,
        reviewCount = 418L,
        imageKey = KEY_PALACE,
        amenities = listOf("मुफ़्त Wi-Fi", "स्विमिंग पूल", "ब्रेकफास्ट", "रॉयल स्पा", "एयरपोर्ट पिकअप"),
        checkInTime = "2:00 PM",
        checkOutTime = "11:00 AM",
        featured = true,
        rooms =
          listOf(
            RoomOption(
              id = "jaipur_deluxe",
              name = "हेरिटेज डीलक्स कक्ष",
              bedType = "किंग साइज़ बेड",
              maxGuests = 2,
              pricePerNight = 7500,
              breakfastIncluded = true,
              highlights = listOf("आंगन दृश्य", "420 sq.ft", "मुफ़्त नाश्ता"),
            ),
            RoomOption(
              id = "jaipur_royal_suite",
              name = "महारानी रॉयल सुइट",
              bedType = "फोर-पोस्टर किंग बेड",
              maxGuests = 4,
              pricePerNight = 12800,
              breakfastIncluded = true,
              highlights = listOf("निजी झरोखा बालकनी", "750 sq.ft", "हाई-टी शामिल"),
            ),
          ),
      ),
      Hotel(
        id = "hotel_goa_resort",
        name = "नीलांबरी बीच रिसॉर्ट",
        location = "अगोड़ा कोव रोड, काबो डी रामा, दक्षिण गोवा",
        price = 6200L,
        description =
          "अरब सागर के शांत नीले पानी और नारियल के पेड़ों के सामने बना सफ़ेद पुर्तगाली शैली का बुटीक बीच रिसॉर्ट।",
        city = "गोवा",
        state = "दक्षिण गोवा",
        categoryName = HotelCategory.BEACH.name,
        rating = 4.8,
        reviewCount = 362L,
        imageKey = KEY_BEACH,
        amenities = listOf("इंफिनिटी पूल", "प्राइवेट बीच", "मुफ़्त Wi-Fi", "ब्रेकफास्ट", "सी-व्यू बार"),
        checkInTime = "2:00 PM",
        checkOutTime = "11:00 AM",
        featured = true,
        rooms =
          listOf(
            RoomOption(
              id = "goa_garden_villa",
              name = "पाम गार्डन विला",
              bedType = "किंग साइज़ बेड",
              maxGuests = 2,
              pricePerNight = 6200,
              breakfastIncluded = true,
              highlights = listOf("प्राइवेट वरांडा", "480 sq.ft", "पूल एक्सेस"),
            ),
            RoomOption(
              id = "goa_ocean_villa",
              name = "ओशन फ्रंट पूल विला",
              bedType = "किंग साइज़ बेड + सोफा",
              maxGuests = 3,
              pricePerNight = 10900,
              breakfastIncluded = true,
              highlights = listOf("सीधा समुद्री दृश्य", "680 sq.ft", "फ्लोटिंग ब्रेकफास्ट"),
            ),
          ),
      ),
      Hotel(
        id = "hotel_manali_chalet",
        name = "हिमधारा पाइन शैले",
        location = "ओल्ड मनाली हिलटॉप, वशिष्ठ रोड, मनाली, हिमाचल प्रदेश",
        price = 4800L,
        description =
          "देवदार के जंगलों और बर्फ़ से ढकी हिमालयी चोटियों के मनोरम दृश्य वाला गर्म लकड़ी व पत्थर का पहाड़ी शैले।",
        city = "मनाली",
        state = "हिमाचल प्रदेश",
        categoryName = HotelCategory.MOUNTAIN.name,
        rating = 4.8,
        reviewCount = 289L,
        imageKey = KEY_MOUNTAIN,
        amenities = listOf("हीटेड रूम", "बोनफायर लाउंज", "मुफ़्त Wi-Fi", "माउंटेन कैफ़े", "पार्किंग"),
        checkInTime = "1:00 PM",
        checkOutTime = "11:00 AM",
        featured = true,
        rooms =
          listOf(
            RoomOption(
              id = "manali_timber",
              name = "कोज़ी पाइन व्यू रूम",
              bedType = "क्वीन साइज़ बेड",
              maxGuests = 2,
              pricePerNight = 4800,
              breakfastIncluded = true,
              highlights = listOf("रूम हीटर", "लकड़ी की बालकनी", "घाटी दृश्य"),
            ),
            RoomOption(
              id = "manali_loft",
              name = "स्नो पीक फ़ैमिली लॉफ़्ट",
              bedType = "2 डबल बेड",
              maxGuests = 4,
              pricePerNight = 8200,
              breakfastIncluded = true,
              highlights = listOf("फायरप्लेस", "700 sq.ft", "शाम की चाय शामिल"),
            ),
          ),
      ),
      Hotel(
        id = "hotel_mumbai_skyline",
        name = "ताजमहल अर्बन सुइट्स",
        location = "बांद्रा-कुर्ला कॉम्प्लेक्स, मुंबई, महाराष्ट्र",
        price = 8900L,
        description =
          "शहर के आधुनिक स्काईलाइन और शांत टेराकोटा-लिनन इंटीरियर के साथ बिज़नेस और परिवार दोनों के लिए प्रीमियम प्रवास।",
        city = "मुंबई",
        state = "महाराष्ट्र",
        categoryName = HotelCategory.LUXURY.name,
        rating = 4.7,
        reviewCount = 514L,
        imageKey = KEY_SUITE,
        amenities = listOf("हाई-स्पीड Wi-Fi", "फिटनेस सेंटर", "ब्रेकफास्ट", "रूफटॉप डाइनिंग", "स्पॉ"),
        checkInTime = "3:00 PM",
        checkOutTime = "12:00 PM",
        rooms =
          listOf(
            RoomOption(
              id = "mumbai_exec",
              name = "एग्ज़िक्यूटिव स्काईलाइन रूम",
              bedType = "किंग साइज़ बेड",
              maxGuests = 2,
              pricePerNight = 8900,
              breakfastIncluded = true,
              highlights = listOf("वर्क डेस्क", "फ्लोर-टू-सीलिंग विंडो", "लाउंज एक्सेस"),
            ),
            RoomOption(
              id = "mumbai_club",
              name = "प्रेसिडेंशियल क्लब सुइट",
              bedType = "किंग बेड + लिविंग एरिया",
              maxGuests = 3,
              pricePerNight = 14500,
              breakfastIncluded = true,
              highlights = listOf("बटलर सेवा", "820 sq.ft", "एयरपोर्ट ट्रांसफ़र"),
            ),
          ),
      ),
      Hotel(
        id = "hotel_udaipur_lake",
        name = "पिछोला राजमहल हवेली",
        location = "चांदपोल झील किनारा, उदयपुर, राजस्थान",
        price = 6900L,
        description =
          "झील पिछोला के किनारे पारंपरिक राजपूताना झरोखों, हस्तशिल्प और शांत रूफटॉप डिनर का अनूठा संगम।",
        city = "उदयपुर",
        state = "राजस्थान",
        categoryName = HotelCategory.PALACE.name,
        rating = 4.9,
        reviewCount = 335L,
        imageKey = KEY_PALACE,
        amenities = listOf("लेक व्यू टेरेस", "बोट राइड", "मुफ़्त Wi-Fi", "ब्रेकफास्ट", "स्विमिंग पूल"),
        checkInTime = "2:00 PM",
        checkOutTime = "11:00 AM",
        rooms =
          listOf(
            RoomOption(
              id = "udaipur_lake_room",
              name = "लेक व्यू हवेली कक्ष",
              bedType = "किंग साइज़ बेड",
              maxGuests = 2,
              pricePerNight = 6900,
              breakfastIncluded = true,
              highlights = listOf("झील का नज़ारा", "मार्बल बाथ", "मुफ़्त नाश्ता"),
            ),
            RoomOption(
              id = "udaipur_maharaja",
              name = "महाराजा जरोखा सुइट",
              bedType = "किंग बेड + सिटिंग",
              maxGuests = 4,
              pricePerNight = 11400,
              breakfastIncluded = true,
              highlights = listOf("प्राइवेट टेरेस", "बोटिंग वाउचर", "कपल स्पा डिस्काउंट"),
            ),
          ),
      ),
      Hotel(
        id = "hotel_rishikesh_retreat",
        name = "गंगातट वेलनेस रिट्रीट",
        location = "तपोवन रिवर बैंक, लक्ष्मण झूला के पास, ऋषिकेश, उत्तराखंड",
        price = 4200L,
        description =
          "कलकल बहती गंगा और हरी पहाड़ियों के बीच सुबह के योग, ध्यान और आरामदायक प्रवास का शांतिपूर्ण ठिकाना।",
        city = "ऋषिकेश",
        state = "उत्तराखंड",
        categoryName = HotelCategory.MOUNTAIN.name,
        rating = 4.7,
        reviewCount = 247L,
        imageKey = KEY_MOUNTAIN,
        amenities = listOf("योग सेशन", "रिवर व्यू", "सात्विक ब्रेकफास्ट", "मुफ़्त Wi-Fi", "आयुर्वेद स्पा"),
        checkInTime = "1:00 PM",
        checkOutTime = "10:30 AM",
        rooms =
          listOf(
            RoomOption(
              id = "rishikesh_serenity",
              name = "सेरेनिटी रिवर कॉटेज",
              bedType = "क्वीन साइज़ बेड",
              maxGuests = 2,
              pricePerNight = 4200,
              breakfastIncluded = true,
              highlights = listOf("सुबह का योग शामिल", "नदी दृश्य बालकनी", "हर्बल टी"),
            ),
            RoomOption(
              id = "rishikesh_family",
              name = "आनंदा फ़ैमिली कॉटेज",
              bedType = "2 क्वीन बेड",
              maxGuests = 4,
              pricePerNight = 7100,
              breakfastIncluded = true,
              highlights = listOf("गार्डन डेक", "620 sq.ft", "आयुर्वेद कंसल्टेशन"),
            ),
          ),
      ),
      Hotel(
        id = "hotel_kerala_lagoon",
        name = "माराकोव लैगून विला",
        location = "फोर्ट कोच्चि बीच रोड, एर्नाकुलम, केरल",
        price = 5400L,
        description =
          "केरल के शांत बैकवाटर और समुद्री हवाओं के बीच पारंपरिक लकड़ी की वास्तुकला वाला सुकून भरा रिसॉर्ट।",
        city = "कोच्चि",
        state = "केरल",
        categoryName = HotelCategory.BEACH.name,
        rating = 4.8,
        reviewCount = 198L,
        imageKey = KEY_BEACH,
        amenities = listOf("बैकवाटर व्यू", "स्विमिंग पूल", "आयुर्वेद", "मुफ़्त Wi-Fi", "ब्रेकफास्ट"),
        checkInTime = "2:00 PM",
        checkOutTime = "11:00 AM",
        rooms =
          listOf(
            RoomOption(
              id = "kochi_veranda",
              name = "लैगून वरांडा कक्ष",
              bedType = "किंग साइज़ बेड",
              maxGuests = 2,
              pricePerNight = 5400,
              breakfastIncluded = true,
              highlights = listOf("बैकवाटर डेक", "केरल ब्रेकफास्ट", "मुफ़्त Wi-Fi"),
            ),
            RoomOption(
              id = "kochi_pool_villa",
              name = "हेरिटेज प्लंज पूल विला",
              bedType = "किंग साइज़ बेड",
              maxGuests = 3,
              pricePerNight = 9200,
              breakfastIncluded = true,
              highlights = listOf("प्राइवेट प्लंज पूल", "710 sq.ft", "सनसेट क्रूज़"),
            ),
          ),
      ),
      Hotel(
        id = "hotel_delhi_boutique",
        name = "लोधी गार्डन रेजीडेंस",
        location = "जोर बाग, लोधी रोड के पास, नई दिल्ली",
        price = 5800L,
        description =
          "हरा-भरा शांत इलाका, आधुनिक कला से सजे कमरे और दिल्ली के प्रमुख दर्शनीय स्थलों तक आसान पहुँच।",
        city = "नई दिल्ली",
        state = "दिल्ली",
        categoryName = HotelCategory.LUXURY.name,
        rating = 4.6,
        reviewCount = 276L,
        imageKey = KEY_SUITE,
        amenities = listOf("मेट्रो के पास", "मुफ़्त Wi-Fi", "ब्रेकफास्ट", "कंसियर्ज", "पार्किंग"),
        checkInTime = "2:00 PM",
        checkOutTime = "12:00 PM",
        rooms =
          listOf(
            RoomOption(
              id = "delhi_studio",
              name = "बुटीक स्टूडियो रूम",
              bedType = "किंग साइज़ बेड",
              maxGuests = 2,
              pricePerNight = 5800,
              breakfastIncluded = true,
              highlights = listOf("गार्डन व्यू", "एयर प्यूरीफायर", "मुफ़्त नाश्ता"),
            ),
            RoomOption(
              id = "delhi_terrace",
              name = "सिग्नेचर टेरेस सुइट",
              bedType = "किंग बेड + लाउंज",
              maxGuests = 3,
              pricePerNight = 9600,
              breakfastIncluded = true,
              highlights = listOf("निजी छत बालकनी", "640 sq.ft", "लेट चेक-आउट"),
            ),
          ),
      ),
    )

  fun findById(hotelId: String): Hotel? = hotels.firstOrNull { it.id == hotelId }
}
