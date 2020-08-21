#!/usr/bin/env python3

import sys
import os
import hashlib
import subprocess

LONG_MAX = (1 << 64) - 1
DEBUG = True

def usage():
    print("Usage: {0} <source files or directories> -- <compiler command>".format(sys.argv[0]))
    sys.exit(1)

def debug(s):
    if DEBUG:
        print("{} {}".format(sys.argv[0], s))

def updateHash(sha1, path):
    if os.path.isdir(path):
        for p in os.listdir(path):
            updateHash(sha1, os.path.join(path, p))
        return

    if os.path.isfile(path):
        filehash = hashlib.sha1()
        with open(path, 'rb') as f:
            while True:
                data = f.read(65536)
                if not data:
                    break
                sha1.update(data)
                filehash.update(data)
        debug("{} => {}".format(filehash.hexdigest(), path))

try:
    index = sys.argv.index("--")
except ValueError:
    usage()

paths = sys.argv[1:index]
compiler_args = sys.argv[index+1:]

if len(paths) == 0 or len(compiler_args) == 0:
    usage()

# Hash all the source files and traverse any directories recursively
sha1 = hashlib.sha1()
for path in paths:
    updateHash(sha1, path)

# Hash the build command too
sha1.update(" ".join(compiler_args).encode())

# Set the SOURCE_DATE_EPOCH environment variable to the hash value
os.environ["SOURCE_DATE_EPOCH"] = str(int(sha1.hexdigest(), base=16) % LONG_MAX)

debug("Total hash: {}".format(sha1.hexdigest()))
debug("SOURCE_DATE_EPOCH: {}".format(os.environ["SOURCE_DATE_EPOCH"]))

# Run the compiler with the environement variable set
subprocess.run(compiler_args)
