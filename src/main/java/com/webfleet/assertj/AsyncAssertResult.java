package com.webfleet.assertj;

import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.function.Consumer;

import org.assertj.core.api.SoftAssertions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class AsyncAssertResult
{
    private static final AssertionError UNDEFINED_ERROR = new AssertionError("Failed to evaluate async assertions");

    private final AssertionError error;

    static AsyncAssertResult undefined()
    {
        return new AsyncAssertResult(UNDEFINED_ERROR);
    }

    static AsyncAssertResult evaluate(@NonNull final Consumer<SoftAssertions> assertionConfigurer)
    {
        final var assertions = new SoftAssertions();
        // catching error in case assertAll is called explicitly by the consumer
        final var caughtError = catchThrowableOfType(() -> assertionConfigurer.accept(assertions), AssertionError.class);
        if (caughtError != null)
        {
            return new AsyncAssertResult(caughtError);
        }
        return new AsyncAssertResult(catchThrowableOfType(assertions::assertAll, AssertionError.class));
    }

    boolean hasFailed()
    {
        return error != null;
    }

    void throwOnFailure(@NonNull final AsyncAssertAwaitConfig config)
    {
        if (hasFailed())
        {
            throw AsyncAssertionErrorCreator.create(config, error);
        }
    }
}
