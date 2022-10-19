# Release Notes and New & Noteworthy page

This is the New & Noteworthy page for CDT 11.0 which is part of Eclipse 2022-12 Simultaneous Release

---

# Release Notes

## Java 17

Jave 17 is now required to build and run Eclipse CDT. See https://github.com/eclipse-cdt/cdt/issues/80

# Debug

## C/C++ Dynamic Printf Breakpoints

Prior to CDT 11 the Dynamic Printf GUI was somewhat over restrictive rejecting formats and parameters that would have been valid for dprintf in the GDB CLI.
To achieve this improvement the checks that verified that the number of format specifiers matched the number of parameters has been removed.
The check that the required the format string to not end with a `)` has been removed as valid format parameters can have a closing paranthesis.
Instead of doing that check, the GUI now displays a `)` to make it obvious to users that the closing `)` should not be included in the setting.

<p align="center"><img src="images/CDT-11.0-dprintf-gui.png" width="50%"></p>

See [Bug 580873](https://bugs.eclipse.org/bugs/show_bug.cgi?id=580873).

# API Changes, current and planned

Please see [CHANGELOG-API](CHANGELOG-API.md) for details on the breaking API changes in this release as well as future planned API changes.

# Bugs Fixed in this Release

See [GitHub milestones](https://github.com/eclipse-cdt/cdt/milestone/2?closed=1) and for bugs that haven't been transitioned to GitHub please see Bugzilla report [Bugs Fixed in CDT 11.0](https://bugs.eclipse.org/bugs/buglist.cgi?bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&classification=Tools&product=CDT&query_format=advanced&resolution=FIXED&target_milestone=11.0.0).
