# Release Notes and New & Noteworthy page

This is the New & Noteworthy page for CDT 12.3 which is part of Eclipse 2025-12 Simultaneous Release

---

# Release Notes

## Gdb Remote launch targets for Core Build projects.

Core Build CMake and Makefile projects now support the Gdb Remote TCP and Serial launch targets.

## Support for WinPTY terminals has been removed

Since CDT 10.6 the default settings in CDT has [been to not use WinPTY](https://github.com/eclipse-cdt/cdt/blob/main/NewAndNoteworthy/CDT-10.6.md#windows-pseudo-console-conpty-the-default).
With this release of CDT the WinPTY version of the code has been removed.
The WinPTY code was known to not work in many circumstances.
The replacement Windows Pseudo Console (ConPTY) API is much more stable.

# API Changes, current and planned


## Breaking API changes

Please see [CHANGELOG-API](CHANGELOG-API.md) for details on the breaking API changes in this release as well as future planned API changes.

# Noteworthy Issues and Pull Requests

See [Noteworthy issues and PRs](https://github.com/eclipse-cdt/cdt/issues?q=is%3Aclosed+label%3Anoteworthy+milestone%3A12.3.0) for this release in the issue/PR tracker.

# Bugs Fixed in this Release

See GitHub milestone for CDT [![12.3.0](https://img.shields.io/github/milestones/issues-total/eclipse-cdt/cdt/21)](https://github.com/eclipse-cdt/cdt/milestone/21?closed=1) and CDT LSP [![3.4.0](https://img.shields.io/github/milestones/issues-total/eclipse-cdt/cdt-lsp/8)](https://github.com/eclipse-cdt/cdt-lsp/milestone/8?closed=1)
