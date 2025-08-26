# Bhilai Sample Location Data

This document contains sample data for testing the Aalay Bhilai APK package with realistic room and mess locations.

## 🏠 Sample Room Locations

```json
[
  {
    "id": "bhilai-room-001",
    "type": "room",
    "name": "Comfort PG for Boys",
    "latitude": 21.2181,
    "longitude": 81.3248,
    "address": "123 Nehru Nagar, Bhilai Sector 1",
    "description": "Clean and comfortable PG accommodation for male students near NIT Raipur.",
    "pricePerMonth": 3500,
    "rating": 4.2,
    "totalRatings": 28,
    "amenities": ["WiFi", "Meals", "Security", "Laundry", "Study Room"],
    "contactPhone": "+91-9876543210",
    "contactEmail": "comfort.pg@example.com",
    "isVerified": true,
    "isFeatured": true,
    "availability": "available",
    "images": [
      "https://example.com/images/comfort-pg-1.jpg",
      "https://example.com/images/comfort-pg-2.jpg"
    ],
    "nearbyColleges": ["NIT Raipur", "Bhilai Institute of Technology"]
  },
  {
    "id": "bhilai-room-002", 
    "type": "room",
    "name": "Student Haven Girls Hostel",
    "latitude": 21.2095,
    "longitude": 81.3156,
    "address": "456 Smriti Nagar, Bhilai Sector 4",
    "description": "Safe and secure hostel for female students with 24/7 security.",
    "pricePerMonth": 4200,
    "rating": 4.5,
    "totalRatings": 42,
    "amenities": ["WiFi", "Meals", "24x7 Security", "AC Rooms", "Recreation Room"],
    "contactPhone": "+91-9876543211",
    "contactEmail": "studenthaven@example.com",
    "isVerified": true,
    "isFeatured": false,
    "availability": "limited",
    "images": [
      "https://example.com/images/student-haven-1.jpg"
    ],
    "nearbyColleges": ["Bhilai Mahila Mahavidyalaya", "Govt. Engineering College"]
  },
  {
    "id": "bhilai-room-003",
    "type": "room", 
    "name": "Economy Sharing Rooms",
    "latitude": 21.2268,
    "longitude": 81.3425,
    "address": "789 Power House Road, Bhilai Sector 8",
    "description": "Budget-friendly sharing accommodation for students.",
    "pricePerMonth": 2500,
    "rating": 3.8,
    "totalRatings": 15,
    "amenities": ["WiFi", "Common Kitchen", "Security"],
    "contactPhone": "+91-9876543212",
    "isVerified": false,
    "isFeatured": false,
    "availability": "available",
    "nearbyColleges": ["Bhilai Institute of Technology"]
  },
  {
    "id": "bhilai-room-004",
    "type": "room",
    "name": "Premium Student Residency",
    "latitude": 21.1987,
    "longitude": 81.3089,
    "address": "321 Civil Lines, Bhilai Sector 2",
    "description": "Premium accommodation with modern amenities for discerning students.",
    "pricePerMonth": 6500,
    "rating": 4.7,
    "totalRatings": 33,
    "amenities": ["WiFi", "AC", "Gym", "Swimming Pool", "Cafeteria", "Study Lounge"],
    "contactPhone": "+91-9876543213",
    "contactEmail": "premium.residency@example.com",
    "isVerified": true,
    "isFeatured": true,
    "availability": "available",
    "images": [
      "https://example.com/images/premium-1.jpg",
      "https://example.com/images/premium-2.jpg",
      "https://example.com/images/premium-3.jpg"
    ],
    "nearbyColleges": ["NIT Raipur", "MATS University"]
  }
]
```

## 🍽️ Sample Mess Locations

```json
[
  {
    "id": "bhilai-mess-001",
    "type": "mess",
    "name": "Sagar Mess - Home Food",
    "latitude": 21.2156,
    "longitude": 81.3201,
    "address": "234 Station Road, Bhilai Sector 6",
    "description": "Authentic home-style meals with variety of North and South Indian dishes.",
    "pricePerMonth": 2800,
    "rating": 4.3,
    "totalRatings": 67,
    "amenities": ["Breakfast", "Lunch", "Dinner", "Tiffin Service", "Special Diet"],
    "contactPhone": "+91-9876543214",
    "contactEmail": "sagarmess@example.com",
    "isVerified": true,
    "isFeatured": true,
    "availability": "available",
    "images": [
      "https://example.com/images/sagar-mess-1.jpg"
    ],
    "nearbyColleges": ["NIT Raipur", "Bhilai Institute of Technology"]
  },
  {
    "id": "bhilai-mess-002",
    "type": "mess",
    "name": "Annapurna Vegetarian Mess",
    "latitude": 21.2034,
    "longitude": 81.3298,
    "address": "567 Temple Street, Bhilai Sector 3", 
    "description": "Pure vegetarian mess serving fresh and healthy meals daily.",
    "pricePerMonth": 2200,
    "rating": 4.0,
    "totalRatings": 45,
    "amenities": ["Pure Veg", "Jain Food", "Breakfast", "Lunch", "Dinner"],
    "contactPhone": "+91-9876543215",
    "isVerified": true,
    "isFeatured": false,
    "availability": "available",
    "nearbyColleges": ["Bhilai Mahila Mahavidyalaya"]
  },
  {
    "id": "bhilai-mess-003",
    "type": "mess",
    "name": "Student Special Canteen",
    "latitude": 21.2301,
    "longitude": 81.3476,
    "address": "890 College Road, Bhilai Sector 9",
    "description": "Budget-friendly canteen popular among local students.",
    "pricePerMonth": 1800,
    "rating": 3.5,
    "totalRatings": 23,
    "amenities": ["Breakfast", "Lunch", "Snacks", "Tea/Coffee"],
    "contactPhone": "+91-9876543216",
    "isVerified": false,
    "isFeatured": false,
    "availability": "available",
    "nearbyColleges": ["Govt. Engineering College"]
  }
]
```

## 🗺️ Coordinate Reference Map

### Bhilai City Boundaries
- **North Boundary**: 21.3° N
- **South Boundary**: 21.1° N  
- **East Boundary**: 81.4° E
- **West Boundary**: 81.2° E

### Key Locations
- **City Center**: 21.2181° N, 81.3248° E
- **NIT Raipur Campus**: 21.2144° N, 81.3367° E
- **Railway Station**: 21.2089° N, 81.3678° E
- **Bus Stand**: 21.2156° N, 81.3456° E

### Distance Calculations
All sample locations are within:
- **5km radius** from city center
- **Walking distance** (≤2km): bhilai-room-001, bhilai-mess-001, bhilai-mess-002
- **Auto/Bus distance** (>2km): bhilai-room-003, bhilai-room-004, bhilai-mess-003

## 📱 Testing Instructions

### 1. Database Population
When the app starts for the first time, these sample locations should be loaded into the Room database for testing.

### 2. Navigation Testing
Each location should generate the following Google Maps URLs:

**Room 001 (Comfort PG)**:
```
https://www.google.com/maps/search/?api=1&query=21.2181,81.3248
```

**Mess 001 (Sagar Mess)**:
```
https://www.google.com/maps/search/?api=1&query=21.2156,81.3201
```

### 3. UI Testing Scenarios

#### Search Functionality
- Search "PG" → Should return room-type locations
- Search "mess" → Should return mess-type locations  
- Search "Sagar" → Should return Sagar Mess
- Search "Sector 1" → Should return locations in that area

#### Filter Testing
- Filter by "Room" → 4 results
- Filter by "Mess" → 3 results
- Filter by "All" → 7 results

#### Distance Verification
- **Comfort PG**: 0.0 km (at center)
- **Sagar Mess**: ~0.5 km from center
- **Premium Residency**: ~2.1 km from center

#### Price Range Testing
- Budget (₹1800-2500): Economy Sharing, Student Special Canteen
- Mid-range (₹2800-4200): Most locations
- Premium (₹6500+): Premium Student Residency

### 4. Doodle UI Elements to Verify

#### Visual Elements
- **Room Cards**: 🏠 icon with blue accent color
- **Mess Cards**: 🍽️ icon with coral accent color
- **Navigation Buttons**: 🗺️ Navigate to [Name] with underlined cursive text
- **Distance Indicators**: 🚶‍♂️ Walking for nearby locations

#### Color Scheme
- **Primary Blue**: #1E88E5
- **Secondary Coral**: #FF6B6B  
- **Success Green**: #4ECDC4 (for verified listings)
- **Background Cream**: #FFFBF7

#### Typography
- **Font Family**: Cursive for all text
- **Navigation Links**: Underlined with doodle-style appearance
- **Headers**: Bold cursive with emoji prefixes

## 🚀 Quick Start Testing

### 1. Build and Install
```bash
./gradlew assembleDebug -Penv=dev
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Launch App
- Open Aalay app
- Navigate to Bhilai Locations screen
- Verify sample data loads

### 3. Test Navigation
- Tap on any location card
- Verify Google Maps opens with correct coordinates
- Test both rooms and mess locations

### 4. Test Features
- Use search functionality
- Toggle between room/mess filters
- Check distance calculations
- Verify doodle-style UI elements

## 📊 Expected Results

### Performance Metrics
- **App Launch**: < 3 seconds
- **Location Loading**: < 2 seconds  
- **Map Navigation**: < 1 second to open Google Maps
- **Search Response**: < 1 second

### UI Responsiveness
- Smooth scrolling in location list
- Quick filter transitions
- Responsive navigation buttons
- Proper loading states

---

**Note**: This sample data is for testing purposes. In production, replace with actual Bhilai accommodation listings from your API or local database.