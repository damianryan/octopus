package com.damianryan.octopus.concurrency

import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.StructuredTaskScope.Joiner

/**
 * Create a new [MdcPropagatingTaskScope] that can be used to fork subtasks that return results of type [T] and join
 * them to produce a result of type [R]. The scope's `join()` method waits for all tasks to succeed or any subtask to
 * fail. The `join()` method returns `null` if all subtasks succeed, or throws an exception if any subtask fails.
 *
 * Example usage:
 * ```kotlin
 * val productCodes = awaitAllSuccessfulOrThrow {
 *     val electricityAgreements = fork { octopus.electricityAgreements() }
 *     val gasAgreements = fork { octopus.gasAgreements() }
 *     join()
 *     val codes = electricityAgreements.get().map { it.productCode }.toMutableSet()
 *     codes.addAll(gasAgreements.get().map { it.productCode })
 *     codes.toSet()
 * }
 * ```
 *
 * @param block the block of code to execute within the task scope
 * @param T result type of the tasks managed by this scope
 * @param R the type of the result returned by joining the scope
 * @throws StructuredTaskScope.FailedException if any subtask fails, with the first encountered exception as the cause
 * @return the result of joining the scope.
 */
inline fun <T, R> awaitAllSuccessfulOrThrow(block: MdcPropagatingTaskScope<T, Void>.() -> R): R =
    MdcPropagatingTaskScope(StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow<T>())).use { it.block() }


