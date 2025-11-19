package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.gif.GifAdapter
import com.uyscuti.social.circuit.adapter.gif.GifPaginatedAdapter
import com.uyscuti.social.circuit.databinding.ActivityGifBinding
import com.uyscuti.social.circuit.viewmodels.comments.GifViewModel
import com.uyscuti.social.network.api.response.gif.allgifs.GifModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



private const val TAG = "GifActivity"


@AndroidEntryPoint
class GifActivity : AppCompatActivity(), GifAdapter.GifClickListener {
    private lateinit var binding: ActivityGifBinding
    private lateinit var viewModel: GifViewModel

    private lateinit var gifAdapter: GifAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityGifBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel = ViewModelProvider(this)[GifViewModel::class.java]

        gifAdapter = GifAdapter(this)
        gifAdapter.recyclerView = binding.recyclerView
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        gifAdapter.setOnPaginationListener(object : GifPaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {
            }

            override fun onNextPage(page: Int) {
                lifecycleScope.launch(Dispatchers.Main) {

                    fetchData(page)
                }
            }

            override fun onFinish() {

            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            fetchData(gifAdapter.startPage)
        }

    }

    private fun fetchData(pageNumber: Int) {
        lifecycleScope.launch {
            val data = viewModel.fetchData(pageNumber)
            Log.d(TAG, "fetchedData: $data")
            gifAdapter.submitItems(data.gifs)
            // Do something with the fetched data
        }
    }

    override fun onUserGifClick(gifEntity: GifModel) {
        val gifUri = gifEntity.gifs[0].url
        Log.d(TAG, "onUserGifClick: $gifUri")
        val resultIntent = Intent()
        resultIntent.putExtra("gifUri", gifUri)
        setResult(RESULT_OK, resultIntent)
        finish()
    }


}