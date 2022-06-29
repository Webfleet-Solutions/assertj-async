package com.webfleet.assertj;

import static java.util.stream.Collectors.summingLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.webfleet.assertj.Time.TimeWaiter;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class AsyncAssertionsEvaluatorTest
{
    @Mock
    private Time time;
    @Spy
    private Time.Measure measure;
    @Mock
    private TimeWaiter waiter;

    private final List<Duration> waitIntervals = new ArrayList<>();
    private final List<Object> waiterMutexObjects = new ArrayList<>();

    @BeforeEach
    void setup()
    {
        waitIntervals.clear();
        waiterMutexObjects.clear();
        doReturn(measure).when(time).measure();

        doAnswer(args -> {
            waiterMutexObjects.add(args.getArgument(0));
            return waiter;
        }).when(time).waiter(any());

        doAnswer(args -> waitIntervals.add(args.getArgument(0)))
            .when(waiter).waitFor(any());

        doAnswer(args -> Duration.ofMillis(waitIntervals.stream().collect(summingLong(Duration::toMillis))))
            .when(measure).duration();
    }

    @ParameterizedTest
    @CsvSource(
        value = {
                // timeout | expected wait interval
                //---------+------------------------
                "PT30S     | PT0.100S",
                "PT5S      | PT0.100S",
                "PT1S      | PT0.100S",
                "PT0.100S  | PT0.050S",
                "PT0.090S  | PT0.050S",
                "PT0.050S  | PT0.050S",
                "PT0.040S  | PT0.040S",
                "PT0.030S  | PT0.030S",
                "PT0.020S  | PT0.020S",
                "PT0.010S  | PT0.010S",
                "PT0.001S  | PT0.001S",
        },
        delimiter = '|')
    void shouldComputeDefaultWaitIntervalFromTimeout(final Duration timeout, final Duration expectedWaitInterval)
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, timeout);

        // when
        tested.untilAllAssertionsArePassed(failOnce());

        // then
        assertThat(waitIntervals).containsExactly(expectedWaitInterval);
        assertThat(waiterMutexObjects).isEmpty();
    }

    @Test
    void shouldChangeWaitInterval()
    {
        // given
        final var waitInterval = Duration.ofSeconds(1);
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(4L))
            .withWaitInterval(waitInterval);

        // when
        tested.untilAllAssertionsArePassed(failOnce());

        // then
        assertThat(waitIntervals).containsExactly(waitInterval);
        assertThat(waiterMutexObjects).isEmpty();
    }

    @Test
    void shouldChangeWaitMutex()
    {
        // given
        final var waitMutex = new Object();

        // when
        new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(4L))
            .withWaitMutex(waitMutex);

        // then
        assertThat(waitIntervals).isEmpty();
        assertThat(waiterMutexObjects).containsExactly(waitMutex);
    }

    @Test
    void shouldPassAssertionEvaluation()
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(1L))
            .withWaitInterval(300, TimeUnit.MILLISECONDS);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAllAssertionsArePassed(fail(3)));

        // then
        assertThat(caughtException).isNull();
        assertThat(waitIntervals).containsExactly(
            Duration.ofMillis(300),
            Duration.ofMillis(300),
            Duration.ofMillis(300));
    }

    @Test
    void shouldThrowExeceptionAfterExceedingTimeout()
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(4L))
            .withWaitInterval(1, TimeUnit.SECONDS);

        // when
        final var caughtException = catchThrowable(() -> tested.untilAllAssertionsArePassed(fail(5)));

        // then
        assertThat(caughtException).isInstanceOf(AssertionError.class);
        assertThat(waitIntervals).containsExactly(
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            Duration.ofSeconds(1));
    }

    @Test
    void shouldThrowExceptionOnNullWaitInterval()
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(1));

        // when
        final var caughtException = catchThrowable(() -> tested.withWaitInterval(null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("waitInterval is marked non-null but is null");
    }

    @Test
    void shouldThrowExceptionOnNullWaitIntervalUnit()
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(1));

        // when
        final var caughtException = catchThrowable(() -> tested.withWaitInterval(1, null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("unit is marked non-null but is null");
    }

    @Test
    void shouldThrowExceptionOnNullWaitMutex()
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(1));

        // when
        final var caughtException = catchThrowable(() -> tested.withWaitMutex(null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("waitMutex is marked non-null but is null");
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void shouldThrowExceptionOnInvalidWaitIntervalDuration(final long waitInterval)
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(1));

        // when
        final var caughtException = catchThrowable(() -> tested.withWaitInterval(Duration.ofMillis(waitInterval)));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("waitInterval must be greater than zero");
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void shouldThrowExceptionOnInvalidWaitInterval(final long waitInterval)
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(1));

        // when
        final var caughtException = catchThrowable(() -> tested.withWaitInterval(waitInterval, TimeUnit.MILLISECONDS));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("waitInterval must be greater than zero");
    }

    @Test
    void shouldThrowExceptionOnWaitIntervalGreaterThanTimeout()
    {
        // given
        final var tested = new AsyncAssertionsEvaluatorImpl(time, waiter, Duration.ofSeconds(1));

        // when
        final var caughtException = catchThrowable(() -> tested.withWaitInterval(Duration.ofMillis(1001L)));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("waitInterval must be lower than or equal to timeout");
    }

    private static Consumer<SoftAssertions> failOnce()
    {
        return fail(1);
    }

    private static Consumer<SoftAssertions> fail(final int times)
    {
        final var condition = new AtomicInteger(Math.max(0, times));
        return softly -> softly.assertThat(condition.getAndDecrement()).isZero();
    }
}
