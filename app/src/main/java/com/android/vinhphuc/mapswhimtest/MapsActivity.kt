package com.android.vinhphuc.mapswhimtest

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.beust.klaxon.Klaxon
import com.beust.klaxon.PathMatcher
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.doAsyncResult
import java.io.StringReader
import java.net.URL
import java.util.regex.Pattern

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    private val BASE_URL = "https://en.wikipedia.org/w/api.php?format=json&action=query&list=geosearch"
    private val RADIUS = 10000
    private val LIMIT = 50
    private var currentLat = 60.1831906
    private var currentLon = 24.9285439
    private val MY_URL = "$BASE_URL&gsradius=$RADIUS&gslimit=$LIMIT&gscoord=$currentLat|$currentLon"

    private val TAG = "My log tag"

    private var places: ArrayList<POIPlace> = ArrayList<POIPlace>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Now transferring you to POI list...", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            val intent = Intent(this, PlaceActivity::class.java)
            startActivity(intent)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        setUpMap()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            } else {

            }
        }

        //Fetch the list of POI places around the device's current location
        places = doAsyncResult {
            readUrl(MY_URL)
        }.get()

        places.forEachIndexed { _, element ->
            val newLatLng = LatLng(element.lat, element.lon)
            mMap.addMarker(MarkerOptions().position(newLatLng).title(element.title))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted!
                    setUpMap()
                } else {
                    // permission denied!
                    return
                }
                return
            }

            else -> {
                return
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        // 1
        val markerOptions = MarkerOptions().position(location)
        // 2
        mMap.addMarker(markerOptions)
    }

    fun readUrl (url : String) : ArrayList<POIPlace> {
        val idList = ArrayList<Int>()
        val titleList = ArrayList<String>()
        val latList = ArrayList<Double>()
        val lonList = ArrayList<Double>()

        val apiResponse = URL(url).readText()

        //Match each of the pageid, title, lat & lon

        val pageidPathMatcher = object : PathMatcher {
            override fun pathMatches(path: String) = Pattern.matches(".*query.*geosearch.*pageid.*", path)

            override fun onMatch(path: String, value: Any) {
                idList.add(value as Int)
            }
        }

        Klaxon().pathMatcher(pageidPathMatcher).parseJsonObject(StringReader(apiResponse))

        val titlePathMatcher = object : PathMatcher {
            override fun pathMatches(path: String) = Pattern.matches(".*query.*geosearch.*title.*", path)

            override fun onMatch(path: String, value: Any) {
                titleList.add(value as String)
            }
        }

        Klaxon().pathMatcher(titlePathMatcher).parseJsonObject(StringReader(apiResponse))

        val lattitudePatMatcher = object : PathMatcher {
            override fun pathMatches(path: String) = Pattern.matches(".*query.*geosearch.*lat.*", path)

            override fun onMatch(path: String, value: Any) {
                latList.add(value as Double)
            }
        }

        Klaxon().pathMatcher(lattitudePatMatcher).parseJsonObject(StringReader(apiResponse))

        val longitudePatMatcher = object : PathMatcher {
            override fun pathMatches(path: String) = Pattern.matches(".*query.*geosearch.*lon.*", path)

            override fun onMatch(path: String, value: Any) {
                lonList.add(value as Double)
            }
        }

        Klaxon().pathMatcher(longitudePatMatcher).parseJsonObject(StringReader(apiResponse))

        //Create list of POI places
        places = ArrayList<POIPlace>()
        idList.forEachIndexed { index, _ ->
            places.add(POIPlace(idList[index], titleList[index], latList[index], lonList[index]))
        }

        return places
    }
}