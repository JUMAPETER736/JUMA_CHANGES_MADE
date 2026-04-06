package com.uyscuti.sharedmodule

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
) : ListAdapter<GroupMember, com.uyscuti.sharedmodule.adapter.GroupMembersAdapter.MemberViewHolder>(DIFF) {

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


    //  Bind

    override fun onBindViewHolder(holder: com.uyscuti.sharedmodule.adapter.GroupMembersAdapter.MemberViewHolder, position: Int) {
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
