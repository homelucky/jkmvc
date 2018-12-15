package com.jkmvc.validate

import java.util.*

/**
 * 校验表达式
 *    校验表达式是由多个(函数调用的)子表达式与运算符组成, 格式为 a(1) & b(1,2) && c(3,4) | d(2) . e(1) > f(5)
 *    子表达式是函数调用, 格式为 a(1,2)
 *    子表达式之间用运算符连接, 运算符有 & && | || . >
 *    运算符的含义:
 *        & 与
 *        && 短路与
 *        | 或
 *        || 短路或
 *         .  字符串连接
 *         > 累积结果
 *   无意于实现完整语义的布尔表达式, 暂时先满足于输入校验与orm保存数据时的校验, 因此:
 *   运算符没有优先级, 只能按顺序执行, 不支持带括号的子表达式
 *
 * @author shijianhang
 * @date 2016-10-19 下午3:40:55
 *
 */
class ValidationExpression(override val exp:String /* 原始表达式 */):IValidationExpression {

	companion object{
		/**
		 * 运算符的正则
		 */
		val RegexOperator: String = "[&\\|\\.\\>]+";

		/**
		 * 函数参数字符的正则
		 */
		val RegexParamChar: String = "\\w\\d-:";

		/**
		 * 函数的正则
		 */
		val RegexFunc: String = "(\\w+)(\\(([\\s" + RegexParamChar + ",]*)\\))?";

		/**
		* 表达式的正则
		 */
		val RegxExp: Regex = ("\\s*(" + RegexOperator + ")?\\s*" + RegexFunc).toRegex();

		/**
		 * 函数参数的正则
		 */
		val RegexParam: Regex = ("([" + RegexParamChar + "]+),?").toRegex();

		/**
		 * 编译 表达式
		 *     表达式是由多个(函数调用的)子表达式组成, 子表达式之间用运算符连接, 运算符有 & && | || . >
		 *
		 * <code>
		 *     val (ops, subexps) = ValidationExpression::compile("trim > notEmpty && email");
		 * </code>
		 *
		 * @param exp
		 * @return
		 */
		public fun compile(exp:String): List<ValidationUint> {
			// 第一个()是操作符，第二个()是函数名，第四个()是函数参数
			val matches: Sequence<MatchResult> = RegxExp.findAll(exp);

			val subexps:MutableList<ValidationUint> = LinkedList<ValidationUint>();
			for(m in matches){
				val op = m.groups[1]?.value; // 操作符
				val func = m.groups[2]!!.value; // 函数名
				val params = m.groups[4]?.value; // 函数参数
				subexps.add(ValidationUint(op, func, compileParams(params)));
			}

			return subexps;
		}

		/**
		 * 编译函数参数
		 *
		 * @param params
		 * @return
		 */
		public fun compileParams(exp:String?): List<String> {
			if(exp == null)
				return emptyList();

			val matches: Sequence<MatchResult> = RegexParam.findAll(exp);
			val result:MutableList<String> = LinkedList<String>();
			for(m in matches){
				result.add(m.groups[1]!!.value)
			}
			return result;
		}
	}

	/**
	 * 子表达式的数组
	 *   一个子表达式 = listOf(操作符, 函数名, 参数数组)
	 *   参数列表 = listOf("1", "2", ":name") 参数有值/变量（如:name）
	 */
	protected val subexps:List<ValidationUint> = compile(exp);

	/**
	 * 执行校验表达式
	 *
	 * <code>
	 * 	   // 编译
	 *     val exp = ValidationExpression("trim > notEmpty && email");
	 *     // 执行
	 *     result = exp.execute(value, data, lastsubexp);
	 * </code>
	 *
	 * @param Any? value 要校验的数值，该值可能被修改
	 * @param variables 变量
	 * @return ValidationResult 结果+最后一个校验单元+最后一个值
	 */
	public override fun execute(value:Any?, variables:Map<String, Any?>):ValidationResult
	{
		if(subexps.isEmpty())
			return ValidationResult(value, null, null);

		// 逐个运算子表达式
		var result:Any? = null;
		var lastValue:Any? = value;
		for (subexp in subexps)
		{
			val (op) = subexp;

			// 短路
			if(isShortReturn(op, result))
				return ValidationResult(result, subexp, lastValue);

			// 累积结果运算: 当前结果 result 作为下一参数 value
			if(op === ">")
				lastValue = result;

			// 运算子表达式
			val curr = subexp.execute(lastValue, variables);

			// 处理结果
			when (op)
			{
				"&", // 与
				"&&" -> // 短路与
					result = (result as Boolean) && (curr as Boolean);
				"|", // 或
				"||" -> // 短路或
					result = (result as Boolean) || (curr as Boolean);
				"." -> // 字符串连接
					result = (result as String) + curr;
				else ->
					result = curr;
			}

			// 短路
			if(isShortReturn(op, result))
				return ValidationResult(result, subexp, lastValue);
		}

		return ValidationResult(result, subexps.last(), lastValue);
	}

	/**
	* 是否短路
	 */
	protected fun isShortReturn(op: String?, result: Any?):Boolean{
		return (op == "&&" && result == false || op == "||" && result == true)
	}
}
