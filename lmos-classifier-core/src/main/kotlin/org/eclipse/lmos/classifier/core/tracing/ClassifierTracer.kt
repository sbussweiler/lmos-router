// SPDX-FileCopyrightText: 2026 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

package org.eclipse.lmos.classifier.core.tracing

/**
 * Tracer for Classifier.
 */
interface ClassifierTracer {
    suspend fun <T> withSpan(
        name: String,
        attributes: Map<String, String> = emptyMap(),
        fn: suspend (Tags) -> T,
    ): T

    suspend fun addToSpan(
        key: String,
        value: String,
    )
}

interface Tags {
    fun tag(
        key: String,
        value: String,
    )

    fun tag(
        key: String,
        value: Long,
    )

    fun tag(
        key: String,
        value: Boolean,
    )

    fun error(ex: Throwable)
}

object NoopTags : Tags {
    override fun tag(
        key: String,
        value: String,
    ) {
        // no-op
    }

    override fun tag(
        key: String,
        value: Long,
    ) {
        // no-op
    }

    override fun tag(
        key: String,
        value: Boolean,
    ) {
        // no-op
    }

    override fun error(ex: Throwable) {
        // no-op
    }
}

class NoopClassifierTracer : ClassifierTracer {
    override suspend fun <T> withSpan(
        name: String,
        attributes: Map<String, String>,
        fn: suspend (Tags) -> T,
    ): T = fn(NoopTags)

    override suspend fun addToSpan(
        key: String,
        value: String,
    ) {
        // no-op
    }
}
