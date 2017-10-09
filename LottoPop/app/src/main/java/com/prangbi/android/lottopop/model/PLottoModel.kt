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
 * 연금복권520
 */
class PLottoInfo {
    data class WinResult(
            var pensionDrawDate: String? = null,
            var rankClass: String? = null,
            var rank: String? = null,
            var rankNo: String? = null,
            var rankAmt: String? = null,
            var round: String? = null,
            var drawDate: String? = null
    )

    data class MyLotto(
            var rankClass: String? = null,
            var rankNo: String? = null,
            var round: String? = null
    )
}

/**
 * 연금복권520
 */
interface IPLottoModel {
    fun getWinResult(context: Context, round: Int, handler: (Array<PLottoInfo.WinResult>?, error: IOException?) -> Unit)
    fun getLatestWinResult(context: Context, handler: (Array<PLottoInfo.WinResult>?, error: IOException?) -> Unit)
    fun insertWinResult(context: Context, round: Int, winResult: Array<PLottoInfo.WinResult>): Long
}

class PLottoModel: IPLottoModel {
    override fun getWinResult(context: Context, round: Int, handler: (Array<PLottoInfo.WinResult>?, error: IOException?) -> Unit) {
        val pLottoDB = PrDatabase.getInstance(context).pLottoDB
        val winResultList = pLottoDB.selectWinResults(round, 1)
        if (0 < winResultList.count()) {
            val winResultJsonString = winResultList[0]["jsonString"].toString()
            val winResultArray = Gson().fromJson(winResultJsonString, Array<PLottoInfo.WinResult>::class.java)
            handler(winResultArray, null)
        } else {
            PrHttpRequest().getPLottoNumber(round, object: PrHttpRequest.ResponseCallback {
                override fun onResponse(api: PrHttpRequest.API, obj: Any?) {
                    if (obj is Array<*> && 0 < obj.count() && obj[0] is PLottoInfo.WinResult) {
                        val winResultArray = obj as Array<PLottoInfo.WinResult>
                        insertWinResult(context, winResultArray[0].round!!.toInt(), winResultArray)
                        handler(winResultArray, null)
                    } else {
                        handler(null, IOException("연금복권520 정보를 가져오지 못했습니다."))
                    }
                }

                override fun onFailure(api: PrHttpRequest.API, error: IOException) {
                    handler(null, error)
                }
            })
        }
    }

    override fun getLatestWinResult(context: Context, handler: (Array<PLottoInfo.WinResult>?, error: IOException?) -> Unit) {
        val latestDrawNumber = Util.latestDrawNumber(Definition.PLOTTO_START_DATE, "yyyy-MM-dd")
        getWinResult(context, latestDrawNumber, handler)
    }

    override fun insertWinResult(context: Context, round: Int, winResult: Array<PLottoInfo.WinResult>): Long {
        val winResultJsonString = Gson().toJson(winResult)
        return PrDatabase.getInstance(context).pLottoDB.insertWinResult(round, winResultJsonString)
    }
}
