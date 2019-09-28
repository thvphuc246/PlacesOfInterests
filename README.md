# Helsinki Places Of Interests
A simple Android application to *fetch the current location* of the device and *get nearby articles* resourced from Wikipedia.

## Features
- Show nearby articles on the map as a point of interest (POI)
- Show nearby articles (up to 50) as a list when tapping on the **round button** at the *bottom left corner* of the map screen
- Show article detail when clicking on the item of the list, including *title* and *summary*

## Resources
- Material Design for Android
- Google Play Services: Maps, Location, Places
- OkHttp
- Klaxon
- Anko

## Prerequisite
Place the Google Maps API key in the value resource file *google_maps_api.xml* before running the app

## Future Plans
- Include *images* and *link* to the wiki article of the place on its detail
- Give route suggestions to reach selected POI
- Deploy *Dagger*, *Gson* and *RxJava2* into the project
