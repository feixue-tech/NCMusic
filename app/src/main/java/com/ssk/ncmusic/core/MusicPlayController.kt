package com.ssk.ncmusic.core

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.accompanist.pager.ExperimentalPagerApi
import com.ssk.ncmusic.core.player.IPlayerListener
import com.ssk.ncmusic.core.player.NCPlayer
import com.ssk.ncmusic.core.player.PlayerStatus
import com.ssk.ncmusic.model.SongBean
import com.ssk.ncmusic.utils.StringUtil
import com.ssk.ncmusic.utils.showToast

/**
 * Created by ssk on 2022/4/23.
 * 音乐播放控制器，控制CpnBottomMusicPlay、PlayMusicPage的UI状态以及歌曲播放
 */
@OptIn(ExperimentalPagerApi::class)
object MusicPlayController : IPlayerListener {

    var songList = mutableStateListOf<SongBean>()
    var curIndex by mutableStateOf(-1)
        private set
    var progress by mutableStateOf(0)
    var curPositionStr by mutableStateOf("00:00")
    var totalDuringStr by mutableStateOf("00:00")
    private var playing by mutableStateOf(false)

    private var totalDuring = 0
    private var seeking = false

    init {
        NCPlayer.addListener(this)
    }

    fun setDataSource(songBeans: List<SongBean>, index: Int) {
        songList.clear()
        songList.addAll(songBeans)
        curIndex = index
        Log.e("ssk", "MusicPlayController setDataSource curIndex=${curIndex}")

        NCPlayer.setDataSource(songList[curIndex])
        NCPlayer.start()
    }

    fun play(index: Int) {
        if (songList.size > curIndex || songList.size - 1 == curIndex) {
            curIndex = index
            Log.e("ssk", "MusicPlayController play curIndex=${curIndex}")
            NCPlayer.setDataSource(songList[curIndex])
            NCPlayer.start()
        }
    }

    fun pause() {
        NCPlayer.pause()
    }

    fun resume() {
        NCPlayer.resume()
    }

    fun isPlaying(): Boolean {
        return playing
    }

    fun seeking(progress: Int) {
        seeking = true
        this.progress = progress
        if (totalDuring != 0) {
            this.curPositionStr = StringUtil.formatMilliseconds(progress * totalDuring / 100)
        }
    }

    fun seekTo(progress: Int) {
        this.progress = progress
        if (totalDuring != 0) {
            NCPlayer.seekTo(progress * totalDuring / 100)
        }
        seeking = false
    }

    fun isPlaying(songBean: SongBean) = songList.getOrNull(curIndex)?.id == songBean.id

    override fun onStatusChanged(status: PlayerStatus) {
        playing = status == PlayerStatus.STARTED
        if (status == PlayerStatus.COMPLETED) {
            autoPlayNext()
        } else if (status == PlayerStatus.ERROR) {
            showToast("播放失败")
            autoPlayNext()
        } else if (status == PlayerStatus.STOPPED) {
            totalDuringStr = "00:00"
            curPositionStr = "00:00"
            this.progress = 0
        }
    }

    private fun autoPlayNext() {
        val newIndex = (songList.size - 1).coerceAtMost(curIndex + 1)
        if (newIndex != curIndex) {
            play(newIndex)
        }
    }

    override fun onProgress(totalDuring: Int, currentPosition: Int, percentage: Int) {
        if (!seeking) {
            this.totalDuring = totalDuring
            totalDuringStr = StringUtil.formatMilliseconds(totalDuring)
            curPositionStr = StringUtil.formatMilliseconds(currentPosition)
            progress = percentage
        }
    }

}