package com.xpl0itu.wiiudownloader_mobile

import android.net.Uri
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
    private var itemList: MutableList<gtitlesWrapper.TitleEntry> = mutableListOf()

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { folderUri ->
            downloadQueue(itemList, folderUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        queueAdapter = QueueAdapter { item -> removeFromQueue(item) }
        recyclerView.adapter = queueAdapter

        // Retrieve the item list from the intent extras or wherever you store it
        itemList = intent.getSerializableExtra("itemList") as MutableList<gtitlesWrapper.TitleEntry>?
            ?: mutableListOf()
        queueAdapter.setItems(itemList)

        val sharedPreferences = getSharedPreferences("QueueStatus", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("itemList", null)
        if (json != null) {
            itemList = gson.fromJson(json, object : TypeToken<MutableList<gtitlesWrapper.TitleEntry>>() {}.type)
            queueAdapter.setItems(itemList)
        }

        val downloadButton = findViewById<Button>(R.id.downloadButton)
        downloadButton.setOnClickListener {
            pickFolder.launch(null)
        }
    }

    private fun downloadQueue(itemList: MutableList<gtitlesWrapper.TitleEntry>, folderUri: Uri?) {
        for (item in itemList) {
            // TODO: Add titlekey generation algorithm
            processTitleId(context = applicationContext, titleId = String.format("%016x", item.tid), titleKey = "393396529b92ab77eb24302996bd4695", name = item.name, outputDir = folderUri)
        }
    }

    private fun removeFromQueue(item: gtitlesWrapper.TitleEntry) {
        itemList.remove(item)
        saveItemListToPreferences()
        queueAdapter.setItems(itemList)
    }

    private fun saveItemListToPreferences() {
        val sharedPreferences = getSharedPreferences("QueueStatus", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(itemList)
        editor.putString("itemList", json)
        editor.apply()
    }
}
