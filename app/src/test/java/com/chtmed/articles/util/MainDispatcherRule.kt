package com.chtmed.articles.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps Dispatchers.Main for a TestDispatcher for the duration of a test, so
 * `viewModelScope.launch { ... }` runs predictably under test.
 *
 * [testDispatcher] is exposed so test methods can run their body via
 * `runTest(mainDispatcherRule.testDispatcher) { ... }` instead of bare
 * `runTest { }`. Without that, `runTest` would drive its own private
 * TestCoroutineScheduler while this rule's StandardTestDispatcher owns a
 * completely separate one — nothing launched via `viewModelScope`
 * (Dispatchers.Main.immediate) would ever run before a turbine
 * `awaitItem()`, since nobody is pumping the scheduler it's queued on. With
 * a shared scheduler, `runTest`/turbine's suspension points advance it
 * step by step, so sequential `_uiState.update` calls remain individually
 * observable instead of collapsing into one final state.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
