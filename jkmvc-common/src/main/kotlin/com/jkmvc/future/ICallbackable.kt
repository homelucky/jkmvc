package com.jkmvc.future

import org.apache.http.concurrent.FutureCallback
import java.util.*

/**
 * 可回调
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-14 11:46 AM
 */
interface ICallbackable<T> {

    /**
     * 添加回调
     * @param callback
     */
    fun addCallback(callback: FutureCallback<T?>)
}