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

import com.webfleet.assertj.Time.ElapsedTime;


@ExtendWith(SoftAssertionsExtension.class)
class AsyncAssertAwaitConfigTest
{
    @Test
    void shouldThrowExceptionOnCreationWithNullTimeout()
    {
        // when
        final var caughtException = catchThrowable(() -> AsyncAssertAwaiConfig.withTimeout(null));

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
        final var caughtException = catchThrowable(() -> AsyncAssertAwaiConfig.withTimeout(timeout));

        // then
        assertThat(caughtException)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("timeout must be greater than zero");
    }

    @ParameterizedTest
    @CsvSource(
        value = {
                // timeout | expected check interval
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
    void shouldComputeInitialCheckIntervalBasedOnTimeout(final Duration timeout,
                                                         final Duration expectedCheckInterval,
                                                         final SoftAssertions softly)
    {
        // when
        final var tested = AsyncAssertAwaiConfig.withTimeout(timeout);

        // then
        softly.assertThat(tested.checkInterval()).isEqualTo(expectedCheckInterval);
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
    void shouldReturnAsyncAssertAwaitConfigWithChangedWaitInterval(final Duration timeout,
                                                                   final Duration waitInterval,
                                                                   final SoftAssertions softly)
    {
        // given
        final var tested = AsyncAssertAwaiConfig.withTimeout(timeout);

        // when
        final var withWaitInterval = tested.withCheckInterval(waitInterval);

        // then
        softly.assertThat(withWaitInterval.checkInterval()).isEqualTo(waitInterval);
        softly.assertThat(tested).isNotEqualTo(withWaitInterval);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
                // timeout | configured check interval | elapsed time | expected check interval
                //---------+---------------------------|--------------|-------------------------
                "PT5S      | PT1S                      | PT0S         | PT1S",
                "PT5S      | PT1S                      | PT1S         | PT1S",
                "PT5S      | PT1S                      | PT2S         | PT1S",
                "PT5S      | PT1S                      | PT3S         | PT1S",
                "PT5S      | PT1S                      | PT4S         | PT1S",
                "PT5S      | PT1S                      | PT4.100S     | PT0.900S",
                "PT5S      | PT1S                      | PT4.200S     | PT0.800S",
                "PT5S      | PT1S                      | PT4.500S     | PT0.500S",
                "PT5S      | PT1S                      | PT4.900S     | PT0.100S",
                "PT5S      | PT1S                      | PT4.999S     | PT0.001S",
                "PT5S      | PT1S                      | PT5S         | PT0S",
                "PT5S      | PT1S                      | PT6S         | PT0S",
        },
        delimiter = '|')
    void shouldShortenCheckIntervalWhenAddedToElapsedTimeExceedsTimeout(final Duration timeout,
                                                                        final Duration configuredCheckInterval,
                                                                        final Duration elapsedTimeDuration,
                                                                        final Duration expectedCheckInterval)
    {
        // given
        final var config = AsyncAssertAwaiConfig.withTimeout(timeout)
            .withCheckInterval(configuredCheckInterval);
        final ElapsedTime elapsedTime = () -> elapsedTimeDuration;

        // when
        final var checkInterval = config.checkInterval(elapsedTime);

        // then
        assertThat(checkInterval).isEqualTo(expectedCheckInterval);
    }

    @Test
    void shouldThrowExceptionWhenChangedCheckIntervalIsGreaterThanTimeout()
    {
        // given
        final var timeout = Duration.ofSeconds(5);
        final var tested = AsyncAssertAwaiConfig.withTimeout(timeout);

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
        final var tested = AsyncAssertAwaiConfig.withTimeout(Duration.ofSeconds(1L));

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
        final var tested = AsyncAssertAwaiConfig.withTimeout(Duration.ofSeconds(1L));

        // when
        final var caughtException = catchThrowable(() -> tested.withCheckInterval(null));

        // then
        assertThat(caughtException)
            .isInstanceOf(NullPointerException.class)
            .hasMessage("checkInterval is marked non-null but is null");
    }
}
