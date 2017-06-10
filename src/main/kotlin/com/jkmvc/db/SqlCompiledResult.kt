package com.jkmvc.db

import java.util.*
import kotlin.collections.ArrayList


/**
 * sql编译结果
 *
 * @author shijianhang
 * @date 2017-6-10 下午8:02:47
 */
class SqlCompiledResult : Cloneable, ISqlCompiledResult {

    /**
     * 编译好的sql
     */
    override var sql:String = ""

    /**
     * 编译后的sql参数 / 静态参数
     */
    public var staticParams: LinkedList<Any?> = LinkedList<Any?>()

    /**
     * 动态参数
     */
    public var dynamicParams: Array<out Any?>? = null

    /**
     * 实际参数的容器，防止每次调用params()都要创建list
     */
    protected val realParams: ArrayList<Any?> by lazy(LazyThreadSafetyMode.NONE) {
        ArrayList<Any?>(staticParams.size);
    }

    /**
     *  实际参数 = 静态参数 + 动态参数
     */
    override val params:List<Any?>
        get(){
            if(dynamicParams == null)
                return staticParams;

            // 使用预先创建好的list来保存最新的实际参数，避免每次都要创建新的list
            realParams.clear()
            var i = 0; // 动态变量的迭代索引
            for (v in staticParams){
                if(v is String && v == "?") // 如果参数值是?，则认为是动态参数
                    realParams.add(dynamicParams!![i++])
                else // 静态参数
                    realParams.add(v)
            }
            return realParams;
        }

    /**
     * 判断是否为空
     */
    override fun isEmpty():Boolean{
        return sql == ""
    }

    /**
     * 清空编译结果
     */
    override fun clear(): SqlCompiledResult {
        sql = "";
        staticParams.clear();
        dynamicParams = null;
        return this;
    }

    /**
     * 克隆对象
     * @return o
     */
    public override fun clone(): Any {
        val o = super.clone() as SqlCompiledResult
        o.staticParams = staticParams.clone() as LinkedList<Any?>
        return o
    }

    /**
     * 预览sql
     * @return
     */
    override fun previewSql(useDynParams:Boolean): String {
        // 替换实参
        var i = 0 // 静态变量的迭代索引
        var j = 0 // 动态变量的迭代索引
        val realSql = sql.replace("\\?".toRegex()) { matches: MatchResult ->
            val param = staticParams[i++] // 静态参数
            if(param is String){
                if(param == "?" && dynamicParams != null)// 如果参数值是?，则认为是动态参数
                    dynamicParams!![j++].toString()
                else
                    "\"$param\""
            } else
                param.toString()
        }
        return realSql
    }
}