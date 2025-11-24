package com.hcgn2005.anikai

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class AnikaiProvider : MainAPI() {

    override var mainUrl = "https://anikai.to"
    override var name = "Anikai"
    override val lang = "en"
    override val hasMainPage = true
    override val supportedTypes = setOf(
        TvType.Anime,
        TvType.AnimeMovie
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = "$mainUrl/home"

        val html = app.get(url).document

        val items = html.select("div.ani-item").mapNotNull {
            val title = it.selectFirst("h3")?.text() ?: return@mapNotNull null
            val link = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = it.selectFirst("img")?.attr("src")

            MovieSearchResponse(
                name = title,
                url = fixUrl(link),
                apiName = this.name,
                posterUrl = fixUrl(poster)
            )
        }

        return HomePageResponse(
            listOf(
                HomePageList("Latest Anime", items)
            )
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search?q=$query"

        val html = app.get(url).document

        return html.select(".ani-item").mapNotNull {
            val title = it.selectFirst("h3")?.text() ?: return@mapNotNull null
            val link = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = it.selectFirst("img")?.attr("src")

            MovieSearchResponse(
                name = title,
                url = fixUrl(link),
                apiName = name,
                posterUrl = fixUrl(poster)
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document

        val title = doc.selectFirst("h1")?.text() ?: "Unknown Title"
        val poster = doc.selectFirst(".ani-cover img")?.attr("src")

        val episodes = doc.select(".episode-item a").mapNotNull {
            val epLink = it.attr("href")
            val epTitle = it.text()
            Episode(fixUrl(epLink), epTitle)
        }

        return AnimeLoadResponse(
            name = title,
            url = url,
            apiName = name,
            type = TvType.Anime,
            posterUrl = fixUrl(poster),
            episodes = episodes
        )
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        val doc = app.get(data).document

        val iframe = doc.selectFirst("iframe")?.attr("src")
            ?: return false

        return loadExtractor(iframe, data, subtitleCallback, callback)
    }
}
