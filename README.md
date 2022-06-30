# AssertJ - Async

AssertJ extension for making asynchronous assertions.

## Usage

The entry point for making asynchronous assertion is `AsyncAssertions` class.

Here is an example of test:
```java
class AsyncAssertionExampleTest
{
    @Test
    void shouldReceiveChangeMessages() {
        // given
        var receivedMessages = new ConcurrentLinkedQueue<String>();

        // when
        listenToChanges(receivedMessages::offer);

        // then
        awaitAtMostOneSecond().untilAssertions(async -> async
            .assertThat(receivedMessages).containsExactly("A", "B", "C"));
    }
    
    private static void listenToChanges(Consumer<String> consumer) {
        // simulation of asynchronous consumer
        var scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> consumer.accept("A"), 100L, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> consumer.accept("B"), 200L, TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> consumer.accept("C"), 300L, TimeUnit.MILLISECONDS);
    }
}
```