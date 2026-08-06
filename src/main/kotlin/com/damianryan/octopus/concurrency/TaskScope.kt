package com.damianryan.octopus.concurrency

import org.slf4j.MDC
import java.util.concurrent.Callable
import java.util.concurrent.StructuredTaskScope

class TaskScope<T, R>(private val scope: StructuredTaskScope<T, R>) : AutoCloseable by scope {

    fun <A : T> fork(fn: () -> A): StructuredTaskScope.Subtask<A> {
        val mdcMap = MDC.getCopyOfContextMap()
        return scope.fork(Callable {
            MDC.setContextMap(mdcMap)
            fn()
        })
    }

    fun join(): R = scope.join()
}

inline fun <T, R> taskScope(block: TaskScope<T, Void>.() -> R): R =
    TaskScope(StructuredTaskScope.open<T>()).use { it.block() }