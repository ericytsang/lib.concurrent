package com.github.ericytsang.lib.concurrent

import java.io.Closeable
import java.util.LinkedHashSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

/**
 * Created by surpl on 10/21/2016.
 */
class ThreadCreator<T>(taskFactory:()->Task<T>,maxWaitingThreadCount:Int = ThreadCreator.DEFAULT_MAX_WAITING_THREAD_COUNT):Closeable
{
    companion object
    {
        const val DEFAULT_MAX_WAITING_THREAD_COUNT = 8
    }

    /**
     * set of all alive threads.
     */
    private val liveWorkerThreads:MutableSet<Thread> = LinkedHashSet()

    /**
     * set of all threads that have yet to return from their call to prepare.
     */
    private val unpreparedWorkerThreads:MutableSet<Thread> = LinkedHashSet()

    /**
     * makes sure only maxWaitingThreadCount are being prepared at a time.
     */
    private val numWaitingThreadsToCreate = Semaphore(maxWaitingThreadCount)

    /**
     * the thread that creates worker threads.
     */
    private val threadCreator = thread()
    {
        while (true)
        {
            // decrement the count because we are making a thread
            try
            {
                numWaitingThreadsToCreate.acquire()
            }
            catch (ex:InterruptedException)
            {
                return@thread
            }

            // make the thread
            val unblockOnAlive = CountDownLatch(1)
            thread()worker@
            {
                try
                {
                    // book keeping
                    liveWorkerThreads.add(Thread.currentThread())
                    unpreparedWorkerThreads.add(Thread.currentThread())

                    // book keeping over, allow thread creator to continue to next iteration
                    unblockOnAlive.countDown()

                    // get the task...
                    val task = taskFactory()

                    // prepare the thread for the task...
                    val prepared = try
                    {
                        val prepared = task.prepare()
                        if (Thread.interrupted()) throw InterruptedException()
                        prepared
                    }
                    catch (ex:InterruptedException)
                    {
                        return@worker
                    }
                    finally
                    {
                        unpreparedWorkerThreads.remove(Thread.currentThread())
                        numWaitingThreadsToCreate.release()
                    }

                    // execute the task...
                    task.work(prepared)
                }
                finally
                {
                    liveWorkerThreads.remove(Thread.currentThread())
                }
            }

            unblockOnAlive.await()
        }
    }

    /**
     * shuts down the thread creator nicely. the thread creator will interrupt
     * all preparing threads, and waits for all preparing and working threads to
     * finish before this method returns.
     */
    override fun close() = synchronized(this)
    {
        threadCreator.interrupt()
        threadCreator.join()
        unpreparedWorkerThreads.toSet().forEach {it.interrupt()}
        liveWorkerThreads.toSet().forEach {it.join()}
    }

    interface Task<Prepared>
    {
        fun prepare():Prepared
        fun work(prepared:Prepared)
    }
}
