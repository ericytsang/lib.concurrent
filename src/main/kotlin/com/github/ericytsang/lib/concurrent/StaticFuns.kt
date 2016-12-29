package com.github.ericytsang.lib.concurrent

import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/**
 * returns a future task that has begun execution right away
 */
fun <V> future(
    isDaemon:Boolean = false,
    contextClassLoader:ClassLoader? = null,
    name:String? = null,
    priority:Int = -1,
    block:()->V):Future<V>
{
    val future = FutureTask(block)
    thread(true,isDaemon,contextClassLoader,name,priority,{future.run()})
    return future
}

/**
 * sleeps for the specified number of milliseconds. returns the number of
 * milliseconds that passed while sleeping. does not throw
 * [InterruptedException] is interrupted while sleeping.
 */
fun sleep(timeoutMillis:Long):Long
{
    return measureTimeMillis()
    {
        try
        {
            Thread.sleep(timeoutMillis)
        }
        catch (ex:InterruptedException)
        {
            // ignore exception
        }
    }
}
