package com.webfleet.assertj;

import java.time.Duration;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class AsyncAssertImpl implements AsyncAssert
{
    private final Time time;
    private final AsyncAssertTimeoutCondition timeCondition;
    private final Object waitMutex;

    AsyncAssertImpl(@NonNull final Time time, @NonNull final AsyncAssertTimeoutCondition timeCondition)
    {
        this(time, timeCondition, new Object());
    }

    @Override
    public AsyncAssert withCheckInterval(@NonNull final Duration checkInterval)
    {
        return new AsyncAssertImpl(time, timeCondition.withCheckInterval(checkInterval), waitMutex);
    }

    @Override
    public AsyncAssert usingWaitMutex(@NonNull final Object waitMutex)
    {
        return new AsyncAssertImpl(time, timeCondition, waitMutex);
    }

    @Override
    public void untilAssertions(@NonNull final Consumer<SoftAssertions> assertionsConfigurer)
    {
        final var elapsedTime = time.measure();
        final var waitCondition = time.waitCondition(waitMutex);

        var result = AsyncAssertResult.undefined();
        while (result.hasFailed() && elapsedTime.isLowerThanOrEqualTo(timeCondition.timeout()) && !Thread.currentThread().isInterrupted())
        {
            result = AsyncAssertResult.evaluate(assertionsConfigurer);
            if (result.hasFailed())
            {
                if (!elapsedTime.isLowerThan(timeCondition.timeout()))
                {
                    break;
                }
                waitCondition.waitFor(timeCondition.checkInterval(elapsedTime));
            }
        }
        result.throwOnFailure(timeCondition);
    }
}
