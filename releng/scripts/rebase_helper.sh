#!this_is_not_really_a_script
echo this is not really a script, run the steps by hand as there are manual steps
exit 1

#####
# Prerequisites
# - Python installed and on PATH
# - EASE + Py4J adapter from http://download.eclipse.org/ease/update/release select the following:
#   - EASE Language Support -> EASE Py4J Support (Incubation)
#   - EASE Modules -> Ease Modules (Incubation)
# - ECLIPSE environment variable set to path to eclipse
#   - or update the script below
# - Import formattersettings.xml and cleanupsettings.xml into your workspace and set Formatter and Cleanup actions to CDT

# Default ECLIPSE if not set
: ${ECLIPSE:=/scratch/eclipse/eclipse-committers-2018-12-M2/eclipse}

#####
# Step 1: Checkout the commit you want to rebase
git checkout FETCH_HEAD

#####
# Step 2: Get the patch in the new CDT code formatting standard on a new branch 'commit_to_rebase'
git checkout -b commit_to_rebase
# Rebase change onto this commit, where all the formatter settings and EPLv2 is done, but the
# code has not been formatted yet. If your original commit is not too far behind this
# will finish without conflicts.
git rebase 35996a5c5ca5c254959ba48241eaada6dbf8628d

##
# Run code cleanup/formatting on all the Java files in the commit
# A. Close all open editors in Eclipse
# B. Open all filed modified in the commit
git diff-tree --no-commit-id --name-only -r commit_to_rebase -- *.java | xargs $ECLIPSE
# C. Run cleanup.py on them using Eclipse EASE
# D. Remove trailing whitespace on all relevant files
git show master:.gitattributes | awk '/# remove trailing whitespace/{getline; print $1}' |
    while read i ; do
        echo "Removing trailing whitespace on $i files"
        git diff-tree --no-commit-id --name-only -r commit_to_rebase -- "$i" | xargs --no-run-if-empty sed -i 's/[ \t]*$//'
    done

##
# Save the formatting on the branch
git add -u
git commit --amend --reuse-message=HEAD


#####
# Step 3: Create a commit that has only the files edited cleaned up, but no other ones
# Start by checking out to a new branch the same commit as above with the formatter settings
git checkout 35996a5c5ca5c254959ba48241eaada6dbf8628d -b commit_to_format

##
# Run cleanup + trailing whitespace removal as above (Step 2 A-D)

##
# Save the files which are now formatted, but without the change you wish to rebase in a commit
git add -u
git commit -m"formatted files"

#####
# Step 4: Apply the change so you have a history on commit_to_format branch that is two ahead of 35996a5c5c
#   the first being the formatted files, the second being the change we are trying to get onto master
# Diff the two branches we just made, that diff is the real work you are trying to get on master, and apply
# that diff
git diff commit_to_format..commit_to_rebase | git apply
# Save the edit, reusing your original commit message (and therefore Change-Id!)
git add -u
git commit --reuse-message=commit_to_rebase

#####
# Step 5: Cherry-pick the new commit onto master
git checkout master
git cherry-pick commit_to_format

#####
# Step 6: Push commit to gerrit
git push origin master:refs/for/master

#####
# Step 7: Cleanup
git branch -D commit_to_rebase
git branch -D commit_to_format
