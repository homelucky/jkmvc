package com.jkmvc.db

/**
 * Db标识符(表名/字段名)转义器
 *
 * @ClassName: IDbIdentifierQuoter
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2018-11-21 7:28 PM
 */
interface IDbIdentifierQuoter{

    /**
     * 转义多个表名
     *
     * @param tables 表名集合，其元素可以是String, 也可以是DbAlias
     * @param with_brackets 当拼接数组时, 是否用()包裹
     * @return
     */
    fun quoteTables(tables:Collection<CharSequence>, with_brackets:Boolean):String {
        // 遍历多个表转义
        return tables.joinToString(", ", if(with_brackets) "(" else "", if(with_brackets) ")" else ""){
            // 单个表转义
            quoteTable(it)
        }
    }

    /**
     * 转义表名
     *   mysql为`table`
     *   oracle为"table"
     *   sql server为"table" [table]
     *
     * @param table 表名或别名 DbAlias
     * @return
     */
    fun quoteTable(table:CharSequence):String {
        return if(table is DbExpr) // 表与别名之间不加 as，虽然mysql可识别，但oracle不能识别
                    table.quoteIdentifier(this, " ")
                else
                    quoteIdentifier(table.toString())
    }

    /**
     * 转义多个字段名
     *
     * @param columns 字段名集合，其元素可以是String, 也可以是DbAlias
     * @param with_brackets 当拼接数组时, 是否用()包裹
     * @return
     */
    fun quoteColumns(columns:Collection<CharSequence>, with_brackets:Boolean = false):String {
        // 遍历多个字段转义
        return columns.joinToString(", ", if(with_brackets) "(" else "", if(with_brackets) ")" else "") {
            // 单个字段转义
            quoteColumn(it)
        }
    }

    /**
     * 转义字段名
     *   mysql为`column`
     *   oracle为"column"
     *   sql server为"column" [column]
     *
     * @param column 字段名, 可能是别名 DbAlias
     * @return
     */
    fun quoteColumn(column:CharSequence):String {
        var table = "";
        var col: String; // 字段
        var alias:String? = null; // 别名
        var colQuoting = true // 是否转义字段
        if(column is DbExpr){
            col = column.exp.toString()
            alias = column.alias
            colQuoting = column.expQuoting
        }else{
            col = column.toString()
        }

        // 转义字段 + 非函数表达式
        if (colQuoting && "^\\w[\\w\\d_\\.\\*]*".toRegex().matches(column))
        {
            // 表名
            if(column.contains('.')){
                var arr = column.split('.');
                table = "${quoteIdentifier(arr[0])}.";
                col = arr[1]
            }

            // 字段名
            if(col == "*" || (isKeyword(col))) { // * 或 关键字不转义
                //...
            }else{ // 其他字段转义
                col = quoteIdentifier(col)
            }
        }

        // 字段别名
        if(alias == null)
            return "$table$col";

        return "$table$col AS ${quoteIdentifier(alias)}"; // 转义
    }

    /**
     * 是否关键字
     * @param col 列
     * @return
     */
    fun isKeyword(col: String): Boolean

    /**
     * 转义标识符(表名/字段名)
     * @param 表名或字段名
     * @return
     */
    fun quoteIdentifier(id: String): String
}