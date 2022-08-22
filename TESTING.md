# Eclipse CDT Testing notes

This document is a collection of various notes on testing and writing JUnit tests in Eclipse CDT.

## Contributing to and Editing Eclipse CDT

Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for contributing information, including setting up a development environment.

### How do I run CDT JUnit test suite?

There are special Java packages to test CDT itself.
Their names generally end with suffix ".tests".
They are normally run during the build of CDT for example you can see results for Jenkins builds [here](https://ci.eclipse.org/cdt/job/cdt/job/main/lastCompletedBuild/testReport).

You can run JUnit tests in UI this way:

1. Select test CDT package (ends with ".tests", for example org.eclipse.cdt.core.tests).
2. Right-click on the class, suite, package, source folder or project you want to run in Package Explorer and select Run As-\>JUnit Plugin Test.

The build machine generally use the standard [Tycho Surefire](https://www.eclipse.org/tycho/sitedocs/tycho-surefire-plugin/plugin-info.html) class name patterns for [includes](https://www.eclipse.org/tycho/sitedocs/tycho-surefire-plugin/integration-test-mojo.html#includes) and [excludes](https://www.eclipse.org/tycho/sitedocs/tycho-surefire-plugin/integration-test-mojo.html#excludes) to identify tests during automated builds.

![run_junit_tests.png](images/run_junit_tests.png "screenshot of how to run junit plug-in tests")


Some sets of tests are grouped in "suites", these can be used to run subsets of tests that may exist across numerous packages.
An example is the CDT core's [AutomatedIntegrationSuite](https://github.com/eclipse-cdt/cdt/blob/main/core/org.eclipse.cdt.core.tests/suite/org/eclipse/cdt/core/suite/AutomatedIntegrationSuite.java).

Note that there could be intermittent failures in random tests, if you are getting those, try to rerun the tests.
Tests can be marked as flaky, see the subsequent section for more details.

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
