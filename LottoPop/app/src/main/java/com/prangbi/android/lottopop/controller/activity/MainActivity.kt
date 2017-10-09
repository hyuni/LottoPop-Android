package com.prangbi.android.lottopop.controller.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.prangbi.android.lottopop.R
import com.prangbi.android.lottopop.base.Definition
import com.prangbi.android.lottopop.helper.Util
import com.prangbi.android.lottopop.model.NLottoInfo
import com.prangbi.android.lottopop.model.NLottoModel
import com.prangbi.android.lottopop.model.PLottoInfo
import com.prangbi.android.lottopop.model.PLottoModel
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.nlotto_winresult_item.*
import kotlinx.android.synthetic.main.plotto_winresult_item.*

/**
 * Created by guprohs on 2017. 7. 12..
 */
class MainActivity: AppCompatActivity() {
    // Variable
    private lateinit var mContext: Activity
    private lateinit var progressDialog: Dialog
    private var nLottoModel = NLottoModel()
    private var pLottoModel = PLottoModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.main_activity)
        progressDialog = Util.makeLoadingDialog(mContext)

        nLottoWinResultItem.setOnClickListener {
            moveToNLottoActivity()
        }
        pLottoWinResultItem.setOnClickListener {
            moveToPLottoActivity()
        }
        termsTextView.setOnClickListener {
            Util.moveToWebActivity(mContext, "개인정보취급방침", Definition.TERMS_URL)
        }
    }

    override fun onResume() {
        super.onResume()
        showProgressDialog()
        nLottoModel.getLatestWinResult(mContext, { winResult, error ->
            if (null != error) {
                Util.showToast(mContext, error.localizedMessage)
            } else if (null != winResult) {
                setNLottoItem(winResult)
            }

            pLottoModel.getLatestWinResult(mContext, { winResultArray, error ->
                if (null != error) {
                    Util.showToast(mContext, error.localizedMessage)
                } else if (null != winResultArray) {
                    setPLottoItem(winResultArray)
                }
                hideProgressDialog()
            })
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_actionbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.qrCode -> {
                scanQrCode()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (null != result) {
            if (null != result.contents) {
                val intent = Intent(mContext, WebActivity::class.java)
                intent.putExtra("title", "당첨결과")
                intent.putExtra("url", result.contents)
                startActivity(intent)
            } else {
                Toast.makeText(mContext, "Cancelled", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Function
    private fun showProgressDialog() {
        progressDialog.show();
    }

    private fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    private fun setNLottoItem(winResult: NLottoInfo.WinResult) {
        nLottoTitleDrwNoTextView.text = winResult.drwNo.toString(10)
        nLottoTitleTextView.text = "나눔로또6/45"
        nLottoTitleDateTextView.text = "(" + winResult.drwNoDate + ")"
        nLottoResultTextView.text = "1등 " + winResult.firstPrzwnerCo.toString(10) + "게임"
        nLottoResultAmountTextView.text = "게임당 " + Util.amountString(winResult.firstWinamnt) + "원"

        nLottoNumber1TextView.text = winResult.drwtNo1.toString(10)
        nLottoNumber2TextView.text = winResult.drwtNo2.toString(10)
        nLottoNumber3TextView.text = winResult.drwtNo3.toString(10)
        nLottoNumber4TextView.text = winResult.drwtNo4.toString(10)
        nLottoNumber5TextView.text = winResult.drwtNo5.toString(10)
        nLottoNumber6TextView.text = winResult.drwtNo6.toString(10)
        nLottoBonusNumberTextView.text = winResult.bnusNo.toString(10)

        nLottoNumber1TextView.background = Util.getNLottoNumberBackground(mContext, winResult.drwtNo1.toInt())
        nLottoNumber2TextView.background = Util.getNLottoNumberBackground(mContext, winResult.drwtNo2.toInt())
        nLottoNumber3TextView.background = Util.getNLottoNumberBackground(mContext, winResult.drwtNo3.toInt())
        nLottoNumber4TextView.background = Util.getNLottoNumberBackground(mContext, winResult.drwtNo4.toInt())
        nLottoNumber5TextView.background = Util.getNLottoNumberBackground(mContext, winResult.drwtNo5.toInt())
        nLottoNumber6TextView.background = Util.getNLottoNumberBackground(mContext, winResult.drwtNo6.toInt())
        nLottoBonusNumberTextView.background = Util.getNLottoNumberBackground(mContext, winResult.bnusNo.toInt())
    }

    private fun setPLottoItem(winResultArray: Array<PLottoInfo.WinResult>) {
        if (0 < winResultArray.count()) {
            val winResult = winResultArray[0]
            val rankClass = winResult.rankClass?.toIntOrNull(10)

            pLottoTitleRountTextView.text = winResult.round
            pLottoTitleTextView.text = "연금복권520"
            pLottoTitleDateTextView.text = "(" + winResult.pensionDrawDate + ")"
            pLottoResultTextView.text = "1등 " + winResultArray.count().toString(10) + "게임"
            pLottoResultAmountTextView.text = "월 500만원 X 20년"
            pLottoGroup1TextView.text = winResult.rankClass + "조"

            pLottoNum1_1TextView.text = winResult.rankNo?.getOrNull(0).toString()
            pLottoNum1_2TextView.text = winResult.rankNo?.getOrNull(1).toString()
            pLottoNum1_3TextView.text = winResult.rankNo?.getOrNull(2).toString()
            pLottoNum1_4TextView.text = winResult.rankNo?.getOrNull(3).toString()
            pLottoNum1_5TextView.text = winResult.rankNo?.getOrNull(4).toString()
            pLottoNum1_6TextView.text = winResult.rankNo?.getOrNull(5).toString()

            pLottoGroup1TextView.background = Util.getPLottoGroupBackground(mContext, if (null != rankClass) { rankClass } else 0)
        }
        if (2 <= winResultArray.count()) {
            val winResult = winResultArray[1]
            val rankClass = winResult.rankClass?.toIntOrNull(10)

            pLottoGroup2TextView.text = winResult.rankClass + "조"
            pLottoNum2_1TextView.text = winResult.rankNo?.getOrNull(0).toString()
            pLottoNum2_2TextView.text = winResult.rankNo?.getOrNull(1).toString()
            pLottoNum2_3TextView.text = winResult.rankNo?.getOrNull(2).toString()
            pLottoNum2_4TextView.text = winResult.rankNo?.getOrNull(3).toString()
            pLottoNum2_5TextView.text = winResult.rankNo?.getOrNull(4).toString()
            pLottoNum2_6TextView.text = winResult.rankNo?.getOrNull(5).toString()

            pLottoGroup2TextView.background = Util.getPLottoGroupBackground(mContext, if (null != rankClass) { rankClass } else 0)
        }
    }

    private fun scanQrCode() {
        IntentIntegrator(mContext).initiateScan()
    }

    private fun moveToNLottoActivity() {
        val intent = Intent(mContext, NLottoWinResultActivity::class.java)
        startActivity(intent)
    }

    private fun moveToPLottoActivity() {
        val intent = Intent(mContext, PLottoWinResultActivity::class.java)
        startActivity(intent)
    }
}
