Dockerfiles
============

The docker images for Eclipse CDT's [Jenkins instance](https://ci.eclipse.org/cdt/) and [GitHub Action check code cleanliness](https://github.com/eclipse-cdt/cdt/blob/main/.github/workflows/code-cleanliness.yml) are stored at [quay.io](https://quay.io/organization/eclipse-cdt) and built using the following scripts.
The individual directories below this one contain Dockerfiles which contain a little more information.

build-images.sh
===============

Builds the images locally. A download of [Xcode_13.1.xip](https://download.developer.apple.com/Developer_Tools/Xcode_13.1.xip/Xcode_13.1.xip.xip) (visit [here](https://developer.apple.com/download/more/) first to logon to Apple if needed) is needed to do a complete build. To save on rebuild times, the xip can be removed from the directory to use the cached cdt-infra-build-macos-sdk image.

deploy-images.sh
================

Builds images (from cache), uploads them to quay.io/eclipse-cdt namespace and then
updates all the Jenkinsfile and yaml files to refer to these new images.

Using the docker images
=======================

The docker images exist mostly for use in Jenkins and GitHub Actions, see the [pod templates](https://github.com/eclipse-cdt/cdt/blob/main/jenkins/pod-templates) and [workflows](https://github.com/eclipse-cdt/cdt/blob/main/.github/workflows/)

They can be used to recreate a consistent environment in other cases too. For example, you can run a full build on a machine that does not already have the tools by using docker:

```
docker run --rm -it -v $(git rev-parse --show-toplevel):/work -w /work/$(git rev-parse --show-prefix) --cap-add=SYS_PTRACE --security-opt seccomp=unconfined quay.io/eclipse-cdt/cdt-infra:latest COMMAND HERE
```

For examples of the above in practice, see the native section of [CDT's building readme](https://github.com/eclipse-cdt/cdt/blob/main/BUILDING.md#native)


