package com.webfleet.assertj;

import static java.util.logging.Level.FINE;

import java.util.Optional;
import java.util.function.BiFunction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log
final class AsyncAssertionErrorCreator
{
    private static final String ASYNC_ASSERTION_ERROR_CLASS = "com.webfleet.assertj.AsyncAssertionError";
    private static final BiFunction<AsyncAssertTimeoutCondition, AssertionError, AssertionError> CREATOR =
        tryLoadAsyncAssertionErrorClass()
            .map(AsyncAssertionErrorCreator::asyncAssertionErrorCreator)
            .orElseGet(AsyncAssertionErrorCreator::fallbackCreator);

    static AssertionError create(@NonNull final AsyncAssertTimeoutCondition timeCondition, @NonNull final AssertionError error)
    {
        return CREATOR.apply(timeCondition, error);
    }

    private static Optional<Class<?>> tryLoadAsyncAssertionErrorClass()
    {
        try
        {
            return Optional.of(Class.forName(ASYNC_ASSERTION_ERROR_CLASS));
        }
        catch (@SuppressWarnings("unused") final ClassNotFoundException e)
        {
            LOG.log(FINE, "Could not load {0} class - provided opentest4j dependency not found in classpath", ASYNC_ASSERTION_ERROR_CLASS);
            return Optional.empty();
        }
    }

    private static BiFunction<AsyncAssertTimeoutCondition, AssertionError, AssertionError> asyncAssertionErrorCreator(final Class<?> asyncAssertionErrorClass)
    {
        final var method = ReflectionCall.run(() -> asyncAssertionErrorClass
            .getDeclaredMethod("create", String.class, AssertionError.class));
        return (timeCondition, error) -> ReflectionCall.run(() -> (AssertionError) method.invoke(null, createHeading(timeCondition), error));
    }

    private static BiFunction<AsyncAssertTimeoutCondition, AssertionError, AssertionError> fallbackCreator()
    {
        return (timecondition, error) -> {
            final var message = createHeading(timecondition) + "\n" + error.getMessage();
            return new AssertionError(message, error);
        };
    }

    private static String createHeading(final AsyncAssertTimeoutCondition timeCondition)
    {
        return String.format("Async assertion failed after exceeding %sms timeout", timeCondition.timeout().toMillis());
    }

    @FunctionalInterface
    private interface ReflectionCall<T>
    {
        T call() throws ReflectiveOperationException;

        static <T> T run(final ReflectionCall<T> action)
        {
            try
            {
                return action.call();
            }
            catch (final ReflectiveOperationException e)
            {
                throw new IllegalStateException("Reflection error", e);
            }
        }
    }
}
