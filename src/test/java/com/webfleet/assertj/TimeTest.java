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

import com.webfleet.assertj.util.ExecutorExtension;


@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class, ExecutorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
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
    void shouldMeasureElaspedTime(final SoftAssertions softly)
    {
        // given
        given(clock.millis()).willReturn(0L, 5000L, 15000L);

        // when
        final var elaspedTime = tested.measure();

        // then
        softly.assertThat(elaspedTime.get()).isEqualTo(Duration.ofSeconds(5));
        softly.assertThat(elaspedTime.get()).isEqualTo(Duration.ofSeconds(15));
        softly.assertThat(elaspedTime.get()).isEqualTo(Duration.ofSeconds(15));

        softly.assertThat(elaspedTime.isLowerThan(Duration.ofSeconds(15))).isFalse();
        softly.assertThat(elaspedTime.isLowerThan(Duration.ofSeconds(15).plusMillis(1))).isTrue();
        softly.assertThat(elaspedTime.isLowerThan(Duration.ofSeconds(15).minusMillis(1))).isFalse();

        softly.assertThat(elaspedTime.isLowerThanOrEqualTo(Duration.ofSeconds(15))).isTrue();
        softly.assertThat(elaspedTime.isLowerThanOrEqualTo(Duration.ofSeconds(15).plusMillis(1))).isTrue();
        softly.assertThat(elaspedTime.isLowerThanOrEqualTo(Duration.ofSeconds(15).minusMillis(1))).isFalse();
    }

    @Test
    void shouldIgnoreWaitingZeroDuration()
    {
        // given
        final var mutexObject = new Object();
        final var waitCondition = tested.waitCondition(mutexObject);

        // when
        final var caughtException = catchThrowable(() -> waitCondition.waitFor(Duration.ZERO));

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

        // when
        final var future = executor.submit(() -> waitCondition.waitFor(Duration.ofSeconds(5)));

        // then
        Thread.sleep(50L); // sleep a while to make sure object.wait was executed
        softly.assertThat(future).isNotDone();

        // when notified
        synchronized (mutexObject)
        {
            mutexObject.notifyAll();
        }

        // then
        future.get(100L, MILLISECONDS);
        softly.assertThat(future).isDone();
    }
}
