package com.webfleet.assertj;

import static java.time.Duration.ZERO;

import java.time.Duration;

import com.webfleet.assertj.Time.ElapsedTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
final class AsyncAssertAwaiConfig
{
    private static final Duration DEFAULT_CHECK_INTERVAL = Duration.ofMillis(100L);
    private static final Duration DEFAULT_SHORT_CHECK_INTERVAL = Duration.ofMillis(50L);

    private final Duration timeout;
    private final Duration checkInterval;

    static AsyncAssertAwaiConfig withTimeout(@NonNull final Duration timeout)
    {
        if (timeout.compareTo(ZERO) <= 0)
        {
            throw new IllegalArgumentException("timeout must be greater than zero");
        }
        final var checkInterval = computeCheckInterval(timeout);
        return new AsyncAssertAwaiConfig(timeout, checkInterval);
    }

    AsyncAssertAwaiConfig withCheckInterval(@NonNull final Duration checkInterval)
    {
        if (checkInterval.compareTo(ZERO) <= 0)
        {
            throw new IllegalArgumentException("checkInterval must be greater than zero");
        }
        if (checkInterval.compareTo(timeout) > 0)
        {
            throw new IllegalArgumentException("checkInterval must be lower than or equal to timeout");
        }
        return new AsyncAssertAwaiConfig(timeout, checkInterval);
    }

    Duration checkInterval(@NonNull final ElapsedTime elapsedTime)
    {
        final var elapsedDuration = elapsedTime.get();
        if (elapsedDuration.plus(checkInterval).compareTo(timeout) > 0)
        {
            final var shortenedCheckInterval = timeout.minus(elapsedDuration);
            return shortenedCheckInterval.isNegative() ? Duration.ZERO : shortenedCheckInterval;
        }
        return checkInterval;
    }

    private static Duration computeCheckInterval(final Duration timeout)
    {
        if (DEFAULT_CHECK_INTERVAL.compareTo(timeout) >= 0)
        {
            if (timeout.compareTo(DEFAULT_SHORT_CHECK_INTERVAL) > 0)
            {
                return DEFAULT_SHORT_CHECK_INTERVAL;
            }
            return timeout;
        }
        return DEFAULT_CHECK_INTERVAL;
    }
}
