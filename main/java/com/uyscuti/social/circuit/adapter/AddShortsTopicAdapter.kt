package com.uyscuti.social.circuit.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.uyscuti.social.circuit.model.AddShortsTopicModel
import com.uyscuti.social.circuit.R

class AddShortsTopicAdapter(
    private val context: Context,
    private val shortsTopics: List<AddShortsTopicModel>,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val FASHION_BEAUTY_VIEW = 0
    private val ANIMALS_VIEW = 1
    private val selectedSubtopics = mutableSetOf<String>()
    fun isTotalSelectedSubtopicsValid(): Boolean {
        return selectedSubtopics.size < 3
    }

    // Method to get selected subtopics
    fun getSelectedSubtopics(): Set<String> {
        return selectedSubtopics
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == FASHION_BEAUTY_VIEW) {
            val fashionBeautyItemView = layoutInflater.inflate(R.layout.shorts_fashion_beauty_topic, parent, false)
//            FashionBeautyItemViewHolder(fashionBeautyItemView)
            AnimalsItemViewHolder(fashionBeautyItemView)

        }else if (viewType == ANIMALS_VIEW) {
            val animalsItemView = layoutInflater.inflate(R.layout.shorts_animals_topic, parent, false)
            AnimalsItemViewHolder(animalsItemView)
        }
        else {
            val fashionBeautyItemView = layoutInflater.inflate(R.layout.shorts_fashion_beauty_topic, parent, false)
            FashionBeautyItemViewHolder(fashionBeautyItemView)
        }
    }

    override fun getItemCount(): Int {
        return shortsTopics.size
    }


    override fun getItemViewType(position: Int): Int {
        if (position < 0 || position >= shortsTopics.size) {
            return super.getItemViewType(position)
        }

        val topic = shortsTopics[position]

        return if (topic.shortsTopicTitle == "FashionAndBeauty")
            FASHION_BEAUTY_VIEW
        else if (topic.shortsTopicTitle == "Animals")
            ANIMALS_VIEW
        else ANIMALS_VIEW


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val topic = shortsTopics[position]
        when (holder) {
            is FashionBeautyItemViewHolder -> {
                holder.bind(topic)
            }
            is AnimalsItemViewHolder -> {
                holder.bind(topic)
            }
        }
    }

    inner class FashionBeautyItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private val title: TextView = itemView.findViewById(R.id.topicTitle)
        private val tags: TextView = itemView.findViewById(R.id.subTopics)
        private val flexBoxLayout: FlexboxLayout = itemView.findViewById(R.id.flexBoxLayout)
        // Add tags to FlexboxLayout dynamically



        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(topic: AddShortsTopicModel) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            title.text = topic.shortsTopicTitle

            // Clear existing views in FlexboxLayout
            flexBoxLayout.removeAllViews()

            // Add subtopics to FlexboxLayout dynamically using CardView
            for (subtopic in topic.subTopics) {
                val subtopicCardView = createSubtopicCardView(subtopic, flexBoxLayout.context)
                flexBoxLayout.addView(subtopicCardView)
            }

        }

        private fun createSubtopicCardView(subtopic: String, context: Context): CardView {
            val cardView = CardView(context)
            val cardLayoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Add right margin to the card view
            cardLayoutParams.setMargins( 4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
            cardView.layoutParams = cardLayoutParams
            cardView.radius = 18.dpToPx().toFloat()
            // Set background tint for CardView
            cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cardBackgroundTint))
            cardView.setContentPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())

            val subtopicTextView = TextView(context)
            subtopicTextView.text = subtopic
            subtopicTextView.setTextColor(ContextCompat.getColor(context, R.color.black))

            cardView.addView(subtopicTextView)

            // Toggle selection state on click
            cardView.setOnClickListener {
                if (selectedSubtopics.contains(subtopic)) {
                    selectedSubtopics.remove(subtopic)
                } else if (isTotalSelectedSubtopicsValid()) {
                    selectedSubtopics.add(subtopic)
                } else {
                    // Show a toast if the total count exceeds 3
                    Toast.makeText(context, "You can only select up to 3 topics", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update background tint based on selection state
                cardView.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        if (selectedSubtopics.contains(subtopic)) R.color.primaryColor else R.color.cardBackgroundTint
                    )
                )
            }

            return cardView
        }
        private fun Int.dpToPx(): Int {
            return (this * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
    inner class AnimalsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views for the first item layout here

        private var isTopicsVisible = true

        private val title: TextView = itemView.findViewById(R.id.topicTitle)
        private val topicIcon: TextView = itemView.findViewById(R.id.topicIcon)
        private val flexBoxLayout: FlexboxLayout = itemView.findViewById(R.id.flexBoxLayout)
        private val seeMoreTextView: TextView = itemView.findViewById(R.id.seeMoreTv)
        private val hideTopics: ImageView = itemView.findViewById(R.id.hideTopics)

        // Determine the number of items to display initially (e.g., 12)
        private val maxItemsToShowInitially = 12


        init {
            val selectableItemBackground = TypedValue()
            itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
            itemView.setBackgroundResource(selectableItemBackground.resourceId)
        }

        fun bind(topic: AddShortsTopicModel) {
            // Bind data for the first item layout here
            // Bind data for the normal item layout here
            val emojiDecimalCode = String(Character.toChars(topic.shortsTopicIcon))
            title.text = topic.shortsTopicTitle
            topicIcon.text = emojiDecimalCode

            val initiallyVisibleItems = topic.subTopics.take(maxItemsToShowInitially)
            seeMoreTextView.visibility = if (topic.subTopics.size > maxItemsToShowInitially) View.VISIBLE else View.GONE

            // Clear existing views in FlexboxLayout
            flexBoxLayout.removeAllViews()

            // Add subtopics to FlexboxLayout dynamically using CardView
            for (subtopic in initiallyVisibleItems) {
                val subtopicCardView = createSubtopicCardView(subtopic, flexBoxLayout.context)
                flexBoxLayout.addView(subtopicCardView)
            }

            hideTopics.setOnClickListener {
                // Toggle visibility of FlexboxLayout
                isTopicsVisible = !isTopicsVisible
                val newVisibility = if (isTopicsVisible) View.VISIBLE else View.GONE

                updateFlexBoxLayoutVisibility(topic)
                // Update visibility of seeMoreTextView based on the number of subtopics
                seeMoreTextView.visibility = if (topic.subTopics.size > maxItemsToShowInitially && isTopicsVisible) View.VISIBLE else View.GONE

                // Change the image for hideTopics based on visibility
                hideTopics.setImageResource(if (isTopicsVisible) R.drawable.up_arrow_svgrepo_com else R.drawable.down_arrow_backup_2_svgrepo_com)

                // Display the first 12 elements initially when hiding topics
//                val itemsToDisplay = if (isTopicsVisible) maxItemsToShowInitially else topic.subTopics.size
                // Display the first 12 elements initially when hiding topics
                val itemsToDisplay = if (isTopicsVisible) maxItemsToShowInitially else 0
                flexBoxLayout.removeAllViews()
                for (i in 0 until itemsToDisplay) {
                    val subtopicCardView = createSubtopicCardView(topic.subTopics[i], flexBoxLayout.context)
                    flexBoxLayout.addView(subtopicCardView)
                }
            }


            seeMoreTextView.setOnClickListener {
                if (isTopicsVisible) {
                    // Show all items only if the FlexboxLayout is currently visible
                    flexBoxLayout.removeAllViews()
                    for (remainingSubtopic in topic.subTopics) {
                        val remainingSubtopicCardView = createSubtopicCardView(remainingSubtopic, flexBoxLayout.context)
                        flexBoxLayout.addView(remainingSubtopicCardView)
                    }
                    // Hide "See More" TextView after showing all items
                    seeMoreTextView.visibility = View.GONE

                    // Update the image for hideTopics when "See More" is clicked
                    hideTopics.setImageResource(R.drawable.down_arrow_backup_2_svgrepo_com)
                }
            }
        }

        private fun createSubtopicCardView(subtopic: String, context: Context): CardView {
            val cardView = CardView(context)
            val cardLayoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            // Add right margin to the card view
            cardLayoutParams.setMargins(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
            cardView.layoutParams = cardLayoutParams
            cardView.radius = 18.dpToPx().toFloat()

            // Set background tint based on selection state
            val isSelected = selectedSubtopics.contains(subtopic)
            cardView.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (isSelected) R.color.primaryColor else R.color.cardBackgroundTint
                )
            )

            cardView.setContentPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())

            val subtopicTextView = TextView(context)
            subtopicTextView.text = subtopic
            subtopicTextView.setTextColor(ContextCompat.getColor(context, R.color.black))

            cardView.addView(subtopicTextView)

            cardView.setOnClickListener {
                if (selectedSubtopics.contains(subtopic)) {
                    selectedSubtopics.remove(subtopic)
                } else if (isTotalSelectedSubtopicsValid()) {
                    selectedSubtopics.add(subtopic)
                } else {
                    // Show a toast if the total count exceeds 3
                    Toast.makeText(context, "You can only select up to 3 topics", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update background tint based on selection state
                cardView.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        if (selectedSubtopics.contains(subtopic)) R.color.primaryColor else R.color.cardBackgroundTint
                    )
                )
            }
            return cardView
        }

        private fun Int.dpToPx(): Int {
            return (this * Resources.getSystem().displayMetrics.density).toInt()
        }

        private fun updateFlexBoxLayoutVisibility(topic: AddShortsTopicModel) {
            // Display the first 12 elements initially when hiding topics

            val itemsToDisplay = if (isTopicsVisible) minOf(maxItemsToShowInitially, topic.subTopics.size) else topic.subTopics.size // Display 0 items to hide all
            flexBoxLayout.removeAllViews()
            for (i in 0 until itemsToDisplay) {
                val subtopicCardView = createSubtopicCardView(topic.subTopics[i], flexBoxLayout.context)
                flexBoxLayout.addView(subtopicCardView)
            }
        }

    }

}
