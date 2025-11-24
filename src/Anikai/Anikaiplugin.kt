package com.hcgn2005.anikai

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class AnikaiProvider : MainAPI() {

    override var mainUrl = "https://anikai.to"
    override var name = "Anikai"
    override val lang = "en"
    override val hasMainPage = true
    override val supportedTypes = setOf(
        TvType.Anime, TvType.AnimeMovie
    )

    // -----------------------------------------
    // ⭐ Home Page (Latest Episodes + Trending)
    // -----------------------------------------
    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {

        val doc = app.get("$mainUrl/home").document

        val latest = doc.select("div.latest-episode .ani-item")
            .mapNotNull { toHomeItem(it) }

        val trending = doc.select("div.trending .ani-item")
            .mapNotNull { toHomeItem(it) }

        return HomePageResponse(
            listOf(
                HomePageList("Latest Episodes", latest),
                HomePageList("Trending Anime", trending)
            )
        )
    }

    private fun toHomeItem(item: Element): SearchResponse? {
        val title = item.selectFirst("h3")?.text() ?: return null
        val link = item.selectFirst("a")?.attr("href") ?: return null
        val poster = item.selectFirst("img")?.attr("src")

        return MovieSearchResponse(
            name = title,
            url = fixUrl(link),
            apiName = name,
            posterUrl = fixUrl(poster)
        )
    }

    // ------------------------------
    // ⭐ Search
    // ------------------------------
    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/search?q=$query").document
        return doc.select(".ani-item").mapNotNull { toHomeItem(it) }
    }

    // ------------------------------
    // ⭐ Load Anime Details
    // ------------------------------
    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        val title = doc.selectFirst("h1")?.text() ?: "Unknown Anime"
        val poster = doc.selectFirst(".ani-cover img")?.attr("src")
        val description = doc.selectFirst(".ani-desc")?.text()

        val episodes = doc.select(".episode-item a").mapNotNull {
            val link = it.attr("href")
            val name = it.text()
            Episode(
                data = fixUrl(link),
                name = name
            )
        }

        return AnimeLoadResponse(
            name = title,
            url = url,
            apiName = name,
            type = TvType.Anime,
            posterUrl = fixUrl(poster),
            plot = description,
            episodes = episodes,
            showStatus = ShowStatus.Ongoing
        )
    }

    // ------------------------------
    // ⭐ Extract Video Servers
    // ------------------------------
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        val doc = app.get(data).document

        // Site uses iframe sources for players
        val serverUrls = doc.select(".player iframe").mapNotNull {
            it.attr("src")
        }

        if (serverUrls.isEmpty()) return false

        serverUrls.forEach { server ->
            loadExtractor(
                url = fixUrl(server),
                referer = data,
                subtitleCallback,
                callback
            )
        }

        return true
    }
}
