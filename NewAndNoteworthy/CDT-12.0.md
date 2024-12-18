# Release Notes and New & Noteworthy page

This is the New & Noteworthy page for CDT 12.0 which is part of Eclipse 2025-03 Simultaneous Release

---

# Release Notes

## Minimum version of GLIBC required

The minimum version of GLIBC required is now 2.31.
This version can be found in Ubuntu 20.04 and later, RHEL 9.0 and later and other distros as well.
CDT's native components will likely work with older versions of glibc too, assuming they provide the required APIs for Eclipse CDT.

# Managed Build

## Other objects for GNU archive files

The managed build system now provides an option to specify other object files to be included when building GNU archive files:

<p align="center"><img src="images/CDT-12.0-gnu-ar-other-objects.png" width="80%"></p>

The new option applies to static library projects using a _Cross GCC_, _Cygwin GCC_, _Linux GCC_ or _MinGW GCC_ toolchain.

# API Changes, current and planned

## Breaking API changes

Please see [CHANGELOG-API](CHANGELOG-API.md) for details on the breaking API changes in this release as well as future planned API changes.

# Noteworthy Issues and Pull Requests

See [Noteworthy issues and PRs](https://github.com/eclipse-cdt/cdt/issues?q=is%3Aclosed+label%3Anoteworthy+milestone%3A12.0.0) for this release in the issue/PR tracker.

## Improved launch configuration for CMake and other Core Build System projects

The CMake and CBS Makefile project launch configurations for local applications have an improved Main tab and new Arguments, Environment, Debugger, Source, and Common tabs.

# Bugs Fixed in this Release

See [GitHub milestones](https://github.com/eclipse-cdt/cdt/milestone/11?closed=1)
