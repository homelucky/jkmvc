package com.jkmvc.common

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.reflect.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.javaType

/**
 * 强制调用克隆方法
 */
public fun Any.forceClone():Any {
    if(!(this is Cloneable))
        throw IllegalArgumentException("非Cloneable对象，不能调用clone()方法")

    val f:KFunction<*> = javaClass.kotlin.findFunction("clone")!!
    return f.call(this) as Any;
}

/**
 * 获得指定类型的默认值
 * @return
 */
public inline val <T: Any> KClass<T>.defaultValue:T
    get(){
        return when (this) {
            Int::class -> 0
            Long::class -> 0L
            Float::class -> 0.0
            Double::class -> 0.0
            Boolean::class -> false
            Short::class -> 0
            Byte::class -> 0
            else -> null
        } as T
    }

/****************************** kotlin反射扩展: KClass *******************************/
/**
 * 匹配方法的名称与参数类型
 * @param name 方法名
 * @param paramTypes 参数类型
 * @return
 */
public fun KFunction<*>.matches(name:String, paramTypes:List<Class<*>> = emptyList()):Boolean{
    // 1 匹配名称
    if(name != this.name)
        return false

    // 2 匹配参数
    // 2.1 匹配参数个数
    if(paramTypes.size != this.parameters.size)
        return false;

    // 2.2 匹配参数类型
    for (i in paramTypes.indices){
        var targetType = this.parameters[i].type.javaType;
        if(targetType is ParameterizedTypeImpl) // 若是泛型类型，则去掉泛型，只保留原始类型
            targetType = targetType.rawType;

        if(paramTypes[i] != targetType)
            return false
    }

    return true;
}

/**
 * 查找方法
 * @param name 方法名
 * @param paramTypes 参数类型
 * @return
 */
public fun KClass<*>.findFunction(name:String, paramTypes:List<Class<*>> = emptyList()): KFunction<*>?{
    var pt = if(paramTypes is MutableList){
        paramTypes
    }else{
        ArrayList<Class<*>>(paramTypes);
    }

    // 第一个参数为this
    pt.add(0, this.java);

    return memberFunctions.find {
        it.matches(name, pt);
    }
}

/**
 * 查找静态方法
 * @param name 方法名
 * @param paramTypes 参数类型
 * @return
 */
public fun KClass<*>.findStaticFunction(name:String, paramTypes:List<Class<*>> = emptyList()): KFunction<*>?{
    return staticFunctions.find {
        it.matches(name, paramTypes);
    }
}

/**
 * 查找构造函数
 * @param paramTypes 参数类型
 * @return
 */
public fun KClass<*>.findConstructor(paramTypes:List<Class<*>> = emptyList()): KFunction<*>?{
    return constructors.find {
        it.matches("<init>", paramTypes); // 构造函数的名称为 <init>
    }
}

/**
 * 查找属性
 * @param name 属性名
 * @return
 */
public fun KClass<*>.findProperty(name:String): KProperty1<*, *>?{
    return this.declaredMemberProperties.find {
        it.name == name;
    }
}

/**
 * 转换参数类型
 * @param value
 * @return
 */
public inline fun KParameter.convert(value: String): Any {
    return value.to(this.type)
}

/****************************** java反射扩展: Class *******************************/
/**
 * 匹配方法的名称与参数类型
 * @param name 方法名
 * @param paramTypes 参数类型
 * @return
 */
public fun Executable.matches(name:String, paramTypes:List<Class<*>> = emptyList()):Boolean{
    // 1 匹配名称
    if(name != this.name)
        return false

    // 2 匹配参数
    // 2.1 匹配参数个数
    if(paramTypes.size != this.parameterTypes.size)
        return false;

    // 2.2 匹配参数类型
    for (i in paramTypes.indices){
        var targetType = this.parameterTypes[i]
        if(targetType is ParameterizedTypeImpl) // 若是泛型类型，则去掉泛型，只保留原始类型
            targetType = targetType.rawType;

        if(paramTypes[i] != targetType)
            return false
    }

    return true;
}

/**
 * 查找方法
 * @param name 方法名
 * @param paramTypes 参数类型
 * @return
 */
public fun Class<*>.findMethod(name:String, paramTypes:List<Class<*>> = emptyList()): Method?{
    var pt = if(paramTypes is MutableList){
        paramTypes
    }else{
        ArrayList<Class<*>>(paramTypes);
    }

    return methods.find {
        it.matches(name, pt);
    }
}

/**
 * 查找构造函数
 * @param paramTypes 参数类型
 * @return
 */
public fun Class<*>.findConstructor(paramTypes:List<Class<*>> = emptyList()): Constructor<*>?{
    return constructors.find {
        it.matches(this.name, paramTypes);
    }
}

/**
 * 查找属性
 * @param name 属性名
 * @return
 */
public fun Class<*>.findField(name:String): Field? {
    return this.fields.find {
        it.name == name;
    }
}

/**
 * 是否静态方法
 */
public val Method.isStatic: Boolean
        get() = Modifier.isStatic(modifiers)

/**
 * 是否抽象类
 */
public val  <T> Class<T>.isAbstract: Boolean
    get() =  Modifier.isAbstract(modifiers)

/**
 * 检查当前类 是否是 指定类的子类
 *
 * @param superClass 父类
 * @return
 */
public fun Class<*>.isSubClass(superClass: Class<*>): Boolean {
    return this != superClass && superClass.isAssignableFrom(this)
}

/**
 * 检查当前类 是否是 指定类的父类
 *
 * @param subClass 子类
 * @return
 */
public fun Class<*>.isSuperClass(subClass: Class<*>): Boolean {
    return this != subClass && this.isAssignableFrom(subClass)
}

/**
 * 获得方法签名
 * @return
 */
public fun Method.getSignature(): String {
    return this.parameterTypes.joinTo(StringBuilder(this.name), ",", "(", ")"){
        it.name
    }.toString().replace("java.lang.", "")
}

/**
 * 获得当前类的方法哈系: <方法签名 to 方法>
 * @return
 */
public fun Class<*>.getMethodMaps(): Map<String, Method> {
    return methods.associate {
        it.getSignature() to it
    }
}