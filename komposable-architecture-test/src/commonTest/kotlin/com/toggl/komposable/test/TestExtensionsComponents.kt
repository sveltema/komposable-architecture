package com.toggl.komposable.test

import com.toggl.komposable.architecture.Effect
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class TestState(val title: String)

enum class TestAction { FirstAction, SecondAction }

class TestEffect : Effect<TestAction> {
    override fun run(): Flow<TestAction> = flowOf(TestAction.SecondAction)
}

class TestReducer : Reducer<TestState, TestAction> {

    companion object {
        val returnedEffects = TestEffect()
    }

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
