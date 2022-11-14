# Migrating from Gerrit, Bugzilla, Wiki, Eclipse Forums

In the summer of 2022 the Eclipse CDT project migrated from using Gerrit, Bugzilla, Eclipse Wiki and Eclipse Forums to using GitHub provided services.

The Eclipse CDT project does not control the Eclipse Foundation infrastructure and much of the reason for this move was down to the planned shutdown of Eclipse provided services. Please see [helpdesk#678](https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/issues/678) and the linked bugs there for the status and timelines on the shutdown of these services.

This means that some of the processes have changed. While an effort has been made to update documentation that incorrectly refers to the previously used technologies, Pull Requests fixing additional places are very welcome.

Here is a high-level summary of the process changes. The intended audience is developers who are familiar with the Gerrit/Bugzilla/Wiki development flow.

Please see the GitHub issues on the migration for current status and things left to do on this migration.

- GitHub issues have replaced Bugzilla bugs. Old bugzilla bugs are not being batch migrated to GitHub. Eventually Bugzilla will be entirely read-only, but it will be preserved in a read-only state for a long time. It is therefore ok to reference Bugzilla bug numbers still.

- New bugs cannot be created in Bugzilla anymore for CDT. Existing bugs can be edited/commented on until Eclipse Foundation fully turns off Bugzilla. Track [helpdesk#679](https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/issues/679) for details.

- Still relevant bugs in Bugzilla can be moved on a case by case basis (manually - there is no automation for pick-and-choose) as it would be better to have relevant bugs in GitHub issues.

- Generally Bugzilla bug numbers are big - most are 6 digits so that can be used to differentiate Bugzilla bugs from GitHub issues (at least for many years). In the worst case it may be necessary to look a number up in both GitHub issues and Bugzilla.
  - Note that one of the drawbacks of decentralized GitHub issues is that each project has its own set of "numbers". 
  - Therefore when referencing an issues from outside of the current repo, prefixing it with the project name is the proper way to do it. e.g. `Organization_name/Repository#Issue` - see [GitHub help](https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls) for more details.

- Bug reports are not necessary on Pull Requests. Issues and Pull Requests in GitHub share a numbering system and the metadata that can be applied to them is the same. So unlike the Bugzilla/Gerrit flow it is no longer necessary to create issues for each PR. See [contribution guide's Creating Pull Requests section](CONTRIBUTING.md#Creating-Pull-Requests) for current policy

- GitHub Pull Requests replace Gerrit. See [contribution guide's Creating Pull Requests section](CONTRIBUTING.md#Creating-Pull-Requests) for step-by-step on creating PRs, and the [Merge Pull Request](CONTRIBUTING.md#Merge-Pull-Request) section for how to review and merge issues

- Where possible we borrow for the Eclipse Platform project on process. The Eclipse Platform have a [recommended workflow](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#recommended-workflow) that is evolving that Eclipse CDT should use.
  - CDT's contribution + pull request process are documented in [CONTRIBUTING.md](CONTRIBUTING.md)
  - When setting up *remotes* in git for GitHub there are a few options. 
    - One suggested way to handle this is to set `eclipse-cdt/cdt` as the `upstream` repo and your fork's `yourname/cdt` as the `origin` repo. Remembering to generally fetch from `upstream` and push to `origin`.
    - The alternative way (as used by at least some active CDT committers) is to set `eclipse-cdt/cdt` as the `origin` repo and your fork's `yourname/cdt` as the `yourname` repo. Remembering to generally fetch from `origin` and push to `yourname`.
    - It is anticipated as the Eclipse EGit + Eclipse Oomph tooling gets better that some of the above will become more automated with defaults that just work.

- Similar to gerrit - all pushes to CDT have to be via PR. See [Direct Pushes, Pull Requests and Reviews](POLICY.md#Direct-Pushes,-Pull-Requests-and-Reviews)

- The master branch has [been renamed](https://github.com/github/renaming) main.

- Eclipse Oomph is now recommended as the default way to setup development environment. See [Setup CDT for development with Oomph](CONTRIBUTING.md#Setup-CDT-for-development-with-Oomph)

- The Eclipse Wiki is being discontinued. The CDT content has being moved to live in the main CDT git repo. The pages on the Eclipse Wiki (under https://wiki.eclipse.org/CDT) have been marked as obsolete or outdated and where appropriate links to the new page have been put in place. The majority of the content is on or linked from the CDT's root [README.md](README.md).

- With all the existing content of the Eclipse Wiki being migrated into CDT's git repo, going forward there won't be a Wiki for Eclipse CDT. While GitHub provides a Wiki functionality, it is not well integrated into other flows (for example, non-committers cannot edit wikis). Making the contributions via Pull Requests to the main repo seems like the better option.
  - The exception is the CDT calls. The CDT calls minutes are stored on hackmd: https://hackmd.io/@jonahgraham/EclipseCDTCalls

- The Eclipse Forum for [C/C++ IDE (CDT)](https://www.eclipse.org/forums/index.php/f/80/) has been shutdown for new posts. The archive will remain as long as the Eclipse Foundation leaves it there. Track [helpdesk#187](https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/issues/187) for details. 

- [GitHub Discussions](https://github.com/eclipse-cdt/cdt/discussions) for CDT have been enabled as the new location for user support. In addition to user support, announcements about CDT releases can be made as pinned posts. The advantage of using discussions is that it brings the user discussions and the developers of Eclipse CDT closer together.

- The Eclipse CDT website (https://www.eclipse.org/cdt) has been discontinued.
Redirects from the CDT website have been put in place to land at suitable locations in Eclipse CDT on GitHub.
The old website content is still available in git with the tag [PRE_MOVE_TO_GITHUB](https://git.eclipse.org/r/plugins/gitiles/www.eclipse.org/cdt/+/refs/tags/PRE_MOVE_TO_GITHUB)
