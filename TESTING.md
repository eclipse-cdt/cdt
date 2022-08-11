# Eclipse CDT Testing notes

This document is a collection of various notes on testing and writing JUnit tests in Eclipse CDT.

## Contributing to and Editing Eclipse CDT

Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for contributing information, including setting up a development environment.

### Marking tests as Slow or Flaky

Tests in CDT can be marked as Slow or Flaky to prevent them running as part of the standard test suites. See excludedGroups to skip slow or flaky tests sections above.

The proper way to mark a test as slow or flaky is to add a JUnit5 @Tag on the test with `flakyTest` or `slowTest`. The canonical values for these are in the JUnit5 base test `org.eclipse.cdt.core.testplugin.util.BaseTestCase5`.

These tags can only be applied to JUnit5 (aka Jupiter) tests. If a test needs converting, do that in a separate commit before adding the tags so that the test refactoring can be verified before excluding the test from normal runs.

### Converting tests to JUnit5

To take advantage of new features, such as excluding flaky and slow tests, individual tests need to JUnit5 (aka Jupiter). If a test is currently written in JUnit4 or JUnit3 style it needs to be converted to JUnit5 first. Those tests that currently derive from `org.eclipse.cdt.core.testplugin.util.BaseTestCase` can change to `org.eclipse.cdt.core.testplugin.util.BaseTestCase5` and make further adjustments. Common adjustments are:
- refactoring `setUp`/`tearDown` methods to use `@BeforeEach` and `@AfterEach` annotations
- refactor complicated uses of TestSuites in JUnit3 that were workarounds for the lack of JUnit features like `@BeforeAll` and `@AfterAll`.
- add `@Test` annotation (make sure to use `org.junit.jupiter.api.Test` and not JUnit4's `org.junit.Test`)
- statically import assert methods from `org.junit.jupiter.api.Assertions` (note that in JUnit5 the message is now last instead of first, this generally leads to an error by changing the imports, except in the case of `assertEquals` where the first and third parameter are `String`)
