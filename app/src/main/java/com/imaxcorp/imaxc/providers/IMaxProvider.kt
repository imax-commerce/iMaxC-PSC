package com.imaxcorp.imaxc.providers

import android.content.Context
import com.imaxcorp.imaxc.data.*
import com.imaxcorp.imaxc.services.IMaxApi
import com.imaxcorp.imaxc.services.RetrofitClient
import retrofit2.Call

class IMaxProvider(context: Context) {
    private var context: Context? = null
    private val baseUrl: String = "https://imaxpanel.herokuapp.com"

    init {
        this.context = context
    }

    fun getHistoryOrder(body: DataOrder): Call<ResponseOrder>? {
        val retrofit = RetrofitClient().getClientOrder(baseUrl)
        return retrofit?.create(IMaxApi::class.java)?.getMyOrder(body)
    }

    fun getDebtPending(body: DataDebt) : Call<ResponseDebt>? {
        val retrofit = RetrofitClient().getClientOrder(baseUrl)
        return retrofit?.create(IMaxApi::class.java)?.getMyDebt(body)
    }

    fun getExpressOrder(body: DataDebt) : Call<ResponseShipping>? {
        val retrofit = RetrofitClient().getClientOrder(baseUrl)
        return retrofit?.create(IMaxApi::class.java)?.getExpressOrder(body)

    }

    fun getShippingOrder(body: DataDebt) : Call<ResponseShipping>? {
        val retrofit = RetrofitClient().getClientOrder(baseUrl)
        return retrofit?.create(IMaxApi::class.java)?.getShippingOrder(body)

    }
}