package com.android.vinhphuc.mapswhimtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.beust.klaxon.Klaxon
import com.beust.klaxon.PathMatcher
import org.jetbrains.anko.doAsyncResult
import java.io.StringReader
import java.net.URL
import java.util.regex.Pattern

class DetailsActivity : AppCompatActivity() {
    private val BASE_URL = "https://en.wikipedia.org/w/api.php?action=query&prop=description|images&format=json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val placeTitleTextView = findViewById<TextView>(R.id.place_title)
        val placeDescriptionTextView = findViewById<TextView>(R.id.place_description)

        //Getting the clicked pageId. In case anything goes wrong, the first place's pageId will be called instead
        val pageId = this.intent.getIntExtra("PLACE_ID", 18806750)
        val PLACE_URL = "$BASE_URL&pageids=$pageId"
        val placeDescription = doAsyncResult {
            readPlaceUrl(PLACE_URL, pageId.toString())
        }.get()

        placeTitleTextView.text = this.intent.getStringExtra("PLACE_TITLE")
        placeDescriptionTextView.text = placeDescription
    }

    fun readPlaceUrl(url: String, pId: String): String {
        val apiResponse = URL(url).readText()
        var res = "No description"

        val descriptionPathMatcher = object : PathMatcher {
            override fun pathMatches(path: String) = Pattern.matches(".*query.*pages.*$pId.description", path)

            override fun onMatch(path: String, value: Any) {
                res = value as String
            }
        }

        Klaxon().pathMatcher(descriptionPathMatcher).parseJsonObject(StringReader(apiResponse))

        return res
    }
}
