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
    private static final AsyncAssertAwaitConfig CONFIG = AsyncAssertAwaitConfig
        .withTimeout(Duration.ofSeconds(5))
        .withCheckInterval(Duration.ofSeconds(1));

    private MockTime time;
    private AsyncAssert tested;

    private final AtomicInteger assertionFailureCount = new AtomicInteger();
    private final Consumer<SoftAssertions> assertionConfigurer = async -> async
        .assertThat(assertionFailureCount.getAndDecrement()).isLessThanOrEqualTo(0);

    @BeforeEach
    void setup()
    {
        assertionFailureCount.set(0);
        time = MockTime.create();
        tested = new AsyncAssertImpl(time, CONFIG);
    }

    private void givenAssertionFailCount(final int failCount)
    {
        assertionFailureCount.set(failCount);
    }

    @Test
    void shouldNotWaitWhenAssertionsArePositiveForTheFirstTime(final SoftAssertions softly)
    {
        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isNull();
        softly.assertThat(time.waitIntervals()).isEmpty();
    }

    @Test
    void shouldKeepCheckingAssertionsWithCheckIntervalWaitTimeUntilSuccess(final SoftAssertions softly)
    {
        // given
        givenAssertionFailCount(5);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isNull();
        softly.assertThat(time.waitIntervals()).containsExactly(
            CONFIG.checkInterval(),
            CONFIG.checkInterval(),
            CONFIG.checkInterval(),
            CONFIG.checkInterval(),
            CONFIG.checkInterval());
    }

    @Test
    void shouldKeepCheckingAssertionsWithCheckIntervalWaitTimeUntilTimeout(final SoftAssertions softly)
    {
        // given
        givenAssertionFailCount(6);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isInstanceOf(AssertionError.class);
        softly.assertThat(time.waitIntervals()).containsExactly(
            CONFIG.checkInterval(),
            CONFIG.checkInterval(),
            CONFIG.checkInterval(),
            CONFIG.checkInterval(),
            CONFIG.checkInterval());
    }

    @Test
    void shouldKeepCheckingAssertionsWithCustomCheckIntervalWaitTimeUntilTimeout(final SoftAssertions softly)
    {
        // given
        final var customCheckInterval = Duration.ofMillis(1777L);
        tested = tested.withCheckInterval(customCheckInterval);
        givenAssertionFailCount(4);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isInstanceOf(AssertionError.class);
        softly.assertThat(time.waitIntervals()).containsExactly(
            customCheckInterval,
            customCheckInterval,
            Duration.ofMillis(1446L)); // the last wait is shortened to not exceed timeout, the next check failure stops the loop
    }

    @Test
    void shouldKeepCheckingAssertionsWithCustomCheckIntervalWaitAndCustomWaitMutexTimeUntilSuccess(final SoftAssertions softly)
    {
        // given
        final var customCheckInterval = Duration.ofMillis(1800L);
        final var customWaitMutex = new Object();
        tested = tested.withCheckInterval(customCheckInterval).usingWaitMutex(customWaitMutex);
        givenAssertionFailCount(3);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isNull();
        softly.assertThat(time.waitIntervals()).containsExactly(
            customCheckInterval,
            customCheckInterval,
            Duration.ofMillis(1400L)); // the last wait is shortened to not exceed timeout, the next check success stops the loop
        softly.assertThat(time.waitMutexObjects()).containsExactly(customWaitMutex);
    }

    @Test
    void shouldShortenCheckIntervalToOneMillisecond(final SoftAssertions softly)
    {
        // given
        final var checkInterval = Duration.ofMillis(4999L);
        tested = tested.withCheckInterval(checkInterval);
        givenAssertionFailCount(2);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAssertions(assertionConfigurer));

        // then
        softly.assertThat(caughtException).isNull();
        softly.assertThat(time.waitIntervals()).containsExactly(
            checkInterval,
            Duration.ofMillis(1L));
    }
}
