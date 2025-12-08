package com.uyscuti.social.network.api.retrofit.interfaces


import com.uyscuti.social.network.api.response.business.response.background.BackgroundVideoResponse
import com.uyscuti.social.network.api.response.business.response.catalogue.GetMyCatalogueResponse
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
import com.uyscuti.social.network.api.request.search.SearchUsersRequest
import com.uyscuti.social.network.api.response.MainResponse
import com.uyscuti.social.network.api.response.allFeedRepostsPost.AllFeedRepostsPost
import com.uyscuti.social.network.api.response.allFeedRepostsPost.RepostRequest
import com.uyscuti.social.network.api.response.business.response.background.BackgroundImageResponse
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
import com.uyscuti.social.network.api.response.favoritefeed.GetFavoriteFeedResponse
import com.uyscuti.social.network.api.response.favoriteshort.ShortsFavoriteResponse
import com.uyscuti.social.network.api.response.feed.FeedUploadResponse
import com.uyscuti.social.network.api.response.feed.deletefeed.DeleteFeedResponse
import com.uyscuti.social.network.api.response.follow_unfollow.OtherUsersFollowersAndFollowingResponse
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
import com.uyscuti.social.network.api.response.notification.GetMyNotifications
import com.uyscuti.social.network.api.response.notification.ReadNotificationResponse
import com.uyscuti.social.network.api.response.otherusersprofile.OtherUsersProfileResponse
import com.uyscuti.social.network.api.response.otherusersprofileshorts.OtherUsersProfileShortsResponse
import com.uyscuti.social.network.api.response.post.GetPostById
import com.uyscuti.social.network.api.response.shorts.ShortsUploadResponse
import com.uyscuti.social.network.api.response.updateavatar.UpdateAvatarResponse
import com.uyscuti.social.network.api.response.userstatus.UserStatusResponse
import com.uyscuti.social.network.api.response.getallshorts.ApiResponse
import com.uyscuti.social.network.api.response.posts.FeedResponse
import com.uyscuti.social.network.api.response.profile.followersList.UserOtherFollowersResponse
import com.uyscuti.social.network.api.response.profile.followingList.OtherUserFollowingResponse
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.PUT

interface IFlashapi {


    @POST("auth/facebook-login")
    suspend fun facebookLogin(@Body request: FacebookLoginRequest): Response<LoginResponse>

    @POST("users/register")
    suspend fun registerUsers(@Body requestPost: RegisterRequest): Response<RegisterResponse>

    @POST("users/login")
    suspend fun  loginUsers(@Body requestPost: LoginRequest): Response<LoginResponse>

    @POST("users/google-login")
    suspend fun  googleLogin(@Body requestPost: GoogleLoginRequest): Response<GoogleLoginResponse>
    @GET("chat-app/chats")
    suspend fun getChats(): Response<ChatsResponse>


    @GET("social-media/profile/{username}/followers")
    suspend fun getOtherUserFollowers(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<UserOtherFollowersResponse>

    @GET("social-media/profile/{username}/following")
    suspend fun getOtherUserFollowing(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<OtherUserFollowingResponse>



    @DELETE("social-media/followers/{userId}")
    suspend fun removeFollower(@Path("userId") userId: String): Response<FollowUnFollowResponse>

    // Unfollow a user by user ID
    @DELETE("social-media/following/{userId}")
    suspend fun unfollowUser(@Path("userId") userId: String): Response<FollowUnFollowResponse>

    @GET("social-media/following/status/{userId}")
    suspend fun getOtherUsersFollowersAndFollowingStatus(
        @Path("userId") userId: String): Response<OtherUsersFollowersAndFollowingResponse>

    // Block a user
    @POST("social-media/block/{userId}")
    suspend fun blockUser(@Path("userId") userId: String): Response<FollowUnFollowResponse>

    // Unblock a user
    @DELETE("social-media/block/{userId}")
    suspend fun unBlockUser(@Path("userId") userId: String): Response<FollowUnFollowResponse>

    @GET("social-media/followers/my")
    suspend fun getMyFollowers(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): Response<UsersResponse>

    // Get who I'm following
    @GET("social-media/following/my")
    suspend fun getMyFollowing(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): Response<UsersResponse>


    // Get blocked users
    @GET("social-media/blocked")
    suspend fun getBlockedUsers(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20
    ): Response<UsersResponse>


    @GET("chat-app/chats/fetchChat/{chatId}")
    suspend fun fetchChat(@Path("chatId") chatId: String): Response<FetchChatResponse>

    @GET("chat-app/chats")
    suspend fun getChats(
        @Query("limit") limit: Int, // Specify the number of items to fetch in a batch
        @Query("offset") offset: Int // Specify the starting point for the next batch
    ): Response<ChatsResponse>

    @POST("users/resend-email-verification")
    suspend fun resendEmailVerification(): Response<MainResponse>

    @GET("chat-app/chats")
    suspend fun getChats(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("sort") sort: String = "-createdAt" // Assuming createdAt is the timestamp field, - indicates descending order
    ): Response<ChatsResponse>

    @GET("chat-app/chats")
    suspend fun fetchChatItems(): String // Adjust the return type as needed

    @GET("chat-app/chats/users")
    suspend fun getUsers(): Response<UsersResponse>
    @POST("chat-app/chats/users/search")
    suspend fun searchUsers(@Body request: SearchUsersRequest): Response<UsersResponse>

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


//    @POST("chat-app/chats/c/{receiverId}")
//    suspend fun createUserChat(@Path("receiverId") receiverId: String): Response<CreateChatResponse>
//
//    @POST("chat-app/chats/c/{receiverId}")
//    suspend fun createGroupChat(@Path("receiverId") receiverId: String): Response<CreateChatResponse>
//
//    @POST("chat-app/chats/group")
//    suspend fun createGroupChat(@Body groupChatRequest: GroupChatRequest): Response<GroupChatResponse>
//
//    @POST("chat-app/chats/c/")
//    suspend fun createChat(@Query("receiverId") receiverId: String): Response<CreateChatResponse>

    @GET("chat-app/messages/{chatId}")
    suspend fun getMessages(@Path("chatId") chatId: String): Response<GetMessagesResponse>


    @GET("chat-app/messages/{chatId}")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<GetMessagesResponse>



    @POST("chat-app/messages/{chatId}")
    suspend fun sendMessage(@Path("chatId") chatId: String, @Body requestPost: SendMessageRequest): Response<SendMessageResponse>



    @Multipart
    @POST("chat-app/messages/{chatId}") // Replace with your actual API endpoint
    suspend fun sendAttachment(
        @Path("chatId") chatId: String,
        @Part filePath: MultipartBody.Part
    ): Response<SendMessageResponse>

    @Multipart
    @POST("chat-app/messages/{chatId}") // Replace with your actual API endpoint
    suspend fun uploadImage(
        @Path("chatId") chatId: String,
        @Part image: MultipartBody.Part
    ): Response<SendFileResponse>




//    @GET("social-media/posts")
//    suspend fun getAllPosts(): Response<GetAllShortsResponse>
//
//    @GET("social-media/posts")
//    suspend fun getAllPosts2(): Response<ShortsGetResponse>

    @GET("social-media/posts")
    suspend fun getAllPosts3(): Response<ApiResponse<ResponseData>>

    @GET("social-media/posts")
    suspend fun getShorts(
        @Query("page") page: String,
//        @Query("limit") limit: String
    ): Response<ApiResponse<ResponseData>>

    @GET("social-media/posts/getAllShortsByFeedShortBusinessId/{feedShortsBusinessId}")
    suspend fun getAllShortsByFeedShortBusinessId(
        @Path("feedShortsBusinessId")feedShortsBusinessId: String,
    ): Response<ApiResponse<ResponseData>>
//    @GET("social-media/posts")
//    suspend fun getShorts(
//        @Query("page") page: String,
////        @Query("limit") limit: String
//    ): Response<GetAllShortsResponse>

    @GET("social-media/posts/get/my")
    suspend fun myShorts(
        @Query("page") page: String,
//        @Query("limit") limit: String
    ): Response<GetAllShortsResponse>

    //    @Multipart
//    @POST("chat-app/messages/{chatId}") // Replace with your actual API endpoint
//    suspend fun uploadShort(
//        @Path("chatId") chatId: String,
//        @Part image: MultipartBody.Part,
//        @Part("content") content: RequestBody
//    ): Response<SendFileResponse>
    @Multipart
    @PATCH("users/avatar")
    suspend fun updateUserAvatar(@Part avatar: MultipartBody.Part): Response<UpdateAvatarResponse>

    @GET("social-media/profile")
    suspend fun getMyProfile(): Response<GetMyProfile>

    @GET("social-media/profile/u/{username}")
    suspend fun getOtherUsersProfileByUsername(@Path("username") username: String): Response<OtherUsersProfileResponse>

    @GET("social-media/posts/get/u/{username}")
    suspend fun getShortsByUsername(@Path("username") username: String): Response<OtherUsersProfileShortsResponse>

    @GET("social-media/posts/get/u/{username}")
    suspend fun getShortsByUsernameWithPage(
        @Path("username") username: String,
        @Query("page") page: String,
    ): Response<OtherUsersProfileShortsResponse>

    @GET("social-media/bookmarks")
    suspend fun getFavoriteShorts(
        @Query("page") page: String,
    ): Response<GetFavoriteShortsResponse>

    @PATCH("social-media/profile")
    suspend fun updateMyProfile(@Body requestPost: UpdateSocialProfileRequest): Response<GetMyProfile>


//    @POST("chat-app/chats/group/{chatId}/{participantId}")
//    suspend fun addParticipantToGroupChat(@Path("chatId") chatId: String, @Path("participantId") participantId: String): Response<AddParticipantResponse>

//    @DELETE("chat-app/chats/group/{chatId}")
//    suspend fun deleteGroupChat(@Path("chatId") chatId: String): Response<DeleteGroupChatResponse>
//

    //    @DELETE("chat-app/chats/leave/group/{chatId}")
//    suspend fun leaveGroupChat(@Path("chatId") chatId: String): Response<LeaveGroup>
//
//    @PATCH("chat-app/chats/group/{chatId}")
//    suspend fun changeGroupName(@Body requestPost: ChangeGroupNameRequest): Response<ChangeGroupName>
//
//    @DELETE("chat-app/chats/remove/{chatId}")
//    suspend fun deleteChat(@Path("chatId") chatId: String): Response<DeleteOneOnOneChat>
//
//    @DELETE("chat-app/chats/group/{chatId}/{participantId}")
//    suspend fun removeParticipant(@Path("chatId") chatId: String, @Path("participantId") participantId: String): Response<RemoveParticipantResponse>
//
////    Social Media
//
    @POST("social-media/follow/{toBeFollowedUserId}")
    suspend fun followUnFollow(@Path("toBeFollowedUserId") toBeFollowedUserId: String): Response<FollowUnFollowResponse>

    @POST("social-media/like/post/{postId}")
    suspend fun likeUnLikeShort(@Path("postId") postId: String): Response<LikeUnLikeShortResponse>

    @POST("feed/like/{postId}")
    suspend fun likeUnLikeFeed(@Path("postId") postId: String): Response<LikeUnLikeShortResponse>

    @POST("feed/like/comment/{commentId}")
    suspend fun likeUnLikeFeedComment(@Path("commentId") commentId: String): Response<LikeUnLikeCommentResponse>

    @POST("social-media/like/comment/{commentId}")
    suspend fun likeUnLikeComment(@Path("commentId") commentId: String): Response<LikeUnLikeCommentResponse>

    @POST("social-media/like/comment/reply/{commentReplyId}")
    suspend fun likeUnLikeCommentReply(@Path("commentReplyId") commentReplyId: String): Response<LikeUnLikeCommentResponse>

    @POST("feed/like/comment/reply/{commentReplyId}")
    suspend fun likeUnLikeFeedCommentReply(@Path("commentReplyId") commentReplyId: String): Response<LikeUnLikeCommentResponse>

    @POST("social-media/bookmarks/{postId}")
    suspend fun favoriteShort(@Path("postId") postId: String): Response<ShortsFavoriteResponse>


    @POST("feed/bookmarks/{postId}")
    suspend fun favoriteFeed(@Path("postId") postId: String): Response<ShortsFavoriteResponse>
    //    @Multipart
    @POST("social-media/comments/post/{postId}")
    suspend fun addComment(
        @Path("postId") postId: String, @Body requestBody: CommentRequestBody,
    ) : Response<ShortCommentResponse>

    @POST("feed/comments/{postId}")
    suspend fun addFeedComment(
        @Path("postId") postId: String, @Body requestBody: CommentRequestBody,
    ) : Response<ShortCommentResponse>

    @POST("social-media/comments/post/{postId}")
    suspend fun addGifComment(
        @Path("postId") postId: String,
        @Body requestBody: GifCommentRequestBody,
    ) : Response<ShortCommentResponse>

    @POST("feed/comments/{postId}")
    suspend fun addGifFeedComment(
        @Path("postId") postId: String,
        @Body requestBody: GifCommentRequestBody,
    ) : Response<ShortCommentResponse>

    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addGifCommentReply(
        @Path("commentId") commentId:String,
        @Body requestBody: GifCommentRequestBody,
    ) : Response<CommentReplyResponse>

    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedGifCommentReply(
        @Path("commentId") commentId:String,
        @Body requestBody: GifCommentRequestBody,
    ) : Response<CommentReplyResponse>

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

    @Multipart
    @POST("social-media/posts")
    suspend fun uploadShortFromFeed(
        @Part("content") content: RequestBody,
        @Part("fileId") fileId: RequestBody,
        @Part images: MultipartBody.Part,
        @Part("tags") tags: Array<RequestBody>? = null,
        @Part thumbnail: MultipartBody.Part
    ): Response<ShortsUploadResponse>

    @POST("feed/post")
    suspend fun uploadTextFeed(
        @Body requestBody: FeedTextUploadRequestBody,
    ): Response<FeedUploadResponse>


    @Multipart
    @POST("feed/post")
    suspend fun uploadFileFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part files: MultipartBody.Part,
        @Part("tags") tags: Array<RequestBody>? = null,
        @Part thumbnail: MultipartBody.Part?

    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadFilesFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part files: MultipartBody.Part,
        @Part("tags") tags: Array<RequestBody>? = null,
    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadMultipleFilesFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part("tags") tags: Array<RequestBody>? = null,
    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadMixedFilesFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("feedShortsBusinessId") feedShortsBusinessId: RequestBody,
//        @Part("duration") duration: List<RequestBody>,
//        @Part parts: List<MultipartBody.Part>,
        @Part("duration") duration: Array<RequestBody>,
        @Part("fileTypes") fileTypes: Array<RequestBody>,
        @Part("numberOfPages") numberOfPages: Array<RequestBody>,
        @Part("fileNames") fileNames: Array<RequestBody>,
        @Part("fileSizes") fileSizes: Array<RequestBody>,
        @Part("fileIds") fileIds: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part thumbnail: List<MultipartBody.Part>,
        @Part("tags") tags: Array<RequestBody>? = null,
    ): Response<FeedUploadResponse>

    @Multipart
    @POST("feed/post")
    suspend fun uploadMultipleVideoFilesFeed(
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part files: List<MultipartBody.Part>,
        @Part thumbnail: List<MultipartBody.Part>,
        @Part("tags") tags: Array<RequestBody>? = null,
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
        @Part("tags") tags: Array<RequestBody>? = null,
    ): Response<FeedUploadResponse>

//    @GET("feed/post")
//    suspend fun getAllFeed(
//        @Query("page") page: String,
//    ): Response<GetRepostsPostsOriginal>


    @GET("feed/post/test")
    suspend fun getAllFeed(
        @Query("page") page: String,
    ): Response<FeedResponse>


    @GET("feed/post/search")
    suspend fun getSearchAllFeed(
        @Query("page") page: String,
        @Query("query") query: String
    ): Response<FeedResponse>


    @GET("social-media/posts/search")
    suspend fun searchAllShorts(
        @Query("query") query: String,
        @Query("page") page: String = "1"
    ): Response<ApiResponse<ResponseData>>

    @Multipart
    @POST("social-media/comments/post/{postId}")
    suspend fun addVideoComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part,
    ) : Response<ShortCommentResponse>

    @Multipart
    @POST("feed/comments/{postId}")
    suspend fun addVideoFeedComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part,
    ) : Response<ShortCommentResponse>

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
        @Part docs: MultipartBody.Part,
    ) : Response<ShortCommentResponse>

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
        @Part docs: MultipartBody.Part,
    ) : Response<ShortCommentResponse>
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
        @Part audio: MultipartBody.Part,
    ) : Response<ShortCommentResponse>

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
        @Part audio: MultipartBody.Part,
    ) : Response<ShortCommentResponse>

    @Multipart
    @POST("social-media/comments/post/{postId}")
    suspend fun addImageComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: MultipartBody.Part,
    ) : Response<ShortCommentResponse>
    @Multipart
    @POST("feed/comments/{postId}")
    suspend fun addImageFeedComment(
        @Path("postId") postId: String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: MultipartBody.Part,
    ) : Response<ShortCommentResponse>

//    @Multipart



    @GET("social-media/comments/post/{postId}")
    suspend fun getShortComments(
        @Path("postId") postId: String,
        @Query("page") page:String
    ): Response<AllShortComments>

    @GET("feed/comments/{postId}")
    suspend fun getFeedComments(
        @Path("postId") postId: String,
        @Query("page") page:String
    ): Response<AllShortComments>


    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addCommentReply(
        @Path("commentId") commentId: String,
        @Body requestBody: CommentRequestBody) : Response<CommentReplyResponse>


    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedCommentReply(
        @Path("commentId") commentId: String,
        @Body requestBody: CommentRequestBody) : Response<CommentReplyResponse>

    @Multipart
    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addCommentReply(
        @Path("commentId") commentId:String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part audio: MultipartBody.Part,
    ) : Response<CommentReplyResponse>

    @Multipart
    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedCommentReply(
        @Path("commentId") commentId:String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part audio: MultipartBody.Part,
    ) : Response<CommentReplyResponse>

    @Multipart
    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addImageCommentReply(
        @Path("commentId") commentId:String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: MultipartBody.Part,
    ) : Response<CommentReplyResponse>

    @Multipart
    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedImageCommentReply(
        @Path("commentId") commentId:String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part image: MultipartBody.Part,
    ) : Response<CommentReplyResponse>

    @Multipart
    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addVideoCommentReply(
        @Path("commentId") commentId:String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part,
    ) : Response<CommentReplyResponse>

    @Multipart
    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedVideoCommentReply(
        @Path("commentId") commentId:String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part,
    ) : Response<CommentReplyResponse>

    @Multipart
    @POST("social-media/comment/reply/comment/{commentId}")
    suspend fun addDocumentCommentReply(
        @Path("commentId") commentId:String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileSize") fileSize: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("numberOfPages") numberOfPages: RequestBody,
        @Part docs: MultipartBody.Part,
    ) : Response<CommentReplyResponse>

    @Multipart
    @POST("feed/comment/reply/comment/{commentId}")
    suspend fun addFeedDocumentCommentReply(
        @Path("commentId") commentId:String,
        @Part("content") content: RequestBody,
        @Part("contentType") contentType: RequestBody,
        @Part("localUpdateId") localUpdateId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileSize") fileSize: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("numberOfPages") numberOfPages: RequestBody,
        @Part docs: MultipartBody.Part,
    ) : Response<CommentReplyResponse>


    @GET("social-media/comment/reply/comment/{commentId}")
    suspend fun getCommentReplies(
        @Path("commentId") commentId: String,
        @Query("page") page:String
    ): Response<AllCommentReplies>

    @GET("feed/comment/reply/comment/{commentId}")
    suspend fun getFeedCommentReplies(
        @Path("commentId") commentId: String,
        @Query("page") page:String
    ): Response<AllCommentReplies>

    @Multipart
    @POST("gif")
    suspend fun addGif(
        @Part("fileType") fileType: RequestBody,
        @Part gif: MultipartBody.Part,
    ) : Response<GifResponse>
    @GET("gif")
    suspend fun getGif(
        @Query("page") page:String
    ): Response<AllGifsResponse>

    @GET("chat-app/chats/users")
    suspend fun getOtherUsers(): Response<GetAvailableUsers>


    @GET("business/profile")
    suspend fun getBusinessProfile(): Response<ProfileResponse>

    @POST("business/profile")
    suspend fun createBusinessProfile(
        @Body profile: CreateBusinessProfile
    ): Response<CreateProfileResponse>

    @GET("business/catalogue")
    suspend fun getCatalogue(): Response<GetMyCatalogueResponse>

    @Multipart
    @PATCH("business/profile/background")
    suspend fun updateBackground(
        @Part avatar: MultipartBody.Part): Response<BackgroundImageResponse>

    @Multipart
    @PATCH("business/profile/livelocation")
    suspend fun updateLiveLocation(
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("accuracy") accuracy: RequestBody,
        @Part("range") range: RequestBody,
    ): Response<LiveLocationResponse>

    @Multipart
    @PATCH("business/profile/v")
    suspend fun updateBackgroundVideo(
        @Part video: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part): Response<BackgroundVideoResponse>


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

    @GET("business/profile/{userId}")
    suspend fun getUserBusinessProfile(@Path("userId") userId: String): Response<GetBusinessProfileById>

    @GET ("business/catalogue/{userId}")
    suspend fun getUserBusinessCatalogue(@Path("userId") userId: String): Response<GetCatalogueByUserId>


    @GET("feed/bookmarks")
    suspend fun getFavoriteFeed(
        @Query("page") page: String,
    ): Response<GetFavoriteFeedResponse>



    @GET("feed/post/get/my")
    suspend fun getMyFeed(
        @Query("page") page: String,
    ): Response<AllFeedRepostsPost>



    suspend fun getOtherUserFeed(
        @Path("username") username: String,
        @Query("page") page: String
    ): Response<AllFeedRepostsPost>



    @DELETE("feed/post/{postId}")
    suspend fun deleteFeed(
        @Path("postId") postId: String): Response<DeleteFeedResponse>



    @POST("feed/post/repost/{postId}")
    suspend fun repostsFeed(
        @Path("postId") postId: String,
        @Body request: RepostRequest): Response<CreateRepostFeedPost>



    @GET("notifications/my")
    suspend fun getMyNotifications(): Response<GetMyNotifications>


    @GET("notifications/myComment")
    suspend fun getCommentNotification():Response<GetCommentNotification>
    @GET("notifications/u")
    suspend fun getMyUnifiedNotifications(): Response<GetUnifiedNotifications>

    @PUT("notifications/read/{notificationId}")
    suspend fun markNotificationRead(
        @Path("notificationId")notificationId:String): Response<ReadNotificationResponse>

    @GET("social-media/comments/{commentId}")
    suspend fun getPageComment(
        @Path("commentId") commentId: String): Response<GetPageCommentId>

    ///new code implemented here
    @GET("social-media/posts/{postId}")
    suspend fun getPostById(
        @Path("postId")postId: String ):Response<GetPostById>


}