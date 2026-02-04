package com.uyscuti.social.network.api.retrofit.interfaces


import com.uyscuti.social.network.api.response.business.response.background.BackgroundVideoResponse
import com.uyscuti.social.network.api.response.business.response.create.CreateProfileResponse
import com.uyscuti.social.network.api.response.business.response.livelocation.LiveLocationResponse
import com.uyscuti.social.network.api.response.business.response.product.AddProductResponse
import com.uyscuti.social.network.api.response.business.response.product.DeleteProductResponse
import com.uyscuti.social.network.api.response.business.response.product.GetProductsResponse
import com.uyscuti.social.network.api.response.business.response.profile.ProfileResponse
import com.uyscuti.social.network.api.ai.SendFileResponse
import com.uyscuti.social.network.api.request.FaceBookLogIn.FacebookLoginRequest
import com.uyscuti.social.network.api.request.business.catalogue.GetCatalogueByUserId
import com.uyscuti.social.network.api.request.business.create.CreateBusinessProfile
import com.uyscuti.social.network.api.request.business.users.GetAvailableUsers
import com.uyscuti.social.network.api.request.business.users.GetBusinessProfileById
import com.uyscuti.social.network.api.request.comment.CommentRequestBody
import com.uyscuti.social.network.api.request.comment.GifCommentRequestBody
import com.uyscuti.social.network.api.request.feed.FeedTextUploadRequestBody
import com.uyscuti.social.network.api.request.googlelogin.GoogleLoginRequest
import com.uyscuti.social.network.api.request.group.RequestGroupChat
import com.uyscuti.social.network.api.request.group.ResponseGroupChat
import com.uyscuti.social.network.api.request.login.LoginRequest
import com.uyscuti.social.network.api.request.messages.SendMessageRequest
import com.uyscuti.social.network.api.request.profile.UpdateSocialProfileRequest
import com.uyscuti.social.network.api.request.register.RegisterRequest
import com.uyscuti.social.network.api.response.MainResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.AllFeedRepostsPost
import com.uyscuti.social.network.api.response.allFeedRepostsPost.BookmarkResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostRequest
import com.uyscuti.social.network.api.response.business.response.background.BackgroundImageResponse
import com.uyscuti.social.network.api.response.business.response.businesslocation.AdvertisementResponse
import com.uyscuti.social.network.api.response.business.response.businesslocation.BusinessLocationResponse
import com.uyscuti.social.network.api.response.business.response.post.BusinessPost
import com.uyscuti.social.network.api.response.business.response.post.comment.BusinessCommentResponse
import com.uyscuti.social.network.api.response.business.response.post.comment.CommentsResponse
import com.uyscuti.social.network.api.response.business.response.post.likes.LikeResponse
import com.uyscuti.social.network.api.response.chats.ChatsResponse
import com.uyscuti.social.network.api.response.chats.FetchChatResponse
import com.uyscuti.social.network.api.response.comment.ShortCommentResponse
import com.uyscuti.social.network.api.response.commentreply.CommentReplyResponse
import com.uyscuti.social.network.api.response.comment.allcomments.AllShortComments
import com.uyscuti.social.network.api.response.comment.like_unlike_comment.LikeUnLikeCommentResponse
import com.uyscuti.social.network.api.response.commentreply.allreplies.AllCommentReplies
import com.uyscuti.social.network.api.response.createRepostFeedPost.CreateRepostFeedPost
import com.uyscuti.social.network.api.response.login.LoginResponse
import com.uyscuti.social.network.api.response.messages.GetMessagesResponse
import com.uyscuti.social.network.api.response.messages.SendMessageResponse
import com.uyscuti.social.network.api.response.register.RegisterResponse
import com.uyscuti.social.network.api.response.users.UsersResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import com.uyscuti.social.network.api.response.createchat.CreateChatResponse
import com.uyscuti.social.network.api.response.favoritefeed.FeedFavoriteResponse
import com.uyscuti.social.network.api.response.favoriteshort.ShortsFavoriteResponse
import com.uyscuti.social.network.api.response.feed.FeedUploadResponse
import com.uyscuti.social.network.api.response.feed.deletefeed.DeleteFeedResponse
import com.uyscuti.social.network.api.response.follow_unfollow.AllBlockedUsersResponse
import com.uyscuti.social.network.api.response.follow_unfollow.BlockUnblockResponse
import com.uyscuti.social.network.api.response.follow_unfollow.FollowUnFollowResponse
import com.uyscuti.social.network.api.response.getCommentNotification.GetCommentNotification
import com.uyscuti.social.network.api.response.getPageComment.GetPageCommentId
import com.uyscuti.social.network.api.response.getUnifiedNotification.GetUnifiedNotifications
import com.uyscuti.social.network.api.response.getallshorts.GetAllShortsResponse
import com.uyscuti.social.network.api.response.getallshorts.ResponseData
import com.uyscuti.social.network.api.response.getfavoriteshorts.GetFavoriteShortsResponse
import com.uyscuti.social.network.api.response.getmyprofile.GetMyProfile
import com.uyscuti.social.network.api.response.gif.GifResponse
import com.uyscuti.social.network.api.response.gif.allgifs.AllGifsResponse
import com.uyscuti.social.network.api.response.googleloginresponse.GoogleLoginResponse
import com.uyscuti.social.network.api.response.lastseen.LastSeenResponse
import com.uyscuti.social.network.api.response.likeunlikeshort.LikeUnLikeShortResponse
import com.uyscuti.social.network.api.response.notification.ReadNotificationResponse
import com.uyscuti.social.network.api.response.otherusersprofile.OtherUsersProfileResponse
import com.uyscuti.social.network.api.response.otherusersprofileshorts.OtherUsersProfileShortsResponse
import com.uyscuti.social.network.api.response.post.GetPostById
import com.uyscuti.social.network.api.response.shorts.ShortsUploadResponse
import com.uyscuti.social.network.api.response.updateavatar.UpdateAvatarResponse
import com.uyscuti.social.network.api.response.userstatus.UserStatusResponse
import com.uyscuti.social.network.api.response.getallshorts.ApiResponse
import com.uyscuti.social.network.api.response.login.ForgotPasswordRequest
import com.uyscuti.social.network.api.response.login.ForgotPasswordResponse
import com.uyscuti.social.network.api.response.login.ResendEmailRequest
import com.uyscuti.social.network.api.response.login.ResetPasswordRequest
import com.uyscuti.social.network.api.response.login.ResetPasswordResponse
import com.uyscuti.social.network.api.response.login.VerifyOTPRequest
import com.uyscuti.social.network.api.response.login.VerifyOTPResponse
import com.uyscuti.social.network.api.response.posts.FeedResponse
import com.uyscuti.social.network.api.response.profile.followersList.UserOtherFollowersResponse
import com.uyscuti.social.network.api.response.profile.followingList.BaseResponse
import com.uyscuti.social.network.api.response.profile.followingList.CloseFriendStatusResponse
import com.uyscuti.social.network.api.response.profile.followingList.CloseFriendsListResponse
import com.uyscuti.social.network.api.response.profile.followingList.FavoriteStatusResponse
import com.uyscuti.social.network.api.response.profile.followingList.FavoritesListResponse
import com.uyscuti.social.network.api.response.profile.followingList.MutedPostsListResponse
import com.uyscuti.social.network.api.response.profile.followingList.MutedPostsStatusResponse
import com.uyscuti.social.network.api.response.profile.followingList.MutedStoriesListResponse
import com.uyscuti.social.network.api.response.profile.followingList.MutedStoriesStatusResponse
import com.uyscuti.social.network.api.response.profile.followingList.OtherUserFollowingResponse
import com.uyscuti.social.network.api.response.profile.followingList.RestrictedListResponse
import com.uyscuti.social.network.api.response.profile.followingList.RestrictedStatusResponse
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.PUT


interface IFlashapi {

    // ==================== AUTHENTICATION ====================

    @POST("auth/facebook-login")
    suspend fun facebookLogin(@Body request: FacebookLoginRequest): Response<LoginResponse>

    @POST("users/register")
    suspend fun registerUsers(@Body requestPost: RegisterRequest): Response<RegisterResponse>

    @POST("users/login")
    suspend fun loginUsers(@Body requestPost: LoginRequest): Response<LoginResponse>

    @POST("users/google-login")
    suspend fun googleLogin(@Body requestPost: GoogleLoginRequest): Response<GoogleLoginResponse>

    @POST("users/forgot-password-otp")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("users/verify-otp")
    suspend fun verifyOTP(@Body request: VerifyOTPRequest): Response<VerifyOTPResponse>

    @POST("users/reset-password-otp")
    suspend fun resetPassword(@Body requestPost: ResetPasswordRequest): Response<ResetPasswordResponse>



    @Multipart
    @PATCH("users/avatar")
    suspend fun updateUserAvatar(@Part avatar: MultipartBody.Part): Response<UpdateAvatarResponse>


    // ==================== CHAT & MESSAGING ====================

    @GET("chat-app/chats")
    suspend fun getChats(): Response<ChatsResponse>

    @GET("chat-app/chats")
    suspend fun getChats(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<ChatsResponse>


    @GET("chat-app/chats/fetchChat/{chatId}")
    suspend fun fetchChat(@Path("chatId") chatId: String): Response<FetchChatResponse>

    @GET("chat-app/chats/users")
    suspend fun getUsers(): Response<UsersResponse>

    @GET("chat-app/chats/users")
    suspend fun getOtherUsers(): Response<GetAvailableUsers>

    @GET("chat-app/chats/users/search")
    suspend fun searchUsers(@Query("query") query: String): Response<UsersResponse>

    @GET("chat-app/chats/users/user-status/{userId}")
    suspend fun getUserStatus(@Path("userId") userId: String): Response<UserStatusResponse>

    @GET("chat-app/chats/users/{userId}/lastseen")
    suspend fun getUserLastSeen(@Path("userId") userId: String): Response<LastSeenResponse>

    @POST("chat-app/chats/c/{receiverId}")
    suspend fun createUserChat(@Path("receiverId") receiverId: String): Response<CreateChatResponse>

    @POST("chat-app/chats/group")
    suspend fun createGroupChat(@Body groupChatRequest: RequestGroupChat): Response<ResponseGroupChat>

    @GET("chat-app/messages/{chatId}")
    suspend fun getMessages(@Path("chatId") chatId: String): Response<GetMessagesResponse>


    @POST("chat-app/messages/{chatId}")
    suspend fun sendMessage(@Path("chatId") chatId: String, @Body requestPost: SendMessageRequest): Response<SendMessageResponse>

    @Multipart
    @POST("chat-app/messages/{chatId}")
    suspend fun sendAttachment(
        @Path("chatId") chatId: String,
        @Part filePath: MultipartBody.Part
    ): Response<SendMessageResponse>

    @Multipart
    @POST("chat-app/messages/{chatId}")
    suspend fun uploadImage(
        @Path("chatId") chatId: String,
        @Part image: MultipartBody.Part
    ): Response<SendFileResponse>


    // ==================== SOCIAL MEDIA - PROFILE ====================

    @GET("social-media/profile")
    suspend fun getMyProfile(): Response<GetMyProfile>

    @PATCH("social-media/profile")
    suspend fun updateMyProfile(@Body requestPost: UpdateSocialProfileRequest): Response<GetMyProfile>

    @GET("social-media/profile/u/{username}")
    suspend fun getOtherUsersProfileByUsername(@Path("username") username: String): Response<OtherUsersProfileResponse>


    // ==================== SOCIAL MEDIA - FOLLOW/UNFOLLOW ====================

    @POST("social-media/follow/{toBeFollowedUserId}")
    suspend fun followUnFollow(@Path("toBeFollowedUserId") toBeFollowedUserId: String): Response<FollowUnFollowResponse>

    @GET("social-media/profile/{username}/followers")
    suspend fun getUserFollowers(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<UserOtherFollowersResponse>

    @GET("social-media/profile/{username}/following")
    suspend fun getUserFollowing(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<OtherUserFollowingResponse>


    @DELETE("social-media/followers/{userId}")
    suspend fun removeFollower(@Path("userId") userId: String): Response<FollowUnFollowResponse>

    @DELETE("social-media/following/{userId}")
    suspend fun unfollowUser(@Path("userId") userId: String): Response<FollowUnFollowResponse>



    // ==================== CLOSE FRIENDS ====================
    @GET("social-media/profile/close-friends")
    suspend fun getCloseFriends(): Response<CloseFriendsListResponse>

    @GET("social-media/profile/close-friends/{userId}")
    suspend fun checkCloseFriendStatus(@Path("userId") userId: String): Response<CloseFriendStatusResponse>

    @POST("social-media/profile/close-friends/{userId}")
    suspend fun addToCloseFriends(@Path("userId") userId: String): Response<BaseResponse>

    @DELETE("social-media/profile/close-friends/{userId}")
    suspend fun removeFromCloseFriends(@Path("userId") userId: String): Response<BaseResponse>

    // ==================== MUTE POSTS ====================
    @GET("social-media/profile/mute/posts")
    suspend fun getMutedPostsUsers(): Response<MutedPostsListResponse>

    @GET("social-media/profile/mute/posts/{userId}")
    suspend fun checkMutedPostsStatus(@Path("userId") userId: String): Response<MutedPostsStatusResponse>

    @POST("social-media/profile/mute/posts/{userId}")
    suspend fun mutePosts(@Path("userId") userId: String): Response<BaseResponse>

    @DELETE("social-media/profile/mute/posts/{userId}")
    suspend fun unMutePosts(@Path("userId") userId: String): Response<BaseResponse>

    // ==================== MUTE STORIES ====================
    @GET("social-media/profile/mute/stories")
    suspend fun getMutedStoriesUsers(): Response<MutedStoriesListResponse>

    @GET("social-media/profile/mute/stories/{userId}")
    suspend fun checkMutedStoriesStatus(@Path("userId") userId: String): Response<MutedStoriesStatusResponse>

    @POST("social-media/profile/mute/stories/{userId}")
    suspend fun muteStories(@Path("userId") userId: String): Response<BaseResponse>

    @DELETE("social-media/profile/mute/stories/{userId}")
    suspend fun unMuteStories(@Path("userId") userId: String): Response<BaseResponse>

    // ==================== FAVORITES ====================
    @GET("social-media/profile/favorites")
    suspend fun getFavorites(): Response<FavoritesListResponse>

    @GET("social-media/profile/favorites/{userId}")
    suspend fun checkFavoriteStatus(@Path("userId") userId: String): Response<FavoriteStatusResponse>

    @POST("social-media/profile/favorites/{userId}")
    suspend fun addToFavorites(@Path("userId") userId: String): Response<BaseResponse>

    @DELETE("social-media/profile/favorites/{userId}")
    suspend fun removeFromFavorites(@Path("userId") userId: String): Response<BaseResponse>

    // ==================== RESTRICT ====================
    @GET("social-media/profile/restrict")
    suspend fun getRestrictedUsers(): Response<RestrictedListResponse>

    @GET("social-media/profile/restrict/{userId}")
    suspend fun checkRestrictedStatus(@Path("userId") userId: String): Response<RestrictedStatusResponse>

    @POST("social-media/profile/restrict/{userId}")
    suspend fun restrictUser(@Path("userId") userId: String): Response<BaseResponse>

    @DELETE("social-media/profile/restrict/{userId}")
    suspend fun unRestrictUser(@Path("userId") userId: String): Response<BaseResponse>


    // ==================== SOCIAL MEDIA - BLOCK/UNBLOCK ====================


    @POST("social-media/block/{userId}")
    suspend fun blockUser(@Path("userId") userId: String): Response<BlockUnblockResponse>

    @DELETE("social-media/block/{userId}")
    suspend fun unBlockUser(@Path("userId") userId: String): Response<BlockUnblockResponse>

    @GET("social-media/block")
    suspend fun getAllBlockedUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<AllBlockedUsersResponse>


    @GET("api/v1/social-media/blocked")
    suspend fun getBlockedUsers(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): Response<UsersResponse>


    // ==================== SOCIAL MEDIA - SHORTS (POSTS) ====================

    @GET("social-media/posts")
    suspend fun getAllPosts3(): Response<ApiResponse<ResponseData>>

    @GET("social-media/posts")
    suspend fun getShorts(@Query("page") page: String): Response<ApiResponse<ResponseData>>


    @GET("social-media/posts/getAllShortsByFeedShortBusinessId/{feedShortsBusinessId}")
    suspend fun getAllShortsByFeedShortBusinessId(
        @Path("feedShortsBusinessId") feedShortsBusinessId: String
    ): Response<ApiResponse<ResponseData>>

    @GET("social-media/posts/get/my")
    suspend fun myShorts(@Query("page") page: String): Response<GetAllShortsResponse>

    @GET("social-media/posts/get/u/{username}")
    suspend fun getShortsByUsername(@Path("username") username: String): Response<OtherUsersProfileShortsResponse>

    @GET("social-media/posts/get/u/{username}")
    suspend fun getShortsByUsernameWithPage(
        @Path("username") username: String,
        @Query("page") page: String
    ): Response<OtherUsersProfileShortsResponse>

    @GET("social-media/posts/{postId}")
    suspend fun getPostById(@Path("postId") postId: String): Response<GetPostById>

    @Multipart
    @POST("social-media/posts")
    suspend fun uploadShort(
        @Part("content") content: RequestBody,
        @Part("fileId") fileId: RequestBody,
        @Part("feedShortsBusinessId") feedShortsBusinessId: RequestBody,
        @Part images: MultipartBody.Part,
        @Part("tags") tags: Array<RequestBody>? = null,
        @Part thumbnail: MultipartBody.Part
    ): Response<ShortsUploadResponse>


    // ==================== SOCIAL MEDIA - LIKES & BOOKMARKS ====================

    @POST("social-media/like/post/{postId}")
    suspend fun likeUnLikeShort(@Path("postId") postId: String): Response<LikeUnLikeShortResponse>

    @POST("social-media/like/comment/{commentId}")
    suspend fun likeUnLikeComment(@Path("commentId") commentId: String): Response<LikeUnLikeCommentResponse>

    @POST("social-media/like/comment/reply/{commentReplyId}")
    suspend fun likeUnLikeCommentReply(@Path("commentReplyId") commentReplyId: String): Response<LikeUnLikeCommentResponse>

    @POST("social-media/bookmarks/{postId}")
    suspend fun favoriteShort(@Path("postId") postId: String): Response<ShortsFavoriteResponse>

    @GET("social-media/bookmarks")
    suspend fun getFavoriteShorts(@Query("page") page: String): Response<GetFavoriteShortsResponse>


    // ==================== SOCIAL MEDIA - COMMENTS ====================

    @GET("social-media/comments/post/{postId}")
    suspend fun getShortComments(
        @Path("postId") postId: String,
        @Query("page") page: String
    ): Response<AllShortComments>

    @GET("social-media/comments/{commentId}")
    suspend fun getPageComment(@Path("commentId") commentId: String): Response<GetPageCommentId>

    @POST("social-media/comments/post/{postId}")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body requestBody: CommentRequestBody
    ): Response<ShortCommentResponse>

    @POST("social-media/comments/post/{postId}")
    suspend fun addGifComment(
        @Path("postId") postId: String,
        @Body requestBody: GifCommentRequestBody
    ): Response<ShortCommentResponse>

    @Multipart
    @POST("social-media/comments/post/{postId}")
    suspend fun addImageComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ShortCommentResponse>

    @Multipart
    @POST("social-media/comments/post/{postId}")
    suspend fun addVideoComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part
    ): Response<ShortCommentResponse>

    @Multipart
    @POST("social-media/comments/post/{postId}")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part audio: MultipartBody.Part
    ): Response<ShortCommentResponse>

    @Multipart
    @POST("social-media/comments/post/{postId}")
    suspend fun addDocumentComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileSize") fileSize: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("numberOfPages") numberOfPages: RequestBody,
        @Part docs: MultipartBody.Part
    ): Response<ShortCommentResponse>


    // ==================== SOCIAL MEDIA - COMMENT REPLIES ====================

    @GET("social-media/comment/reply/comment/{commentId}")
    suspend fun getCommentReplies(
        @Path("commentId") commentId: String,
        @Query("page") page: String
    ): Response<AllCommentReplies>

    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addCommentReply(
        @Path("commentId") commentId: String,
        @Body requestBody: CommentRequestBody
    ): Response<CommentReplyResponse>

    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addGifCommentReply(
        @Path("commentId") commentId: String,
        @Body requestBody: GifCommentRequestBody
    ): Response<CommentReplyResponse>

    @Multipart
    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addCommentReply(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part audio: MultipartBody.Part
    ): Response<CommentReplyResponse>

    @Multipart
    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addImageCommentReply(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<CommentReplyResponse>

    @Multipart
    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addVideoCommentReply(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part
    ): Response<CommentReplyResponse>

    @Multipart
    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addDocumentCommentReply(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileSize") fileSize: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("numberOfPages") numberOfPages: RequestBody,
        @Part docs: MultipartBody.Part
    ): Response<CommentReplyResponse>


    // ==================== FEED (POSTS) ====================

    @GET("feed/post/test")
    suspend fun getAllFeed(@Query("page") page: String): Response<FeedResponse>

    @GET("feed/post/test")
    suspend fun getUserFeedForSearch(
        @Query("username") username: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<FeedResponse>


    @GET("feed/post/get/my")
    suspend fun getMyFeed(@Query("page") page: String): Response<AllFeedRepostsPost>


    @POST("feed/post")
    suspend fun uploadTextFeed(@Body requestBody: FeedTextUploadRequestBody): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadFileFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part files: MultipartBody.Part,
        @Part("tags") tags: Array<RequestBody>? = null,
        @Part thumbnail: MultipartBody.Part
    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadFilesFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part files: MultipartBody.Part,
        @Part("tags") tags: Array<RequestBody>? = null
    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadMultipleFilesFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part("tags") tags: Array<RequestBody>? = null
    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadMixedFilesFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("feedShortsBusinessId") feedShortsBusinessId: RequestBody,
        @Part("duration") duration: Array<RequestBody>,
        @Part("fileTypes") fileTypes: Array<RequestBody>,
        @Part("numberOfPages") numberOfPages: Array<RequestBody>,
        @Part("fileNames") fileNames: Array<RequestBody>,
        @Part("fileSizes") fileSizes: Array<RequestBody>,
        @Part("fileIds") fileIds: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part thumbnail: List<MultipartBody.Part>,
        @Part("tags") tags: Array<RequestBody>? = null
    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadMultipleVideoFilesFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part thumbnail: List<MultipartBody.Part>,
        @Part("tags") tags: Array<RequestBody>? = null
    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadFeedDoc(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("numberOfPages") numberOfPages: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("docType") docType: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part thumbnail: List<MultipartBody.Part>,
        @Part("tags") tags: Array<RequestBody>? = null
    ): Response<FeedUploadResponse>

    @DELETE("feed/post/{postId}")
    suspend fun deleteFeed(@Path("postId") postId: String): Response<DeleteFeedResponse>

    @POST("feed/post/repost/{postId}")
    suspend fun repostsFeed(
        @Path("postId") postId: String,
        @Body request: RepostRequest
    ): Response<CreateRepostFeedPost>


    // ==================== FEED - LIKES & BOOKMARKS ====================

    @POST("feed/like/{postId}")
    suspend fun likeUnLikeFeed(@Path("postId") postId: String): Response<LikeUnLikeShortResponse>

    @POST("feed/like/comment/{commentId}")
    suspend fun likeUnLikeFeedComment(@Path("commentId") commentId: String): Response<LikeUnLikeCommentResponse>

    @POST("feed/like/comment/reply/{commentReplyId}")
    suspend fun likeUnLikeFeedCommentReply(@Path("commentReplyId") commentReplyId: String): Response<LikeUnLikeCommentResponse>

    @POST("feed/bookmarks/{postId}")
    suspend fun favoriteFeed(@Path("postId") postId: String): Response<ShortsFavoriteResponse>

    @GET("feed/bookmarks/")
    suspend fun getFavoriteFeed(@Query("page") page: String): Response<FeedFavoriteResponse>

    @POST("feed/bookmark/{postId}")
    suspend fun bookmarkPost(@Path("postId") postId: String): Response<BookmarkResponse>

    @DELETE("feed/bookmark/{postId}")
    suspend fun unbookmarkPost(@Path("postId") postId: String): Response<BookmarkResponse>


    // ==================== FEED - COMMENTS ====================

    @GET("feed/comments/{postId}")
    suspend fun getFeedComments(
        @Path("postId") postId: String,
        @Query("page") page: String
    ): Response<AllShortComments>

    @POST("feed/comments/{postId}")
    suspend fun addFeedComment(
        @Path("postId") postId: String,
        @Body requestBody: CommentRequestBody
    ): Response<ShortCommentResponse>

    @POST("feed/comments/{postId}")
    suspend fun addGifFeedComment(
        @Path("postId") postId: String,
        @Body requestBody: GifCommentRequestBody
    ): Response<ShortCommentResponse>

    @Multipart
    @POST("feed/comments/{postId}")
    suspend fun addImageFeedComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ShortCommentResponse>

    @Multipart
    @POST("feed/comments/{postId}")
    suspend fun addVideoFeedComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part
    ): Response<ShortCommentResponse>

    @Multipart
    @POST("feed/comments/{postId}")
    suspend fun addFeedComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part audio: MultipartBody.Part
    ): Response<ShortCommentResponse>

    @Multipart
    @POST("feed/comments/{postId}")
    suspend fun addDocumentFeedComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileSize") fileSize: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("numberOfPages") numberOfPages: RequestBody,
        @Part docs: MultipartBody.Part
    ): Response<ShortCommentResponse>


    // ==================== FEED - COMMENT REPLIES ====================

    @GET("feed/comment/reply/comment/{commentId}")
    suspend fun getFeedCommentReplies(
        @Path("commentId") commentId: String,
        @Query("page") page: String
    ): Response<AllCommentReplies>

    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedCommentReply(
        @Path("commentId") commentId: String,
        @Body requestBody: CommentRequestBody
    ): Response<CommentReplyResponse>

    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedGifCommentReply(
        @Path("commentId") commentId: String,
        @Body requestBody: GifCommentRequestBody
    ): Response<CommentReplyResponse>

    @Multipart
    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedCommentReply(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part audio: MultipartBody.Part
    ): Response<CommentReplyResponse>

    @Multipart
    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedImageCommentReply(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<CommentReplyResponse>

    @Multipart
    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedVideoCommentReply(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part
    ): Response<CommentReplyResponse>

    @Multipart
    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedDocumentCommentReply(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileSize") fileSize: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("numberOfPages") numberOfPages: RequestBody,
        @Part docs: MultipartBody.Part
    ): Response<CommentReplyResponse>


    // ==================== GIFS ====================

    @Multipart
    @POST("gif")
    suspend fun addGif(
        @Part("fileType") fileType: RequestBody,
        @Part gif: MultipartBody.Part
    ): Response<GifResponse>

    @GET("gif")
    suspend fun getGif(@Query("page") page: String): Response<AllGifsResponse>


    // ==================== BUSINESS - PROFILE ====================

    @GET("business/profile")
    suspend fun getBusinessProfile(): Response<ProfileResponse>

    @POST("business/profile")
    suspend fun createBusinessProfile(@Body profile: CreateBusinessProfile): Response<CreateProfileResponse>

    @GET("business/profile/{userId}")
    suspend fun getUserBusinessProfile(@Path("userId") userId: String): Response<GetBusinessProfileById>

    @Multipart
    @PATCH("business/profile/background")
    suspend fun updateBackground(@Part avatar: MultipartBody.Part): Response<BackgroundImageResponse>

    @Multipart
    @PATCH("business/profile/v")
    suspend fun updateBackgroundVideo(
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part
    ): Response<BackgroundVideoResponse>

    @Multipart
    @PATCH("business/profile/livelocation")
    suspend fun updateLiveLocation(
        @Part("enabled") enabled: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("accuracy") accuracy: RequestBody,
        @Part("range") range: RequestBody
    ): Response<LiveLocationResponse>

    @Multipart
    @PATCH("business/profile/businesslocation")
    suspend fun updateBusinessLocation(
        @Part("enabled") enabled: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("accuracy") accuracy: RequestBody,
        @Part("range") range: RequestBody
    ): Response<BusinessLocationResponse>


    // ==================== BUSINESS - CATALOGUE & PRODUCTS ====================


    @GET("business/catalogue/{userId}")
    suspend fun getUserBusinessCatalogue(@Path("userId") userId: String): Response<GetCatalogueByUserId>

    @Multipart
    @POST("business/catalogue/product")
    suspend fun addProduct(
        @Part("itemName") itemName: RequestBody,
        @Part("description") description: RequestBody,
        @Part("features") features: RequestBody,
        @Part("price") price: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<AddProductResponse>

    @GET("business/catalogue/m/products")
    suspend fun getProducts(): Response<GetProductsResponse>

    @DELETE("business/catalogue/products/{productId}")
    suspend fun deleteProduct(@Path("productId") productId: String): Response<DeleteProductResponse>


    // ==================== BUSINESS - POSTS ====================

    @GET("business/product-posts/post")
    suspend fun getBusinessPost(@Query("page") page: String): Response<BusinessPost>

    @POST("business/product-posts/likes/{businessPostId}")
    suspend fun likeAndUnlikeBusinessPost(@Path("businessPostId") businessPostId: String): Response<LikeResponse>

    @POST("business/products-posts/bookmarks/{businessPostId}")
    suspend fun bookmarkBusinessPost(@Path("businessPostId") businessPostId: String): Response<ShortsFavoriteResponse>

    @POST("business/product-posts/follow/{userToBeFollowed}")
    suspend fun followUnFollowBusinessOwner(@Path("userToBeFollowed") userToBeFollowed: String): Response<FollowUnFollowResponse>


    // ==================== BUSINESS - COMMENTS ====================

    @GET("business/product-posts/comments/{postId}")
    suspend fun getBusinessPostComment(
        @Path("postId") postId: String,
        @Query("page") page: String
    ): Response<CommentsResponse>

    @Multipart
    @POST("business/product-posts/comments/{businessPostId}")
    suspend fun addBusinessComment(
        @Path("businessPostId") businessPostId: String,
        @Part("content") content: RequestBody? = null,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: List<MultipartBody.Part>? = null,
        @Part video: List<MultipartBody.Part>? = null,
        @Part thumbnail: List<MultipartBody.Part>? = null,
        @Part audio: List<MultipartBody.Part>? = null,
        @Part docs: List<MultipartBody.Part>? = null,
        @Part("duration") duration: RequestBody? = null,
        @Part("fileName") fileName: RequestBody? = null,
        @Part("fileType") fileType: RequestBody? = null,
        @Part("fileSize") fileSize: RequestBody? = null,
        @Part("numberOfPages") numberOfPages: RequestBody? = null,
        @Part("gif") gif: RequestBody? = null
    ): Response<BusinessCommentResponse>

    @POST("business/product-posts/likes/comment/{commentId}")
    suspend fun likeAndUnlikeBusinessComment(@Path("commentId") commentId: String): Response<LikeUnLikeCommentResponse>


    // ==================== BUSINESS - COMMENT REPLIES ====================

    @GET("business/product-posts/comments/replies/{commentId}")
    suspend fun getBusinessCommentReplies(
        @Path("commentId") postId: String,
        @Query("page") page: String
    ): Response<AllCommentReplies>

    @Multipart
    @POST("business/product-posts/comments/replies/{commentId}")
    suspend fun addReplyBusinessComment(
        @Path("commentId") commentId: String,
        @Part("content") content: RequestBody? = null,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: List<MultipartBody.Part>? = null,
        @Part video: List<MultipartBody.Part>? = null,
        @Part thumbnail: List<MultipartBody.Part>? = null,
        @Part audio: List<MultipartBody.Part>? = null,
        @Part docs: List<MultipartBody.Part>? = null,
        @Part("duration") duration: RequestBody? = null,
        @Part("fileName") fileName: RequestBody? = null,
        @Part("fileType") fileType: RequestBody? = null,
        @Part("fileSize") fileSize: RequestBody? = null,
        @Part("numberOfPages") numberOfPages: RequestBody? = null,
        @Part("gif") gif: RequestBody? = null
    ): Response<BusinessCommentResponse>

    @POST("business/product-posts/likes/comment/reply/{commentReplyId}")
    suspend fun likeAndUnlikeBusinessCommentReply(@Path("commentReplyId") commentReplyId: String): Response<LikeUnLikeCommentResponse>


    // ==================== LOCATION & ADVERTISEMENTS ====================

    @Multipart
    @POST("userlocation/businesslocationadvertisement")
    suspend fun processUserLocationAndBusinessLocation(
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("accuracy") accuracy: RequestBody
    ): Response<AdvertisementResponse>

    @Multipart
    @POST("userlocation/walkingbillboardadvertisement")
    suspend fun processUserLocationAndWalkingBillboardLocation(
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("accuracy") accuracy: RequestBody
    ): Response<AdvertisementResponse>


    // ==================== NOTIFICATIONS ====================


    @GET("notifications/myComment")
    suspend fun getCommentNotification(): Response<GetCommentNotification>

    @GET("notifications/u")
    suspend fun getMyUnifiedNotifications(@Query("limit") page: String): Response<GetUnifiedNotifications>

    @PUT("notifications/read/{notificationId}")
    suspend fun markNotificationRead(@Path("notificationId") notificationId: String): Response<ReadNotificationResponse>


}