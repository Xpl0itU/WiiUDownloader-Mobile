package com.xpl0itu.wiiudownloader_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val queue: MutableList<gtitlesWrapper.TitleEntry> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadData(recyclerView)

        val queueButton = findViewById<Button>(R.id.queueButton)
        queueButton.setOnClickListener {
            val intent = Intent(this, QueueActivity::class.java)
            intent.putExtra("itemList", ArrayList(queue)) // Pass the current state of the queue
            startActivity(intent)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadData(recyclerView: RecyclerView) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val titleEntriesPtr = gtitlesWrapper.getTitleEntries(4)
                populateData(titleEntriesPtr, recyclerView)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun populateData(titleEntries: Array<gtitlesWrapper.TitleEntry>, recyclerView: RecyclerView) {
        withContext(Dispatchers.Main) {
            try {
                val adapter = ItemAdapter(titleEntries) { item -> addToQueue(item) }
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addToQueue(item: gtitlesWrapper.TitleEntry) {
        queue.add(item)
    }
}
