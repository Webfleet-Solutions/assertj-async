package com.webfleet.assertj;

import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SoftAssertionsExtension.class)
class AsyncAssertTest
{
    private static final AsyncAssertTimeoutCondition TIMEOUT = AsyncAssertTimeoutCondition
        .withTimeout(Duration.ofSeconds(5))
        .withCheckInterval(Duration.ofSeconds(1));

    private MockTime time;
    private AsyncAssert tested;
    private final AtomicInteger failures = new AtomicInteger();
    private final Consumer<SoftAssertions> assertionConfigurer = async -> async
        .assertThat(failures.getAndDecrement()).isLessThanOrEqualTo(0);

    @BeforeEach
    void setup()
    {
        failures.set(0);
        time = MockTime.create();
        tested = new AsyncAssertImpl(time, TIMEOUT);
    }

    @Test
    void shouldNotWaitWhenAssertionIsPositiveForTheFirstTime(final SoftAssertions softly)
    {
        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isNull();
        softly.assertThat(time.waitIntervals).isEmpty();
    }

    @Test
    void shouldEvaluateAssertionUntilSuccess(final SoftAssertions softly)
    {
        // given
        failures.set(5);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isNull();
        softly.assertThat(time.waitIntervals).containsExactly(
            TIMEOUT.checkInterval(),
            TIMEOUT.checkInterval(),
            TIMEOUT.checkInterval(),
            TIMEOUT.checkInterval(),
            TIMEOUT.checkInterval());
    }

    @Test
    void shouldEvaluateAssertionUntilTimeout(final SoftAssertions softly)
    {
        // given
        failures.set(6);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isInstanceOf(AssertionError.class);
        softly.assertThat(time.waitIntervals).containsExactly(
            TIMEOUT.checkInterval(),
            TIMEOUT.checkInterval(),
            TIMEOUT.checkInterval(),
            TIMEOUT.checkInterval(),
            TIMEOUT.checkInterval());
    }

    @Test
    void shouldEvaluateAssertionUntilTimeoutWithChangedCheckInterval(final SoftAssertions softly)
    {
        // given
        final var checkInterval = Duration.ofMillis(1777L);
        tested = tested.withCheckInterval(checkInterval);
        failures.set(4);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isInstanceOf(AssertionError.class);
        softly.assertThat(time.waitIntervals).containsExactly(
            checkInterval,
            checkInterval,
            Duration.ofMillis(1446L));
    }

    @Test
    void shouldEvaluateAssertionUntilSuccessUsingCustomWaitMutex(final SoftAssertions softly)
    {
        // given
        final var checkInterval = Duration.ofMillis(1800L);
        final var waitMutex = new Object();
        tested = tested.withCheckInterval(checkInterval).usingWaitMutex(waitMutex);
        failures.set(3);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isNull();
        softly.assertThat(time.waitIntervals).containsExactly(
            checkInterval,
            checkInterval,
            Duration.ofMillis(1400L));
        softly.assertThat(time.waitMutexObjects).containsExactly(waitMutex);
    }

    @Test
    void shouldShortenWaitIntervalToOneMillisecond(final SoftAssertions softly)
    {
        // given
        final var checkInterval = Duration.ofMillis(4999L);
        tested = tested.withCheckInterval(checkInterval);
        failures.set(2);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isNull();
        softly.assertThat(time.waitIntervals).containsExactly(
            checkInterval,
            Duration.ofMillis(1L));
    }
}
