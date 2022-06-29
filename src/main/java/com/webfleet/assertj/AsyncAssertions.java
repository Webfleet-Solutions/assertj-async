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
     * See {@link AsyncAssertionsEvaluator} for more details.
     *
     * @param timeout timeout for the assertions
     * @return {@link AsyncAssertionsEvaluator}
     */
    public static AsyncAssertionsEvaluator awaitAtMost(@NonNull final Duration timeout)
    {
        return new AsyncAssertionsEvaluatorImpl(timeout);
    }

    /**
     * Returns evaluator of asynchronous assertions with given timeout.
     * See {@link AsyncAssertionsEvaluator} for more details.
     *
     * @param timeout timeout value for the assertions
     * @param unit timeout unit
     * @return {@link AsyncAssertionsEvaluator}
     */
    public static AsyncAssertionsEvaluator awaitAtMost(final long timeout, @NonNull final TimeUnit unit)
    {
        return awaitAtMost(Duration.ofMillis(unit.toMillis(timeout)));
    }

    /**
     * Returns evaluator of asynchronous assertions with 1 second timeout.
     * See {@link AsyncAssertionsEvaluator} for more details.
     *
     * @return {@link AsyncAssertionsEvaluator}
     */
    public static AsyncAssertionsEvaluator awaitAtMostOneSecond()
    {
        return awaitAtMost(Duration.ofSeconds(1));
    }

    /**
     * Returns evaluator of asynchronous assertions with 2 seconds timeout.
     * See {@link AsyncAssertionsEvaluator} for more details.
     *
     * @return {@link AsyncAssertionsEvaluator}
     */
    public static AsyncAssertionsEvaluator awaitAtMostTwoSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(2));
    }

    /**
     * Returns evaluator of asynchronous assertions with 5 seconds timeout.
     * See {@link AsyncAssertionsEvaluator} for more details.
     *
     * @return {@link AsyncAssertionsEvaluator}
     */
    public static AsyncAssertionsEvaluator awaitAtMostFiveSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(5));
    }

    /**
     * Returns evaluator of asynchronous assertions with 15 seconds timeout.
     * See {@link AsyncAssertionsEvaluator} for more details.
     *
     * @return {@link AsyncAssertionsEvaluator}
     */
    public static AsyncAssertionsEvaluator awaitAtMostFifteenSeconds()
    {
        return awaitAtMost(Duration.ofSeconds(15));
    }

    /**
     * Returns evaluator of asynchronous assertions with 30 seconds timeout.
     * See {@link AsyncAssertionsEvaluator} for more details.
     *
     * @return {@link AsyncAssertionsEvaluator}
     */
    public static AsyncAssertionsEvaluator awaitAtMostThirtySeconds()
    {
        return awaitAtMost(Duration.ofSeconds(30));
    }
}
