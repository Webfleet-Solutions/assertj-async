package com.webfleet.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.opentest4j.MultipleFailuresError;


class AsyncAssertionErrorCreatorTest
{
    private static final AsyncAssertAwaitConfig CONFIG = AsyncAssertAwaitConfig.withTimeout(Duration.ofMillis(1500L));

    @Test
    void shouldCreateAsyncAssertionErrorForSingleError()
    {
        // given
        final var error = new AssertionError("test");

        // when
        final var asyncAssertionError = AsyncAssertionErrorCreator.create(CONFIG, error);

        // then
        assertThat(asyncAssertionError)
            .isInstanceOf(AsyncAssertionError.class)
            .hasMessageContaining("Async assertion failed after exceeding 1500ms timeout (1 failure)")
            .hasMessageContaining("-- failure 1 --test");
    }

    @Test
    void shouldCreateAsyncAssertionErrorForMultipleErrors()
    {
        // given
        final var error1 = new AssertionError("error-1");
        final var error2 = new AssertionError("error-2");
        final var error3 = new AssertionError("error-3");
        final var multiErrors = new MultipleFailuresError("we have some errors here", List.of(error1, error2, error3));

        // when
        final var asyncAssertionError = AsyncAssertionErrorCreator.create(CONFIG, multiErrors);

        // then
        assertThat(asyncAssertionError)
            .isInstanceOf(AsyncAssertionError.class)
            .hasMessageContaining("Async assertion failed after exceeding 1500ms timeout (3 failures)")
            .hasMessageContaining("-- failure 1 --error-1")
            .hasMessageContaining("-- failure 2 --error-2")
            .hasMessageContaining("-- failure 3 --error-3");
    }
}
