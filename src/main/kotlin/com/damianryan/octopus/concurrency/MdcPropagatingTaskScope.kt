package com.damianryan.octopus.concurrency

import java.util.concurrent.Callable
import java.util.concurrent.StructuredTaskScope
import org.slf4j.MDC

/**
 * A wrapper around [StructuredTaskScope] that propagates the MDC context to forked tasks. Since [AutoCloseable] is an
 * interface, the wrapper can delegate the `close()` method to the underlying [StructuredTaskScope] instance, allowing
 * it to be used in a `use` block (the Kotlin equivalent of Java's try-with-resources) to ensure that the scope is
 * properly closed after use.
 *
 * @property scope underlying [StructuredTaskScope] that manages the tasks.
 * @param T result type of the tasks managed by this scope
 * @param R the type of the result returned by [join]
 */
class MdcPropagatingTaskScope<T, R>(private val scope: StructuredTaskScope<T, R>) : AutoCloseable by scope {

    /**
     * Fork a new task in this scope, propagating the MDC to it. Accepts a task that returns a result of type [A],
     * which must be a subtype of [T]. The forked task will run in the same MDC context as the caller. Returns a
     * [StructuredTaskScope.Subtask] representing the forked task, allowing you to keep the more specific result type
     * [A] while reusing the scope's [T].
     *
     * @param task the task to run
     * @param A the type of the result returned by the task, which must be a subtype of [T]
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
