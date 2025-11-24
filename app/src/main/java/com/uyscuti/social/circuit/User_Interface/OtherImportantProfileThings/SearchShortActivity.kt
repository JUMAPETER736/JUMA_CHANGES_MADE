package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivitySearchShortBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchShortActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchShortBinding
    private lateinit var searchAdapter: SearchResultsAdapter
    private val allResults = mutableListOf<SearchResult>()
    private val filteredResults = mutableListOf<SearchResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchShortBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSearch()
        loadAllResults()
    }

    private fun setupSearch() {
        searchAdapter = SearchResultsAdapter(filteredResults) { result ->
            onSearchResultClicked(result)
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchShortActivity)
            adapter = searchAdapter
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    filteredResults.clear()
                    searchAdapter.notifyDataSetChanged()
                    binding.noResultsText.visibility = View.GONE
                } else {
                    performSearch(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.clearSearchButton.setOnClickListener {
            binding.searchEditText.text.clear()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun performSearch(query: String) {
        GlobalScope.launch(Dispatchers.Default) {
            val results = allResults.filter { result ->
                result.title.contains(query, ignoreCase = true) ||
                        result.description.contains(query, ignoreCase = true) ||
                        result.username.contains(query, ignoreCase = true)
            }.toMutableList()

            withContext(Dispatchers.Main) {
                filteredResults.clear()
                filteredResults.addAll(results)
                searchAdapter.notifyDataSetChanged()

                if (filteredResults.isEmpty()) {
                    binding.noResultsText.visibility = View.VISIBLE
                    binding.noResultsText.text = "No results found for \"$query\""
                } else {
                    binding.noResultsText.visibility = View.GONE
                }
            }
        }
    }

    private fun loadAllResults() {
        // Replace with your actual data loading logic
        // This could be from database, API, or local data source
        allResults.addAll(
            listOf(
                SearchResult(
                    id = 1,
                    title = "Sample Post",
                    description = "This is a sample short video",
                    username = "john_doe",
                    thumbnailUrl = "",
                    type = "short"
                ),
                SearchResult(
                    id = 2,
                    title = "Another Video",
                    description = "Check out this amazing content",
                    username = "jane_smith",
                    thumbnailUrl = "",
                    type = "short"
                ),
                SearchResult(
                    id = 3,
                    title = "Trending Now",
                    description = "What everyone is watching",
                    username = "trend_master",
                    thumbnailUrl = "",
                    type = "short"
                )
            )
        )
    }

    private fun onSearchResultClicked(result: SearchResult) {
        // Handle result click - navigate to video or profile
        // Example: startActivity(Intent(this, VideoPlayerActivity::class.java).apply {
        //     putExtra("video_id", result.id)
        // })
    }
}

data class SearchResult(
    val id: Int,
    val title: String,
    val description: String,
    val username: String,
    val thumbnailUrl: String,
    val type: String
)

class SearchResultsAdapter(
    private val results: List<SearchResult>,
    private val onItemClick: (SearchResult) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<SearchResultsAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: androidx.viewgroup.ViewGroup, viewType: Int): SearchViewHolder {
        val binding = android.widget.LinearLayout(parent.context).apply {
            layoutParams = androidx.viewgroup.ViewGroup.LayoutParams(
                androidx.viewgroup.ViewGroup.LayoutParams.MATCH_PARENT,
                androidx.viewgroup.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        return SearchViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount() = results.size

    inner class SearchViewHolder(
        private val itemView: android.widget.LinearLayout,
        private val onItemClick: (SearchResult) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bind(result: SearchResult) {
            itemView.apply {
                removeAllViews()

                // Title
                val titleView = android.widget.TextView(context).apply {
                    text = result.title
                    textSize = 16f
                    setTextColor(android.graphics.Color.BLACK)
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 8 }
                }
                addView(titleView)

                // Username
                val usernameView = android.widget.TextView(context).apply {
                    text = "@${result.username}"
                    textSize = 14f
                    setTextColor(android.graphics.Color.GRAY)
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 8 }
                }
                addView(usernameView)

                // Description
                val descriptionView = android.widget.TextView(context).apply {
                    text = result.description
                    textSize = 14f
                    setTextColor(android.graphics.Color.DKGRAY)
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 12 }
                }
                addView(descriptionView)

                setOnClickListener { onItemClick(result) }
            }
        }
    }
}