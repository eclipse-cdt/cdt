This is the Release plan and TODO list for CDT.

## Steps for Release

Items at the beginning of development


Prepare main branch for next release

- Update the target platform in cdt.target
    - Update the Eclipse platform to the latest I-build if possible, it may be best to stay on last release of platform until platform's i-builds are stable e.g. https://download.eclipse.org/eclipse/updates/4.37-I-builds/ - later in the development cycle this can be locked down to milestone versions for stability
    - update the target file for maven dependencies, this is semi-automated with a report and updated target file that can be copied-pasted (links https://github.com/eclipse-orbit/orbit-simrel/blob/main/report/maven-osgi/cdt/REPORT.md and https://github.com/eclipse-orbit/orbit-simrel/blob/main/report/maven-osgi/cdt/updated.target)
    - update the orbit-aggregation to next simrel version number (this may need to be deferred until later if doing this step too early as orbit-aggregation comes online for the next release around the same time as this release is done)
    - update sequenceNumber at top of target file (this may be historical and unneeded, someone can investigate and remove it if desired)
- Update baseline target in cdt-baseline.target
    - you may have to set this to the temporary build (e.g. the rc2 one created above) until CDT releases.
    - Update the cdt-lsp version too
    - ensure any newly created features are added (it has been a while since a new CDT feature was created, but sometimes when we adopt code from other projects, like the terminal, those features should be listed. There are some API warnings in the workspace that appear if plug-ins are missing from the baseline, so check that there aren't any unexpected warnings)
- make the target platform changes to CDT.setup too
- in the root pom.xml update comparator.repo, api-baseline.repo, and api-baseline.repo.simrel with the same URLs as used above in the target files/setup files
- Update versions to CDT.
    - Do a global find/replace, for example:
    - 12.1.0-SNAPSHOT -> 12.2.0-SNAPSHOT
    - 12.1.0.qualifier -> 12.2.0.qualifier
    - the above two can be done quickly as the chance of a false positive is very low, after applying them, search for remaining 12.1.0 and 12.1 strings in the codebase and update them - being careful as some of those search results should not be changed. Version ranges in dependencies can be done now, or handled with "update dependency version" as above
- run check_code_cleanliness to make sure that everything looks good `docker run --rm -it -v $(git rev-parse --show-toplevel):/work -w /work/$(git rev-parse --show-prefix) quay.io/eclipse-cdt/cdt-infra:latest releng/scripts/check_code_cleanliness.sh` - if version bumps are needed, see "bump bundle versions" below
- create a PR with the above and push it to main when it succeeds.
- test CDT.setup works

Items in the days ahead of Release day:

- Add new committers (send them private email with draft nomination statement, then start election, trying to time it around a CDT call to ensure maximum engagement on the voting)
- Retire inactive committers - this happens on occasion, not with each release (past example https://github.com/eclipse-cdt/cdt/issues/1183)
- Make sure CDT build is green (https://ci.eclipse.org/cdt/job/cdt/job/main/)
- Create and push a branch of CDT (e.g. `git fetch origin && git checkout origin/main -b cdt_12_1 && git push origin cdt_12_1`)
- On the branch, update cdt.target to the stable/final location for dependencies
    - this is not always possible, especially when we depend on I-builds from platform, in that case use provisional or RC2 I-build and update it again after they release
    - The contents of the `aggrcon` files in https://github.com/eclipse-simrel/simrel.build is useful to know latest p2 URLs for dependencies
    - discretion is used to limit which sites are updated, generally at a minimum eclipse platform, lsp4e, and linuxtools docker should be updated because we use lots of APIs from them
    - because of the cyclic dependencies it may be necessary to update CDT's own reference in cdt.target
    - make sure the correct orbit-aggregation is in use
    - update the target file for maven dependencies, this is semi-automated with a report and updated target file that can be copied-pasted (links https://github.com/eclipse-orbit/orbit-simrel/blob/main/report/maven-osgi/cdt/REPORT.md and https://github.com/eclipse-orbit/orbit-simrel/blob/main/report/maven-osgi/cdt/updated.target)
- Update version of tycho (make sure to review release notes to see if anything else needs updating) https://github.com/eclipse-tycho/tycho/blob/main/RELEASE_NOTES.md
- Update all manifests to ensure correct version compatibility. 
    - It is recommended to use docker for this step to both provide a clean environment and to use the same tool versions the build machine uses. `docker run --rm -it -v $(git rev-parse --show-toplevel):/work -w /work/$(git rev-parse --show-prefix) quay.io/eclipse-cdt/cdt-infra:latest bash`
    - Then in the launched bash (we stay in the bash shell so that we don't have to download dependencies repeatedly):
    - update dependency version `mvn org.eclipse.tycho.extras:tycho-version-bump-plugin:update-manifest`
    - bump bundle versions for compare and replace `mvn verify org.eclipse.tycho:tycho-versions-plugin:bump-versions -Dtycho.bump-versions.increment=100 -DskipDoc=true -DskipTests=true -P baseline-compare-and-replace -fae -Djgit.dirtyWorkingTree-cdtDefault=warning` <-- simply running this over and over again sometimes doesn't work and a manual version bump may be needed
- create a PR with all the above against the new branch - see https://github.com/eclipse-cdt/cdt/pull/1184 for a past example
- merge the PR once it is clean
- wait for the build on the branch to complete that has this change https://ci.eclipse.org/cdt/job/cdt/job/cdt_12_1/
- test that the build works with simrel (See https://github.com/eclipse-simrel/ for details of simrel) by pointing cdt.aggrcon to the just-built p2 repo (e.g. https://download.eclipse.org/tools/cdt/builds/cdt/cdt_12_1/), updating versions and running validate aggregation

