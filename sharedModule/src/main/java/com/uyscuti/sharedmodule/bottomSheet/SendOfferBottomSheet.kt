package com.uyscuti.sharedmodule.bottomSheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.uyscuti.sharedmodule.R
import java.util.Locale

class SendOfferBottomSheet : BottomSheetDialogFragment() {

    private lateinit var offerAmountInput: TextInputEditText
    private lateinit var offerAmountLayout: TextInputLayout
    private lateinit var messageInput: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var offerProductImage: ShapeableImageView
    private lateinit var offerProductName: TextView
    private lateinit var offerListingPrice: TextView
    private lateinit var chip90Percent: Chip
    private lateinit var chip80Percent: Chip
    private lateinit var chip70Percent: Chip

    private var productName: String = ""
    private var listingPrice: Double = 0.0
    private var imageUri: String = ""

    // Callback for when offer is submitted
    var onOfferSubmitted: ((amount: Double, message: String) -> Unit)? = null

    companion object {
        private const val ARG_PRODUCT_NAME = "product_name"
        private const val ARG_LISTING_PRICE = "listing_price"
        private const val ARG_ITEM_IMAGE = "item_image"

        fun newInstance(productName: String, listingPrice: Double, itemImage: String): SendOfferBottomSheet {
            return SendOfferBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRODUCT_NAME, productName)
                    putDouble(ARG_LISTING_PRICE, listingPrice)
                    putString(ARG_ITEM_IMAGE, itemImage)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productName = it.getString(ARG_PRODUCT_NAME, "")
            listingPrice = it.getDouble(ARG_LISTING_PRICE, 0.0)
            imageUri = it.getString(ARG_ITEM_IMAGE, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.send_offer_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupQuickOfferChips(view)
        setupButtons()
        setupProductDetails()
        setupQuickOffer()
    }

    private fun initializeViews(view: View) {
        offerAmountInput = view.findViewById(R.id.offerAmountInput)
        offerAmountLayout = view.findViewById(R.id.offerAmountLayout)
        messageInput = view.findViewById(R.id.messageInput)
        submitButton = view.findViewById(R.id.submitOfferButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        offerProductImage = view.findViewById(R.id.offerProductImage)
        offerProductName = view.findViewById(R.id.offerProductName)
        offerListingPrice = view.findViewById(R.id.offerListingPrice)
        chip90Percent = view.findViewById(R.id.chip90Percent)
        chip80Percent = view.findViewById(R.id.chip80Percent)
        chip70Percent = view.findViewById(R.id.chip70Percent)
    }


    @SuppressLint("SetTextI18n")
    private fun setupQuickOffer() {

        val chip90Value = listingPrice * 0.9
        val chip80Value = listingPrice * 0.8
        val chip70Value = listingPrice * 0.7

        chip90Percent.text = "${chip90Value.toInt()} (90%)"
        chip80Percent.text = "${chip80Value.toInt()} (80%)"
        chip70Percent.text = "${chip70Value.toInt()} (70%)"

    }

    @SuppressLint("SetTextI18n")
    private fun setupProductDetails() {
        offerProductName.text = productName
        offerListingPrice.text = "Listed at MWK $listingPrice"
        Glide.with(requireActivity())
            .load(imageUri)
            .placeholder(R.drawable.product_placeholder)
            .error(R.drawable.product_placeholder)
            .into(offerProductImage)
    }

    private fun setupQuickOfferChips(view: View) {
        // 90% offer
        view.findViewById<Chip>(R.id.chip90Percent).apply {
            setOnClickListener {
                val amount = listingPrice * 0.9
                offerAmountInput.setText(String.format(Locale.US,"%.2f", amount))
            }
        }

        // 80% offer
        view.findViewById<Chip>(R.id.chip80Percent).apply {
            setOnClickListener {
                val amount = listingPrice * 0.8
                offerAmountInput.setText(String.format(Locale.US,"%.2f", amount))
            }
        }

        // 70% offer
        view.findViewById<Chip>(R.id.chip70Percent).apply {
            setOnClickListener {
                val amount = listingPrice * 0.7
                offerAmountInput.setText(String.format(Locale.US,"%.2f", amount))
            }
        }
    }

    private fun setupButtons() {
        cancelButton.setOnClickListener {
            dismiss()
        }

        submitButton.setOnClickListener {
            validateAndSubmitOffer()
        }
    }

    private fun validateAndSubmitOffer() {
        val offerAmountText = offerAmountInput.text.toString()

        // Validate offer amount
        if (offerAmountText.isEmpty()) {
            offerAmountLayout.error = "Please enter an offer amount"
            return
        }

        val offerAmount = offerAmountText.toDoubleOrNull()

        if (offerAmount == null || offerAmount <= 0) {
            offerAmountLayout.error = "Please enter a valid amount"
            return
        }

        if (offerAmount > listingPrice) {
            offerAmountLayout.error = "Offer cannot exceed listing price"
            return
        }

        // Clear any errors
        offerAmountLayout.error = null

        // Get optional message
        val message = messageInput.text.toString()

        // Notify callback
        onOfferSubmitted?.invoke(offerAmount, message)

        // Show success message
        Toast.makeText(
            requireContext(),
            "Offer submitted successfully!",
            Toast.LENGTH_SHORT
        ).show()

        // Dismiss the bottom sheet
        dismiss()
    }

    override fun getTheme(): Int {
        return R.style.SendOfferBottomSheetDialogTheme
    }
}