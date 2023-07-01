package org.brightmindenrichment.street_care.YouTube


class Playlist(val kind: String, val etag: String, val pageInfo: PageInfo, val items: List<Item>)


data class PageInfo(val totalResults: Int, val resultsPerPage: Int)

data class Item(val kind: String, val etag: String, val id: String, val snippet: Snippet, val contentDetails: ContentDetails)

data class Snippet(val title: String, val thumbnails: Thumbnails, val description: String)

data class ContentDetails(val videoId: String)

data class Thumbnails(val standard: Thumbnail)

data class Thumbnail(val url: String, val width: Int, val height: Int)