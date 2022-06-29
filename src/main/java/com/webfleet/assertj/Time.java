package com.webfleet.assertj;

import java.time.Duration;


interface Time
{
    Measure measure();

    TimeWaiter waiter(Object mutex);

    @FunctionalInterface
    interface Measure
    {
        Duration duration();

        default long millis()
        {
            return duration().toMillis();
        }
    }

    @FunctionalInterface
    interface TimeWaiter
    {
        void waitFor(Duration interval);
    }
}
