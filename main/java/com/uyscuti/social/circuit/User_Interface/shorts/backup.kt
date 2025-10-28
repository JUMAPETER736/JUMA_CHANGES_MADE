package com.uyscut.flashdesign.ui.shorts


//                val uniqueShortsSet =
//                    mutableSetOf<UserShortsEntity>() // Replace YourItemType with the actual type of your items

// Add existing items to the set
//                uniqueShortsSet.addAll(viewModel.mutableShortsList)

// Add new items to the set
//                uniqueShortsSet.addAll(shortsList)
// Update the mutableFavoriteShortsList with unique items
//                viewModel.mutableShortsList.clear()
//                viewModel.mutableShortsList.addAll(uniqueShortsSet)
//                shortsAdapter.clear()

//                shortsAdapter.notifyDataSetChanged()


////
//<?xml version="1.0" encoding="utf-8"?>
//<LinearLayout
//xmlns:android="http://schemas.android.com/apk/res/android"
//xmlns:app="http://schemas.android.com/apk/res-auto"
//xmlns:tools="http://schemas.android.com/tools"
//android:layout_width="match_parent"
//android:layout_height="match_parent"
//android:background="@drawable/bottom_sheet_background"
//android:orientation="vertical">
//
//<LinearLayout
//android:id="@+id/topLayout"
//android:layout_width="match_parent"
//android:layout_height="wrap_content"
//android:layout_marginVertical="10dp"
//android:orientation="vertical"
//android:layout_weight="0">
//
//<LinearLayout
//android:layout_width="match_parent"
//android:layout_height="wrap_content"
//android:orientation="horizontal">
//
//<TextView
//android:layout_width="0dp"
//android:layout_height="wrap_content"
//android:layout_weight="1"
//android:text="Comments"
//android:textSize="16sp"
//android:layout_gravity="center"
//android:gravity="center_horizontal"/>
//
//<ImageView
//android:layout_width="20dp"
//android:layout_height="20dp"
//android:layout_gravity="center"
//android:src="@drawable/more_horizontal_svgrepo_com"
//android:layout_marginEnd="20dp"/>
//</LinearLayout>
//</LinearLayout>
//
//<View
//android:id="@+id/view_line"
//android:layout_width="match_parent"
//android:layout_height="1dp"
//android:layout_marginLeft="16dp"
//android:layout_marginRight="16dp"
//android:background="@color/bluejeans"/>
//
//<androidx.recyclerview.widget.RecyclerView
//android:id="@+id/recyclerView"
//android:layout_width="match_parent"
//android:layout_height="0dp"
//android:layout_marginTop="10dp"
//android:layout_weight="1"
//tools:listitem="@layout/bottom_sheet_1_item"
//
//app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"/>
//
//<View
//android:id="@+id/line_view"
//android:layout_width="match_parent"
//android:layout_height="1dp"
//android:layout_marginLeft="16dp"
//android:layout_marginRight="16dp"
//android:background="@color/bluejeans"/>
//
//<com.uyscut.chatsuit.messages.MessageInput
//android:id="@+id/input"
//android:visibility="visible"
//android:layout_width="match_parent"
//android:layout_height="wrap_content"
//app:attachmentButtonDefaultIconColor="@color/bluejeans"
//app:attachmentButtonDefaultIconPressedColor="@color/bluejeans"
//app:inputButtonDefaultBgColor="@color/bluejeans"
//app:inputButtonDefaultBgPressedColor="@color/bluejeans"
//app:inputCursorDrawable="@drawable/shape_custom_cursor"
//app:inputHint="@string/hint_enter_a_message"
//app:showAttachmentButton="true"/>
//</LinearLayout>

//var progress = 0
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onProgressEvent(event: ProgressEvent) {
//        // Update your UI with the progress value
////        Log.d("Progress", "Progress ${event.progress}")
//        progress += event.progress
//        Log.d("Progress", "c-u: $progress")
//        progressBarLayout.visibility = View.VISIBLE
//        progressBar.progress = progress
//
//
//        wifiAnimation!!.start()
//
//        // Check if the progress is 100%, and hide the ProgressBar
//        if (progress >= 200) {
//            progressBarLayout.visibility = View.GONE
//            wifiAnimation!!.stop()
//        }
//    }
//


//// Get the current text from the EditText
//val currentText = binding.editTextText.text?.toString() ?: ""
//
//// Create a SpannableString from the current text
//val spannableString = SpannableString(currentText)
//
//// Define the blue color
//val blueColor = ContextCompat.getColor(requireContext(), R.color.blue)
//
//// Get the list of tags from the text
//val tags = Regex("#\\w+").findAll(spannableString)
//for (tag in tags) {
//    // Apply blue color to each tag
//    spannableString.setSpan(ForegroundColorSpan(blueColor), tag.range.start, tag.range.endInclusive + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//}
//
//// Set the SpannableString to the EditText
//binding.editTextText.text = spannableString


//inner class AnimalsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    // Bind views for the first item layout here
//
//    private val title: TextView = itemView.findViewById(R.id.topicTitle)
//    private val flexBoxLayout: FlexboxLayout = itemView.findViewById(R.id.flexBoxLayout)
//    private val seeMoreTextView: TextView = itemView.findViewById(R.id.seeMoreTv)
//    private val hideTopics: ImageView = itemView.findViewById(R.id.hideTopics)
//
//    // Determine the number of items to display initially (e.g., 12)
//    private val maxItemsToShowInitially = 12
//
//    init {
//        val selectableItemBackground = TypedValue()
//        itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true)
//        itemView.setBackgroundResource(selectableItemBackground.resourceId)
//    }
//
//    fun bind(topic: AddShortsTopicModel) {
//        // Bind data for the first item layout here
//        // Bind data for the normal item layout here
//        title.text = topic.shortsTopicTitle
//        hideTopics.setOnClickListener {
////                flexBoxLayout.visibility = View.GONE
//            flexBoxLayout.visibility = if (flexBoxLayout.visibility == View.VISIBLE) {
//                View.GONE
//            } else {
//                View.VISIBLE
//            }
//        }
//
//        // Clear existing views in FlexboxLayout
//        flexBoxLayout.removeAllViews()
//
//        // Add subtopics to FlexboxLayout dynamically using CardView
////            for (subtopic in topic.subTopics) {
////                val subtopicCardView = createSubtopicCardView(subtopic, flexBoxLayout.context)
////                flexBoxLayout.addView(subtopicCardView)
////            }
//
//        // Add subtopics to FlexboxLayout dynamically using CardView
//        for ((index, subtopic) in topic.subTopics.withIndex()) {
//            if (index < maxItemsToShowInitially) {
//                val subtopicCardView = createSubtopicCardView(subtopic, flexBoxLayout.context)
//                flexBoxLayout.addView(subtopicCardView)
//            } else {
//                // If there are more than 12 items, show "See More" TextView
//                seeMoreTextView.visibility = View.VISIBLE
//
//                seeMoreTextView.setOnClickListener {
//                    // Handle click on "See More" to show all items
//                    flexBoxLayout.removeAllViews()
////                        for (remainingSubtopic in topic.subTopics.drop(maxItemsToShowInitially)) {
////                            val subtopicCardView = createSubtopicCardView(remainingSubtopic, flexBoxLayout.context)
////                            flexBoxLayout.addView(subtopicCardView)
////                        }
//
//                    seeMoreTextView.visibility = View.GONE  // Hide "See More" TextView
//                    for (remainingSubtopic in topic.subTopics) {
//                        val remainingSubtopicCardView = createSubtopicCardView(remainingSubtopic, flexBoxLayout.context)
//                        flexBoxLayout.addView(remainingSubtopicCardView)
//                    }
////                        for (subtopic in topic.subTopics) {
////                            val subtopicCardView =
////                                createSubtopicCardView(subtopic, flexBoxLayout.context)
////                            flexBoxLayout.addView(subtopicCardView)
////                        }
//                    // Hide "See More" TextView after showing all items
////                        seeMoreTextView.visibility = View.GONE
//                }
//
//                break
//            }
//        }
//    }
//
//    private fun createSubtopicCardView(subtopic: String, context: Context): CardView {
//        val cardView = CardView(context)
//        val cardLayoutParams = FlexboxLayout.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        // Add right margin to the card view
//        cardLayoutParams.setMargins( 4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
//        cardView.layoutParams = cardLayoutParams
//        cardView.radius = 18.dpToPx().toFloat()
//        // Set background tint for CardView
//        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cardBackgroundTint))
//        cardView.setContentPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
//
//        val subtopicTextView = TextView(context)
//        subtopicTextView.text = subtopic
//        subtopicTextView.setTextColor(ContextCompat.getColor(context, R.color.black))
//
//        cardView.addView(subtopicTextView)
//
//        return cardView
//    }
//    private fun Int.dpToPx(): Int {
//        return (this * Resources.getSystem().displayMetrics.density).toInt()
//    }
//
//}
