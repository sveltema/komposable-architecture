package com.toggl.komposable.reducer.pullback

import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.common.LocalTestReducer
import com.toggl.komposable.common.LocalTestState
import com.toggl.komposable.common.TestAction
import com.toggl.komposable.common.TestState
import com.toggl.komposable.extensions.optionalPullback
import com.toggl.komposable.test.testReduce
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OptionalPullbackTests : BasePullbackTests() {
    override val localReducer = LocalTestReducer()

    override val pulledBackReducer: Reducer<TestState, TestAction> = localReducer.optionalPullback(
        mapToLocalState = { if (it.testIntProperty != 0) LocalTestState(it.testIntProperty) else null },
        mapToLocalAction = { if (it is TestAction.LocalActionWrapper) it.action else null },
        mapToGlobalState = { globalState, localState ->
            globalState.copy(testIntProperty = localState?.testIntProperty ?: 0)
        },
        mapToGlobalAction = { TestAction.LocalActionWrapper(it) },
    )

    @Test
    fun `local reducer should not be called when mapToLocalState returns null`() = runTest {
        val globalState = TestState("", 0) // 0 will cause the mapToLocalState to return null
        val action = TestAction.ChangeTestProperty("")

        pulledBackReducer.testReduce(globalState, action) { state, effect ->
            state shouldBe globalState
            effect shouldBe NoEffect
        }

        assertTrue { localReducer.invocations.isEmpty() }
    }
}
