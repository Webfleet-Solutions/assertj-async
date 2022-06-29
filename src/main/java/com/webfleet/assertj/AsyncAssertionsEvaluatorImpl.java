package com.webfleet.assertj;

import java.time.Duration;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.VisibleForTesting;

import com.webfleet.assertj.Time.TimeWaiter;

import lombok.NonNull;


final class AsyncAssertionsEvaluatorImpl implements AsyncAssertionsEvaluator
{
    private static final TimeWaiter DEFAULT_WAITER = SystemTime.UTC.waiter(new Object());
    private static final Duration DEFAULT_WAIT_INTERVAL = Duration.ofMillis(100L);
    private static final Duration DEFAULT_SHORT_WAIT_INTERVAL = Duration.ofMillis(50L);

    private final Time time;
    private final TimeWaiter waiter;
    private final Duration timeout;
    private final Duration waitInterval;

    private AsyncAssertionsEvaluatorImpl(@NonNull final Time time,
                                         @NonNull final TimeWaiter waiter,
                                         @NonNull final Duration timeout,
                                         @NonNull final Duration waitInterval)
    {
        if (timeout.isNegative() || timeout.isZero())
        {
            throw new IllegalArgumentException("timeout must be greater than zero");
        }
        if (waitInterval.isNegative() || waitInterval.isZero())
        {
            throw new IllegalArgumentException("waitInterval must be greater than zero");
        }
        if (waitInterval.compareTo(timeout) > 0)
        {
            throw new IllegalArgumentException("waitInterval must be lower than or equal to timeout");
        }
        this.time = time;
        this.waiter = waiter;
        this.timeout = timeout;
        this.waitInterval = waitInterval;
    }

    @VisibleForTesting
    AsyncAssertionsEvaluatorImpl(@NonNull final Time time,
                                 @NonNull final TimeWaiter waiter,
                                 @NonNull final Duration timeout)
    {
        this(time, waiter, timeout, computeDefaultWaitInterval(timeout));
    }

    AsyncAssertionsEvaluatorImpl(@NonNull final Duration timeout)
    {
        this(SystemTime.UTC, DEFAULT_WAITER, timeout);
    }

    private static Duration computeDefaultWaitInterval(final Duration timeout)
    {
        if (DEFAULT_WAIT_INTERVAL.compareTo(timeout) >= 0)
        {
            if (timeout.compareTo(DEFAULT_SHORT_WAIT_INTERVAL) > 0)
            {
                return DEFAULT_SHORT_WAIT_INTERVAL;
            }
            return timeout;
        }
        return DEFAULT_WAIT_INTERVAL;
    }

    @Override
    public AsyncAssertionsEvaluator withWaitInterval(@NonNull final Duration waitInterval)
    {
        return new AsyncAssertionsEvaluatorImpl(time, waiter, timeout, waitInterval);
    }

    @Override
    public AsyncAssertionsEvaluator withWaitMutex(@NonNull final Object waitMutex)
    {
        return new AsyncAssertionsEvaluatorImpl(time, time.waiter(waitMutex), timeout, waitInterval);
    }

    @Override
    public void untilAllAssertionsArePassed(@NonNull final Consumer<SoftAssertions> assertionsConfigurer)
    {
        var result = AsyncAssertionsResult.undefined();
        final var elapsed = time.measure();
        while (result.hasFailed() && elapsed.millis() <= timeout.toMillis() && !Thread.currentThread().isInterrupted())
        {
            result = AsyncAssertionsResult.evaluate(assertionsConfigurer);
            if (result.hasFailed())
            {
                waiter.waitFor(waitInterval);
            }
        }
        result.failOnErrorWithTimeout(timeout);
    }
}
