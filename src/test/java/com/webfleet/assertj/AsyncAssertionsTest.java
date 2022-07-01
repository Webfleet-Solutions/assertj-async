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
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.webfleet.assertj.util.EnableScheduledExecutor;


@ExtendWith(SoftAssertionsExtension.class)
@EnableScheduledExecutor
class AsyncAssertionsTest
{
    private final AtomicInteger checkCount = new AtomicInteger();

    @AfterEach
    void resetCheckCounter()
    {
        checkCount.set(0);
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    void shouldCheckPositiveAssertionsOnce(final SoftAssertions softly)
    {
        // given
        final var condition = new AtomicBoolean(true);

        // when
        final var caughtError = catchAsyncAssertError(awaitAtMostFiveSeconds(), async -> async
            .assertThat(condition).isTrue());

        // then
        softly.assertThat(caughtError).isNull();
        softly.assertThat(checkCount).hasValue(1);
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    void shouldCheckAssertionThreeTimesUntilPositiveResult(final SoftAssertions softly)
    {
        // given
        final var condition = new AtomicInteger(3);

        // when
        final var caughtError = catchAsyncAssertError(awaitAtMostFiveSeconds(), async -> async
            .assertThat(condition.decrementAndGet()).isZero());

        // then
        softly.assertThat(caughtError).isNull();
        softly.assertThat(checkCount).hasValue(3);
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    void shouldCatchErrorProducedByExplicitAssertAllCallsByAssertionConfigurer(final SoftAssertions softly)
    {
        // given
        final var condition = new AtomicInteger(2);

        // when
        final var caughtError = catchAsyncAssertError(awaitAtMostFiveSeconds(), async -> {
            async.assertThat(condition.decrementAndGet()).isZero();
            async.assertAll(); // assertAll is called automatically, but if used explicitly it shouldn't produce failures
        });

        // then
        softly.assertThat(caughtError).isNull();
        softly.assertThat(checkCount).hasValue(2);
    }

    @Test
    void shouldKeepCheckingAssertionUntilConditionIsChangedInOtherThreadToExpectedResult(final ScheduledExecutorService executor,
                                                                                         final SoftAssertions softly)
        throws Exception
    {
        // given
        final var condition = new AtomicBoolean(false);
        final var asyncAssert = awaitAtMostFiveSeconds()
            .withCheckInterval(50, MILLISECONDS);
        final var caughtErrorFuture = executor.submit(() -> catchAsyncAssertError(asyncAssert, async -> async
            .assertThat(condition).isTrue()));
        awaitForFirstAssertionCheck(); // make sure the assertion checks begun
        softly.assertThat(caughtErrorFuture).isNotDone();

        // when
        condition.set(true);

        // then
        final var caughtError = caughtErrorFuture.get(100L, MILLISECONDS);
        softly.assertThat(caughtError).isNull();
    }

    @Test
    void shouldKeepCheckingAssertionUntilCustomTimeout(final SoftAssertions softly)
    {
        // given
        final var timeoutMs = 777L;
        final var condition = new AtomicBoolean(false);

        // when
        final var caughtError = catchAsyncAssertError(awaitAtMost(timeoutMs, MILLISECONDS), async -> async
            .assertThat(condition.get()).isTrue());

        // then
        softly.assertThat(caughtError).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Async assertion failed after exceeding 777ms timeout (1 failure)")
            .hasMessageContaining("Expecting value to be true but was false");
        softly.assertThat(checkCount).hasPositiveValue();
    }

    @Test
    void shouldKeepCheckingAssertionWithMultipleConditionsUntilTimeout(final SoftAssertions softly)
    {
        // given
        final var timeoutMs = 500L;
        final var condition1 = new AtomicBoolean(false);
        final var condition2 = new AtomicReference<>();

        // when
        final var caughtError = catchAsyncAssertError(awaitAtMost(timeoutMs, MILLISECONDS), async -> {
            async.assertThat(condition1.get()).isTrue();
            async.assertThat(condition2.get()).isNotNull();
        });

        // then
        softly.assertThat(caughtError).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Async assertion failed after exceeding 500ms timeout (2 failures)")
            .hasMessageContaining("Expecting value to be true but was false")
            .hasMessageContaining("Expecting actual not to be null");
        softly.assertThat(checkCount).hasPositiveValue();
    }

    @Test
    void shouldTimeoutAssertionChecksWhenAtLeastOneConditionIsNotPositive(final ScheduledExecutorService executor, final SoftAssertions softly)
        throws Exception
    {
        // given
        final var timeoutMs = 1000L;
        final var condition1 = new AtomicBoolean(false);
        final var condition2 = new AtomicReference<>();
        final var caughtErrorFuture = executor.submit(() -> catchAsyncAssertError(awaitAtMost(timeoutMs, MILLISECONDS), async -> {
            async.assertThat(condition1.get()).isTrue();
            async.assertThat(condition2.get()).isNotNull();
        }));
        awaitForFirstAssertionCheck(); // make sure the assertion checks begun
        softly.assertThat(caughtErrorFuture).isNotDone();

        // when
        condition2.set(new Object());

        // then
        final var caughtError = caughtErrorFuture.get(timeoutMs, MILLISECONDS);
        softly.assertThat(caughtError).isInstanceOf(AssertionError.class)
            .hasMessageContaining("Async assertion failed after exceeding 1000ms timeout (1 failure)")
            .hasMessageContaining("Expecting value to be true but was false");
        softly.assertThat(checkCount).hasPositiveValue();
    }

    @Test
    void shouldInterruptCheckIntervalWaitWhenWaitMutexObjectIsNotified(final ScheduledExecutorService executor, final SoftAssertions softly)
        throws Exception
    {
        // given
        final var condition = new AtomicBoolean(false);
        final var waitMutex = new Object();
        final var asyncAssert = awaitAtMostFiveSeconds()
            .withCheckInterval(4, SECONDS)
            .usingWaitMutex(waitMutex);
        final var caughtErrorFuture = executor.submit(() -> catchAsyncAssertError(asyncAssert, async -> async.assertThat(condition).isTrue()));
        awaitForFirstAssertionCheck(); // make sure the assertion checks begun

        // when
        condition.set(true);
        synchronized (waitMutex)
        {
            waitMutex.notifyAll();
        }

        // then 4 second wait should be interrupted
        final var caughtError = caughtErrorFuture.get(100L, MILLISECONDS);
        softly.assertThat(caughtError).isNull();
        softly.assertThat(checkCount).hasValue(2);
    }

    @Test
    void shouldThrowExceptionWhenAwaitTimeoutIsSetToNull()
    {
        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("timeout is marked non-null but is null");
    }

    @Test
    void shouldThrowExceptionWhenAwaitTimeoutTimeUnitIsSetToNull()
    {
        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(1L, null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("timeUnit is marked non-null but is null");
    }

    @ParameterizedTest
    @CsvSource({"PT0S", "-PT0.001S"})
    void shouldThrowExceptionWhenAwaitTimeoutIsSetToZeroOrNegativeDuration(final Duration timeout)
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
    void shouldThrowExceptionWhenTimeoutIsSetToZeroOrNegativeValueInMilliseconds(final long timeout)
    {
        // when
        final var caughtException = catchThrowable(() -> awaitAtMost(timeout, TimeUnit.MILLISECONDS));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("timeout must be greater than zero");
    }

    @Test
    void testThatPredefinedAwaitTimeoutDurationsAreValid(final SoftAssertions softly)
    {
        softly.assertThat(awaitAtMostOneSecond()).extracting("config.timeout").isEqualTo(Duration.ofSeconds(1));
        softly.assertThat(awaitAtMostTwoSeconds()).extracting("config.timeout").isEqualTo(Duration.ofSeconds(2));
        softly.assertThat(awaitAtMostFiveSeconds()).extracting("config.timeout").isEqualTo(Duration.ofSeconds(5));
        softly.assertThat(awaitAtMostFifteenSeconds()).extracting("config.timeout").isEqualTo(Duration.ofSeconds(15));
        softly.assertThat(awaitAtMostThirtySeconds()).extracting("config.timeout").isEqualTo(Duration.ofSeconds(30));
    }

    private Throwable catchAsyncAssertError(final AsyncAssert asyncAssert,
                                            final Consumer<SoftAssertions> assertionConfigurer)
    {
        return catchThrowable(() -> asyncAssert.untilAssertions(async -> {
            checkCount.incrementAndGet();
            assertionConfigurer.accept(async);
        }));
    }

    private void awaitForFirstAssertionCheck()
    {
        awaitAtMostOneSecond().untilAssertions(async -> async.assertThat(checkCount).hasPositiveValue());
    }
}
