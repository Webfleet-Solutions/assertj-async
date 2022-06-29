package com.webfleet.assertj;

import static java.util.logging.Level.WARNING;

import java.time.Clock;
import java.time.Duration;
import java.util.logging.Logger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;


@AllArgsConstructor(staticName = "from")
final class SystemTime implements Time
{
    static final SystemTime UTC = SystemTime.from(Clock.systemUTC());

    @NonNull
    private final Clock clock;

    @Override
    public Measure measure()
    {
        final var startTime = clock.millis();
        return () -> Duration.ofMillis(clock.millis() - startTime);
    }

    @Override
    public TimeWaiter waiter(@NonNull final Object waitMutex)
    {
        return MutexWaiter.of(waitMutex);
    }

    @AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    private static final class MutexWaiter implements TimeWaiter
    {
        private final Object waitMutex;

        @Override
        @SuppressWarnings("squid:S2274")
        public void waitFor(@NonNull final Duration waitInterval)
        {
            synchronized (waitMutex)
            {
                try
                {
                    waitMutex.wait(waitInterval.toMillis());
                }
                catch (final InterruptedException e)
                {
                    Logger.getLogger(TimeWaiter.class.getName()).log(WARNING, "Wait interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
