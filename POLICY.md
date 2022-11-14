## Code Formatting

These are the coding style recommendations that are in place with project setting. They are enforced by the [build process](https://github.com/eclipse-cdt/cdt-infra/tree/master/jenkins/pipelines/cdt/verify#cdt-verify-code-cleanliness) and auto-save actions in Eclipse.

* Preserve formatting and style of old code when making patches
* Use default "Eclipse" code formatting for Java for new code
* Organize Imports action on save
* It is recommended to use code blocks (curly brackets) for `for`/`while`/`do`/`if` statement bodies, with the exception of simple terminating statements, i.e. `if (a) return;`

## Internationalization

Externalize strings (excluding exception arguments, tests and special identifiers)

## Eclipse Java Errors/Warnings

All CDT plugins override default compiler error/warning and use project specific errors/warnings. This enforced by the [build process](https://github.com/eclipse-cdt/cdt-infra/tree/master/jenkins/pipelines/cdt/verify#cdt-verify-code-cleanliness). 

All committers and contributors submitting patches should enable [API tooling](http://wiki.eclipse.org/PDE/API_Tools/User_Guide#API_Tooling_Setup) by setting target baseline platform. Do not commit code with API errors.

*Patches with errors listed above including API errors will not be accepted without corrections.*

### Evolving Warning and Error settings

The warnings and error settings can and should be evolved as needed. For example, a bundle with no warning may want to ensure no new warnings are introduced and should change the project specific settings to upgrade those warnings to errors as needed. This change may require changes to the to [check_code_cleanliness](releng/scripts/check_code_cleanliness.sh) that ensure warnings/errors are at the correct level.


## Copyright

Use Eclipse copyright header: http://www.eclipse.org/legal/copyrightandlicensenotice.php. Here is an example:


```java
/********************************************************************************
 * Copyright (c) {date} {owner}[ and others]
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
 ```

A contributors line can be added for significant changes in the file but this is optional.

## API

The Eclipse CDT project follows the same guidelines and policies as the Eclipse Platform project, with exceptions noted below. See [Eclipse API Central](https://wiki.eclipse.org/Eclipse/API_Central) for more details.


Note: you can still use internal API if you want to, if java visibility allows you (even if it does not, you can hack it, using reflection). The only problem with this approach is that the internal API can be changed without notice or trace, if this happened, you would have a hard time making your code compile or run with new version of the tool. The best approach in this case is to submit a bug asking to create an API for functionality you are looking for.

### New default methods in interfaces

According to [Eclipse API Central](https://wiki.eclipse.org/Eclipse/API_Central) and the PDE API tooling, adding a new method to an interface breaks compatibility. However CDT allows an exception. A new default method is allowed in an interface if there is good reason to believe that the new method name will not conflict with any existing implementations. The reason for this exception is to avoid the common and more complicated use case of adding new subinterfaces with *2 naming scheme. An API filter will be needed in this case to prevent the API tooling error that would require a major version bump. Please send an [email to cdt-dev](CONTRIBUTING.md#Contact) to consult with the community if you are unsure.

### Deprecating and Deleting API

The Eclipse CDT project follows the same guidelines and policies as the Eclipse Platform project. See [Deprecation Policy](https://wiki.eclipse.org/Eclipse/API_Central/Deprecation_Policy). The changes to the policy are:

- Removal of any Eclipse CDT API requires approval from the Eclipse CDT Committers.
- Announcement must be made on [cdt-dev](CONTRIBUTING.md#Contact) mailing list - it can be additionally made to  [cross-project-issues-dev](https://accounts.eclipse.org/mailing-list/cross-project-issues-dev) mailing list if there is concern that the impact of such removal would affect other Eclipse projects.
- Non-breaking API changes and improvements should be considered for listing as a [New and Noteworthy](NewAndNoteworthy/) item for a release.
- API removals and API breakages, actual or planned, should be added to [New and Noteworthy's API Changelog](NewAndNoteworthy/CHANGELOG-API.md).

### Using API Tooling

All committers and contributors should enable API Tooling when writing patches for CDT. This is done automatically when using Oomph to setup your development environment.

To set up the API baseline manually, follow these steps:

1. Select "Window -> Preferences". In the window that opens, select "Plug-in Development -> API Baselines" on the left pane.
2. Click on "Add Baseline..."
3. Choose "A target platform" and click Next.
4. In the next page check the box "cdt-baseline".
5. Click "Refresh" to download the contents of the target.
6. Specify a name for this baseline in the top area, like "cdt-baseline" for example.
7/ Click "Finish", then "OK" in the next dialog.

Once this is done, a full build will be triggered. After that, any changes that don't follow the API rules will be shown as an error.

**Note that when a new version of CDT is released or you change branches, you will need to refresh the baseline**
1. Go back to "Window -> Preferences", "Plug-in Development -> API Baselines"
2. Select the cdt baseline and click on Edit...
3. In the next page check the box "cdt-baseline".
4. Click "Refresh" to update the contents of the target.
5. Click "Finish", then "OK" in the next dialog.

(In the future, refreshing manually might not be required, see [bug 479055](https://bugs.eclipse.org/bugs/show_bug.cgi?id=479055))

## Javadoc

All public API classes and interfaces must have meaningful javadoc header, as well as all public API members.


## Direct Pushes, Pull Requests and Reviews

- The Eclipse CDT branches are protected from direct pushes. All changes must be made via a Pull Request.
- Generally it is good practice for pull requests to be reviewed by non-author before pull requestis merged. One of the priviledges of being a CDT Committer is being able to merge pull requests, therefore a review is not required.
- During Release Candidate weeks, all changes should be reviewed by non-author committer. Normal exception is releng changes.

## Version Numbering

See [Eclipse Version Numbering Guildlines](https://wiki.eclipse.org/Version_Numbering)


