import com.github.ericytsang.lib.concurrent.ThreadCreator
import org.junit.Test

/**
 * Created by surpl on 10/21/2016.
 */
class ThreadCreatorTest
{
    var prepareStartCount = 0

    var prepareEndCount = 0

    var workStartCount = 0

    var workEndCount = 0

    val longPrepareTask = object:ThreadCreator.Task<Unit>
    {
        override fun prepare()
        {
            synchronized(this@ThreadCreatorTest) {prepareStartCount++}
            println("prepareStartCount++")
            waitLong()
            synchronized(this@ThreadCreatorTest) {prepareEndCount++}
            println("prepareEndCount++")
        }

        override fun work(prepared:Unit)
        {
            synchronized(this@ThreadCreatorTest) {workStartCount++}
            println("workStartCount++")
            waitShort()
            synchronized(this@ThreadCreatorTest) {workEndCount++}
            println("workEndCount++")
        }
    }

    val shortPrepareTask = object:ThreadCreator.Task<Unit>
    {
        override fun prepare()
        {
            synchronized(this@ThreadCreatorTest) {prepareStartCount++}
            println("prepareStartCount++")
            waitShort()
            synchronized(this@ThreadCreatorTest) {prepareEndCount++}
            println("prepareEndCount++")
        }

        override fun work(prepared:Unit)
        {
            synchronized(this@ThreadCreatorTest) {workStartCount++}
            println("workStartCount++")
            waitShort()
            synchronized(this@ThreadCreatorTest) {workEndCount++}
            println("workEndCount++")
        }
    }

    fun waitHalfShort()
    {
        Thread.sleep(1000)
    }

    fun waitShort()
    {
        Thread.sleep(2000)
    }

    fun waitLong()
    {
        Thread.sleep(4000)
    }

    /**
     * tests that the ThreadCreator can close.
     */
    @Test
    fun shutdown1()
    {
        ThreadCreator({shortPrepareTask}).close()
    }

    /**
     * tests that the ThreadCreator can close.
     */
    @Test
    fun shutdownMidPrepare1()
    {
        val tc = ThreadCreator({shortPrepareTask},4)
        waitHalfShort()
        tc.close()
        assert(prepareStartCount == 4) {"prepareStartCount: $prepareStartCount"}
        assert(prepareEndCount == 0) {"prepareEndCount: $prepareEndCount"}
        assert(workStartCount == 0) {"workStartCount: $workStartCount"}
        assert(workEndCount == 0) {"workEndCount: $workEndCount"}
    }

    /**
     * tests that the ThreadCreator can close.
     */
    @Test
    fun shutdownMidPrepare2()
    {
        val it = listOf(longPrepareTask,longPrepareTask,shortPrepareTask,shortPrepareTask,shortPrepareTask,shortPrepareTask,shortPrepareTask).iterator()
        val tc = ThreadCreator({it.next()},4)
        waitShort()
        waitHalfShort()
        tc.close()
        assert(prepareStartCount == 6) {"prepareStartCount: $prepareStartCount"}
        assert(prepareEndCount == 2) {"prepareEndCount: $prepareEndCount"}
        assert(workStartCount == 2) {"workStartCount: $workStartCount"}
        assert(workEndCount == 2) {"workEndCount: $workEndCount"}
    }
}
