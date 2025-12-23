
package com.uyscuti.social.network.api.response.allFeedRepostsPost
import com.uyscuti.social.network.api.response.comment.allcomments.Comment
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.Duration
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileName
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.FileSize
import com.uyscuti.social.network.api.response.feed.getallfeed.more_feed_data_classes.NumberOfPages
import com.uyscuti.social.network.api.response.getfeedandresposts.Thumbnail
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.File
import com.uyscuti.social.network.api.response.getrepostsPostsoriginal.FileType
import retrofit2.*
import retrofit2.http.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable



data class Post(
    val __v: Int,
    var _id: String,
    val author: Author,
    var comments: Int,
    var content: String,
    val contentType: String,
    val createdAt: String,
    var duration: List<Duration>,
    var fileIds: List<Any?>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>,
    var fileTypes: List<FileType>,
    var files: List<File>,
    val isExpanded: Boolean,
    val isLocal: Boolean,
    val isFollowing: Boolean,
    val feedShortsBusinessId: String,
    val numberOfPages: List<NumberOfPages>,
    val originalPost: List<OriginalPost>,
    val repostedByUserId: String,
    val repostedUser: RepostedUser?,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    var thumbnail: List<Thumbnail>,
    val updatedAt: String,
    var likes: Int,
    var bookmarkCount: Int,
    var shareCount: Int = 0,
    var commentCount: Int? = 0,
    var repostCount: Int? = 0,
    var isLiked: Boolean,
    var isBookmarked: Boolean,
    var isShared: Boolean = false,
    var isReposted: Boolean,
    val url: String,
    val type: String,
    val profilePicUrl: String?,
    val username: String?,
    val fullName: String?,
    val isLikedCount: Boolean = false,
    val isBookmarkCount: Boolean = false,

    val description: String,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isFromFriend: Boolean = false,
    val isFromFamily: Boolean = false,
    val isFromFollower: Boolean = false,
    val isFromMutualFriend: Boolean = false,
    val isFromCloseFriend: Boolean = false,
    val isFromCelebrity: Boolean = false,
    val isFromInfluencer: Boolean = false,





    ) : Serializable {

    val safeCommentCount: Int get() = commentCount ?: comments
    val safeLikes: Int get() = likes
    val safeBookmarkCount: Int get() = bookmarkCount
    val safeRepostCount: Int get() = repostCount ?: 0
    val safeShareCount: Int get() = shareCount


    fun toggleLike() {
        isLiked = !isLiked
    }

    fun toggleBookmark() {
        isBookmarked = !isBookmarked
    }

    fun toggleShare() {
        isShared = !isShared
    }

}



data class CommentsResponse(
    val success: Boolean,
    val comments: List<Comment>,
    val totalCount: Int,
    val page: Int,
    val hasMore: Boolean,
    val message: String? = null
)

data class AddCommentRequest(
    val content: String,
    val contentType: String = "text",
    val parentCommentId: String? = null // For replies
)

data class DeleteResponse(
    val success: Boolean,
    val message: String
)


data class BookmarkResponse(
    val success: Boolean,
    val message: String,
    val isBookmarked: Boolean,
    val bookmarkCount: Int
)



data class RepostRequest(
    val isReposted: Boolean,
    val comment: String,
    val files: List<File>? = null,
    val tags: List<String>? = null
)

data class RepostRequestMultiPart(
    val comment: String,
    val originalPostId: String,
    val hasNewFiles: Boolean = false
)

data class RepostResponse(
    val success: Boolean,
    val message: String,
    val repostCount: Int,
    val isReposted: Boolean = false
)

data class LikeResponse(
    val success: Boolean,
    val message: String,
    val likesCount: Int,
    val isLiked: Boolean
)


data class CommentCountResponse(
    val success: Boolean,
    val count: Int,
    val message: String? = null
)


data class BookmarkRequest(
    val isBookmarked: Boolean
)


data class LikeRequest(
    val isLiked: Boolean
)


interface BookmarkService {
    @POST("posts/{postId}/bookmark")
    fun toggleBookmark(
        @Path("postId") postId: String,
        @Body bookmarkRequest: BookmarkRequest
    ): Call<BookmarkResponse>
}

interface ShareService {
    @POST("posts/{postId}/share")
    fun incrementShare(@Path("postId") postId: String): Call<ShareResponse>
}

// Updated RepostService interface
interface RepostService {
    @POST("posts/{postId}/repost")
    fun incrementRepost(@Path("postId") postId: String): Call<RepostResponse>

    @DELETE("posts/{postId}/repost")
    fun decrementRepost(@Path("postId") postId: String): Call<RepostResponse>

    // Alternative: If your API uses a toggle endpoint instead
    @POST("posts/{postId}/repost/toggle")
    fun toggleRepost(
        @Path("postId") postId: String,
        @Body repostRequest: RepostRequest
    ): Call<RepostResponse>
}


interface LikeService {
    @POST("posts/{postId}/like")
    fun toggleLike(
        @Path("postId") postId: String,
        @Body likeRequest: LikeRequest
    ): Call<LikeResponse>
}


interface CommentService {

    // NEW: Shorts Comments (social-media/comments/post/{postId})
    @GET("social-media/comments/post/{postId}")
    fun getShortsComments(
        @Path("postId") postId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<CommentsResponse>

    // NEW: Feed Comments (feed/comments/{postId})
    @GET("feed/comments/{postId}")
    fun getFeedComments(
        @Path("postId") postId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<CommentsResponse>

    @GET("posts/{postId}/comments")
    fun getCommentsForPost(
        @Path("postId") postId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<CommentsResponse>

    @POST("posts/{postId}/comments")
    fun addComment(
        @Path("postId") postId: String,
        @Body commentRequest: AddCommentRequest
    ): Call<Comment>

    @DELETE("comments/{commentId}")
    fun deleteComment(@Path("commentId") commentId: String): Call<DeleteResponse>

    @GET("posts/{postId}/comments/count")
    fun getCommentCount(@Path("postId") postId: String): Call<CommentCountResponse>

    @PUT("comments/{commentId}/like")
    fun toggleCommentLike(
        @Path("commentId") commentId: String,
        @Body likeRequest: LikeRequest
    ): Call<LikeResponse>
}



data class ShareResponse(
    val success: Boolean,
    val shareCount: Int,
    val message: String? = null
)


object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.103:8080/api/v1/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val bookmarkService: BookmarkService = retrofit.create(BookmarkService::class.java)
    val repostService: RepostService = retrofit.create(RepostService::class.java)
    val likeService: LikeService = retrofit.create(LikeService::class.java)
    val shareService: ShareService = retrofit.create(ShareService::class.java)
    val commentService: CommentService by lazy {
        retrofit.create(CommentService::class.java)
    }
}




