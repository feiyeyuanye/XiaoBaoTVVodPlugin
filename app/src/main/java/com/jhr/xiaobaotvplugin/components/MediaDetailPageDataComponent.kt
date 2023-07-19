package com.jhr.xiaobaotvplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import com.jhr.xiaobaotvplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.TextUtil.urlEncode
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class MediaDetailPageDataComponent : IMediaDetailPageDataComponent {

    override suspend fun getMediaDetailData(partUrl: String): Triple<String, String, List<BaseData>> {
        var cover = ""
        var title = ""
        var desc = ""
        val score = -1F
        // 导演
        var director = ""
        // 主演
        var protagonist = ""
        // 更新时间
        var upState = ""
        val url = Const.host + partUrl
        val tags = mutableListOf<TagData>()
        val details = mutableListOf<BaseData>()

        val document = JsoupUtil.getDocument(url)

        // ------------- 番剧头部信息
        cover = document.select(".myui-content__thumb").select("img").attr("data-original")
        title = document.select(".myui-content__detail").select(".title").text()
        // 更新状况
        val upStateItems = document.select(".myui-content__detail").select(".data")
        for (upStateEm in upStateItems){
            val t = upStateEm.text()
            when{
                t.contains("导演：") -> director = t
                t.contains("主演：") -> protagonist = t
                t.contains("更新：") -> upState = t
                t.contains("分类：") -> {
                    upStateEm.select("a").forEach {
                        tags.add(TagData(it.text()).apply {
                            action = ClassifyAction.obtain(it.attr("href"), "", it.text())
                        })
                    }
                }
            }
        }
        desc = document.select("div[class='myui-panel myui-panel-bg clearfix']")[1].select("span[class='data']").text()
        // ---------------------------------- 播放列表+header
        val module = document.select("div[class='myui-panel myui-panel-bg clearfix']")[2]
        val playNameList = module.select(".myui-panel_hd").select("ul")
        val playEpisodeList = module.select("#playlist1").select("ul")
        for (index in 0..playNameList.size) {
            val playName = playNameList.getOrNull(index)
            val playEpisode = playEpisodeList.getOrNull(index)
            if (playName != null && playEpisode != null) {
                val episodes = parseEpisodes(playEpisode)
                if (episodes.isNullOrEmpty())
                    continue
                details.add(
                    SimpleTextData(
                        playName.text() + "(${episodes.size}集)"
                    ).apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )
                details.add(EpisodeListData(episodes))
            }
        }
        // ----------------------------------  系列动漫推荐
        val seriess = document.select("div[class='myui-panel myui-panel-bg clearfix']")[3].select("div[class='tab-content myui-panel_bd']")
            val series = parseSeries(seriess)
            if (series.isNotEmpty()) {
                details.add(
                    SimpleTextData("其他系列作品").apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )
                details.addAll(series)
            }
        return Triple(cover, title, mutableListOf<BaseData>().apply {
            add(Cover1Data(cover, score = score).apply {
                layoutConfig =
                    BaseData.LayoutConfig(
                        itemSpacing = 12.dp,
                        listLeftEdge = 12.dp,
                        listRightEdge = 12.dp
                    )
            })
            add(
                SimpleTextData(title).apply {
                    fontColor = Color.WHITE
                    fontSize = 20F
                    gravity = Gravity.CENTER
                    fontStyle = 1
                }
            )
            add(TagFlowData(tags))
            add(
                LongTextData(desc).apply {
                    fontColor = Color.WHITE
                }
            )
            add(SimpleTextData("·$director").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$protagonist").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(SimpleTextData("·$upState").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            add(LongTextData(douBanSearch(title)).apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            addAll(details)
        })
    }

    private fun parseEpisodes(element: Element): List<EpisodeData> {
        val episodeList = mutableListOf<EpisodeData>()
        val elements: Elements = element.select("li").select("a")
        for (k in elements.indices) {
            val episodeUrl = elements[k].attr("href")
            episodeList.add(
                EpisodeData(elements[k].text(), episodeUrl).apply {
                    action = PlayAction.obtain(episodeUrl)
                }
            )
        }
        return episodeList
    }

    private fun parseSeries(element: Elements): List<MediaInfo1Data> {
        val videoInfoItemDataList = mutableListOf<MediaInfo1Data>()
        val uls = element.select("ul")[0]
            val results = uls.select("li")
            for (i in results.indices) {
                val a = results[i].select("a")[0]
                val cover = a.attr("data-original")
                val title = a.attr("title")
                val url = a.attr("href")
                val item = MediaInfo1Data(
                    title, cover, Const.host + url,
                    nameColor = Color.WHITE, coverHeight = 120.dp
                ).apply {
                    action = DetailAction.obtain(url)
                }
                videoInfoItemDataList.add(item)
            }
        return videoInfoItemDataList
    }

    private fun douBanSearch(name: String) =
        "·豆瓣评分：https://m.douban.com/search/?query=${name.urlEncode()}"
}