package com.jhr.xiaobaotvplugin.components

import android.util.Log
import com.jhr.xiaobaotvplugin.components.Const.host
import com.jhr.xiaobaotvplugin.util.JsoupUtil
import com.jhr.xiaobaotvplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val searchResultList = mutableListOf<BaseData>()
        // https://xiaobaotv.net/index.php/vod/search/page/2/wd/%E9%BE%99.html
        val url = "${host}/index.php/vod/search/page/${page}/wd/${keyWord}.html"
        Log.e("TAG", url)

        val document = JsoupUtil.getDocument(url)
        searchResultList.addAll(ParseHtmlUtil.parseSearchEm(document, url))
        return searchResultList
    }

}