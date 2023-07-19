package com.jhr.xiaobaotvplugin

import com.jhr.xiaobaotvplugin.components.MediaDetailPageDataComponent
import com.jhr.xiaobaotvplugin.components.MediaSearchPageDataComponent
import com.jhr.xiaobaotvplugin.components.MediaUpdateDataComponent
import com.jhr.xiaobaotvplugin.components.Const
import com.jhr.xiaobaotvplugin.components.HomePageDataComponent
import com.jhr.xiaobaotvplugin.components.MediaClassifyPageDataComponent
import com.jhr.xiaobaotvplugin.components.PopularityListPageDataComponent
import com.jhr.xiaobaotvplugin.components.RecentUpdatesPageDataComponent
import com.jhr.xiaobaotvplugin.components.VideoPlayPageDataComponent
import com.jhr.xiaobaotvplugin.danmaku.OyydsDanmaku
import com.su.mediabox.pluginapi.IPluginFactory
import com.su.mediabox.pluginapi.components.IBasePageDataComponent
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.components.IMediaUpdateDataComponent
import com.su.mediabox.pluginapi.components.IVideoPlayPageDataComponent
import com.su.mediabox.pluginapi.util.PluginPreferenceIns

/**
 * 每个插件必须实现本类
 *
 * 注意包和类名都要相同，且必须提供公开的无参数构造方法
 */
class PluginFactory : IPluginFactory() {

    override val host: String = Const.host

    override fun pluginLaunch() {
        PluginPreferenceIns.initKey(OyydsDanmaku.OYYDS_DANMAKU_ENABLE, defaultValue = true)
    }

    override fun <T : IBasePageDataComponent> createComponent(clazz: Class<T>) = when (clazz) {
        IHomePageDataComponent::class.java -> HomePageDataComponent()  // 主页
        IMediaSearchPageDataComponent::class.java -> MediaSearchPageDataComponent()  // 搜索
        IMediaDetailPageDataComponent::class.java -> MediaDetailPageDataComponent()  // 详情
        IMediaClassifyPageDataComponent::class.java -> MediaClassifyPageDataComponent()  // 媒体分类
        IMediaUpdateDataComponent::class.java -> MediaUpdateDataComponent
        IVideoPlayPageDataComponent::class.java -> VideoPlayPageDataComponent() // 视频播放
        //自定义页面，需要使用具体类而不是它的基类（接口）
        RecentUpdatesPageDataComponent::class.java -> RecentUpdatesPageDataComponent() // 最近更新
        PopularityListPageDataComponent::class.java -> PopularityListPageDataComponent() // 人气榜单
        else -> null
    } as? T

}