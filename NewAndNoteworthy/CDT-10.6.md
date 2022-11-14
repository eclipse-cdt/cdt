# Release Notes and New & Noteworthy page

This is the New & Noteworthy page for CDT 10.6 which is part of Eclipse 2022-03 Simultaneous Release

---

# Release Notes

## CDT Update Site enabled by default

When installing Eclipse CDT, the update site for the latest Eclipse CDT is added to the available software sites by default to allow Check for Updates to notify users that a new version of CDT is available. See [Bug 575046](https://bugs.eclipse.org/bugs/show_bug.cgi?id=575046).

## org.eclipse.remote part of CDT project

The org.eclipse.remote code base is now part of the Eclipse CDT project.
The code has been merged into the CDT repo in the [remote](../remote/) directory with full git history retained. Releases of org.eclipse.remote plug-ins and features will be in the Eclipse CDT p2 repos found on [download.eclipse.org/tools/cdt/releases](https://download.eclipse.org/tools/cdt/releases/)

See [Bug 500768](https://bugs.eclipse.org/bugs/show_bug.cgi?id=500768).

# Known Issues

## InaccessibleObjectException when using Docker Container Launch support

Installing the CDT C/C++ Docker Container Launch support feature into an existing Eclipse may report: java.lang.reflect.InaccessibleObjectException in the error log when attempts
are made to access the Docker terminal (e.g. display log).
This is caused because the Docker UI uses reflection to implement terminal support and this is restricted in latest JVMs unless specific JVM options are used to allow the access.
A fix has been made to have the Docker Tooling feature (installed indirectly) add needed JVM options to the Eclipse configuration file: eclipse.ini.
When installing, the user will be asked to restart Eclipse.
Unfortunately, an Eclipse restart from the UI does not re-read the eclipse.ini file and the error will continue (this is a known reported issue).
Closing Eclipse and restarting manually after the install will fix the problem for good.
The 2022-03 C/C++ EPP download already has the needed JVM options specified so this will not occur.
This problem can also be averted by running Eclipse with JVM 11.

# Debug

## Stop auto-opening Modules view (new in CDT 10.6.2)

The Modules view does not do much and in most situations is not particularly relevant to many users. Therefore starting in this release starting a debug session will no longer auto-open it. The view can still be manually opened as any normal view can, with _Window -> Show View -> Other... -> Debug -> Modules_, or using the Ctrl-3 shortcut and typing Modules.
([bug 579759](https://bugs.eclipse.org/bugs/show_bug.cgi?id=579759))

# Terminal

## Windows Pseudo Console (ConPTY) the default

In [CDT 10.3](https://github.com/eclipse-cdt/cdt/blob/main/NewAndNoteworthy/CDT-10.3.md#terminal---windows-pseudo-console) the ConPTY implementation was introduced in preview mode.
The response has been good and then ConPTY is now the default in the terminal on Windows where available.

See [Bug 562776](https://bugs.eclipse.org/bugs/show_bug.cgi?id=562776) for details on the implementation.

# Bugs Fixed in this Release

See Bugzilla report [Bugs Fixed in CDT 10.6](https://bugs.eclipse.org/bugs/buglist.cgi?bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&classification=Tools&product=CDT&query_format=advanced&resolution=FIXED&target_milestone=10.6.0&target_milestone=10.6.1&target_milestone=10.6.2)
