package com.webfleet.assertj;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;


/**
 * Entry point for asynchronous assertions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AsyncAssertions
{
    /**
     * Returns evaluator of asynchronous assertions with given timeout.
     * See {@link AsyncAssert} for more details.
     *
     * <pre><code class='java'>
     * awaitAtMost(Duration.ofSeconds(5)).untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * </code></pre>
     *
     * @param timeout timeout for the assertions
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMost(@NonNull final Duration timeout)
    {
        return new AsyncAssertImpl(SystemTime.UTC, AsyncAssertAwaitConfig.withTimeout(timeout));
    }

    /**
     * Returns evaluator of asynchronous assertions with given timeout.
     * See {@link AsyncAssert} for more details.
     *
     * <pre><code class='java'>
     * awaitAtMost(5, SECONDS).untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * </code></pre>
     *
     * @param timeout timeout value for the assertions
     * @param timeUnit timeout unit
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMost(final long timeout, @NonNull final TimeUnit timeUnit)
    {
        return awaitAtMost(Duration.ofMillis(timeUnit.toMillis(timeout)));
    }

    /**
     * Returns evaluator of asynchronous assertions with 1 second timeout.
     * See {@link AsyncAssert} for more details.
     *
     * <pre><code class='java'>
     * awaitAtMostOneSecond().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * </code></pre>
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostOneSecond()
    {
        return awaitAtMost(Duration.ofSeconds(1));
    }

    /**
     * Returns evaluator of asynchronous assertions with 2 seconds timeout.
     * See {@link AsyncAssert} for more details.
     *
     * <pre><code class='java'>
     * awaitAtMostTwoSeconds().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * </code></pre>
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostTwoSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(2));
    }

    /**
     * Returns evaluator of asynchronous assertions with 5 seconds timeout.
     * See {@link AsyncAssert} for more details.
     *
     * <pre><code class='java'>
     * awaitAtMostFiveSeconds().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * </code></pre>
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostFiveSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(5));
    }

    /**
     * Returns evaluator of asynchronous assertions with 15 seconds timeout.
     * See {@link AsyncAssert} for more details.
     *
     * <pre><code class='java'>
     * awaitAtMostFifteenSeconds().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * </code></pre>
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostFifteenSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(15));
    }

    /**
     * Returns evaluator of asynchronous assertions with 30 seconds timeout.
     * See {@link AsyncAssert} for more details.
     *
     * <pre><code class='java'>
     * awaitAtMostThirtySeconds().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * </code></pre>
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostThirtySeconds()
    {
        return awaitAtMost(Duration.ofSeconds(30));
    }
}
