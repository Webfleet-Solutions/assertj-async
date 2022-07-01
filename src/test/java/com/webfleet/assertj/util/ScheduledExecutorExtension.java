package com.webfleet.assertj.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;


final class ScheduledExecutorExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver
{
    private ScheduledExecutorService executor;

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception
    {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception
    {
        if (executor != null)
        {
            executor.shutdownNow();
            executor = null;
        }
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        final var parameterType = parameterContext.getParameter().getType();
        return Executor.class.isAssignableFrom(parameterType) &&
            parameterType.isAssignableFrom(ScheduledExecutorService.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return executor;
    }
}
