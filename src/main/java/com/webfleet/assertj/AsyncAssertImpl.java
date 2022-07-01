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
    private final AsyncAssertAwaitConfig config;
    private final Object waitMutex;

    AsyncAssertImpl(@NonNull final Time time, @NonNull final AsyncAssertAwaitConfig config)
    {
        this(time, config, new Object());
    }

    @Override
    public AsyncAssert withCheckInterval(@NonNull final Duration checkInterval)
    {
        return new AsyncAssertImpl(time, config.withCheckInterval(checkInterval), waitMutex);
    }

    @Override
    public AsyncAssert usingWaitMutex(@NonNull final Object waitMutex)
    {
        return new AsyncAssertImpl(time, config, waitMutex);
    }

    @Override
    public void untilAssertions(@NonNull final Consumer<SoftAssertions> assertionsConfigurer)
    {
        final var elapsedTime = time.measure();
        final var waitCondition = time.waitCondition(waitMutex);

        var result = AsyncAssertResult.undefined();
        while (result.hasFailed() && elapsedTime.isLowerThanOrEqualTo(config.timeout()) && !Thread.currentThread().isInterrupted())
        {
            result = AsyncAssertResult.evaluate(assertionsConfigurer);
            if (result.hasFailed())
            {
                if (!elapsedTime.isLowerThan(config.timeout()))
                {
                    break;
                }
                waitCondition.waitFor(config.checkInterval(elapsedTime));
            }
        }
        result.throwOnFailure(config);
    }
}
