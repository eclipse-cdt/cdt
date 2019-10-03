### How to rebuild natives.

The goal of these instructions is to have a cross-platform build of the natives in CDT. Using tools in CDT's 
[docker](https://github.com/eclipse-cdt/cdt-infra/blob/master/docker/cdt-infra-eclipse-full/ubuntu-18.04/Dockerfile) build image
(quay.io/eclipse-cdt/cdt-infra-eclipse-full:latest).

It is fairly straightforward to biuild the natives, run this command:

```
docker run --rm -it -v $(git rev-parse --show-toplevel):/work -w /work/$(git rev-parse --show-prefix) quay.io/eclipse-cdt/cdt-infra-eclipse-full:latest  make -C jni rebuild
```

However, the challenge is that dll files on Windows have a timestamp in them. To have reproducible builds, we need to have a reproducible 
timestamp. Therefore we use the commit time of the commit to derive a timestamp (See the Makefile for more info). Because we want
to keep the DLL checked in so that contributors don't need to rebuild it all the time we need a way to have to check in the dll with 
the same commit time. To do this we use GIT_COMMITTER_DATE. So, after editing and committing your change, you need to rebuild one last
time with the commit date and the commit it without changing the commit date again using:

```
GIT_COMMITTER_DATE=$(git log -1 --pretty=format:%cI -- .) git commit --amend -a --reuse-message=HEAD
```
