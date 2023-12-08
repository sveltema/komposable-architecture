package com.toggl.komposable.store

import com.toggl.komposable.common.StoreCoroutineTest
import com.toggl.komposable.common.TestAction
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class StoreSubscriptionTests : StoreCoroutineTest() {

    @Test
    fun `subscription's method subscribe is called on store creation`() = runTest {
        assertTrue { testSubscription.invocations == 1 }
    }

    @Test
    fun `all actions coming from a subscription are reduced in correct order`() = runTest {
        testSubscription.stateFlow.value = TestAction.DoNothingAction
        runCurrent()
        testSubscription.stateFlow.value = TestAction.DoNothingFromEffectAction
        runCurrent()

        assertTrue {
            testReducer.invocations.map { it.second }.containsAll(
                listOf(
                    TestAction.DoNothingAction,
                    TestAction.DoNothingFromEffectAction,
                ),
            )
        }
    }
}
