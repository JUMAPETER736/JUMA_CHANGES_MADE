package com.uyscuti.social.circuit.model

import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.utils.waveformseekbar.WaveformSeekBar
import com.uyscuti.social.circuit.data.model.Comment
import com.uyscuti.social.core.common.data.room.entity.FollowUnFollowEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.UserShortsEntity
import com.uyscuti.social.network.api.response.posts.OriginalPost
import com.uyscuti.social.network.api.response.posts.Post

data class ProgressEvent(
    val eventId: String,
    val progress: Int
)

data class UploadSuccessful(
    val success: Boolean
)

data class FeedUploadProgress(
    val maxProgress: Int,
    val currentProgress: Int,
//    val maxProgress:
)

data class FeedUploadSuccessful(
    val success: Boolean,
    val filesToDelete: MutableList<String>
)

data class ShortsCacheEvent(
    val videoPath: ArrayList<String>
)

data class LoadMoreShorts(
    val loadMore: Boolean
)

data class PausePlayEvent(
    val pausePlay: Boolean,
)

data class PauseShort(
    val pause: Boolean,
)

data class PlayPauseEvent(
    val pausePlay: Boolean
)

data class FeedDetailPage(
    val position: Int,
    val data: Post
)

data class FeedOriginalPostDetailPage(
    val position: Int,
    val data: OriginalPost
)

class GoToUserProfileFragment

class GoToShortsFragment(
    var feedPostPosition: Int,
    var feedShortsBusinessId: String,
    var fileId: String
)

class GoToFeedFragment(
    var feedPostPosition: Int
)

class GoToUserProfileShortsPlayerFragment(
    val userShortsList: ArrayList<UserShortsEntity>,
    val clickedShort: UserShortsEntity,
    val isFromFavorite: Boolean
)

class GoToUserProfileFragment2
class InformAdapter
data class UpdateButtonEvent(val newText: String)


data class ShortsFollowButtonClicked(
    val followUnFollowEntity: FollowUnFollowEntity
)

data class ShortsLikeUnLike(
    val userId: String,
    val isLiked: Boolean
)

data class ShortsLikeUnLike2(
    val userId: String
)

data class ShortsLikeUnLikeButton(
    var shortsEntity: ShortsEntity,
    var likeUnLikeButton: ImageButton,
    var isLiked: Boolean,
    var likeCount: TextView
)


data class ShortsLikeUnLikeButton2(
    var shortsEntity: UserShortsEntity, var likeUnLikeButton: ImageButton,
    var isLiked: Boolean, var likeCount: TextView
)

data class ShortsBookmarkButton(
    var shortsEntity: ShortsEntity, var favoriteButton: ImageView,
)


class ShortsCommentButtonClicked(var position: Int, var userShortEntity: UserShortsEntity)
class ToggleReplyToTextView(var comment: Comment, var position: Int)
class CleanCache(var comment: Comment, var position: Int)
class ReplyAdapterHandler(var comment: Comment, var repliesRecyclerView: RecyclerView)
class CommentRepliesHandler(
    var comment: Comment,
    var commentReplyTV: TextView,
    var repliesRecyclerView: RecyclerView,
    var position: Int
)

class ShortAdapterNotifyDatasetChanged

class FeedAdapterNotifyDatasetChanged(var position: Int)

data class ShortsBookmarkButton2(
    var shortsEntity: UserShortsEntity, var favoriteButton: ImageView
)

data class ShortsFavoriteUnFavorite(val postId: String)
class UserProfileShortsStartGet

class ProfileImageEvent(val profilePic: String)

class UserProfileShortsOnClickEvent(val shortsEntity: List<UserShortsEntity>?)

data class ShowHideBottomNav(val showHideBottomNav: Boolean)
class ShowBottomNav(bool: Boolean)
class HideBottomNav
class ShowAppBar(bool: Boolean)
class HideAppBar

data class HandleInShortsFollowButtonClick(
    val followButton: AppCompatButton,
    val userId: String,
    val username: String
)

data class AudioPlayerHandler(
    val audioPath: String,
    val audioWave: WaveformSeekBar,
    val leftDuration: TextView,
    val waveProgress: Float,
    var position: Int
)

data class CommentAudioPlayerHandler(
    val audioPath: String,
    val audioSeekBar: SeekBar,
    val leftDuration: TextView,
    val seekBarProgress: Float,
    var position: Int,
    var maxDuration: Long
)

data class PauseCommentAudio(
    var pauseCommentAudio: Boolean = false
)

data class SecondWaveForm(
    val audioWave: WaveformSeekBar
)

//data class RefreshParentComment(
//    val position: Int
//)
class LikeCommentReply(
    var commentReply: com.uyscuti.social.network.api.response.commentreply.allreplies.Comment,
    var comment: Comment,
    var position: Int
)


data class LikeComment(val data: Comment, val position: Int)


data class FeedCommentClick(
    val position: Int,
    val data: com.uyscuti.social.network.api.response.posts.OriginalPost
)

data class FeedCommentClicked(
    val position: Int,
    val data: Post
)
