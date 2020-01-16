# Eclipse CDT

## Contributing

Please see [CONTRIBUTING.md] for contributing information

## Developing CDT

Most developers, developing CDT in the Eclipse IDE, should use https://wiki.eclipse.org/Getting_started_with_CDT_development.

## Command-line Build instructions with Maven

Eclipse CDT uses the standard Maven and Tycho workflow for building CDT using Maven 3.6.0 and Java 8. Therefore to package CDT do:

```
mvn package
```

and the resulting p2 repository will be in `releng/org.eclipse.cdt.repo/target/repository`

The current set of options to Maven used for building on the CI can be seen in the Jenkinsfiles
on cdt-infra: https://github.com/eclipse-cdt/cdt-infra/tree/master/jenkins/pipelines/cdt

To build CDT plug-ins you need a standard Maven & Java developement environment. The Dockerfiles used for CDT's images are
published in cdt-infra https://github.com/eclipse-cdt/cdt-infra/tree/master/docker. The requirements for running all tests
successfully and for rebuilding non-Java parts of CDT are much more extensive than standard Maven & Java and include
items such as GCC, GDB, yarn, Node, etc. Refer to the Dockerfiles for the current versions of those dependencies.

### Profiles

There are a number of profiles (-P to mvn) to control the behaviour of the build.

#### cdtRepo, simrelRepo, defaultCdtTarget

Individual p2 repos can be turned on and off to allow building CDT, or parts of CDT against
different target platforms easily.
For example, you can:
- test CDT against a pre-built CDT by using the cdtRepo profile.
- build the standalone rcp debugger against the latest simrel `mvn verify -DuseSimrelRepo -f debug/org.eclipse.cdt.debug.application.product`

#### build-standalone-debugger-rcp

Using the `build-standalone-debugger-rcp` profile will include the standalone debugger, located
in `debug/org.eclipse.cdt.debug.application.product`

#### skip-all-tests, skip-tests-except-cdt-ui, skip-tests-except-dsf-gdb, skip-tests-except-lsp, skip-tests-except-cdt-other

Using any of the above profiles can skip large sets of tests. The CI build uses this to parallelize tests. See https://ci.eclipse.org/cdt/view/Gerrit/

#### baseline-compare-and-replace

`baseline-compare-and-replace` profile controls whether baseline replace and compare
is performed. On a local build you want to avoid baseline replace and compare, 
especially if you have different versions of Java than the baseline was built with.

If you have the same version of Java as the build machine you can run baseline comparison and
replace. To do that run with the `baseline-compare-and-replace` profile.

Requires verify phase of maven to run, i.e. will not run with `mvn package` even if profile is specified.

#### production

Runs the production steps of the build. This profile can only be run on the CDT CI machines
as access to Eclipse key signing server is needed to sign the jars.

### Profiles

There are a number of properties (-D to mvn) to control the behaviour of the build. Refer to the
pom.xml for the full list. Many of the properties are not intended to be set at the command
line.

#### skipDoc

Documentation generation for CDT can be time consuming. For local builds this can be skipped
with `-DskipDoc=true`

#### skipTests

Running tests for CDT can be time consuming. For local builds this can be skipped
with `-DskipTests=true`.

#### jgit.dirtyWorkingTree-cdtDefault

Running a build with uncommitted changes will normally cause an error. To run a build with
uncommited changes use `-Djgit.dirtyWorkingTree-cdtDefault=warning`

#### dsf.gdb.tests.gdbPath

For running CDT's DSF-GDB tests, this specifies the path to the location of gdb.

#### cdt.tests.dsf.gdb.versions

For running CDT's DSF-GDB tests, this specifies the executable names of the gdbs to run, comma-separated.
