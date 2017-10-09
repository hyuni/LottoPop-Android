package com.prangbi.android.lottopop.controller.activity

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.AbsListView
import com.prangbi.android.lottopop.R
import com.prangbi.android.lottopop.base.Definition
import com.prangbi.android.lottopop.controller.adapter.PLottoWinResultAdapter
import com.prangbi.android.lottopop.helper.Util
import com.prangbi.android.lottopop.model.PLottoInfo
import com.prangbi.android.lottopop.model.PLottoModel
import kotlinx.android.synthetic.main.plotto_winresult_activity.*

/**
 * Created by guprohs on 2017. 8. 21..
 */
class PLottoWinResultActivity: AppCompatActivity() {
    // Companion
    companion object {
        const val WIN_RESULT_WEB_URL = Definition.SERVER_URL + "/gameResult.do?method=win520&Round="
    }

    // Variable
    private lateinit var mContext: Activity
    private val winResultAdapter = PLottoWinResultAdapter()
    private var broadcastReceiver: BroadcastReceiver? = null
    private lateinit var progressDialog: Dialog
    private var pLottoModel = PLottoModel()
    private var canConnect = false
    private var nextRound = 0
    private var isLoading = false
    private var autoRequestCount = 0

    // Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plotto_winresult_activity)
        mContext = this

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progressDialog = Util.makeLoadingDialog(mContext)
        initWinResultListView()
        initBroadcastReciever()

//        mainLayout.setOnClickListener {
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(numberInputEt.getWindowToken(), 0)
//        }

        canConnect = Util.canConnectToInternet(mContext)
        isLoading = true
        nextRound = 0
        autoRequestCount = 0
        showProgressDialog()
        getWinResult()
    }

    override fun onDestroy() {
        super.onDestroy()

        broadcastReceiver?.let {
            unregisterReceiver(broadcastReceiver)
        }
    }

    // Event
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                // NavUtils.navigateUpFromSameTask(context);
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Function
    private fun initBroadcastReciever() {
        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.getAction()
                if ("com.prangbi.android.lottopop.CONNECTIVITY_CHANGE" == action) {
                    canConnect = Util.canConnectToInternet(mContext)
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.prangbi.android.lottopop.CONNECTIVITY_CHANGE")
        broadcastReceiver?.let {
            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    private fun initWinResultListView() {
        winResultListView.adapter = winResultAdapter
        winResultListView.setOnItemClickListener { parent, view, position, id ->
            val item = parent.adapter.getItem(position) as Array<PLottoInfo.WinResult>
            if(0 < item.count()) {
                val url = WIN_RESULT_WEB_URL + item[0].round
                Util.moveToWebActivity(mContext, "당첨결과", url)
            }

        }
        winResultListView.setOnScrollListener(object: AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if (0 >= nextRound || true == isLoading || false == canConnect) {
                    return
                }

                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    isLoading = true
                    autoRequestCount = 0
                    showProgressDialog()
                    getWinResult()
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {

            }

        })
    }

    private fun showProgressDialog() {
        progressDialog.show();
    }

    private fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    private fun getWinResult() {
        pLottoModel.getWinResult(mContext, nextRound, { winResultArray, error ->
            if (null == error) {
                var shouldRequestMore = false
                if (null != winResultArray && 0 < winResultArray.count()) {
                    winResultAdapter.addItem(winResultArray)
                    nextRound = winResultArray[0].round!!.toInt() - 1

                    if (Definition.COUNT_PER_PAGE > ++autoRequestCount && 0 < nextRound) {
                        shouldRequestMore = true
                    }
                }

                if (true == shouldRequestMore) {
                    getWinResult()
                } else {
                    winResultAdapter.notifyDataSetChanged()
                    hideProgressDialog()
                    isLoading = false
                }
            } else {
                Util.showToast(mContext, error.localizedMessage)
                hideProgressDialog()
                isLoading = false
            }
        })
    }
}
