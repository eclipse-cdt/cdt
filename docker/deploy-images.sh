#!/usr/bin/env bash

set -eux

toplevel_cdt_lsp=$1
if [ -z "$toplevel_cdt_lsp" ]; then
    echo "Usage: $0 <toplevel_cdt_lsp> [namespace]"
    exit 1
fi

pushd $toplevel_cdt_lsp
    if ! git diff-index HEAD --quiet; then
        echo "CDT LSP git tree is dirty, please commit changes before deploying images"
        exit 1
    fi
popd

namespace=${2:-quay.io/eclipse-cdt}
shorthash=$(git rev-parse --short HEAD)
toplevel_cdt=$(git rev-parse --show-toplevel)

images="cdt-infra:latest cdt-infra-jipp:latest cdt-infra-github:latest"

$toplevel_cdt/docker/build-images.sh

for image in $images; do
    docker tag $image ${namespace}/${image}-${shorthash}
    docker push ${namespace}/${image}-${shorthash}
    nameonly=$(echo $image | sed -es,:.*,,)
    docker tag $image ${namespace}/${nameonly}:latest
    docker push ${namespace}/${nameonly}:latest
done

echo "The following images have been pushed."
for image in $images; do
    hashname=$(docker inspect --format='{{range .RepoDigests}}{{println .}}{{end}}' $image | grep "^${namespace}/" | head -n1)
    [ -n "$hashname" ] || { echo "No digest found for $image in $namespace"; exit 1; }
    echo $image "-->" $hashname
    nameonly=$(echo $image | sed -es,:.*,,)
    find $toplevel_cdt -name \*\.Jenkinsfile -or -name \*\.yaml -or -name \*\.yml | while read file; do
        sed -i "s#image: $namespace/$nameonly[:@].*#image: $hashname#" $file
        git add $file
    done
    pushd $toplevel_cdt_lsp
        find $toplevel_cdt_lsp -name \*\.Jenkinsfile -or -name \*\.yaml -or -name \*\.yml | while read file; do
            sed -i "s#image: $namespace/$nameonly[:@].*#image: $hashname#" $file
            git add $file
        done
    popd
done
echo "Finished pushing to $namespace with tag hash $shorthash"
git commit -m"Update images to Dockerfiles from commit $shorthash"
(cd $toplevel_cdt_lsp && git commit -m"Update images to Dockerfiles from CDT commit $shorthash")
echo "The .yaml and .Jenkinsfiles have been updated to new image and committed, now Push both CDT and CDT LSP!"
