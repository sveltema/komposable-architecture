package com.toggl.komposable.test

import com.toggl.komposable.architecture.Effect
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

data class TestState(val title: String)
enum class TestAction { FirstAction, SecondAction }
class TestEffect : Effect<TestAction> {
    override fun run(): Flow<TestAction> = flowOf(TestAction.SecondAction)
}

private val returnedEffects = TestEffect()

class TestReducer : Reducer<TestState, TestAction> {

    val invocations: MutableList<Pair<TestState, TestAction>> = mutableListOf()

    override fun reduce(state: TestState, action: TestAction): ReduceResult<TestState, TestAction> {
        invocations.add(state to action)
        return ReduceResult(state, returnedEffects)
    }
}

class NoEffectTestReducer : Reducer<TestState, TestAction> {

    val invocations: MutableList<Pair<TestState, TestAction>> = mutableListOf()

    override fun reduce(state: TestState, action: TestAction): ReduceResult<TestState, TestAction> {
        invocations.add(state to action)
        return ReduceResult(state, NoEffect)
    }
}

class ExceptionTestReducer<Ex : Exception>(private val exception: Ex) : Reducer<TestState, TestAction> {

    val invocations: MutableList<Pair<TestState, TestAction>> = mutableListOf()

    override fun reduce(state: TestState, action: TestAction): ReduceResult<TestState, TestAction> {
        invocations.add(state to action)
        throw exception
    }
}

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
        assertEquals(returnedEffects, testEffect)
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
        assertEquals(returnedEffects, testEffect)
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
        assertFails {
            reducer.testReduceNoEffect(initState, inputAction)
        }
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
        assertFails {
            reducer.testReduceNoOp(initState, inputAction)
        }
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
