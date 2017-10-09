package com.prangbi.android.lottopop.model

import android.content.Context
import com.google.gson.Gson
import com.prangbi.android.lottopop.base.Definition
import com.prangbi.android.lottopop.helper.PrHttpRequest
import com.prangbi.android.lottopop.helper.Util
import com.prangbi.android.lottopop.helper.database.PrDatabase
import java.io.IOException

/**
 * Created by guprohs on 2017. 8. 21..
 */
/**
 * 나눔로또
 */
class NLottoInfo {
    data class WinResult(
            var drwNo: Int = 0,
            var drwNoDate: String? = null,
            var totSellamnt: Long = 0,
            var firstWinamnt: Long = 0,
            var firstPrzwnerCo: Int = 0,
            var drwtNo1: Short = 0,
            var drwtNo2: Short = 0,
            var drwtNo3: Short = 0,
            var drwtNo4: Short = 0,
            var drwtNo5: Short = 0,
            var drwtNo6: Short = 0,
            var bnusNo: Short = 0
    )

    // 02 03 08 18 23 32 + 45
    data class MyLotto(
            var id: Int = 0,
            var drwNo: Int = 0,
            var drwtNo1: Short = 0,
            var drwtNo2: Short = 0,
            var drwtNo3: Short = 0,
            var drwtNo4: Short = 0,
            var drwtNo5: Short = 0,
            var drwtNo6: Short = 0,
            var bnusNo: Short = 0
    )
}

/**
 * 나눔로또
 */
interface INLottoModel {
    fun getWinResult(context: Context, drwNo: Int, handler: (NLottoInfo.WinResult?, error: IOException?) -> Unit)
    fun getLatestWinResult(context: Context, handler: (NLottoInfo.WinResult?, error: IOException?) -> Unit)
    fun insertWinResult(context: Context, drwNo: Int, winResult: NLottoInfo.WinResult): Long
}

class NLottoModel: INLottoModel {
    override fun getWinResult(context: Context, drwNo: Int, handler: (NLottoInfo.WinResult?, error: IOException?) -> Unit) {
        val nLottoDB = PrDatabase.getInstance(context).nLottoDB
        val winResultList = nLottoDB.selectWinResults(drwNo, 1)
        if (0 < winResultList.count()) {
            val winResultJsonString = winResultList[0]["jsonString"].toString()
            val winResult = Gson().fromJson(winResultJsonString, NLottoInfo.WinResult::class.java)
            handler(winResult, null)
        } else {
            PrHttpRequest().getNLottoNumber(drwNo, object: PrHttpRequest.ResponseCallback {
                override fun onResponse(api: PrHttpRequest.API, obj: Any?) {
                    if (obj is NLottoInfo.WinResult) {
                        insertWinResult(context, obj.drwNo, obj)
                        handler(obj, null)
                    } else {
                        handler(null, IOException("나눔로또 정보를 가져오지 못했습니다."))
                    }
                }

                override fun onFailure(api: PrHttpRequest.API, error: IOException) {
                    handler(null, error)
                }
            })
        }
    }

    override fun getLatestWinResult(context: Context, handler: (NLottoInfo.WinResult?, error: IOException?) -> Unit) {
        val latestDrawNumber = Util.latestDrawNumber(Definition.NLOTTO_START_DATE, "yyyy-MM-dd")
        getWinResult(context, latestDrawNumber, handler)
    }

    override fun insertWinResult(context: Context, drwNo: Int, winResult: NLottoInfo.WinResult): Long {
        val winResultJsonString = Gson().toJson(winResult)
        return PrDatabase.getInstance(context).nLottoDB.insertWinResult(drwNo, winResultJsonString)
    }
}
