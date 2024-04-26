Building GDBs in a separate image
=================================

The GDBs take a long time to build and it is very inconvenient to have to build them
because something else in a Docker file has changed. Therefore, this image *only*
builds the GDBs using the download-build-gdb.sh script.

Other images can then use these prebuilt GDBs by copying them to their image.

For example, to copy all the GDBs use this line:

```
COPY --from=cdt-infra-all-gdbs:ubuntu-18.04 /shared/common/gdb/gdb-all /shared/common/gdb/gdb-all
```

Or, to copy a specific version only, do this:

```
# Copy install directory
COPY --from=cdt-infra-all-gdbs:ubuntu-18.04 /shared/common/gdb/gdb-all/install/gdb-8.2.1 /shared/common/gdb/gdb-all/install/gdb-8.2.1
# Copy versioned links
COPY --from=cdt-infra-all-gdbs:ubuntu-18.04 /shared/common/gdb/gdb-all/bin/gdb.8.2 /shared/common/gdb/gdb-all/bin/gdb.8.2
COPY --from=cdt-infra-all-gdbs:ubuntu-18.04 /shared/common/gdb/gdb-all/bin/gdb.8.2.1 /shared/common/gdb/gdb-all/bin/gdb.8.2.1
COPY --from=cdt-infra-all-gdbs:ubuntu-18.04 /shared/common/gdb/gdb-all/bin/gdbserver.8.2 /shared/common/gdb/gdb-all/bin/gdbserver.8.2
COPY --from=cdt-infra-all-gdbs:ubuntu-18.04 /shared/common/gdb/gdb-all/bin/gdbserver.8.2.1 /shared/common/gdb/gdb-all/bin/gdbserver.8.2.1
```
