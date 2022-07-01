package com.webfleet.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.SoftAssertionError;
import org.junit.Test;


/**
 * Test checking compatibility with JUnit4.
 */
public class AsyncAssertionErrorCreatorJUnit4Test
{
    private static final AsyncAssertAwaitConfig CONFIG = AsyncAssertAwaitConfig.withTimeout(Duration.ofSeconds(4));

    @Test
    public void shouldCreateAsyncAssertionErrorForSingleError()
    {
        // given
        final var error = new AssertionError("test");

        // when
        final var asyncAssertionError = AsyncAssertionErrorCreator.create(CONFIG, error);

        // then
        assertThat(asyncAssertionError)
            .isInstanceOf(AssertionError.class)
            .hasMessage("Async assertion failed after exceeding 4000ms timeout\n"
                + "test");
    }

    @Test
    public void shouldCreateAssertionErrorForSingleSoftAssertionError()
    {
        // given
        final var multiErrors = new SoftAssertionError(List.of("my error"));

        // when
        final var asyncAssertionError = AsyncAssertionErrorCreator.create(CONFIG, multiErrors);

        // then
        assertThat(asyncAssertionError)
            .isInstanceOf(AssertionError.class)
            .hasMessage("Async assertion failed after exceeding 4000ms timeout\n"
                + "my error");
    }

    @Test
    public void shouldCreateAssertionErrorForMultipleErrors()
    {
        // given
        final var multiErrors = new SoftAssertionError(List.of("error-1", "error-2", "error-3"));

        // when
        final var asyncAssertionError = AsyncAssertionErrorCreator.create(CONFIG, multiErrors);

        // then
        assertThat(asyncAssertionError)
            .isInstanceOf(AssertionError.class)
            .hasMessage("Async assertion failed after exceeding 4000ms timeout (failures 3)\n"
                + "-- failure 1 --error-1\n"
                + "-- failure 2 --error-2\n"
                + "-- failure 3 --error-3");
    }
}
