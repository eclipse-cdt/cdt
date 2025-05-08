# Eclipse CDT Command Line Build Instructions

This document covers how to build Eclipse CDT from the command line (e.g. like you may do on Jenkins).

## Contributing to and Editing Eclipse CDT

Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for contributing information, including setting up a development environment.

## Command-line Build instructions with Maven

Eclipse CDT uses the standard Maven and Tycho workflow for building CDT using Maven 3.9 and Java 21. Therefore to package CDT do:

```
mvn package
```

and the resulting p2 repository will be in `releng/org.eclipse.cdt.repo/target/repository`

The current set of options to Maven used for building on the CI can be seen in the [Jenkinsfile](https://github.com/eclipse-cdt/cdt/blob/main/Jenkinsfile) and GitHub actions [workflows](https://github.com/eclipse-cdt/cdt/tree/main/.github/workflows)

To build CDT plug-ins you need a standard Maven & Java development environment. The Dockerfiles used for CDT's images are published in [docker](https://github.com/eclipse-cdt/cdt/tree/main/docker). The requirements for running all tests successfully and for rebuilding non-Java parts of CDT are much more extensive than standard Maven & Java and include items such as GCC, GDB, yarn, Node, etc. Refer to the Dockerfiles for the current versions of those dependencies.

### Checking code cleanliness

The CI build automatically run code cleanliness checks. To run them on your computer it is recommended to use the Docker image, for example like this from the root of the checked out CDT repo:

```sh
docker run --rm -it -v $(git rev-parse --show-toplevel):/work -w /work/$(git rev-parse --show-prefix) --cap-add=SYS_PTRACE --security-opt seccomp=unconfined quay.io/eclipse-cdt/cdt-infra:latest releng/scripts/check_code_cleanliness.sh
```

#### Diagnosing code cleanliness failures

##### `baseline and build artifacts have same version but different contents`

An error like this:

```
Error:  Failed to execute goal org.eclipse.tycho:tycho-p2-plugin:4.0.12:p2-metadata (baselinereplace-p2-metadata) on project org.eclipse.cdt.ui: baseline and build artifacts have same version but different contents
Error:     no-classifier: different
Error:        org/eclipse/cdt/internal/ui/ImageCombo.class: different
```

may be caused because a dependency has changed API in a way that the same CDT source compiles to different binary.
To diagnose such a problem you can compare the disassembled class files to see what changed:

1. Run the check_code_cleanliness locally using docker (see above for command line)
2. extract the class from the baseline jar (e.g. `core/org.eclipse.cdt.ui/target/baseline/org.eclipse.cdt.ui-9.0.0.202502172234.jar`)
3. Using javap, disassemble the baseline and compiled class file. e.g:
  * `javap -c  baseline/osgi.bundle/org/eclipse/cdt/internal/ui/ImageCombo.class > baseline.asm`
  * `javap -c  classes/org/eclipse/cdt/internal/ui/ImageCombo.class > after.asm`
4. Compare the the asm files. e.g:
  * `diff baseline.asm after.asm` will output something like:

```java
270c270
<       17: invokespecial #132                // Method org/eclipse/swt/widgets/TypedListener."<init>":(Lorg/eclipse/swt/internal/SWTEventListener;)V
---
>       17: invokespecial #132                // Method org/eclipse/swt/widgets/TypedListener."<init>":(Ljava/util/EventListener;)V
```

5. With the above information you can track down what may have changed. e.g. See https://github.com/eclipse-cdt/cdt/pull/1159#issuecomment-2858751463 for the results of this worked example.

### Profiles

There are a number of profiles (-P to mvn) to control the behaviour of the build.

#### cdtRepo, simrelRepo, defaultCdtTarget

Individual p2 repos can be turned on and off to allow building CDT, or parts of CDT against different target platforms easily. For example, you can:
- test CDT against a pre-built CDT by using the cdtRepo profile.
- build the standalone rcp debugger against the latest simrel `mvn verify -DuseSimrelRepo -f debug/org.eclipse.cdt.debug.application.product`

#### skip-all-tests, skip-tests-except-cdt-ui, skip-tests-except-dsf-gdb, skip-tests-except-cdt-other

Using any of the above profiles can skip large sets of tests. The CI build uses this to parallelize tests. See https://ci.eclipse.org/cdt/view/Gerrit/

#### terminal-only

The terminal directory has a special profile that enables only the terminal and its dependencies when used. The allows
running maven like this `mvn -f terminal/pom.xml verify -P only-terminal` to build and test only the terminal
and its dependencies. A special terminal only p2 site is created in `terminal/repo/target/repository`. The CI build
uses this to speedup turnaround on changes only affecting the terminal. See https://ci.eclipse.org/cdt/view/Gerrit/

#### baseline-compare-and-replace

`baseline-compare-and-replace` profile controls whether baseline replace and compare
is performed. On a local build you want to avoid baseline replace and compare, 
especially if you have different versions of Java than the baseline was built with.

If you have the same version of Java as the build machine you can run baseline comparison and
replace. To do that run with the `baseline-compare-and-replace` profile.

Requires verify phase of maven to run, i.e. will not run with `mvn package` even if profile is specified.

#### api-baseline-check

`api-baseline-check` checks the API of CDT matches the [API policy](https://github.com/eclipse-cdt/cdt/blob/main/POLICY.md#api)

#### production

Runs the production steps of the build. This profile can only be run on the CDT CI machines
as access to Eclipse key signing server is needed to sign the jars.

#### jniheaders

The `jniheaders` profile can be used on the `core/org.eclipse.cdt.core.native` and
`native/org.eclipse.cdt.native.serial` to rebuild the header files for JNI natives.
See also `native` property below.

### Properties

There are a number of properties (-D to mvn) to control the behaviour of the build. Refer to the
pom.xml for the full list. Many of the properties are not intended to be set at the command
line.

#### skipDoc

Documentation generation for CDT can be time consuming. For local builds this can be skipped
with `-DskipDoc=true`

#### skipTests

Running tests for CDT can be time consuming. For local builds this can be skipped
with `-DskipTests=true`.

#### excludedGroups to skip slow or flaky tests

Some tests in CDT are fairly slow to run and rarely are exercising actively changing code. Some tests in CDT are fairly flaky to run and rarely are exercising actively changing code. These tests are excluded from the main CDT builds (both master/branch and gerrit verify jobs) and are instead run in a special job. Therefore the Jenkinsfiles for master/branch and gerrit use excludedGroups by default.

To skip slow tests use `-DexcludedGroups=slowTest`
To skip flaky tests use `-DexcludedGroups=flakyTest`
To skip both use `-DexcludedGroups=flakyTest,slowTest`

See section below on marking tests for how to annotate a test properly.

#### jgit.dirtyWorkingTree-cdtDefault

Running a build with uncommitted changes will normally cause an error. To run a build with
uncommited changes use `-Djgit.dirtyWorkingTree-cdtDefault=warning`

#### dsf.gdb.tests.gdbPath

For running CDT's DSF-GDB tests, this specifies the path to the location of gdb.

The default, defined in the root pom.xml, it is blank, which uses gdb from the `PATH`.
See BaseTestCase for more info.

#### cdt.tests.dsf.gdb.versions

For running CDT's DSF-GDB tests, this specifies the executable names of the gdbs to run, comma-separated.

There are a few special values that can be specified (see BaseParametrizedTestCase for source):

- all: run all versions listed in ITestConstants.ALL_KNOWN_VERSIONS
- supported: run all versions listed in ITestConstants.ALL_SUPPORTED_VERSIONS
- unsupported: run all versions listed in ITestConstants.ALL_UNSUPPORTED_VERSIONS

The default, defined in the root pom.xml, it is blank, which uses `gdb` and `gdbserver`.
See BaseParametrizedTestCase for more info.

To build all gdb versions for testing CDT see [download-build-gdb.sh](https://github.com/eclipse-cdt/cdt/blob/main/docker/scripts/download-build-gdb.sh)

#### native

The `native` property can be used to build the native libraries. Defining the `native` property will activate profiles to add the extra steps to compile the natives libraries used by CDT. The main CDT build by default will not build the libraries, but instead use the versions of the libraries checked into git. Therefore when users modify the sources of the native libraries, they have to build and commit the changed library binaries as part of the commit.

The `releng/scripts/check_code_cleanliness.sh`, which is run on the build machine as part of the gerrit and main build flows, will ensure that the libraries that are checked in are indeed up to date with their sources.

The `native` property can be one of the following:

- `linux.x86_64` - uses local tools and builds only linux.x86_64 libraries
- `linux.ppc64le` - uses local tools and builds only linux.ppc64le libraries
- `docker` - uses CDT's docker releng images to do the native builds for all platforms
- `all` - uses local tools to do the native builds for all platforms

Therefore to build all the natives using docker add `-Dnative=docker` to your maven command line (e.g. `mvn verify -Dnative=docker`). 

To build only the native libraries `mvn process-resources` can be used on the individual bundles with the simrel target platform, e.g.:

- Serial library: `mvn process-resources -Dnative=docker  -DuseSimrelRepo -f native/org.eclipse.cdt.native.serial`
- Core library: `mvn process-resources -Dnative=docker  -DuseSimrelRepo -f core/org.eclipse.cdt.core.native`

However, the challenge is that dll files on Windows have a timestamp in them. To have reproducible builds, we need to have a reproducible timestamp. As [Microsoft](https://devblogs.microsoft.com/oldnewthing/20180103-00/?p=97705) has moved away from using a timestamp to rather use a hash of the source files as the value, we therefore hash the source files used by the library and the header files for the Java API and use that as the value.

An additional tip is to set the following in `.gitconfig` to allow you to diff `.dll` files. This will show the timestamp of the DLL in the diff as part of the DLL headers.

```
[diff "dll"]
    textconv = objdump -x
    binary = true
```

When the host is Windows, getting docker to behave as encoded in the pom.xml may be challenging, instead a command like this will probably work (replace your path to git root). Note that running this in git bash causes problems because of the /work in the command line arguments. (TODO integrate this command line way of running into the pom.xml so the original instructions work.)

`docker 'run' '--rm' '-t' '-v' 'D:\cdt\git\org.eclipse.cdt:/work' '-w' '/work/core/org.eclipse.cdt.core.native' 'quay.io/eclipse-cdt/cdt-infra:latest' 'make' '-C' 'native_src' 'rebuild'`

See also `jniheaders` profile above.

