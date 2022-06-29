package com.webfleet.assertj;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.error.AssertJMultipleFailuresError;
import org.opentest4j.MultipleFailuresError;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class AsyncAssertionsResult
{
    private static final AssertionError UNDEFINED_ERROR = new AssertionError("Failed to evaluate assertions");

    private final AssertionError error;

    static AsyncAssertionsResult undefined()
    {
        return new AsyncAssertionsResult(UNDEFINED_ERROR);
    }

    static AsyncAssertionsResult evaluate(@NonNull final Consumer<SoftAssertions> assertionConfigurer)
    {
        final var assertions = new SoftAssertions();
        assertionConfigurer.accept(assertions);
        final var caughtError = catchThrowableOfType(assertions::assertAll, AssertionError.class);
        return new AsyncAssertionsResult(caughtError);
    }

    boolean hasFailed()
    {
        return error != null;
    }

    void failOnErrorWithTimeout(final Duration timeout)
    {
        if (hasFailed())
        {
            final var heading = String.format("Async assertions failed after exceeding %sms timeout", timeout.toMillis());
            throw new AssertionError(new AssertJMultipleFailuresError(heading, assertionErrors()).getMessage());
        }
    }

    private List<? extends Throwable> assertionErrors()
    {
        if (error instanceof MultipleFailuresError)
        {
            return ((MultipleFailuresError) error).getFailures();
        }
        return singletonList(error);
    }
}
