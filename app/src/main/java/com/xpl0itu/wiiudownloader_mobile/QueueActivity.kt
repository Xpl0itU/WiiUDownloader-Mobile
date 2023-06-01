package com.xpl0itu.wiiudownloader_mobile

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class QueueActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var queueAdapter: QueueAdapter
    private var itemList: MutableList<Item> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        queueAdapter = QueueAdapter { position -> removeFromQueue(position) }
        recyclerView.adapter = queueAdapter

        // Retrieve the item list from the intent extras or wherever you store it
        itemList = intent.getSerializableExtra("itemList") as MutableList<Item>
        queueAdapter.setItems(itemList)

        val sharedPreferences = getSharedPreferences("QueuePrefs", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("itemList", null)
        if (json != null) {
            itemList = gson.fromJson(json, object : TypeToken<MutableList<Item>>() {}.type)
            queueAdapter.setItems(itemList)
        }

        val downloadButton = findViewById<Button>(R.id.downloadButton)
        downloadButton.setOnClickListener {
            downloadQueue(itemList)
        }
    }

    private fun downloadQueue(itemList: MutableList<Item>) {
        println(itemList)
    }

    private fun removeFromQueue(item: Item) {
        itemList.remove(item)
        saveItemListToPreferences()
        queueAdapter.setItems(itemList)
    }

    private fun removeFromQueue(position: Int) {
        val item = itemList.removeAt(position)
        saveItemListToPreferences()
        queueAdapter.setItems(itemList)
    }

    private fun saveItemListToPreferences() {
        val sharedPreferences = getSharedPreferences("QueuePrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(itemList)
        editor.putString("itemList", json)
        editor.apply()
    }
}
