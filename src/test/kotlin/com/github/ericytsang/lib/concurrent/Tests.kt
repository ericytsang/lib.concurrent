package com.github.ericytsang.lib.concurrent

import org.junit.Test
import java.util.concurrent.ExecutionException
import kotlin.concurrent.thread

class Tests
{
    @Test
    fun sleepNoInterruptTest()
    {
        val result = sleep(1000)
        assert(result.sleepDuration >= 900)
        assert(!result.wasInterrupted)
    }

    @Test
    fun sleepAndInterruptTest()
    {
        val mainThread = Thread.currentThread()
        thread {
            sleep(500)
            mainThread.interrupt()
        }
        val result = sleep(1000)
        assert(result.sleepDuration <= 600)
        assert(result.wasInterrupted)
        assert(!Thread.interrupted())
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
