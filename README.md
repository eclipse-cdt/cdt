# Eclipse CDT

## Contributing

Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for contributing information

## Developing CDT

Most developers, developing CDT in the Eclipse IDE, should use [Getting_started_with_CDT_development](https://wiki.eclipse.org/Getting_started_with_CDT_development).

## Command-line Build instructions with Maven

Eclipse CDT uses the standard Maven and Tycho workflow for building CDT using Maven 3.6.0 and Java 8. Therefore to package CDT do:

```
mvn package
```

and the resulting p2 repository will be in `releng/org.eclipse.cdt.repo/target/repository`

The current set of options to Maven used for building on the CI can be seen in the Jenkinsfiles
on [cdt-infra](https://github.com/eclipse-cdt/cdt-infra/tree/master/jenkins/pipelines/cdt)

To build CDT plug-ins you need a standard Maven & Java developement environment. The Dockerfiles used for CDT's images are
published in [cdt-infra](https://github.com/eclipse-cdt/cdt-infra/tree/master/docker). The requirements for running all tests
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

#### regenHelp

Some of the help systems in Eclipse CDT require the `regenHelp` profile to rebuild their HTML from the source documents. For example, to regenerate the help
for Autotools or Meson do:

```
mvn generate-resources -DuseSimrelRepo -f build/org.eclipse.cdt.meson.docs -PregenHelp
```

```
mvn generate-resources -DuseSimrelRepo -f build/org.eclipse.cdt.autotools.docs -PregenHelp
```

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

#### jgit.dirtyWorkingTree-cdtDefault

Running a build with uncommitted changes will normally cause an error. To run a build with
uncommited changes use `-Djgit.dirtyWorkingTree-cdtDefault=warning`

#### dsf.gdb.tests.gdbPath

For running CDT's DSF-GDB tests, this specifies the path to the location of gdb.

#### cdt.tests.dsf.gdb.versions

For running CDT's DSF-GDB tests, this specifies the executable names of the gdbs to run, comma-separated.

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

`docker 'run' '--rm' '-t' '-v' 'D:\cdt\git\org.eclipse.cdt:/work' '-w' '/work/core/org.eclipse.cdt.core.native' 'quay.io/eclipse-cdt/cdt-infra-eclipse-full:latest' 'make' '-C' 'native_src' 'rebuild'`

See also `jniheaders` profile above.

DUMMY EDIT
