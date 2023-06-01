package com.xpl0itu.wiiudownloader_mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import android.content.Intent
import android.widget.Button
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    private val queue: MutableList<Item> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadData()

        val queueButton = findViewById<Button>(R.id.queueButton)
        queueButton.setOnClickListener {
            val intent = Intent(this, QueueActivity::class.java)
            intent.putExtra("itemList", ArrayList(queue)) // Pass the queue list to the QueueActivity
            startActivity(intent)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadData() {
        GlobalScope.launch(Dispatchers.IO) {
            val url = "http://titlekeys.ovh/json/titlekeys.json"
            val request = Request.Builder()
                .url(url)
                .build()

            val client = OkHttpClient()
            try {
                val response = client.newCall(request).execute()
                val jsonData = response.body()?.string()
                parseJson(jsonData)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun parseJson(jsonData: String?) {
        withContext(Dispatchers.Main) {
            try {
                val items = Gson().fromJson(jsonData, Array<Item>::class.java).toList()
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                val adapter = ItemAdapter(items) { item -> addToQueue(item) }
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addToQueue(item: Item) {
        queue.add(item)
    }
}
