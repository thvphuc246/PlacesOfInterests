package com.android.vinhphuc.mapswhimtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.beust.klaxon.Klaxon
import com.beust.klaxon.PathMatcher
import org.jetbrains.anko.doAsyncResult
import java.io.StringReader
import java.net.URL
import java.util.regex.Pattern

class PlaceActivity : AppCompatActivity() {
    private val BASE_URL = "https://en.wikipedia.org/w/api.php?format=json&action=query&list=geosearch"
    private val RADIUS = 10000
    private val LIMIT = 50
    private var currentLat = 60.1831906
    private var currentLon = 24.9285439
    private val MY_URL = "$BASE_URL&gsradius=$RADIUS&gslimit=$LIMIT&gscoord=$currentLat|$currentLon"

    private var places: ArrayList<POIPlace> = ArrayList<POIPlace>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)

        places = doAsyncResult {
            readUrl(MY_URL)
        }.get()

        val listView = findViewById<ListView>(R.id.listview)
        listView.adapter = PlaceAdapter(this, places)

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, DetailsActivity::class.java)
            Log.e("CHECK", "${places[position].pageid}")
            intent.putExtra("PLACE_ID", places[position].pageid)
            intent.putExtra("PLACE_TITLE", places[position].title)
            startActivity(intent)
        }
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