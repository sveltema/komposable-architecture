package com.toggl.komposable.common

import com.toggl.komposable.architecture.Store
import com.toggl.komposable.scope.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class StoreCoroutineTest {
    private val testDispatcher = StandardTestDispatcher()
    val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, testDispatcher)
    val testCoroutineScope = TestScope(testDispatcher)
    lateinit var testStore: Store<TestState, TestAction>
    lateinit var testReducer: TestReducer
    lateinit var testSubscription: TestSubscription
    lateinit var testExceptionHandler: TestStoreExceptionHandler


    @BeforeTest
    open fun beforeTest() {
        Dispatchers.setMain(testDispatcher)
        testReducer = TestReducer()
        testSubscription = TestSubscription()
        testExceptionHandler = TestStoreExceptionHandler()
        testStore =
            createTestStore(
                reducer = testReducer,
                subscription = testSubscription,
                defaultExceptionHandler = testExceptionHandler,
            )
    }

    @AfterTest
    open fun afterTest() {
        Dispatchers.resetMain()
        testDispatcher.scheduler.runCurrent()
    }
}
