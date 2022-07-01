package com.webfleet.assertj;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.summingLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


final class MockTime implements Time
{
    private final ElapsedTime elapsedTime = spy(ElapsedTime.class);
    private final WaitCondition waitCondition = mock(WaitCondition.class);
    private final List<Duration> waitIntervals = new ArrayList<>();
    private final List<Object> waitMutexObjects = new ArrayList<>();

    static MockTime create()
    {
        final var mockTime = new MockTime();
        doAnswer(args -> mockTime.waitIntervals.add(args.getArgument(0)))
            .when(mockTime.waitCondition).waitFor(any());
        doAnswer(args -> mockTime.computeElapsedTime())
            .when(mockTime.elapsedTime).get();
        return mockTime;
    }

    @Override
    public ElapsedTime measure()
    {
        return elapsedTime;
    }

    @Override
    public WaitCondition waitCondition(final Object mutex)
    {
        waitMutexObjects.add(mutex);
        return waitCondition;
    }

    List<Duration> waitIntervals()
    {
        return unmodifiableList(waitIntervals);
    }

    List<Object> waitMutexObjects()
    {
        return unmodifiableList(waitMutexObjects);
    }

    private Duration computeElapsedTime()
    {
        return Duration.ofMillis(waitIntervals.stream().collect(summingLong(Duration::toMillis)));
    }

}
