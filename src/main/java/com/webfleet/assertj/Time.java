package com.webfleet.assertj;

import java.time.Duration;

import lombok.NonNull;


interface Time
{
    ElapsedTime measure();

    WaitCondition waitCondition(Object mutex);

    @FunctionalInterface
    interface ElapsedTime
    {
        Duration get();

        default boolean isLowerThanOrEqualTo(@NonNull final Duration duration)
        {
            return get().compareTo(duration) <= 0;
        }

        default boolean isLowerThan(@NonNull final Duration duration)
        {
            return get().compareTo(duration) < 0;
        }
    }

    @FunctionalInterface
    interface WaitCondition
    {
        void waitFor(Duration interval);
    }
}
