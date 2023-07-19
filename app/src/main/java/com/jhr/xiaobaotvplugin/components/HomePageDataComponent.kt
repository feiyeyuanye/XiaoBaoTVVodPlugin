package com.jhr.xiaobaotvplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import com.jhr.xiaobaotvplugin.components.Const.host
import com.jhr.xiaobaotvplugin.components.Const.layoutSpanCount
import com.jhr.xiaobaotvplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.CustomPageAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp

class HomePageDataComponent : IHomePageDataComponent {

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = host
        val data = mutableListOf<BaseData>()
        val doc = JsoupUtil.getDocument(url)
        //1.横幅
        doc.select("#home_slide").apply {
            val bannerItems = mutableListOf<BannerData.BannerItemData>()
            val div = select(".carousel-inner").select(".item")
            val li = select("ul").select("li")
            for ((index,em) in li.withIndex()){
                val nameEm = em.select("h4").text()
                val ext = em.select("p").text()
                val videoUrl = div[index].select("a").attr("href")
                val bannerImage = host+div[index].select("img").attr("src")
                if (bannerImage.isNotBlank()) {
//                    Log.e("TAG", "添加横幅项 封面：$bannerImage 链接：$videoUrl")
                    bannerItems.add(
                        BannerData.BannerItemData(bannerImage,nameEm, ext).apply {
                            if (!videoUrl.isNullOrBlank())
                                action = DetailAction.obtain(videoUrl)
                        }
                    )
                }
            }
            if (bannerItems.isNotEmpty())
                data.add(BannerData(bannerItems, 6.dp).apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount
                })
        }

        //2.菜单第一行
        data.add(
            MediaInfo1Data(
                "", Const.Icon.RANK, "", "最近更新",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                layoutConfig = BaseData.LayoutConfig(layoutSpanCount)
                spanSize = layoutSpanCount / 2
                action = CustomPageAction.obtain(RecentUpdatesPageDataComponent::class.java)
            })
        data.add(
            MediaInfo1Data(
                "", Const.Icon.TABLE, "", "人气榜单",
                otherColor = 0xff757575.toInt(),
                coverScaleType = ImageView.ScaleType.FIT_CENTER,
                coverHeight = 24.dp,
                gravity = Gravity.CENTER
            ).apply {
                spanSize = layoutSpanCount / 2
                action = CustomPageAction.obtain(PopularityListPageDataComponent::class.java)
            })

        //3.各类推荐
        val modules = doc.select("div[class='myui-panel myui-panel-bg clearfix']")
        var hasUpdate = false
        val update = mutableListOf<BaseData>()
        for (em in modules){
            val moduleHeading = em.select(".myui-panel_hd").first()
            val type = moduleHeading?.select("h3")
            val typeName = type?.text()
            val typeUrl = moduleHeading?.select(".more")?.attr("href")
            if (!typeName.isNullOrBlank()) {
                typeName.contains("推荐").also {
                    if (!it && hasUpdate) {
                        //示例使用水平列表视图组件
                        data.add(HorizontalListData(update, 120.dp).apply {
                            spanSize = layoutSpanCount
                        })
                    }
                    hasUpdate = it
                }
                data.add(SimpleTextData(typeName).apply {
                    fontSize = 15F
                    fontStyle = Typeface.BOLD
                    fontColor = Color.BLACK
                    spanSize = layoutSpanCount / 2
                })
                if (!hasUpdate) data.add(SimpleTextData("查看更多 >").apply {
                        fontSize = 12F
                        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                        fontColor = Const.INVALID_GREY
                        spanSize = layoutSpanCount / 2
                    }.apply {
                        action = ClassifyAction.obtain(typeUrl, typeName)
                    })
            }
            val li = em.select("ul").select("li")
            for ((index,video) in li.withIndex()){
                video.apply {
                    val name = select("a").attr("title")
                    val videoUrl = select("a").attr("href")
                    val coverUrl = select("a").attr("data-original")
                    val episode = select(".text-right").text()

                    if (!name.isNullOrBlank() && !videoUrl.isNullOrBlank() && !coverUrl.isNullOrBlank()) {
                        (if (hasUpdate) update else data).add(
                            MediaInfo1Data(name, coverUrl, videoUrl, episode ?: "")
                                .apply {
                                    spanSize = layoutSpanCount / 3
                                    action = DetailAction.obtain(videoUrl)
                                    if (hasUpdate) {
                                        paddingRight = 8.dp
                                    }
                                })
//                        Log.e("TAG", "添加视频 ($name) ($videoUrl) ($coverUrl) ($episode)")
                    }
                }
            }
        }
        return data
    }
}