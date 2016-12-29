package com.github.ericytsang.lib.concurrent

import org.junit.Test
import java.util.concurrent.ExecutionException
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class Tests
{
    @Test
    fun sleepNoInterruptTest()
    {
        assert(measureTimeMillis {sleep(1000)} >= 900)
    }

    @Test
    fun sleepAndInterruptTest()
    {
        val mainThread = Thread.currentThread()
        thread {
            sleep(500)
            mainThread.interrupt()
        }
        assert(measureTimeMillis {sleep(1000)} <= 600)
    }

    @Test
    fun futureExceptionTest()
    {
        val f = future {
            sleep(1000)
            throw RuntimeException()
            Unit
        }
        try
        {
            f.get()
            assert(false)
        }
        catch (ex:ExecutionException)
        {
            // ignore exception
        }
    }

    @Test
    fun futureTest()
    {
        val f = future {
            sleep(1000)
            Unit
        }
        assert(f.get() == Unit)
    }
}
