package com.toggl.komposable.store

import com.toggl.komposable.common.StoreCoroutineTest
import com.toggl.komposable.common.TestAction
import com.toggl.komposable.common.TestException
import com.toggl.komposable.common.TestExceptionSubscription
import com.toggl.komposable.common.createTestStore
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class StoreExceptionHandlingTests : StoreCoroutineTest() {

    @Test
    fun `reduce exception should be handled`() = runTest {
        testStore.send(TestAction.ThrowExceptionAction)
        runCurrent()
        assertTrue { testExceptionHandler.invocations == listOf(TestException) }
    }

    @Test
    fun `effect exception should be handled`() = runTest {
        testStore.send(TestAction.StartExceptionThrowingEffectAction)
        runCurrent()
        assertTrue { testExceptionHandler.invocations == listOf(TestException) }
    }


    @Test
    fun `subscription exception should be handled`() = runTest {
        val subscription = TestExceptionSubscription()

        assertFails {
            createTestStore(subscription = subscription, defaultExceptionHandler = testExceptionHandler)
        }
        runCurrent()
        assertTrue { testExceptionHandler.invocations.isNotEmpty() }
    }
}
