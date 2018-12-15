package com.jkmvc.db

import java.io.Closeable
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * 封装db操作
 *
 * @author shijianhang
 * @date 2016-10-8 下午8:02:47
 */
interface IDb: IDbValueQuoter, Closeable{

    /**
     * 获得数据库类型
     *   根据driverClass来获得
     */
    val dbType:DbType

    /**
     * sql标示符（表/字段）的转义字符
     *   mysql为 `table`.`column`
     *   oracle为 "table"."column"
     *   sql server为 "table"."column" 或 [table].[column]
     */
    val identifierQuoteString:String

    /**
     * 执行事务
     * @param statement db操作过程
     * @return
     */
    fun <T> transaction(statement: () -> T):T;

    /**
     * 执行事务
     * @param fake 不真正使用事务
     * @param statement db操作过程
     * @return
     */
    fun <T> transaction(fake: Boolean, statement: () -> T):T{
        if(fake)
            return statement()

        return transaction(statement)
    }

    /**
     * 是否在事务中
     * @return
     */
    fun isInTransaction(): Boolean

    /**
     * 获得表的所有列
     * @param table
     * @return
     */
    fun listColumns(table:String): List<String>;

    /**
     * 执行更新
     * @param sql
     * @param params
     * @param generatedColumn 返回的自动生成的主键名
     * @return
     */
    fun execute(sql: String, params: List<Any?>? = null, generatedColumn:String? = null): Int;

    /**
     * 批量更新: 每次更新sql参数不一样
     *
     * @param sql
     * @param paramses 多次处理的参数的汇总，一次处理取 paramSize 个参数，必须保证他的大小是 paramSize 的整数倍
     * @param paramSize 一次处理的参数个数
     * @return
     */
    fun batchExecute(sql: String, paramses: List<Any?>, paramSize:Int): IntArray;

    /**
     * 查询多行
     * @param sql
     * @param params
     * @param action 转换结果的函数
     * @return
     */
    fun <T> queryResult(sql: String, params: List<Any?>? = null, action: (ResultSet) -> T): T;

    /**
     * 查询多行
     * @param sql
     * @param params
     * @param transform 转换结果的函数
     * @return
     */
    fun <T> queryRows(sql: String, params: List<Any?>? = null, transform: (MutableMap<String, Any?>) -> T): List<T>;

    /**
     * 查询一行(多列)
     * @param sql
     * @param params
     * @param transform 转换结果的函数
     * @return
     */
    fun <T> queryRow(sql: String, params: List<Any?>? = null, transform: (MutableMap<String, Any?>) -> T): T?;

    /**
     * 查询一列(多行)
     * @param sql
     * @param params
     * @param clazz 值类型
     * @param transform 转换结果的函数
     * @return
     */
    fun <T:Any> queryColumn(sql: String, params: List<Any?>? = null, clazz: KClass<T>? = null): List<T?>

    /**
     * 查询一列(多行)
     * @param sql
     * @param clazz 值类型
     * @param transform 转换结果的函数
     * @return
     */
    fun <T:Any> queryColumn(sql: String, clazz: KClass<T>): List<T?>{
        return queryColumn(sql, null, clazz)
    }

    /**
     * 查询一列(多行)
     * @param sql
     * @param params
     * @return
     */
    fun queryIntColumn(sql: String, params: List<Any?>? = null): List<Int>{
        // 只要指定值类型, 则返回的列表元素类型就是该类型, 不会为null, 因此返回值不是List<Int?>, 而是List<Int>
        return queryColumn(sql, params, Int::class) as List<Int>
    }

    /**
     * 查询一列(多行)
     * @param sql
     * @param params
     * @return
     */
    fun queryLongColumn(sql: String, params: List<Any?>? = null): List<Long>{
        return queryColumn(sql, params, Long::class) as List<Long>
    }

    /**
     * 查询一列(多行)
     * @param sql
     * @param params
     * @return
     */
    fun queryBooleanColumn(sql: String, params: List<Any?>? = null): List<Boolean>{
        return queryColumn(sql, params, Boolean::class) as List<Boolean>
    }

    /**
     * 查询一列(多行)
     * @param sql
     * @param params
     * @return
     */
    fun queryStringColumn(sql: String, params: List<Any?>? = null): List<String>{
        return queryColumn(sql, params, String::class) as List<String>
    }

    /**
     * 查询一行一列
     * @param sql
     * @param params
     * @param clazz 值类型
     * @return
     */
    fun <T:Any> queryCell(sql: String, params: List<Any?>? = null, clazz: KClass<T>? = null): Pair<Boolean, T?>;

    /**
     * 查询一行一列
     * @param sql
     * @param clazz 值类型
     * @return
     */
    fun <T:Any> queryCell(sql: String, clazz: KClass<T>): Pair<Boolean, T?>{
        return queryCell(sql, null, clazz)
    }

    /**
     * 查询一行一列
     * @param sql
     * @param params
     * @return
     */
    fun queryInt(sql: String, params: List<Any?>? = null): Int?{
        val (hasNext, result) = queryCell(sql, params, Int::class)
        return if(hasNext) result else null
    }

    /**
     * 查询一行一列
     * @param sql
     * @param params
     * @return
     */
    fun queryLong(sql: String, params: List<Any?>? = null): Long?{
        val (hasNext, result) = queryCell(sql, params, Long::class)
        return if(hasNext) result else null
    }

    /**
     * 查询一行一列
     * @param sql
     * @param params
     * @return
     */
    fun queryBoolean(sql: String, params: List<Any?>? = null): Boolean?{
        val (hasNext, result) = queryCell(sql, params, Boolean::class)
        return if(hasNext) result else null
    }

    /**
     * 查询一行一列
     * @param sql
     * @param params
     * @return
     */
    fun queryString(sql: String, params: List<Any?>? = null): String?{
        val (hasNext, result) = queryCell(sql, params, String::class)
        return if(hasNext) result else null
    }

    /**
     * 开启事务
     */
    fun begin():Unit;


    /**
     * 提交
     */
    fun commit():Boolean;

    /**
     * 回滚
     */
    fun rollback():Boolean;

    /**
     * 转义多个表名
     *
     * @param Collection<CharSequence> tables 表名集合，其元素可以是String, 也可以是DbAlias
     * @return
     */
    fun quoteTables(tables:Collection<CharSequence>, with_brackets:Boolean = false):String;

    /**
     * 转义多个字段名
     *
     * @param Collection<CharSequence> columns 表名集合，其元素可以是String, 也可以是DbAlias
     * @param bool with_brackets 当拼接数组时, 是否用()包裹
     * @return
     */
    fun quoteColumns(columns:Collection<CharSequence>, with_brackets:Boolean = false):String;

    /**
     * 转义表名
     *   mysql为`table`
     *   oracle为"table"
     *   sql server为"table" [table]
     *
     * @param table
     * @return
     */
    fun quoteTable(table:CharSequence):String

    /**
     * 转义字段名
     *   mysql为`column`
     *   oracle为"column"
     *   sql server为"column" [column]
     *
     * @param column 字段名, 可以是字段数组
     * @return
     */
    fun quoteColumn(column:CharSequence):String

    /**
     * 根据对象属性名，获得db字段名
     *    可根据实际需要在 model 类中重写
     *
     * @param prop 对象属性名
     * @return db字段名
     */
    fun prop2Column(prop:String): String

    /**
     * 根据db字段名，获得对象属性名
     *    可根据实际需要在 model 类中重写
     *
     * @param column db字段名
     * @return 对象属性名
     */
    fun column2Prop(column:String): String

    /**
     * 预览sql
     * @param sql
     * @param params sql参数
     * @return
     */
    fun previewSql(sql: String, params: List<Any?>? = null): String
}