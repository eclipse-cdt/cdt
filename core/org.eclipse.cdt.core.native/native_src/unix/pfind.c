/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Wind River Systems, Inc.
 *     Mikhail Sennikovsky - bug 145737
 *     Everton Rufino Constantino (IBM) - bug 237611
 *******************************************************************************/
/*
 * pfind.c - Search for a binary in $PATH.
 */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <sys/stat.h>

#ifndef PATH_MAX
#define PATH_MAX 1024
#endif

#define PATH_DEF "PATH="
const int path_def_len = 5; /* strlen(PATH_DEF); */

char *path_val(char *const envp[]) {
    if (!envp || !envp[0]) {
        return getenv("PATH");
    }

    for (int i = 0; envp[i]; i++) {
        char *p = envp[i];
        if (!strncmp(PATH_DEF, p, path_def_len)) {
            return p + path_def_len;
        }
    }

    return NULL;
}

char *pfind(const char *name, char *const envp[]) {
    char *tok;
    char *sp;
    char *path;
    char fullpath[PATH_MAX + 1];
    struct stat sb;

    /* Sanity check.  */
    if (!name) {
        fprintf(stderr, "pfind(): Null argument.\n");
        return NULL;
    }

    /* For absolute name or name with a path, check if it is an executable. */
    if (name[0] == '/' || name[0] == '.') {
        if (access(name, X_OK) == 0) {
            return strdup(name);
        }
        return NULL;
    }

    /* Search in the PATH environment. */
    path = path_val(envp);

    if (!path || strlen(path) <= 0) {
        fprintf(stderr, "Unable to get $PATH.\n");
        return NULL;
    }

    /* The value return by getenv() is read-only */
    path = strdup(path);

    tok = strtok_r(path, ":", &sp);
    while (tok) {
        snprintf(fullpath, sizeof(fullpath) - 1, "%s/%s", tok, name);

        if (stat(fullpath, &sb) == 0 && S_ISREG(sb.st_mode)) { /* fullpath is a file */
            if (access(fullpath, X_OK) == 0) {                 /* fullpath is executable */
                free(path);
                return strdup(fullpath);
            }
        }

        tok = strtok_r(NULL, ":", &sp);
    }

    free(path);
    return NULL;
}

#ifdef BUILD_WITH_MAIN
int main(int argc, char **argv) {
    for (int i = 1; i < argc; i++) {
        char *fullpath = pfind(argv[i], NULL);
        if (fullpath) {
            printf("Found %s @ %s.\n", argv[i], fullpath);
        } else {
            printf("Unable to find %s in $PATH.\n", argv[i]);
        }
    }
}
#endif
