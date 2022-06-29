package com.webfleet.assertj;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;

import lombok.NonNull;


/**
 * Evaluator of asynchronous assertions.
 */
public interface AsyncAssertionsEvaluator
{
    /**
     * Awaits until all assertions are passed or timeout is exceeded.
     * The assertions are provided in call-back function for {@link SoftAssertions} object.
     * The evaluation of the assertions is executed in fixed intervals, configured with {@link AsyncAssertionsEvaluator#withWaitInterval} method.
     * If at least one assertion is not valid after the timeout is exceeded then {@link AssertionError} will be thrown.
     *
     * @param assertionsConfiguerer {@link SoftAssertions} configuration call-back
     */
    void untilAllAssertionsArePassed(Consumer<SoftAssertions> assertionsConfiguerer);

    /**
     * Configures optional object used for waiting logic.
     * In multi-threaded applications, the mutex object can be used to notify the other thread about state change.
     * For asynchronous assertion, the mutex object can be used to reduce the wait time between evaluation with {@link Object#notifyAll()} call.
     *
     * @param mutex mutex object
     * @return self
     */
    AsyncAssertionsEvaluator withWaitMutex(Object mutex);

    /**
     * Configures wait interval in between evaluation of the assertions.
     * The interval must be greater than zero and lower than timeout.
     *
     * @param waitInterval wait interval
     * @return self
     */
    AsyncAssertionsEvaluator withWaitInterval(Duration waitInterval);

    /**
     * Configures wait interval in between evaluation of the assertions.
     * The interval must be greater than zero and lower than timeout.
     *
     * @param waitInterval wait interval value
     * @param unit interval unit
     * @return self
     */
    default AsyncAssertionsEvaluator withWaitInterval(final long waitInterval, @NonNull final TimeUnit unit)
    {
        return withWaitInterval(Duration.ofMillis(unit.toMillis(waitInterval)));
    }
}
