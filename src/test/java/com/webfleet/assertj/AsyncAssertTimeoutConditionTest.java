package com.webfleet.assertj;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


@ExtendWith(SoftAssertionsExtension.class)
class AsyncAssertTimeoutConditionTest
{
    @Test
    void shouldThrowExceptionOnCreationWithNullTimeout()
    {
        // when
        final var caughtException = catchThrowable(() -> AsyncAssertTimeoutCondition.withTimeout(null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("timeout is marked non-null but is null");
    }

    @ParameterizedTest
    @CsvSource({"-PT0.001S", "PT0S"})
    void shouldThrowExceptionOnCreationWithNegativeOrZeroTimeout(final Duration timeout)
    {
        // when
        final var caughtException = catchThrowable(() -> AsyncAssertTimeoutCondition.withTimeout(timeout));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("timeout must be greater than zero");
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
    void shouldComputeInitialWaitIntervalBasedOnTimeout(final Duration timeout,
                                                        final Duration expectedWaitInterval,
                                                        final SoftAssertions softly)
    {
        // when
        final var tested = AsyncAssertTimeoutCondition.withTimeout(timeout);

        // then
        softly.assertThat(tested.checkInterval()).isEqualTo(expectedWaitInterval);
        softly.assertThat(tested.timeout()).isEqualTo(timeout);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
                // timeout | wait interval
                //---------+------------------------
                "PT1S      | PT0.500S",
                "PT1S      | PT1S",
        },
        delimiter = '|')
    void shouldReturnAsyncAssertTimeoutConditionWithChangedWaitInterval(final Duration timeout,
                                                                        final Duration waitInterval,
                                                                        final SoftAssertions softly)
    {
        // given
        final var tested = AsyncAssertTimeoutCondition.withTimeout(timeout);

        // when
        final var withWaitInterval = tested.withCheckInterval(waitInterval);

        // then
        softly.assertThat(withWaitInterval.checkInterval()).isEqualTo(waitInterval);
        softly.assertThat(tested).isNotEqualTo(withWaitInterval);
    }

    @Test
    void shouldThrowExceptionWhenChangedCheckIntervalIsGreaterThanTimeout()
    {
        // given
        final var timeout = Duration.ofSeconds(5);
        final var tested = AsyncAssertTimeoutCondition.withTimeout(timeout);

        // when
        final var caughtException = catchThrowable(() -> tested.withCheckInterval(timeout.plusMillis(1)));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("checkInterval must be lower than or equal to timeout");
    }

    @ParameterizedTest
    @CsvSource({"-PT0.001S", "PT0S"})
    void shouldThrowExceptionWhenChangedCheckIntervalIsNegativeOrZero(final Duration checkInterval)
    {
        // given
        final var tested = AsyncAssertTimeoutCondition.withTimeout(Duration.ofSeconds(1L));

        // when
        final var caughtException = catchThrowable(() -> tested.withCheckInterval(checkInterval));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("checkInterval must be greater than zero");
    }

    @Test
    void shouldThrowExceptionWhenChangedCheckIntervalIsNull()
    {
        // given
        final var tested = AsyncAssertTimeoutCondition.withTimeout(Duration.ofSeconds(1L));

        // when
        final var caughtException = catchThrowable(() -> tested.withCheckInterval(null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("checkInterval is marked non-null but is null");
    }
}
