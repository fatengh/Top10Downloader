package com.example.top10downloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {
    lateinit var tvfeed : TextView
    lateinit var rv : RecyclerView
    lateinit var feedBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate process")

        tvfeed = findViewById(R.id.tv)
        feedBtn = findViewById(R.id.btnGet)
        rv = findViewById(R.id.rv)

        feedBtn.setOnClickListener{
            requestApi()
            rv.layoutManager = LinearLayoutManager(this)
            rv.setHasFixedSize(true)
        }
    }

    private fun downloadXML(): String {
        val xmlResult = StringBuilder()
        try {
            val url = URL("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            val response = conn.responseCode
            Log.d("MainActivity", "response code was $response")
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val inputBuff = CharArray(500)
            var charsRead = 0
            while (charsRead >= 0) {
                charsRead = reader.read(inputBuff)
                if (charsRead > 0) {
                    xmlResult.append(String(inputBuff, 0, charsRead))
                }
            }
            reader.close()
            Log.d("MainActivity", "Received ${xmlResult.length} bytes")
            return xmlResult.toString()
        } catch (e: MalformedURLException) {
            Log.e("MainActivity", "downloadXML: Invalid URL ${e.message}")
        } catch (e: IOException) {
            Log.e("MainActivity", "downloadXML: IO Exception reading data: ${e.message}")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Log.e("MainActivity", "downloadXML: Security exception.  Needs permissions? ${e.message}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Unknown error: ${e.message}")
        }
        return ""
    }

    private fun requestApi(){
        var listItems = ArrayList<FeedEntry>()
        CoroutineScope(Dispatchers.IO).launch {
            val rssFeed = async {
                downloadXML()
            }.await()
            if (rssFeed.isEmpty()) {
                Log.e("MainActivity", "Error downloading")
            } else {
                val parseApplications = async {
                    FeedPars()
                }.await()
                parseApplications.parse(rssFeed)
                listItems = parseApplications.getParsedList()
                withContext(Dispatchers.Main) {
                    rv.adapter = MyAdap(listItems)
                }
            }
        }

    }

}