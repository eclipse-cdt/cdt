# Release Notes and New & Noteworthy page

This is the New & Noteworthy page for CDT 12.0 which is part of Eclipse 2025-03 Simultaneous Release

---

# Release Notes

# Managed Build

## Other objects for GNU archive files

The managed build system now provides an option to specify other object files to be included when building GNU archive files:

<p align="center"><img src="images/CDT-12.0-gnu-ar-other-objects.png" width="80%"></p>

The new option applies to static library projects using a _Cross GCC_, _Cygwin GCC_, _Linux GCC_ or _MinGW GCC_ toolchain.

# Debug

## DSF Preference Pages always visible

The DSF Preference pages (Preferences -> C/C++ -> Debug -> GDB and children) are not always shown to users.
Prior to CDT 12 these were not visible until after the first debug session was started.

At the first debug session CDT enables the [activity](https://help.eclipse.org/latest/topic/org.eclipse.platform.doc.isv/reference/extension-points/org_eclipse_ui_activities.html) for DSF (`org.eclipse.cdt.debug.dsfgdbActivity`), with this change nothing in CDT is bound to this activity key anymore.

## Preferences -> Run/Debug -> View Performance relocated

The View Performance preference page, which is CDT specific, has been relocated to the Preferences -> C/C++ -> Debug section of preferences.
This only affects where in the Preferences tree the page is located, the preferences and key names have not changed.
In addition, this page is always visible.

# API Changes, current and planned

## Breaking API changes

Please see [CHANGELOG-API](CHANGELOG-API.md) for details on the breaking API changes in this release as well as future planned API changes.

# Noteworthy Issues and Pull Requests

See [Noteworthy issues and PRs](https://github.com/eclipse-cdt/cdt/issues?q=is%3Aclosed+label%3Anoteworthy+milestone%3A12.0.0) for this release in the issue/PR tracker.

# Bugs Fixed in this Release

See [GitHub milestones](https://github.com/eclipse-cdt/cdt/milestone/11?closed=1)
