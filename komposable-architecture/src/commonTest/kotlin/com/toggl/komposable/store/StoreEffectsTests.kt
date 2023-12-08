package com.toggl.komposable.store

import com.toggl.komposable.architecture.Effect
import com.toggl.komposable.common.StoreCoroutineTest
import com.toggl.komposable.common.TestAction
import com.toggl.komposable.common.TestEffect
import com.toggl.komposable.extensions.cancel
import com.toggl.komposable.extensions.cancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class StoreEffectsTests : StoreCoroutineTest() {

    @Test
    fun `multiple effects can continue sending actions simultaneously`() = runTest {
        val effect1Flow = MutableStateFlow(TestAction.ChangeTestProperty("0"))
        val effect1 = Effect {
            effect1Flow
        }

        val effect2Flow = MutableStateFlow(TestAction.ChangeTestProperty("10"))
        val effect2 = Effect {
            effect2Flow
        }

        testStore.send(TestAction.StartEffectAction(effect1))
        runCurrent()
        effect1Flow.value = TestAction.ChangeTestProperty("1")
        runCurrent()
        effect1Flow.value = TestAction.ChangeTestProperty("2")
        runCurrent()
        testStore.send(TestAction.StartEffectAction(effect2))
        runCurrent()
        effect2Flow.value = TestAction.ChangeTestProperty("11")
        runCurrent()
        effect1Flow.value = TestAction.ChangeTestProperty("3")
        runCurrent()
        effect2Flow.value = TestAction.ChangeTestProperty("12")
        runCurrent()

        assertTrue { testReducer.invocations.map { it.second } == listOf(
            TestAction.StartEffectAction(effect1),
            TestAction.ChangeTestProperty("0"),
            TestAction.ChangeTestProperty("1"),
            TestAction.ChangeTestProperty("2"),
            TestAction.StartEffectAction(effect2),
            TestAction.ChangeTestProperty("10"),
            TestAction.ChangeTestProperty("11"),
            TestAction.ChangeTestProperty("3"),
            TestAction.ChangeTestProperty("12"),
        ) }
    }

    @Test
    fun `effects are cancelled correctly in flight`() = runTest {
        val effect1Flow = MutableStateFlow(TestAction.ChangeTestProperty("0"))
        val effect1 = Effect {
            effect1Flow
        }.cancellable("effect", cancelInFlight = false)

        val effect2Flow = MutableStateFlow(TestAction.ChangeTestProperty("10"))
        val effect2 = Effect {
            effect2Flow
        }.cancellable("effect", cancelInFlight = true)

        testStore.send(TestAction.StartEffectAction(effect1))
        runCurrent()
        effect1Flow.value = TestAction.ChangeTestProperty("1")
        runCurrent()
        testStore.send(TestAction.StartEffectAction(effect2))
        runCurrent()
        effect1Flow.value = TestAction.ChangeTestProperty("2") // should be cancelled by now
        runCurrent()
        effect2Flow.value = TestAction.ChangeTestProperty("11")
        runCurrent()

        assertTrue { testReducer.invocations.map { it.second } == listOf(
            TestAction.StartEffectAction(effect1),
            TestAction.ChangeTestProperty("0"),
            TestAction.ChangeTestProperty("1"),
            TestAction.StartEffectAction(effect2),
            TestAction.ChangeTestProperty("10"),
            TestAction.ChangeTestProperty("11"),
        ) }
    }

    @Test
    fun `effects are cancelled correctly with cancel effect`() = runTest {
        val effect1Flow = MutableStateFlow(TestAction.ChangeTestProperty("0"))
        val effect1 = Effect {
            effect1Flow
        }.cancellable("effect1", cancelInFlight = false)
        val cancelEffect1 = Effect.cancel("effect1")

        val effect2Flow = MutableStateFlow(TestAction.ChangeTestProperty("10"))
        val effect2 = Effect {
            effect2Flow
        }.cancellable("effect2", cancelInFlight = false)
        val cancelEffect2 = Effect.cancel("effect2")

        testStore.send(TestAction.StartEffectAction(effect1))
        runCurrent()
        effect1Flow.value = TestAction.ChangeTestProperty("1")
        runCurrent()
        testStore.send(TestAction.StartEffectAction(effect2))
        runCurrent()
        effect2Flow.value = TestAction.ChangeTestProperty("11")
        runCurrent()
        effect1Flow.value = TestAction.ChangeTestProperty("3")
        runCurrent()
        testStore.send(TestAction.StartEffectAction(cancelEffect1))
        runCurrent()
        effect1Flow.value = TestAction.ChangeTestProperty("4") // should be cancelled by now
        runCurrent()
        effect2Flow.value = TestAction.ChangeTestProperty("12")
        runCurrent()
        testStore.send(TestAction.StartEffectAction(cancelEffect2))
        runCurrent()
        effect2Flow.value = TestAction.ChangeTestProperty("13") // should also be cancelled by now
        runCurrent()

        assertTrue {
            testReducer.invocations.map { it.second } == listOf(
                TestAction.StartEffectAction(effect1),
                TestAction.ChangeTestProperty("0"),
                TestAction.ChangeTestProperty("1"),
                TestAction.StartEffectAction(effect2),
                TestAction.ChangeTestProperty("10"),
                TestAction.ChangeTestProperty("11"),
                TestAction.ChangeTestProperty("3"),
                TestAction.StartEffectAction(cancelEffect1),
                TestAction.ChangeTestProperty("12"),
                TestAction.StartEffectAction(cancelEffect2),
            )
        }
    }

    @Test
    fun `all actions are reduced before any effect gets processed`() = runTest {
        val uselessEffect = TestEffect(TestAction.DoNothingFromEffectAction)
        val clearPropertyEffect = TestEffect(TestAction.ClearTestPropertyFromEffect)

        val startUselessEffectAction = TestAction.StartEffectAction(uselessEffect)
        val startClearPropertyEffectAction = TestAction.StartEffectAction(clearPropertyEffect)

        val changeTestPropertyAction = TestAction.ChangeTestProperty("123")
        val addTestPropertyAction = TestAction.AddToTestProperty("4")

        testStore.send(
            listOf(
                TestAction.DoNothingAction,
                changeTestPropertyAction,
                addTestPropertyAction,
                startUselessEffectAction,
                startClearPropertyEffectAction,
                TestAction.DoNothingAction,
            ),
        )

        runCurrent()

        assertTrue { testReducer.invocations.take(6).map { it.second } == listOf(
            TestAction.DoNothingAction,
            changeTestPropertyAction,
            addTestPropertyAction,
            startUselessEffectAction,
            startClearPropertyEffectAction,
            TestAction.DoNothingAction,
        ) }

        // second: execute effects
        uselessEffect.run()
        clearPropertyEffect.run()

        assertTrue { testReducer.invocations.drop(6).map { it.second } == listOf(
            TestAction.ClearTestPropertyFromEffect,
            TestAction.DoNothingFromEffectAction,
        ) }
     }
}
