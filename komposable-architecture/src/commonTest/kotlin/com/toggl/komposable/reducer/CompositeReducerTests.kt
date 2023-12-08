package com.toggl.komposable.reducer

import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.common.TestAction
import com.toggl.komposable.common.TestState
import com.toggl.komposable.extensions.combine
import com.toggl.komposable.test.testReduceState
import io.kotest.matchers.shouldBe
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import io.mockative.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class CompositeReducerTests {
    private val originalState = TestState("original")
    private val stateFromFirst = TestState("first")
    private val stateFromSecond = TestState("second")

    @Mock
    private val firstReducer = mock(classOf<Reducer<TestState, TestAction>>())

    @Mock
    private val secondReducer = mock(classOf<Reducer<TestState, TestAction>>())

    private val combinedReducer = combine(firstReducer, secondReducer)

    @BeforeTest
    fun beforeTest() {
        every { firstReducer.reduce(originalState, TestAction.DoNothingAction) }
            .returns(ReduceResult(stateFromFirst, NoEffect))
        every { secondReducer.reduce(stateFromFirst, TestAction.DoNothingAction) }
            .returns(ReduceResult(stateFromSecond, NoEffect))
    }

    @Test
    fun `reducers should be called sequentially`() = runTest {
        combinedReducer.testReduceState(originalState, TestAction.DoNothingAction) { state ->
            state shouldBe stateFromSecond
        }

        verify {
            firstReducer.reduce(originalState, TestAction.DoNothingAction)
            secondReducer.reduce(stateFromFirst, TestAction.DoNothingAction)
        }.wasInvoked(1)
    }
}
