package com.uyscuti.social.business.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uyscuti.sharedmodule.model.Catalogue
import com.uyscuti.social.business.R
import com.uyscuti.sharedmodule.adapter.CatalogueAdapter
import com.uyscuti.sharedmodule.model.HideBottomNav
import com.uyscuti.sharedmodule.model.ShowBottomNav
import com.uyscuti.sharedmodule.popupDialog.BusinessProfileDialogFragment
import com.uyscuti.social.business.interfaces.BottomNavController
import com.uyscuti.social.business.model.business.BusinessProfile
import com.uyscuti.social.business.repository.IFlashApiRepositoryImplementation
import com.uyscuti.social.business.retro.CreateCatalogueActivity
import com.uyscuti.social.business.viewmodel.CatalogueAdapterViewModel
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CatalogueFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class CatalogueFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

   // private val API_TAG = "ApiService"

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private lateinit var repository: IFlashApiRepositoryImplementation


    private var businessProfile: Result<BusinessProfile>? = null
    private var profileDeferred: Deferred<Boolean>? = null
    private var hasBusinessProfile: Boolean = false

    private var isScrollingDown  = false


    private var bottomNavController: BottomNavController? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var catalogueFab: FloatingActionButton
    private lateinit var catalogueText: TextView

    private lateinit var progressBar: ProgressBar

    private val catalogueItems: ArrayList<Catalogue> = arrayListOf()
    private lateinit var catalogueAdapter: CatalogueAdapter
    private val catalogueViewModel: CatalogueAdapterViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    private suspend fun setUpBusinessProfile(): Boolean {
        return try {
            businessProfile = repository.getBusinessProfile()

            if (businessProfile!!.isSuccess) {
                true // Return the result
            } else {
                false // Return the result
            }
        } catch (e: Exception) {
            false // Return false on error
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = parentFragment as BottomNavController
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_catalogue, container, false)

        recyclerView = view.findViewById(R.id.catalogue_recycler_view)
        catalogueFab = view.findViewById(R.id.fabCatalogue)
        catalogueText = view.findViewById(R.id.catalogue_text)
        progressBar = view.findViewById(R.id.progress)

        catalogueAdapter = CatalogueAdapter(requireActivity(), catalogueItems)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        recyclerView.adapter = catalogueAdapter

        handleOnClickListener()

        observeViewModel()


        repository = IFlashApiRepositoryImplementation(retrofitInstance)

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CatalogueFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CatalogueFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileDeferred = lifecycleScope.async {
            setUpBusinessProfile()
        }


    }

    private fun observeViewModel() {
        // Observe items from ViewModel
        catalogueViewModel.catalogueItems.observe(viewLifecycleOwner) { updatedList ->
            // Update local list and notify adapter
            catalogueAdapter.updateCatalogue(updatedList)
            catalogueText.visibility = if(updatedList.isEmpty()) View.VISIBLE else View.GONE
            catalogueText.text = if(updatedList.isEmpty()) "Create, Delete, Edit and View Catalogues for your business.\nYou have no catalogues" else ""
            recyclerView.visibility = if(updatedList.isEmpty()) View.GONE else View.VISIBLE
        }

        catalogueViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if(isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun handleOnClickListener() {


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // dy > 0 means scrolling down, dy < 0 means scrolling up
                if (dy > 0 && !isScrollingDown) {
                    // Scrolling down - hide FAB
                    isScrollingDown = true
                    catalogueFab.hide()
                    EventBus.getDefault().post(HideBottomNav())
                } else if (dy < 0 && isScrollingDown) {
                    // Scrolling up - show FAB
                    isScrollingDown = false
                    catalogueFab.show()
                    EventBus.getDefault().post(ShowBottomNav(false))
                }
            }
        })



        catalogueFab.setOnClickListener {

            viewLifecycleOwner.lifecycleScope.launch {
                profileDeferred?.let { deffered ->

                    if (deffered.isCompleted) {
                        val hasProfile = deffered.await()
                        hasBusinessProfile  = hasProfile

                        if(hasBusinessProfile) {
                            val intent = Intent(
                                requireContext(),
                                CreateCatalogueActivity::class.java
                            )

                            requireActivity().startActivityForResult(intent, 111)

                        } else {

                            val dialog = BusinessProfileDialogFragment.newInstance()
                            dialog.setListener(object :
                                BusinessProfileDialogFragment.BusinessProfileDialogListener {

                                override fun onCreateProfile() {
                                    bottomNavController?.navigateToChildFragments(1)
                                    dialog.dismiss()
                                }
                            })
                            dialog.show(childFragmentManager, "BusinessProfileDialog")
                        }
                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().post(ShowBottomNav(false))
        if(!catalogueFab.isShown) {
            catalogueFab.show()
        }
    }


}