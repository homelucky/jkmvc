package net.jkcode.jkmvc.orm

/**
 * ORM之持久化，主要是负责数据库的增删改查
 *
 * @author shijianhang
 * @date 2016-10-10
 *
 */
interface IOrmPersistent : IOrmValid {

	/**
	 * 元数据
	 */
	val ormMeta: IOrmMeta;

	/**
	 * 判断当前记录是否存在于db: 有原始数据就认为它是存在的
	 */
	var loaded:Boolean;

	/**
	 * 获得主键值
	 */
	val pk:DbKeyValues

	/**
	 * 获得原始主键值
	 *   update()时用到，因为主键可能被修改
	 */
	val oldPk:DbKeyValues

	/************************************ 持久化处理 *************************************/
	/**
	 * 获得sql构建器
	 * @return
	 */
	fun queryBuilder(): OrmQueryBuilder

	/**
	 * 根据主键值来加载数据
	 *   如果是复合主键, 则参数按 ormMeta.primaryKey 中定义的字段的属性来传值
	 *
	 * @param pk
	 */
	fun loadByPk(vararg pk: Any): Unit

	/**
	 * 保存数据
	 *
	 * @return
	 */
	fun save(): Boolean {
		if(loaded)
			return update();

		return create() > 0;
	}

	/**
	 * 插入数据: insert sql
	 *
	 * <code>
	 *    val user = UserModel();
	 *    user.name = "shi";
	 *    user.age = 24;
	 *    user.create();
	 * </code>
	 * 
	 * @return 新增数据的主键
	 */
	fun create(): Long;

	/**
	 * 更新数据: update sql
	 *
	 * <code>
	 *    val user = UserModel.queryBuilder().where("id", 1).find<UserModel>();
	 *    user.name = "li";
	 *    user.update();
	 * </code>
	 * 
	 * @return 
	 */
	fun update(): Boolean;

	/**
	 * 删除数据: delete sql
	 *
	 *　<code>
	 *    val user = UserModel.queryBuilder().where("id", "=", 1).find<UserModel>();
	 *    user.delete();
	 *　</code>
	 *
	 * @return 
	 */
	fun delete(): Boolean;

	/**
	 * 字段值自增: update t1 set col1 = col1 + 1
	 *
	 * <code>
	 *    val user = UserModel.queryBuilder().where("id", 1).find<UserModel>();
	 *    user.incr("age", 1);
	 * </code>
	 *
	 * @return
	 */
	fun incr(prop: String, step: Int): Boolean;

	/************************************ 持久化事件 *************************************/
	/**
	 * 处理create前置事件
	 */
	fun beforeCreate(){}

	/**
	 * 处理create后置事件
	 */
	fun afterCreate(){}

	/**
	 * 处理update前置事件
	 */
	fun beforeUpdate(){}

	/**
	 * 处理update后置事件
	 */
	fun afterUpdate(){}

	/**
	 * 处理save前置事件
	 */
	fun beforeSave(){}

	/**
	 * 处理save后置事件
	 */
	fun afterSave(){}

	/**
	 * 处理delete前置事件
	 */
	fun beforeDelete(){}

	/**
	 * 处理delete后置事件
	 */
	fun afterDelete(){}
}
