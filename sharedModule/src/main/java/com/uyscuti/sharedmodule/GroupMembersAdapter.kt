package com.uyscuti.sharedmodule.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.R
import com.uyscuti.social.network.api.request.group.GroupMember

/**
 * GroupMembersAdapter
 *
 * Tap the ⋮ button (or long-press any row) to open a context menu.
 *
 * Admin seeing a member    → Make Moderator | Make Admin | Mute/Unmute | Remove
 * Admin seeing a moderator → Remove as Moderator | Make Admin | Mute/Unmute | Remove
 * Admin seeing an admin    → (no actions — peer admins are untouchable)
 * Moderator seeing a member→ Mute/Unmute | Remove
 * Member / self            → no actions
 *
 * Requires isMuted field on GroupMember data class (add with default false).
 */
class GroupMembersAdapter(
    private val context:  Context,
    private val myRole:   String,       // "admin" | "moderator" | "member"
    private val myUserId: String,
    private val onAction: (member: GroupMember, action: String) -> Unit
) : ListAdapter<GroupMember, GroupMembersAdapter.MemberViewHolder>(DIFF) {


    companion object {
        val DIFF = object : DiffUtil.ItemCallback<GroupMember>() {
            override fun areItemsTheSame(a: GroupMember, b: GroupMember) =
                a.user._id == b.user._id

            override fun areContentsTheSame(a: GroupMember, b: GroupMember) =
                a.role    == b.role    &&
                        a.isMuted == b.isMuted &&
                        a.user._id == b.user._id
        }
    }
    //  ViewHolder

    inner class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar:     ImageView = view.findViewById(R.id.memberAvatar)
        val name:       TextView  = view.findViewById(R.id.memberName)
        val roleLabel:  TextView  = view.findViewById(R.id.memberRoleLabel)
        val mutedBadge: TextView  = view.findViewById(R.id.memberMutedBadge)
        val moreBtn:    ImageView = view.findViewById(R.id.memberMoreBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder =
        MemberViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group_member, parent, false)
        )

    //  Bind

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member     = getItem(position)
        val targetRole = member.role.name          // "admin" | "moderator" | "member"
        val isSelf     = member.user._id == myUserId
        val isMuted    = member.isMuted             // direct field — no reflection needed

        //  Avatar
        val avatarUrl = member.user.avatar?.url?.trim() ?: ""
        if (avatarUrl.isNotEmpty()) {
            Glide.with(context).load(avatarUrl).circleCrop()
                .placeholder(R.drawable.baseline_account_circle_24)
                .error(R.drawable.baseline_account_circle_24)
                .into(holder.avatar)
        } else {
            holder.avatar.setImageResource(R.drawable.baseline_account_circle_24)
        }

        //  Name
        val displayName = member.user.username ?: member.user.fullName ?: "Unknown"
        holder.name.text = if (isSelf) "$displayName (You)" else displayName

        //  Role badge — pill colour changes per role ─
        when (targetRole) {
            "admin" -> {
                holder.roleLabel.text = "Admin"
                holder.roleLabel.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#1565C0")) // dark blue
                holder.roleLabel.visibility = View.VISIBLE
            }
            "moderator" -> {
                holder.roleLabel.text = "Moderator"
                holder.roleLabel.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#6A1B9A")) // purple
                holder.roleLabel.visibility = View.VISIBLE
            }
            else -> holder.roleLabel.visibility = View.GONE
        }

        //  Muted badge
        holder.mutedBadge.visibility = if (isMuted) View.VISIBLE else View.GONE

        //  More / kebab button ─
        val actions = buildActions(targetRole, isSelf, isMuted)
        holder.moreBtn.visibility = if (actions.isEmpty()) View.GONE else View.VISIBLE

        holder.moreBtn.setOnClickListener {
            showActionMenu(holder.itemView.context, member, actions)
        }
        holder.itemView.setOnLongClickListener {
            if (actions.isNotEmpty()) {
                showActionMenu(holder.itemView.context, member, actions)
                true
            } else false
        }
        // Also allow tapping the name area to open the menu (more discoverable)
        holder.name.setOnClickListener {
            if (actions.isNotEmpty()) showActionMenu(holder.itemView.context, member, actions)
        }
    }

    //  Build action list ─

    /**
     * Returns ordered (label, actionKey) pairs the current user may perform
     * on a member with [targetRole].
     *
     * Action keys match GroupParticipantsFragment.handleMemberAction():
     *   make_moderator, make_admin, make_member, mute, unmute, remove
     */
    private fun buildActions(
        targetRole: String,
        isSelf:     Boolean,
        isMuted:    Boolean
    ): List<Pair<String, String>> {
        if (isSelf) return emptyList()

        val actions = mutableListOf<Pair<String, String>>()

        when (myRole) {
            "admin" -> when (targetRole) {
                "member" -> {
                    actions += "Make Moderator"    to "make_moderator"
                    actions += "Make Admin"         to "make_admin"
                    actions += if (isMuted)
                        "Unmute Member" to "unmute"
                    else
                        "Mute Member"   to "mute"
                    actions += "Remove from group" to "remove"
                }
                "moderator" -> {
                    actions += "Remove as Moderator" to "make_member"
                    actions += "Make Admin"           to "make_admin"
                    actions += if (isMuted)
                        "Unmute Moderator" to "unmute"
                    else
                        "Mute Moderator"   to "mute"
                    actions += "Remove from group"   to "remove"
                }
                // Peer admin — can demote to moderator or member, but NOT remove
                // (the server also enforces this: admins cannot remove other admins)
                "admin" -> {
                    actions += "Make Moderator"   to "make_moderator"
                    actions += "Make Member"       to "make_member"
                    // Admins cannot be muted or removed — intentionally omitted
                }
            }
            "moderator" -> {
                if (targetRole == "member") {
                    actions += if (isMuted)
                        "Unmute Member" to "unmute"
                    else
                        "Mute Member"   to "mute"
                    actions += "Remove from group" to "remove"
                }
            }
            // "member" → no actions
        }

        return actions
    }

    //  Action menu dialog

    private fun showActionMenu(
        ctx:     Context,
        member:  GroupMember,
        actions: List<Pair<String, String>>
    ) {
        val displayName = member.user.username ?: member.user.fullName ?: "Member"
        AlertDialog.Builder(ctx)
            .setTitle(displayName)
            .setItems(actions.map { it.first }.toTypedArray()) { _, which ->
                onAction(member, actions[which].second)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //  DiffUtil


}