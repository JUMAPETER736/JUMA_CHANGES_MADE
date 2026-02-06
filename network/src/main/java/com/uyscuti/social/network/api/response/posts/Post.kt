package com.uyscuti.social.network.api.response.posts
import java.io.Serializable

data class Post(
    val __v: Int,
    var _id: String,
    val author: Author,
    var bookmarkCount: Int,
    var comments: Int,
    var content: String,
    var contentType: String,
    val createdAt: String,
    val duration: List<Duration>,
    val feedShortsBusinessId: String,
    val fileIds: List<String>,
    val fileNames: List<FileName>,
    val fileSizes: List<FileSize>,
    val fileTypes: List<FileType>,
    val files: ArrayList<File>,
    var isBookmarked: Boolean,
    var isExpanded: Boolean,
    var isFollowing: Boolean,
    var isLiked: Boolean,
    var isLocal: Boolean,
    var isReposted: Boolean,
    var likes: Int,
    val numberOfPages: List<NumberOfPageX>,
    val originalPost: List<OriginalPost>,
    val repostedByUserId: String?,
    val repostedUser: RepostedUser?,
    val repostedUsers: List<String>,
    val tags: List<Any?>,
    val thumbnail: List<ThumbnailX>,
    val updatedAt: String,

    // Share related fields
    var shareCount: Int = 0,
    var isShared: Boolean = false,
    val sharedByUserIds: List<String> = emptyList(),
    val sharedBy: String? = null,
    val sharedAt: String? = null,
    val shareId: String? = null,

    // Repost related fields
    var repostCount: Int = 0,
    val repostedByUserIds: List<String> = emptyList(),
    val repostedBy: String? = null,
    val repostedAt: String? = null,
    val repostId: String? = null,

    // Business/Shop related fields
    val isBusinessPost: Boolean? = null,
    val category: String? = null,
    val businessDetails: BusinessPost? = null,

    // Bookmark/Favorites related fields
    var isFavorited: Boolean? = null,
    val favorites: List<String>? = null,
    val bookmarkId: String? = null,
    val bookmarkedBy: String? = null,
    val bookmarkedAt: String? = null,
    val bookmarkedByUserIds: List<String> = emptyList(),

    // Likes related fields
    val likedByUserIds: List<String> = emptyList(),

    // Privacy / relationship flags
    var isInCloseFriends: Boolean? = null,
    var isPostsMuted: Boolean? = null,
    var isStoriesMuted: Boolean? = null,
    var isRestricted: Boolean? = null,
    var isFavorite: Boolean? = null,
): Serializable


data class BusinessPost(
    val _id: String,
    val owner: String,
    val catalogue: String,
    val itemName: String,
    val description: String,
    val features: List<String>,
    val images: List<String>,
    val price: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val author: AuthorB,
    val businessProfile: BusinessProfile
): Serializable

data class AuthorB(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val account: AccountB
): Serializable

data class AccountB(
    val _id: String,
    val avatar: AvatarB,
    val username: String
): Serializable

data class AvatarB(
    val url: String,
    val localPath: String,
    val _id: String
): Serializable

data class BusinessProfile(
    val _id: String,
    val businessName: String,
    val businessType: String,
    val businessDescription: String,
    val backgroundPhoto: BackgroundPhoto
): Serializable

data class BackgroundPhoto(
    val url: String
): Serializable