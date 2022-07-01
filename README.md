# AssertJ - Async

Extension for [AssertJ](https://github.com/assertj/assertj-core) API for making asynchronous assertions.

[![Build Status](https://github.com/Webfleet-Solutions/assertj-async/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Webfleet-Solutions/assertj-async/actions/workflows/build.yml)
[![CodeQL](https://github.com/Webfleet-Solutions/assertj-async/actions/workflows/codeql-analysis.yml/badge.svg?branch=main)](https://github.com/Webfleet-Solutions/assertj-async/actions/workflows/codeql-analysis.yml)
[![license](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://opensource.org/licenses/MIT)

## Usage

The entry point for making asynchronous assertion is `com.webfleet.assertj.AsyncAssertions` class.

To make the assertion first, you need to specify the timeout for your assertion:
* `awaitAtMost(Duration.ofSeconds(5))`
* `awaitAtMost(5, TimeUnit.SECONDS)`
* `awaitAtMostOneSecond()`
* `awaitAtMostTwoSeconds()`
* `awaitAtMostFiveSeconds()`
* `awaitAtMostFifteenSeconds()`
* `awaitAtMostThirtySeconds()`

Now you can optionally configure following parameters:
* **Check interval** - the time to be waited in between assertion checks:
  * `.withCheckInterval(Duration.ofMillis(500))`
  * `.withCheckInterval(500, TimeUnit.MILLISECONDS)`
  * By default, it's configured to `100ms` 
* **Wait mutex** - the object to be used for check interval wait logic:
  * `.usingWaitMutex(mutex)`
  * It can be used to optimize the wait time with `notifyAll()` call on state change ending the wait and forcing assertion check 

Finally, you can make your assertions by providing lambda consumer function for `SoftAssertions` object:
```java
awaitAtMostOneSecond().untilAssertions(async -> {
        async.assertThat(condition1).isTrue();
        async.assertThat(condition2).isTrue();
    });
```

The assertions will be periodically checked in provided check interval duration until success or exceeding the timeout.

When timeout is exceeded `AssertionError` will be thrown with error from last failed check.


## License

This code is licensed under [MIT License.](https://opensource.org/licenses/MIT) 