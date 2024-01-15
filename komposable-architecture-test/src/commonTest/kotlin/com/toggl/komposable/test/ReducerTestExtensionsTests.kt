package com.toggl.komposable.test

import com.toggl.komposable.architecture.Effect
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ReducerTestExtensionsTests {

    private val initState = TestState("")
    private val inputAction = TestAction.FirstAction

    @Test
    fun `testReduce calls the right methods`() = runTest {
        var testState: TestState? = null
        var testEffect: Effect<TestAction>? = null
        val testCase: suspend (TestState, Effect<TestAction>) -> Unit = { state: TestState, effect: Effect<TestAction> ->
            testState = state
            testEffect = effect
        }

        val reducer = TestReducer()

        reducer.testReduce(initState, inputAction, testCase)

        assertTrue { reducer.invocations == listOf(initState to inputAction) }
        assertEquals(initState, testState)
        assertEquals(TestReducer.returnedEffects, testEffect)
    }

    @Test
    fun `testReduceState calls the right methods`() = runTest {
        var testState: TestState? = null
        val testCase: suspend (TestState) -> Unit = { state: TestState ->
            testState = state
        }
        val reducer = TestReducer()

        reducer.testReduceState(initState, inputAction, testCase)

        assertTrue { reducer.invocations == listOf(initState to inputAction) }
        assertEquals(initState, testState)
    }


    @Test
    fun `testReduceEffects calls the right methods`() = runTest {
        var testEffect: Effect<TestAction>? = null
        val testCase: suspend (Effect<TestAction>) -> Unit = { effect: Effect<TestAction> ->
            testEffect = effect
        }

        val reducer = TestReducer()

        reducer.testReduceEffect(initState, inputAction, testCase)

        assertTrue { reducer.invocations == listOf(initState to inputAction) }
        assertEquals(TestReducer.returnedEffects, testEffect)
    }


    @Test
    fun `testReduceNoEffects calls the right methods`() = runTest {
        val reducer = NoEffectTestReducer()
        reducer.testReduceNoEffect(initState, inputAction)

        assertTrue { reducer.invocations == listOf(initState to inputAction) }
    }


    @Test
    fun `testReduceNoEffects should fail when some effects are returned`() = runTest {
        val reducer = TestReducer()
        assertFails { reducer.testReduceNoEffect(initState, inputAction) }
    }


    @Test
    fun `testReduceNoOp calls the right methods`() = runTest {
        val reducer = NoEffectTestReducer()
        reducer.testReduceNoOp(initState, inputAction)
        assertTrue { reducer.invocations == listOf(initState to inputAction) }
    }


    @Test
    fun `testReduceNoOp fails when some effects are returned`() = runTest {
        val reducer = TestReducer()
        assertFails { reducer.testReduceNoOp(initState, inputAction) }
    }


    @Test
    fun `testReduceNoOp fails when state has been changed`() = runTest {
        val reducer = TestReducer()
        reducer.reduce(TestState("Changed"), TestAction.FirstAction)
        assertFails { reducer.testReduceNoOp(initState, inputAction) }
    }

    @Test
    fun `testReduceException calls the right methods`() = runTest {
        val reducer = ExceptionTestReducer(IllegalStateException())
        reducer.testReduceException(initState, inputAction, IllegalStateException::class)

        assertTrue { reducer.invocations == listOf(initState to inputAction) }
    }


    @Test
    fun `testReduceException fails when non expected exception is thrown`() = runTest {
        val reducer = ExceptionTestReducer(IllegalArgumentException())
        assertFails { reducer.testReduceException(initState, inputAction, IllegalStateException::class) }
    }


    @Test
    fun `testReduceException fails when no exception is thrown`() = runTest {
        val reducer = NoEffectTestReducer()
        assertFails { reducer.testReduceException(initState, inputAction, IllegalStateException::class) }
    }
}
