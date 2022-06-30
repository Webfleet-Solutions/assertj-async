package com.webfleet.assertj;

import static java.util.Collections.singletonList;

import java.util.List;

import org.assertj.core.error.AssertJMultipleFailuresError;
import org.opentest4j.MultipleFailuresError;

import lombok.NonNull;


final class AsyncAssertionError extends AssertJMultipleFailuresError
{
    private static final long serialVersionUID = 5698500663401729094L;

    private AsyncAssertionError(final String heading, final List<? extends Throwable> failures)
    {
        super(heading, failures);
    }

    static AssertJMultipleFailuresError create(@NonNull final String heading, @NonNull final AssertionError error)
    {
        if (error instanceof MultipleFailuresError)
        {
            return new AsyncAssertionError(heading, ((MultipleFailuresError) error).getFailures());
        }
        return new AsyncAssertionError(heading, singletonList(error));
    }
}
