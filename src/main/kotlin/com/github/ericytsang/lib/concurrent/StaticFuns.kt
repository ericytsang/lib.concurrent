package com.github.ericytsang.lib.concurrent

import java.io.Serializable
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
fun sleep(timeoutMillis:Long):SleepResult
{
    var wasInterrupted:Boolean = false
    val sleepDuration = measureTimeMillis()
    {
        wasInterrupted = try
        {
            Thread.sleep(timeoutMillis)
            false
        }
        catch (ex:InterruptedException)
        {
            true
        }
    }
    return SleepResult(wasInterrupted,sleepDuration)
}

data class SleepResult(val wasInterrupted:Boolean,val sleepDuration:Long):Serializable
