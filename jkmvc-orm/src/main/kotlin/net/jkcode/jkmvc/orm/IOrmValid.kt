package net.jkcode.jkmvc.orm

/**
 * ORM之数据校验
 *
 * @author shijianhang
 * @date 2016-10-10 上午12:52:34
 *
 */
interface IOrmValid : IOrmEntity {

    /**
     * 校验数据
     * @return
     */
    fun validate(): Boolean;

}
