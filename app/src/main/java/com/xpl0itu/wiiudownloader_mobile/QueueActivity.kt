package com.xpl0itu.wiiudownloader_mobile

import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class QueueActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var queueAdapter: QueueAdapter
    private var itemList: MutableList<Item> = mutableListOf()

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { folderUri ->
            val folderPath = folderUri.path
            downloadQueue(itemList, folderPath)
        }
    }

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
            pickFolder.launch(null)
        }
    }

    private fun downloadQueue(itemList: MutableList<Item>, folderPath: String?) {
        for (item in itemList) {
            processTitleId(context = applicationContext, titleId = item.titleID, titleKey = item.titleKey, name = item.name, outputDir = folderPath)
        }
    }

    private fun removeFromQueue(item: Item) {
        itemList.remove(item)
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