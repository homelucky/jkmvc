package com.jkmvc.db

import kotlin.reflect.KClass

/**
 * 查询sql执行
 *
 * 1 db的动态性
 *   所有sql执行方法的最后一个参数都是 db 对象, 每次执行sql都需要指定一个 db, 这是为了抽取子类的共同需求
 *   1.1 DbQueryBuilder 需要递延指定 db, 在最后要编译与执行sql时才指定, 方便实现按需选择不同的连接, 如读写分离
 *   1.2 CompiledSql 只为缓存编译的sql, 跨请求的存在, 不可能预先确定 db
 *
 * 2 db的预设性
 *   在Orm模型中, db是预先定义在元数据中, 以后执行sql也不需要你重新指定db
 *   => 抽象属性 defaultDb 来记录预设的 db
 *
 * 3 注：为什么不是接口，而是抽象类？
 *    因为我需要实现 inline fun <reified T:Any> find(): T? / inline fun <reified T:Any>  findAll(): List<T>
 *    这两个方法都需要具体化泛型，因此需要内联实现inline，但是inline不能用于接口方法/抽象方法，因此我直接在该类中实现这两个方法，该类也只能由接口变为抽象类
 */
abstract class IDbQuery{

    /**
     * 默认db名
     *   用在sql执行方法中最后一个参数中, 默认使用该属性对应的db
     */
    public abstract val defaultDb: IDb

    /**
     * 获得记录转换器
     * @param clazz 要转换的类
     * @return 转换的匿名函数
     */
    public open fun <T:Any> getRecordTranformer(clazz: KClass<T>): ((MutableMap<String, Any?>) -> T) {
        return clazz.recordTranformer
    }

    /**
     * 查找多个： select 语句
     *
     * @param params 参数
     * @param db 数据库连接
     * @param transform 转换函数
     * @return 列表
     */
    public abstract fun <T:Any> findAll(params: List<Any?> = emptyList(), db: IDb = defaultDb, transform: (Map<String, Any?>) -> T): List<T>;

    /**
     * 查找多个： select 语句
     *  对 findAll(transform: (Map<String, Any?>) 的精简版，直接根据泛型 T 来找到对应的记录转换器
     *  泛型 T 有3类情况，会生成不同的记录转换器
     *  1 Orm类：实例化并调用setOriginal()
     *  2 Map类: 直接返回记录数据，不用转换
     *  3 其他类：如果实现带 Map 参数的构造函数，如 constructor(data: MutableMap<String, Any?>)，就调用
     *
     * @param params 参数
     * @param db 数据库连接
     * @return 列表
     */
    public inline fun <reified T:Any> findAll(params: List<Any?> = emptyList(), db: IDb = defaultDb): List<T> {
        return findAll(params, db, getRecordTranformer<T>(T::class))
    }

    /**
     * 查找一个： select ... limit 1语句
     *
     * @param params 参数
     * @param db 数据库连接
     * @param transform 转换函数
     * @return 一个数据
     */
    public abstract fun <T:Any> find(params: List<Any?> = emptyList(),  db: IDb = defaultDb, transform: (Map<String, Any?>) -> T): T?;

    /**
     * 查找一个： select ... limit 1语句
     *  对 find(transform: (Map<String, Any?>) 的精简版，直接根据泛型 T 来找到对应的记录转换器
     *  泛型 T 有3类情况，会生成不同的记录转换器
     *  1 Orm类：实例化并调用setOriginal()
     *  2 Map类: 直接返回记录数据，不用转换
     *  3 其他类：如果实现带 Map 参数的构造函数，如 constructor(data: MutableMap<String, Any?>)，就调用
     *
     * @param params 参数
     * @param db 数据库连接
     * @return 一个数据
     */
    public inline fun <reified T:Any> find(params: List<Any?> = emptyList(), db: IDb = defaultDb): T? {
        return find(params, db, getRecordTranformer<T>(T::class));
    }

    /**
     * 查询一列（多行）
     *
     * @param params 参数
     * @param clazz 值类型
     * @param db 数据库连接
     * @return
     */
    public abstract fun <T:Any> findColumn(params: List<Any?> = emptyList(), clazz: KClass<T>? = null, db: IDb = defaultDb): List<T?>

    /**
     * 查询一列（多行）
     *
     * @param params 参数
     * @param db 数据库连接
     * @return
     */
    public inline fun <reified T:Any> findColumn(params: List<Any?> = emptyList(), db: IDb = defaultDb): List<T?> {
        return findColumn(params, T::class, db)
    }

    /**
     * 查询一行一列
     *
     * @param params 参数
     * @param clazz 值类型
     * @param db 数据库连接
     * @return
     */
    public abstract fun <T:Any> findCell(params: List<Any?> = emptyList(), clazz: KClass<T>? = null, db: IDb = defaultDb): Cell<T>

    /**
     * 查询一行一列
     *
     * @param params 参数
     * @param db 数据库连接
     * @return
     */
    public inline fun <reified T:Any> findCell(params: List<Any?> = emptyList(), db: IDb = defaultDb): Cell<T> {
        return findCell(params, T::class, db)
    }

}