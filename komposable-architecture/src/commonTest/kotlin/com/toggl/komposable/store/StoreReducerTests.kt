package com.toggl.komposable.store

import com.toggl.komposable.common.StoreCoroutineTest
import com.toggl.komposable.common.TestAction
import com.toggl.komposable.common.TestEffect
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class StoreReducerTests : StoreCoroutineTest() {

    @Test
    fun `reducer shouldn't be called if the list of sent actions is empty`() = runTest {
        testStore.send(emptyList())
        runCurrent()
        assertTrue { testReducer.invocations.isEmpty() }
    }

    @Test
    fun `reducer should be called exactly once if one action is sent`() = runTest {
        testStore.send(TestAction.DoNothingAction)
        runCurrent()
        assertTrue {
            testReducer.invocations.map { it.second }.contains(TestAction.DoNothingAction)
        }
    }

    @Test
    fun `reducer should be called for each action sent in order in which they were provided`() =
        runTest {
            val startUselessEffectAction =
                TestAction.StartEffectAction(TestEffect(TestAction.DoNothingFromEffectAction))
            testStore.send(
                listOf(
                    TestAction.DoNothingAction,
                    startUselessEffectAction,
                    TestAction.DoNothingAction,
                ),
            )

            runCurrent()

            assertTrue {
                testReducer.invocations.map { it.second }.containsAll(
                    listOf(
                        TestAction.DoNothingAction,
                        startUselessEffectAction,
                        TestAction.DoNothingAction,
                        TestAction.DoNothingFromEffectAction,
                    ),
                )
            }
        }

    @Test
    fun `effects with multiple actions are processed correctly`() = runTest {

        val flowEffect = TestEffect(
            TestAction.ClearTestPropertyFromEffect,
            TestAction.ChangeTestProperty("123"),
            TestAction.AddToTestProperty("4"),
        )
        val startFlowEffectAction = TestAction.StartEffectAction(flowEffect)

        testStore.send(
            listOf(
                TestAction.DoNothingAction,
                startFlowEffectAction,
            ),
        )

        runCurrent()

        assertTrue {
            testReducer.invocations.map { it.second }.containsAll(
                listOf(
                    // first: reduce sent actions
                    TestAction.DoNothingAction,
                    startFlowEffectAction,

                    // second: reduce action coming from effect
                    TestAction.ClearTestPropertyFromEffect,
                    TestAction.ChangeTestProperty("123"),
                    TestAction.AddToTestProperty("4"),
                ),
            )
        }
    }
}
