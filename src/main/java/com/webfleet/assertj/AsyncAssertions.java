package com.webfleet.assertj;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;


/**
 * Entry point for building asynchronous assertions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AsyncAssertions
{
    /**
     * Builds asynchronous assertion with given timeout.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMost(Duration.ofSeconds(5)).untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * }</pre>
     * See {@link AsyncAssert} for more details.
     *
     * @param timeout timeout for the assertions
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMost(@NonNull final Duration timeout)
    {
        return new AsyncAssertImpl(SystemTime.UTC, AsyncAssertAwaitConfig.withTimeout(timeout));
    }

    /**
     * Builds asynchronous assertion with given timeout value and time unit.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMost(5, SECONDS).untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * }</pre>
     * See {@link AsyncAssert} for more details.
     *
     * @param timeout timeout value for the assertion
     * @param timeUnit timeout unit
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMost(final long timeout, @NonNull final TimeUnit timeUnit)
    {
        return awaitAtMost(Duration.ofMillis(timeUnit.toMillis(timeout)));
    }

    /**
     * Builds asynchronous assertion with 1 second timeout.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMostOneSecond().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * }</pre>
     * See {@link AsyncAssert} for more details.
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostOneSecond()
    {
        return awaitAtMost(Duration.ofSeconds(1));
    }

    /**
     * Builds asynchronous assertion with 2 seconds timeout.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMostTwoSeconds().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * }</pre>
     * See {@link AsyncAssert} for more details.
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostTwoSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(2));
    }

    /**
     * Builds asynchronous assertion with 5 seconds timeout.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMostFiveSeconds().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * }</pre>
     * See {@link AsyncAssert} for more details.
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostFiveSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(5));
    }

    /**
     * Builds asynchronous assertion with 15 seconds timeout.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMostFifteenSeconds().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * }</pre>
     * See {@link AsyncAssert} for more details.
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostFifteenSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(15));
    }

    /**
     * Builds asynchronous assertion with 30 seconds timeout.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMostThirtySeconds().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     * });
     * }</pre>
     * See {@link AsyncAssert} for more details.
     *
     * @return {@link AsyncAssert}
     */
    public static AsyncAssert awaitAtMostThirtySeconds()
    {
        return awaitAtMost(Duration.ofSeconds(30));
    }
}
