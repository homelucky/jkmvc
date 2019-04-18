package net.jkcode.jkmvc.common

import io.netty.util.HashedWheelTimer
import net.jkcode.jkmvc.closing.ClosingOnShutdown
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

/**
 * 公共的毫秒级定时器
 *   HashedWheelTimer 是单线程的, 因此每个定时任务执行耗时不能太长, 如果有耗时任务, 则扔到其他线程池(如ForkJoinPool.commonPool())中处理
 */
public val CommonMilliTimer by lazy{
    HashedWheelTimer(1, TimeUnit.MILLISECONDS, 256 /* 2的次幂 */)
}

/**
 * 公共的秒级定时器
 *   HashedWheelTimer 是单线程的, 因此每个定时任务执行耗时不能太长, 如果有耗时任务, 则扔到其他线程池(如ForkJoinPool.commonPool())中处理
 */
public val CommonSecondTimer by lazy{
    HashedWheelTimer(200, TimeUnit.MILLISECONDS, 64 /* 2的次幂 */)
}

/**
 * 公共的线程池
 *   执行任务时要处理好异常
 */
public val CommonThreadPool: ExecutorService = ForkJoinPool.commonPool()
//public val CommonThreadPool: ExecutorService = Executors.newFixedThreadPool(8)

/**
 * 关闭定时器与线程池
 */
public val closer = object: ClosingOnShutdown(){
    override fun close() {
        // 1 关闭定时器
        CommonMilliTimer.stop()
        CommonSecondTimer.stop()

        // 2 关闭线程池
        println("-- 关闭线程池, 并等待任务完成 --")
        // 停止工作线程: 不接收新任务
        CommonThreadPool.shutdown()

        // 等待任务完成
        CommonThreadPool.awaitTermination(1, TimeUnit.DAYS) // 等长一点 = 死等
    }

}

/**
 * 单个线程的启动+等待
 * @return
 */
public fun Thread.startAndJoin(): Thread {
    start()
    join()
    return this
}

/**
 * 多个个线程的启动+等待
 * @return
 */
public fun List<Thread>.startAndJoin(): List<Thread> {
    for(t in this)
        t.start()
    for(t in this)
        t.join()
    return this
}

/**
 * 创建线程
 * @param num 线程数
 * @param runnable 线程体
 * @return
 */
public fun makeThreads(num: Int, runnable: () -> Unit): List<Thread> {
    return (0 until num).map { Thread(runnable, "thread_$it") }.startAndJoin()
}