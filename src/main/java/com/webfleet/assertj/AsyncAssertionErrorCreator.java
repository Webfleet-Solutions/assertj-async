package com.webfleet.assertj;

import static java.util.Collections.singletonList;
import static java.util.logging.Level.FINE;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.assertj.core.api.SoftAssertionError;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log
final class AsyncAssertionErrorCreator
{
    /**
     * The {@link AsyncAssertionError} requires dependency to `opentest4j` which is optional in compileOnly scope.
     * To enable the usage in JUnit4 the class is loaded with reflections.
     * If class couldn't be loaded, alternative exception creation method is used  {@link AsyncAssertionErrorCreator#fallbackCreator()}.
     */
    private static final String ASYNC_ASSERTION_ERROR_CLASS = "com.webfleet.assertj.AsyncAssertionError";

    private static final BiFunction<AsyncAssertAwaitConfig, AssertionError, AssertionError> CREATOR =
        tryLoadAsyncAssertionErrorClass()
            .map(AsyncAssertionErrorCreator::asyncAssertionErrorCreator)
            .orElseGet(AsyncAssertionErrorCreator::fallbackCreator);

    static AssertionError create(@NonNull final AsyncAssertAwaitConfig config, @NonNull final AssertionError error)
    {
        return CREATOR.apply(config, error);
    }

    private static Optional<Class<?>> tryLoadAsyncAssertionErrorClass()
    {
        try
        {
            return Optional.of(Class.forName(ASYNC_ASSERTION_ERROR_CLASS));
        }
        catch (@SuppressWarnings("unused") final ClassNotFoundException | NoClassDefFoundError e)
        {
            LOG.log(FINE, "Could not load {0} class - provided opentest4j dependency not found in classpath", ASYNC_ASSERTION_ERROR_CLASS);
            return Optional.empty();
        }
    }

    private static BiFunction<AsyncAssertAwaitConfig, AssertionError, AssertionError> asyncAssertionErrorCreator(final Class<?> asyncAssertionErrorClass)
    {
        final var method = ReflectionCall.run(() -> asyncAssertionErrorClass
            .getDeclaredMethod("create", String.class, AssertionError.class));
        return (config, error) -> ReflectionCall.run(() -> (AssertionError) method.invoke(null, createHeading(config), error));
    }

    private static BiFunction<AsyncAssertAwaitConfig, AssertionError, AssertionError> fallbackCreator()
    {
        return (config, error) -> {
            final var errors = aggregateErrors(error);
            if (errors.size() == 1)
            {
                return new AssertionError(createHeading(config) + "\n" + errors.get(0));
            }
            final var message = new StringBuilder(createHeading(config))
                .append(" (failures ").append(errors.size()).append(")\n")
                .append(IntStream.range(0, errors.size())
                    .mapToObj(i -> "-- failure " + (i + 1) + " --" + errors.get(i))
                    .collect(joining("\n")))
                .toString();
            return new AssertionError(message, error);
        };
    }

    private static List<String> aggregateErrors(final AssertionError error)
    {
        if (error instanceof SoftAssertionError)
        {
            return ((SoftAssertionError) error).getErrors();
        }
        return singletonList(error.getMessage());
    }

    private static String createHeading(final AsyncAssertAwaitConfig config)
    {
        return String.format("Async assertion failed after exceeding %sms timeout", config.timeout().toMillis());
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
