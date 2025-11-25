# Release Notes and New & Noteworthy page

This is the New & Noteworthy page for CDT 12.3 which is part of Eclipse 2025-12 Simultaneous Release

---

# Release Notes

## CMake toolchain selection is respected

[See Issue #1140](https://github.com/eclipse-cdt/cdt/issues/1140)

When you choose a **GCC** or **Clang** toolchain for a CMake project in **Launch Bar → Launch Configuration → Build Settings**, that selection is now honoured during builds.

The environment variables `CC` (C compiler) and `CXX` (C++ compiler) are set to the chosen compiler in the process that runs CMake. During **configure**, CMake reads these and maps them to `CMAKE_C_COMPILER` and `CMAKE_CXX_COMPILER`.

### Precedence

- If `CC`/`CXX` are already defined in the system environment (e.g., `gcc`/`g++`) but a **Clang** toolchain is selected in Build Settings, the Build Settings selection **overrides** the system environment and Clang is used.

- Explicit CMake cache variables take precedence over Build Settings `CC`/`CXX` environment variables. If you pass `-D CMAKE_C_COMPILER=clang -D CMAKE_CXX_COMPILER=clang++` in **Additional CMake arguments** while the Build Settings toolchain is set to GCC, the build will use **Clang**.

> Note: CMake chooses compilers at the first configure of a build directory. If you change toolchains, re-configure with a clean cache or a new build directory.

More information: [CMake FAQ - Using a different compiler](https://gitlab.kitware.com/cmake/community/-/wikis/FAQ#how-do-i-use-a-different-compiler).


## Gdb Remote launch targets for Core Build projects.

Core Build CMake and Makefile projects now support the Gdb Remote TCP and Serial launch targets.

## Support for WinPTY terminals has been removed

Since CDT 10.6 the default settings in CDT has [been to not use WinPTY](https://github.com/eclipse-cdt/cdt/blob/main/NewAndNoteworthy/CDT-10.6.md#windows-pseudo-console-conpty-the-default).
With this release of CDT the WinPTY version of the code has been removed.
The WinPTY code was known to not work in many circumstances.
The replacement Windows Pseudo Console (ConPTY) API is much more stable.

## Clearer wizards for Makefile projects

Avoiding confusion by naming legacy Managed Build System projects "Classic C/C++" and
Core Build System projects "Core Makefile".

## Enhanced LLDB debugger support

The _Memory_ view now supports the editing of memory when debugging using LLDB.
The _Variables_ view now supports the editing of local variables when debugging using LLDB.
C/C++ Watchpoints may now be set on variables in the _Expressions_ view and modified from the _Breakpoint Properties_ dialog via the _Breakpoints_ view.
These enhancements require installation of the optional [C/C++ LLDB Debugger Integration](https://github.com/eclipse-cdt/cdt/tree/main/FAQ#how-do-i-install-the-lldb-debugger-integration) feature and a build of the `lldb-mi` tool from sources more recent than version 0.0.1.
On macOS hosts, `lldb-mi` may be built from the latest sources and installed using the following command:

```
brew install --HEAD eclipse-cdt/tools/lldb-mi
```

# API Changes, current and planned


## Breaking API changes

Please see [CHANGELOG-API](CHANGELOG-API.md) for details on the breaking API changes in this release as well as future planned API changes.

# Noteworthy Issues and Pull Requests

See [Noteworthy issues and PRs](https://github.com/eclipse-cdt/cdt/issues?q=is%3Aclosed+label%3Anoteworthy+milestone%3A12.3.0) for this release in the issue/PR tracker.

# Bugs Fixed in this Release

See GitHub milestone for CDT [![12.3.0](https://img.shields.io/github/milestones/issues-total/eclipse-cdt/cdt/21)](https://github.com/eclipse-cdt/cdt/milestone/21?closed=1) and CDT LSP [![3.4.0](https://img.shields.io/github/milestones/issues-total/eclipse-cdt/cdt-lsp/8)](https://github.com/eclipse-cdt/cdt-lsp/milestone/8?closed=1)
