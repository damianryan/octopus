package com.damianryan.octopus.concurrency

import java.util.concurrent.StructuredTaskScope

/**
 * Create a new [MdcPropagatingTaskScope] and execute the given block within it.
 *
 * @param block the block of code to execute within the task scope
 * @param T result type of the tasks managed by this scope
 * @param R the type of the result returned by joining the scope
 * @return the result of joining the scope.
 */
inline fun <T, R> awaitAllSuccessfulOrThrow(block: MdcPropagatingTaskScope<T, Void>.() -> R): R =
    MdcPropagatingTaskScope(StructuredTaskScope.open<T>()).use { it.block() }
