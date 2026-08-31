package com.damianryan.octopus.concurrency

import java.util.concurrent.Callable
import java.util.concurrent.StructuredTaskScope
import org.slf4j.MDC

/**
 * A wrapper around [StructuredTaskScope] that propagates the MDC context to forked tasks.
 *
 * @property scope underlying [StructuredTaskScope] that manages the tasks.
 * @param T result type of the tasks managed by this scope
 * @param R the type of the result returned by [join]
 */
class MdcPropagatingTaskScope<T, R>(private val scope: StructuredTaskScope<T, R>) : AutoCloseable by scope {

    /**
     * Fork a new task in this scope, propagating the MDC to it.
     *
     * @param task the task to run
     * @return a [StructuredTaskScope.Subtask] representing the forked task
     */
    fun <A : T> fork(task: () -> A): StructuredTaskScope.Subtask<A> {
        val mdcMap = MDC.getCopyOfContextMap()
        return scope.fork(
            Callable {
                MDC.setContextMap(mdcMap)
                task()
            })
    }

    /**
     * Join all forked tasks in this scope, waiting for their completion and returning the result.
     *
     * @return the result of the joined tasks
     */
    fun join(): R = scope.join()
}
