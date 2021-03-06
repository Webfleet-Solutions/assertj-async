package com.webfleet.assertj;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;

import lombok.NonNull;


/**
 * Asynchronous assertion.
 */
public interface AsyncAssert
{
    /**
     * Awaits, until all configured assertions are passed or timeout is exceeded.
     * <p>
     * Assertions are configured in lambda consumer of {@link SoftAssertions} object on each check.
     * The checks are executed periodically with check interval delay configured with {@link AsyncAssert#withCheckInterval} method.
     * After exceeding timeout {@link AssertionError} will be thrown with failures from last assertion check.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMostOneSecond().untilAssertions(async -> {
     *     async.assertThat(getValue()).isEqualTo(expected);
     *     async.assertThat(isDone()).isTrue();
     * });
     * }</pre>
     *
     * @param assertionsConfigurer lambda consumer configuring {@link SoftAssertions} object
     */
    void untilAssertions(Consumer<SoftAssertions> assertionsConfigurer);

    /**
     * Configures assertion to use given mutex object for check interval wait logic.
     * <p>
     * In multi-thread applications, the mutex object can be used to notify the other thread about state change.
     * For asynchronous assertion, the mutex object can be used to reduce the wait time between checks with {@link Object#notifyAll()} call.
     * <p>
     * Example usage:
     * <pre>{@code
     * // given
     * var condition = new AtomicBoolean();
     * var waitMutex = new Object();
     * var executor = Executors.newSingleThreadExecutor();
     *
     * // when
     * executor.execute(() -> {
     *     // ... asynchronous logic
     *     condition.set(true);
     *     // notify after done
     *     synchronized (waitMutex) {
     *         waitMutex.notifyAll();
     *     }
     * });
     *
     * // then
     * awaitAtMostOneSecond()
     *     .usingWaitMutex(waitMutex)
     *     .untilAssertions(async -> {
     *         async.assertThat(condition).isTrue();
     *     });
     * }</pre>
     *
     * @param waitMutex mutex object
     * @return new {@link AsyncAssert} using given wait mutex
     */
    AsyncAssert usingWaitMutex(Object waitMutex);

    /**
     * Configures the interval to be waited between assertions checks.
     * The interval must be greater than zero and lower than timeout.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMostOneSecond()
     *     .withCheckInterval(Duration.ofMillis(500))
     *     .untilAssertions(async -> {
     *         async.assertThat(condition).isTrue();
     *     });
     * }</pre>
     *
     * @param checkInterval check interval
     * @return new {@link AsyncAssert} with set check interval
     */
    AsyncAssert withCheckInterval(Duration checkInterval);

    /**
     * Configures the interval to be waited between assertions checks.
     * The interval must be greater than zero and lower than timeout.
     * <p>
     * Example usage:
     * <pre>{@code
     * awaitAtMostOneSecond()
     *     .withCheckInterval(500, TimeUnit.MILLISECONDS)
     *     .untilAssertions(async -> {
     *         async.assertThat(condition).isTrue();
     *     });
     * }</pre>
     *
     * @param checkInterval check interval
     * @param timeUnit the time unit of the check interval
     * @return new {@link AsyncAssert} with set check interval
     */
    default AsyncAssert withCheckInterval(final long checkInterval, @NonNull final TimeUnit timeUnit)
    {
        return withCheckInterval(Duration.ofMillis(timeUnit.toMillis(checkInterval)));
    }
}
