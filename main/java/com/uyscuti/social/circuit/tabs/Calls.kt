package com.uyscuti.social.circuit.tabs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.calls.CallInfoActivity
import com.uyscuti.social.circuit.calls.adapter.CallLogAdapter
import com.uyscuti.social.circuit.calls.viewmodel.CallViewModel
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.interfaces.OnBackPressedListener
import com.uyscuti.social.circuit.presentation.MainViewModel
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import com.uyscuti.social.core.common.data.room.repository.calls.CallLogRepository
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Calls.newInstance] factory method to
 * create an instance of this fragment.
 */

@UnstableApi
@AndroidEntryPoint
class Calls : Fragment(), CallLogAdapter.CallLogClickListener , OnBackPressedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var callLogRepository: CallLogRepository

    // Declare an instance of your CallLogAdapter
    private lateinit var callLogAdapter: CallLogAdapter

    // Declare a RecyclerView
    private lateinit var callLogList: RecyclerView
    private val callViewModel: CallViewModel by viewModels()

    private var selectedCallLogsCount = 0


    private val mainViewModel: MainViewModel by activityViewModels()


    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel.resetSelectedDialogsCount()
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView =
            inflater.inflate(R.layout.fragment_calls, container, false)

        callLogList = rootView.findViewById(R.id.callList)
        callLogList.layoutManager = LinearLayoutManager(context)

        callLogAdapter = CallLogAdapter(requireContext())

        // Set the adapter on the RecyclerView
        callLogList.adapter = callLogAdapter

        callLogAdapter.setCallLogClickListener(this)
        callViewModel.getAllCallLogs().observe(viewLifecycleOwner, Observer { callLogs ->
            if (callLogs != null){
                // Update your RecyclerView adapter or UI with chatList

                val ordered = callLogs.sortedByDescending { it.createdAt }

//                Log.d("CallFragment", "Call log List Found In DB: $callLogs")
                callLogAdapter.addCallLogs(ordered)

            }else {
                Log.d("CallFragment", "Call log List Found empty")
            }
        })

        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.addOnBackPressedListener(this)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Calls.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Calls().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCallLogClick(callLog: CallLogEntity) {
        val selectedCallLogs = mainViewModel.selectedCallLogs
        val isAlreadySelected = selectedCallLogs.contains(callLog)

        if (selectedCallLogsCount == 0){
            openCallLogDetails(callLog)
        } else {
            if (isAlreadySelected){
                deselectCallLog(callLog)
            } else {
                selectCallLog(callLog)
            }
        }
    }

    private fun selectCallLog(callLog: CallLogEntity) {
        selectedCallLogsCount++
        mainViewModel.incrementSelectedCallLogs(callLog)
        callLog.isSelected = true
        callLogAdapter.updateItem(callLog)
    }

    private fun deselectCallLog(callLog: CallLogEntity) {
        selectedCallLogsCount--
        mainViewModel.decrementSelectedCallLogs(callLog)
        callLog.isSelected = false
        callLogAdapter.updateItem(callLog)
    }

    private fun openCallLogDetails(callLog: CallLogEntity) {
        val intent = Intent(requireContext(), CallInfoActivity::class.java)

        val date = Date(callLog.createdAt)

        // Define the desired time format
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Format the time
        val formattedTime = timeFormat.format(date)

        val duration = formatDuration(callLog.callDuration)
        val callerId = callLog.callerId

        val callDate = checkDate(date)
        intent.putExtra("caller", callLog.callerName)
        intent.putExtra("date", callDate)
        intent.putExtra("duration", duration)
        intent.putExtra("time", formattedTime)
        intent.putExtra("type", callLog.callType)
        intent.putExtra("isVideo", false)
        intent.putExtra("status", callLog.callStatus)
        intent.putExtra("avatar", callLog.callerAvatar)
        intent.putExtra("callerId", callerId)
        startActivity(intent)
    }

    override fun onCallLogLongClick(callLog: CallLogEntity) {

        if (selectedCallLogsCount == 0){
            selectCallLog(callLog)
        }

    }


    private fun checkDate(date: Date, locale: Locale = Locale.getDefault()): String {
        val currentDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Subtract 1 day to get yesterday's date

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", locale)

        return when {
            isSameDay(date, currentDate) -> "Today"
            isSameDay(date, calendar.time) -> "Yesterday"
            else -> dateFormat.format(date)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(date1) == dateFormat.format(date2)
    }


    private fun formatDuration(milliseconds: Long): String {
        val seconds = (milliseconds / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60
        val remainingSeconds = seconds % 60
        val remainingMinutes = minutes % 60

        val formattedDuration = StringBuilder()

        if (hours > 0) {
            formattedDuration.append("$hours ${if (hours == 1) "hour" else "hours"} ")
        }
        if (remainingMinutes > 0) {
            formattedDuration.append("$remainingMinutes ${if (remainingMinutes == 1) "min" else "mins"} ")
        }
        if (remainingSeconds > 0) {
            formattedDuration.append("$remainingSeconds ${if (remainingSeconds == 1) "sec" else "secs"}")
        }

        return if (formattedDuration.isEmpty()) "0 secs" else formattedDuration.toString().trim()
    }

    override fun onBackButtonPressed() {
        selectedCallLogsCount = 0
        callLogAdapter.deselectAllCallLogs()
    }
}