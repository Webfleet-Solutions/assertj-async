package com.webfleet.assertj;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.webfleet.assertj.util.EnableScheduledExecutor;


@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
@EnableScheduledExecutor
class TimeTest
{
    @Mock
    private Clock clock;
    private Time tested;

    @BeforeEach
    void setup()
    {
        tested = SystemTime.withClock(clock);
    }

    @Test
    void shouldMeasureElapsedTimeUsingClock(final SoftAssertions softly)
    {
        // given
        given(clock.millis()).willReturn(0L, 5000L, 15000L);

        // when
        final var elapsedTime = tested.measure();

        // then
        softly.assertThat(elapsedTime.get()).isEqualTo(Duration.ofSeconds(5));
        softly.assertThat(elapsedTime.get()).isEqualTo(Duration.ofSeconds(15));
        softly.assertThat(elapsedTime.get()).isEqualTo(Duration.ofSeconds(15));

        softly.assertThat(elapsedTime.isLowerThan(Duration.ofSeconds(15))).isFalse();
        softly.assertThat(elapsedTime.isLowerThan(Duration.ofSeconds(15).plusMillis(1))).isTrue();
        softly.assertThat(elapsedTime.isLowerThan(Duration.ofSeconds(15).minusMillis(1))).isFalse();

        softly.assertThat(elapsedTime.isLowerThanOrEqualTo(Duration.ofSeconds(15))).isTrue();
        softly.assertThat(elapsedTime.isLowerThanOrEqualTo(Duration.ofSeconds(15).plusMillis(1))).isTrue();
        softly.assertThat(elapsedTime.isLowerThanOrEqualTo(Duration.ofSeconds(15).minusMillis(1))).isFalse();
    }

    @Test
    void shouldIgnoreWaitingZeroDuration()
    {
        // given
        final var mutexObject = new Object();
        final var waitCondition = tested.waitCondition(mutexObject);

        // when
        final var caughtException = catchThrowable(() -> waitCondition.waitFor(Duration.ZERO));

        // then
        assertThat(caughtException).isNull();
    }

    @Test
    void shouldIgnoreWaitingNegativeDuration()
    {
        // given
        final var mutexObject = new Object();
        final var waitCondition = tested.waitCondition(mutexObject);

        // when
        final var caughtException = catchThrowable(() -> waitCondition.waitFor(Duration.ofMillis(-1L)));

        // then
        assertThat(caughtException).isNull();
    }

    @Test
    @Timeout(value = 600L, unit = MILLISECONDS)
    void shouldWaitForFiveHundredMilliseconds()
    {
        // given
        final var mutexObject = new Object();
        final var waitCondition = tested.waitCondition(mutexObject);
        final var startTime = System.currentTimeMillis();

        // when
        waitCondition.waitFor(Duration.ofMillis(500L));

        // then
        final var elapsed = System.currentTimeMillis() - startTime;
        assertThat(elapsed).isBetween(500L, 600L);
    }

    @Test
    void shouldStopWaitingWhenMutexObjectIsNotified(final ExecutorService executor, final SoftAssertions softly) throws Exception
    {
        // given
        final var mutexObject = new Object();
        final var waitCondition = tested.waitCondition(mutexObject);
        final var future = executor.submit(() -> waitCondition.waitFor(Duration.ofSeconds(5)));
        Thread.sleep(50L); // sleep a while to make sure object.wait was executed
        softly.assertThat(future).isNotDone();

        // when notified
        synchronized (mutexObject)
        {
            mutexObject.notifyAll();
        }

        // then
        softly.assertThat(catchThrowable(() -> future.get(100L, MILLISECONDS))).isNull(); // 100ms margin for future completion
        softly.assertThat(future).isDone();
    }
}
