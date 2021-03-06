package net.jkcode.jkmvc.tests

import net.jkcode.jkmvc.common.*
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.serialize.ISerializer
import net.jkcode.jkmvc.tests.entity.MessageEntity
import net.jkcode.jkmvc.tests.model.MessageModel
import org.junit.Test

class EntityTests{

    @Test
    fun testEntity(){
        val msg = buildEntity()
        println(msg)
    }

    private fun buildEntity(): MessageEntity {
        val msg = MessageEntity()
        msg.fromUid = randomInt(10)
        msg.toUid = randomInt(10)
        msg.content = "hello entity"
        return msg
    }

    private fun buildModel(): MessageModel {
        var msg = MessageModel()
        msg.fromUid = randomInt(10)
        msg.toUid = randomInt(10)
        msg.content = "hello orm"
        return msg
    }

    @Test
    fun testModelPersist(){
        var msg = MessageModel()
        msg.fromUid = randomInt(10)
        msg.toUid = randomInt(10)
        msg.content = "hello orm"
        msg.create()
        val id = msg.id
        println("create: " + msg)

        msg = MessageModel.queryBuilder().where("id", id).findModel<MessageModel>()!!
        println("find: " + msg)

        msg.content = "reply orm"
        msg.update()
        println("update: " + msg)

//        msg.delete()
//        println("delete: " + id)
    }

    /**
     * Orm与db相关, 尽量不使用 ISerializer 来序列化, 只序列化OrmEntity就好
     */
    @Test
    fun testModelSerialize(){
        var msg = buildModel()
        // toString()
        println(msg.toString())

        // toMap()
        println(msg.toMap())
    }

    @Test
    fun testEntitySerialize(){
        var msg: MessageEntity = buildEntity()
        println(msg)

        val instance = ISerializer.instance("fst")
        val bs = instance.serialize(msg)
        if(bs != null) {
            val msg2: MessageEntity = instance.unserialize(bs!!) as MessageEntity
            println(msg2)
            println("" + msg2.fromUid + " => " + msg2.toUid + " : " + msg2.content)
        }
    }

    @Test
    fun testModel2Entity(){
        val entity = buildEntity()

        // 实体转模型
        val orm = MessageModel()
        orm.fromEntity(entity)
        orm.save()

        // 模型转实体
        val entity2: MessageEntity = orm.toEntity()
        println(orm)
    }

    /**
     * 测试实体查询
     */
    @Test
    fun testFindEntity(){
        val entities = MessageModel.queryBuilder().findEntity<MessageModel, MessageEntity>()
        println(entities)
    }

    /**
     * 测试实现接口的代理
     */
    @Test
    fun testInterfaceDelegate(){
        val fs = MessageModel::class.java.getInterfaceDelegateFields()
        println(fs)

        val f = MessageModel::class.java.getInterfaceDelegateField(IOrm::class.java)
        println(f)

        val model = MessageModel()
        val dele = model.getInterfaceDelegate(IOrm::class.java)
        println(dele)
    }

    /**
     * 测试实现属性读写的代理
     */
    @Test
    fun testPropDelegate(){
        val fs = MessageEntity::class.java.getPropDelegateFields()
        println(fs)

        val f = MessageEntity::class.java.getPropoDelegateField("id")
        println(f)

        val model = MessageEntity()
        val dele = model.getPropDelegate("id")
        println(dele)
    }

}