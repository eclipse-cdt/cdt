# Release Notes and New & Noteworthy page

This is the New & Noteworthy page for CDT 11.0 which is part of Eclipse 2022-12 Simultaneous Release

---

# Release Notes

## GitHub

The Eclipse CDT project is now hosted on GitHub.
[Eclipse CDT's GitHub Repo](https://github.com/eclipse-cdt/cdt) is the primary source for the CDT project and all other resources (Bugzilla, Gerrit, Wiki) have migrated to GitHub.
Please see the [Git Hub Migration](https://github.com/eclipse-cdt/cdt/blob/main/GitHubMigration.md) for detailed information and the tracking issue https://github.com/eclipse-cdt/cdt/issues/32 for discussion and the TODO list.


## Java 17

Jave 17 is now required to build and run Eclipse CDT. See https://github.com/eclipse-cdt/cdt/issues/80

## Removed plug-ins and features

Various plug-ins and features are no longer part of the CDT release.
Please see the corresponding issue for more details.

- Qt plug-ins (`org.eclipse.cdt.qt.ui/core/feature`) https://github.com/eclipse-cdt/cdt/issues/123 _Note:_ the `org.eclipse.cdt.testsrunner.qttest` plug-in is still part of CDT.
- Non-universal Welcome Screen Content has been removed from `org.eclipse.cdt` plug-in. The universal content is in `org.eclipse.cdt.doc.user`. https://github.com/eclipse-cdt/cdt/pull/136
- CDT's specific LSP and DAP plug-ins. https://github.com/eclipse-cdt/cdt/issues/139, specifically these plug-ins:
  - `org.eclipse.cdt.debug.dap`
  - `org.eclipse.cdt.debug.dap.gdbjtag`
  - `org.eclipse.cdt.lsp.clangd`
  - `org.eclipse.cdt.lsp.core`
  - `org.eclipse.cdt.lsp.cquery`
  - `org.eclipse.cdt.lsp.ui`

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

## New Job Family for background build settings update

When the project is modified the managed build settings needs to update settings.

While much of this update happens synchronously to the `IResourceChangeListener` calls, the final update is done with a job.
If an update needs to be tracked for completion, after making an update the `IJobManager` can be queried using this family.

e.g. a join on the job being completed:

```java
Job.getJobManager().join(ManagedBuilderCorePlugin.BUILD_SETTING_UPDATE_JOB_FAMILY, null);
```

This new job family was added to improve stability of tests to ensure background operations are complete.
It may have other uses to other API consumers as well and is therefore included here.

## New method ManagedBuildManager#createConfigurationForProject()

This should allow ISV's to create MBS based project with a vendor-specific build-system ID without using internal API.

## Binary Parser code uses AutoCloseable

The binary parser classes which open binary files now implement AutoCloseable so they can (and should) be used in a try-with-resources block.
See https://github.com/eclipse-cdt/cdt/pull/132

# Bugs Fixed in this Release

See [GitHub milestones](https://github.com/eclipse-cdt/cdt/milestone/2?closed=1) and for bugs that haven't been transitioned to GitHub please see Bugzilla report [Bugs Fixed in CDT 11.0](https://bugs.eclipse.org/bugs/buglist.cgi?bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&classification=Tools&product=CDT&query_format=advanced&resolution=FIXED&target_milestone=11.0.0).
