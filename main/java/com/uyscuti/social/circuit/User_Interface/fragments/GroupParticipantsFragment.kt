package com.uyscuti.social.circuit.User_Interface.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.GroupParticipantAdapter
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.data.model.User

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupParticipantsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupParticipantsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var groupParticipantAdapter: GroupParticipantAdapter
    private lateinit var groupParticipantRecyclerView: RecyclerView
    private lateinit var participantsNumber: TextView
    private lateinit var dialog: Dialog
    private lateinit var participantsList: List<User>
    private lateinit var adminId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialog = it.getParcelable("DIALOG")!!
            participantsList = dialog.users
            param1 = it.getString(ARG_PARAM1)
            adminId = param1.toString()
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group_participants, container, false)
        groupParticipantRecyclerView = view.findViewById(R.id.participantsListRV)
        groupParticipantRecyclerView.layoutManager = LinearLayoutManager(context)
        participantsNumber = view.findViewById(R.id.participantsNumber)

        val number = participantsList.size
        val text = "$number participants"
        participantsNumber.text = text

//        Log.d("Participants", participantsList.toString())
        for(i in participantsList) {

        }

        participantsList.map { user->
            if (user.id == adminId) {
                Log.d("Participants", "Admin is ${user.name}")
            }
        }

        val sorted = participantsList.sortedWith(compareBy(
            // First, sort by a custom order where "admin" comes first
            { if (it.id == adminId) 0 else 1 },
            // Then, sort alphabetically by name for other users
            { it.name }
        ))
        groupParticipantAdapter = GroupParticipantAdapter(sorted)
        groupParticipantAdapter.setAdminId(adminId)
        groupParticipantRecyclerView.adapter = groupParticipantAdapter
        return view
    }

    companion object {
        fun newInstance(dialog: Dialog, adminId: String, arg2: String): GroupParticipantsFragment {
            val fragment = GroupParticipantsFragment()
            val args = Bundle()
            args.putParcelable("DIALOG", dialog)
            args.putString(ARG_PARAM1, adminId)
            args.putString(ARG_PARAM2, arg2)
            fragment.arguments = args
            return fragment
        }
    }
}