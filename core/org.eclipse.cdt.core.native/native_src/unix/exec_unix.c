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
 *******************************************************************************/
#include "exec0.h"
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <libgen.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <dirent.h>
#include <ctype.h>

/* from pfind.c */
extern char *pfind(const char *name, char *const envp[]);

static int sys_close_range_wrapper(unsigned int from_fd_inclusive) {
    // Use fast `close_range` (https://man7.org/linux/man-pages/man2/close_range.2.html) if available.
    // Cannot call `close_range` from libc, as it may be unavailable in older libc.
#if defined(__linux__) && defined(SYS_close_range) && defined(CLOSE_RANGE_UNSHARE)
    return syscall(SYS_close_range, from_fd_inclusive, ~0U, CLOSE_RANGE_UNSHARE);
#else
    errno = ENOSYS;
    return -1;
#endif
}

static int close_all_fds_using_parsing(unsigned int from_fd_inclusive) {
    // If `opendir` is implemented using a file descriptor, we may close it accidentally.
    // Let's close a few lowest file descriptors, in hope that `opendir` will use it.
    int lowest_fds_to_close = 2;
    for (int i = 0; i < lowest_fds_to_close; i++) {
        close(from_fd_inclusive + i);
    }

#if defined(__APPLE__)
#define FD_DIR "/dev/fd"
#else
#define FD_DIR "/proc/self/fd"
#endif

    DIR *dirp = opendir(FD_DIR);
    if (dirp == NULL) {
        return -1;
    }

    struct dirent *direntp;

    while ((direntp = readdir(dirp)) != NULL) {
        if (isdigit(direntp->d_name[0])) {
            int fd = strtol(direntp->d_name, NULL, 10);
            if (fd >= from_fd_inclusive + lowest_fds_to_close && fd != dirfd(dirp)) {
                close(fd);
            }
        }
    }

    closedir(dirp);

    return 0;
}

static void close_all_fds_fallback(unsigned int from_fd_inclusive) {
    int fdlimit = sysconf(_SC_OPEN_MAX);
    if (fdlimit == -1) {
        fdlimit = 65535; // arbitrary default, just in case
    }
    for (int fd = from_fd_inclusive; fd < fdlimit; fd++) {
        close(fd);
    }
}

static void close_all_fds(void) {
    unsigned int from_fd = STDERR_FILENO + 1;
    if (sys_close_range_wrapper(from_fd) == 0) {
        return;
    }
    if (close_all_fds_using_parsing(from_fd) == 0) {
        return;
    }
    close_all_fds_fallback(from_fd);
}

pid_t exec0(const char *path, char *const argv[], char *const envp[], const char *dirpath, int channels[3]) {
    int pipe0[2], pipe1[2], pipe2[2];
    pid_t childpid;
    char *full_path;

    /*
     * We use pfind() to check that the program exists and is an executable.
     * If not pass the error up.  Also execve() wants a full path.
     */
    full_path = pfind(path, envp);
    if (full_path == NULL) {
        fprintf(stderr, "Unable to find full path for \"%s\"\n", (path) ? path : "");
        return -1;
    }

    /*
     * Make sure we can create our pipes before forking.
     */
    if (channels) {
        if (pipe(pipe0) < 0 || pipe(pipe1) < 0 || pipe(pipe2) < 0) {
            fprintf(stderr, "%s(%d): returning due to error.\n", __func__, __LINE__);
            free(full_path);
            return -1;
        }
    }

    childpid = fork();

    if (childpid < 0) {
        fprintf(stderr, "%s(%d): returning due to error: %s\n", __func__, __LINE__, strerror(errno));
        free(full_path);
        return -1;
    } else if (childpid == 0) { /* child */
        chdir(dirpath);

        if (channels) {
            /* Close the write end of pipe0 */
            if (close(pipe0[1]) == -1) {
                perror("close(pipe0[1])");
            }

            /* Close the read end of pipe1 */
            if (close(pipe1[0]) == -1) {
                perror("close(pipe1[0])");
            }

            /* Close the read end of pipe2 */
            if (close(pipe2[0]) == -1) {
                perror("close(pipe2[0]))");
            }

            /* redirections */
            dup2(pipe0[0], STDIN_FILENO);  /* dup stdin */
            dup2(pipe1[1], STDOUT_FILENO); /* dup stdout */
            dup2(pipe2[1], STDERR_FILENO); /* dup stderr */
        }

        /* Close all the fd's in the child */
        close_all_fds();

        setpgid(getpid(), getpid());

        if (envp && envp[0]) {
            execve(full_path, argv, envp);
        } else {
            execv(full_path, argv);
        }

        _exit(127);

    } else if (childpid != 0) { /* parent */
        if (channels) {
            /* close the read end of pipe1 */
            if (close(pipe0[0]) == -1) {
                perror("close(pipe0[0])");
            }

            /* close the write end of pipe2 */
            if (close(pipe1[1]) == -1) {
                perror("close(pipe1[1])");
            }

            /* close the write end of pipe2 */
            if (close(pipe2[1]) == -1) {
                perror("close(pipe2[1])");
            }

            channels[0] = pipe0[1]; /* Output Stream. */
            channels[1] = pipe1[0]; /* Input Stream.  */
            channels[2] = pipe2[0]; /* Input Stream.  */
        }

        free(full_path);
        return childpid;
    }

    free(full_path);
    return -1; /*NOT REACHED */
}

int wait0(pid_t pid) {
    int status;
    int val = -1;

    if (pid < 0) {
        return -1;
    }

    for (;;) {
        if (waitpid(pid, &status, 0) < 0) {
            if (errno == EINTR) {
                // interrupted system call - retry
                continue;
            }
        }
        break;
    }
    if (WIFEXITED(status)) {
        val = WEXITSTATUS(status);
    }

    return val;
}
