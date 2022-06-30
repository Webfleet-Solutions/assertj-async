package com.webfleet.assertj;

import static com.webfleet.assertj.AsyncAssertions.awaitAtMostOneSecond;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;


class AsyncAssertionExampleTest
{
    @Test
    void shouldReceiveChangeMessages()
    {
        // given
        final var receivedMessages = new ConcurrentLinkedQueue<String>();

        // when
        listenToChanges(receivedMessages::offer);

        // then
        awaitAtMostOneSecond().untilAssertions(async -> async
            .assertThat(receivedMessages).containsExactly("A", "B", "C"));

    }

    private static void listenToChanges(final Consumer<String> consumer)
    {
        // simulation of asynchronous consumer
        final var scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> consumer.accept("A"), 100L, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> consumer.accept("B"), 200L, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> consumer.accept("C"), 300L, TimeUnit.MILLISECONDS);
    }
}
