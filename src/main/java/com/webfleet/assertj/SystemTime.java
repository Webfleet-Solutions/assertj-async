package com.webfleet.assertj;

import static java.util.logging.Level.WARNING;

import java.time.Clock;
import java.time.Duration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;


@AllArgsConstructor(staticName = "withClock")
final class SystemTime implements Time
{
    static final SystemTime UTC = SystemTime.withClock(Clock.systemUTC());

    @NonNull
    private final Clock clock;

    @Override
    public ElapsedTime measure()
    {
        final var startTime = clock.millis();
        return () -> Duration.ofMillis(clock.millis() - startTime);
    }

    @Override
    public WaitCondition waitCondition(@NonNull final Object waitMutex)
    {
        return MutexWaitCondition.create(waitMutex);
    }

    @AllArgsConstructor(staticName = "create", access = AccessLevel.PRIVATE)
    @Log
    private static final class MutexWaitCondition implements WaitCondition
    {
        private final Object waitMutex;

        @Override
        @SuppressWarnings("squid:S2274")
        public void waitFor(@NonNull final Duration waitInterval)
        {
            if (waitInterval.compareTo(Duration.ZERO) <= 0)
            {
                return;
            }
            synchronized (waitMutex)
            {
                try
                {
                    waitMutex.wait(waitInterval.toMillis());
                }
                catch (@SuppressWarnings("unused") final InterruptedException e)
                {
                    LOG.log(WARNING, "Wait interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
