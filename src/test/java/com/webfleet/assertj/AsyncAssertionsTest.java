package com.webfleet.assertj;

import static com.webfleet.assertj.AsyncAssertions.awaitAtMost;
import static com.webfleet.assertj.AsyncAssertions.awaitAtMostFifteenSeconds;
import static com.webfleet.assertj.AsyncAssertions.awaitAtMostFiveSeconds;
import static com.webfleet.assertj.AsyncAssertions.awaitAtMostOneSecond;
import static com.webfleet.assertj.AsyncAssertions.awaitAtMostThirtySeconds;
import static com.webfleet.assertj.AsyncAssertions.awaitAtMostTwoSeconds;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.webfleet.assertj.util.ExecutorExtension;


@ExtendWith({SoftAssertionsExtension.class, ExecutorExtension.class})
class AsyncAssertionsTest
{
    @Test
    void shouldPassAssertionOnFirstCheck()
    {
        // given
        final var condition = new AtomicBoolean(true);
        final var checkCount = new AtomicInteger();

        // when
        final var caughtException = catchThrowable(() -> awaitAtMostOneSecond()
            .untilAssertions(async -> {
                async.assertThat(condition).isTrue();
                checkCount.incrementAndGet();
            }));

        // then
        assertThat(caughtException).isNull();
        assertThat(checkCount).hasValue(1);
    }

    @Test
    void shouldPassAssertionOnThirdCheck()
    {
        // given
        final var condition = new AtomicInteger(3);
        final var checkCount = new AtomicInteger();

        // when
        final var caughtException = catchThrowable(() -> awaitAtMostOneSecond()
            .untilAssertions(async -> {
                checkCount.incrementAndGet();
                async.assertThat(condition.decrementAndGet()).isZero();
            }));

        // then
        assertThat(caughtException).isNull();
        assertThat(checkCount).hasValue(3);
    }

    @Test
    void shouldPassAssertionWithExplicitAssertAllCall()
    {
        // given
        final var condition = new AtomicInteger(2);
        final var checkCount = new AtomicInteger();

        // when
        final var caughtException = catchThrowable(() -> awaitAtMostOneSecond()
            .untilAssertions(async -> {
                checkCount.incrementAndGet();
                async.assertThat(condition.decrementAndGet()).isZero();
                async.assertAll();
            }));

        // then
        assertThat(caughtException).isNull();
        assertThat(checkCount).hasValue(2);
    }

    @Test
    void shouldPassAssertionAfterChangeInOtherThread(final ScheduledExecutorService executor)
    {
        // given
        final var condition = new AtomicBoolean(false);
        final var checkCount = new AtomicInteger();
        executor.schedule(() -> condition.set(true), 100L, TimeUnit.MILLISECONDS);

        // when
        final var caughtException = catchThrowable(() -> awaitAtMostOneSecond()
            .untilAssertions(async -> {
                async.assertThat(condition).isTrue();
                checkCount.incrementAndGet();
            }));

        // then
        assertThat(caughtException).isNull();
        assertThat(checkCount).hasPositiveValue();
    }

    @Test
    void shouldFailAssertionAfterExceedingTimeout()
    {
        // given
        final var timeoutMs = 777L;
        final var condition = new AtomicBoolean(false);
        final var checkCount = new AtomicInteger();

        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(timeoutMs, MILLISECONDS)
            .untilAssertions(async -> {
                async.assertThat(condition.get()).isTrue();
                checkCount.incrementAndGet();
            }));

        // then
        assertThat(caughtException).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Async assertion failed after exceeding 777ms timeout (1 failure)")
            .hasMessageContaining("Expecting value to be true but was false");
        assertThat(checkCount).hasPositiveValue();
    }

    @Test
    void shouldFailMultipleAssertionAfterExceedingTimeout()
    {
        // given
        final var timeoutMs = 500L;
        final var condition1 = new AtomicBoolean(false);
        final var condition2 = new AtomicReference<>();
        final var checkCount = new AtomicInteger();

        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(timeoutMs, MILLISECONDS)
            .untilAssertions(async -> {
                async.assertThat(condition1.get()).isTrue();
                async.assertThat(condition2.get()).isNotNull();
                checkCount.incrementAndGet();
            }));

        // then
        assertThat(caughtException).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Async assertion failed after exceeding 500ms timeout (2 failures)")
            .hasMessageContaining("Expecting value to be true but was false")
            .hasMessageContaining("Expecting actual not to be null");
        assertThat(checkCount).hasPositiveValue();
    }

    @Test
    void shouldFailSingleAssertionAfterExceedingTimeout(final ScheduledExecutorService executor)
    {
        // given
        final var timeoutMs = 1000L;
        final var condition1 = new AtomicBoolean(false);
        final var condition2 = new AtomicReference<>();
        final var checkCount = new AtomicInteger();
        executor.schedule(() -> condition2.set(new Object()), 77L, MILLISECONDS);

        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(timeoutMs, MILLISECONDS)
            .untilAssertions(async -> {
                async.assertThat(condition1.get()).isTrue();
                async.assertThat(condition2.get()).isNotNull();
                checkCount.incrementAndGet();
            }));

        // then
        assertThat(caughtException).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Async assertion failed after exceeding 1000ms timeout (1 failure)")
            .hasMessageContaining("Expecting value to be true but was false");
        assertThat(checkCount).hasPositiveValue();
    }

    @Test
    void shouldWaitForAssertionsWithMutexObject(final ScheduledExecutorService executor)
    {
        // given
        final var condition = new AtomicBoolean(false);
        final var checkCount = new AtomicInteger();
        final var mutex = new Object();
        final var assertionFutue = executor.submit(() -> awaitAtMostFiveSeconds()
            .withWaitInterval(4, SECONDS)
            .usingWaitMutex(mutex).untilAssertions(async -> {
                async.assertThat(condition).isTrue();
                checkCount.incrementAndGet();
            }));
        awaitAtMostOneSecond().untilAssertions(softly -> softly.assertThat(checkCount).hasPositiveValue());

        // when
        condition.set(true);
        synchronized (mutex)
        {
            mutex.notifyAll();
        }

        // then
        awaitAtMostOneSecond().untilAssertions(async -> {
            async.assertThat(assertionFutue).isDone();
            async.assertThat(checkCount).hasPositiveValue();
        });
    }

    @Test
    void shouldThrowExceptionOnNullTimeout()
    {
        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("timeout is marked non-null but is null");
    }

    @Test
    void shouldThrowExceptionOnNullTimeoutUnit()
    {
        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(1L, null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("unit is marked non-null but is null");
    }

    @ParameterizedTest
    @CsvSource({"PT0S", "-PT0.001S"})
    void shouldThrowExceptionOnTimeoutDurationLowerThanOrEqualToZero(final Duration timeout)
    {
        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(timeout));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("timeout must be greater than zero");
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void shouldThrowExceptionOnTimeoutIntervalLowerThanOrEqualToZero(final long timeout)
    {
        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(timeout, TimeUnit.MILLISECONDS));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("timeout must be greater than zero");
    }

    @Test
    void testThatPredefinedAwaitTimeoutsAreValid(final SoftAssertions softly)
    {
        softly.assertThat(awaitAtMostOneSecond()).extracting("timeCondition.timeout").isEqualTo(Duration.ofSeconds(1));
        softly.assertThat(awaitAtMostTwoSeconds()).extracting("timeCondition.timeout").isEqualTo(Duration.ofSeconds(2));
        softly.assertThat(awaitAtMostFiveSeconds()).extracting("timeCondition.timeout").isEqualTo(Duration.ofSeconds(5));
        softly.assertThat(awaitAtMostFifteenSeconds()).extracting("timeCondition.timeout").isEqualTo(Duration.ofSeconds(15));
        softly.assertThat(awaitAtMostThirtySeconds()).extracting("timeCondition.timeout").isEqualTo(Duration.ofSeconds(30));
    }
}
