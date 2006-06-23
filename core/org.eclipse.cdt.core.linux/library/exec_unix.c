/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Wind River Systems, Inc.  
 *******************************************************************************/
#include "exec0.h"
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <libgen.h>
#include <stdlib.h>

/* from pfind.c */
extern char *pfind(const char *name);

pid_t
exec0(const char *path, char *const argv[], char *const envp[],
      const char *dirpath, int channels[3])
{
	int pipe0[2], pipe1[2], pipe2[2];
	pid_t childpid;
	char *full_path;

	/*
	 * We use pfind() to check that the program exists and is an executable.
	 * If not pass the error up.  Also execve() wants a full path.
	 */ 
	full_path = pfind(path);
	if (full_path == NULL) {
		fprintf(stderr, "Unable to find full path for \"%s\"\n", (path) ? path : "");
		return -1;
	}

	/*
	 *  Make sure we can create our pipes before forking.
	 */ 
	if (channels != NULL) {
		if (pipe(pipe0) < 0 || pipe(pipe1) < 0 || pipe(pipe2) < 0) {
			fprintf(stderr, "%s(%d): returning due to error.\n",
				__FUNCTION__, __LINE__);
			free(full_path);
			return -1;
		}
	}

	childpid = fork();

	if (childpid < 0) {
		fprintf(stderr, "%s(%d): returning due to error: %s\n",
			__FUNCTION__, __LINE__, strerror(errno));
		free(full_path);
		return -1;
	} else if (childpid == 0) { /* child */
		char *ptr;

		chdir(dirpath);

		if (channels != NULL) {
			/* Close the write end of pipe0 */
			if (close(pipe0[1]) == -1)
				perror("close(pipe0[1])");

			/* Close the read end of pipe1 */
			if (close(pipe1[0]) == -1)
				perror("close(pipe1[0])");

			/* Close the read end of pipe2 */
			if (close(pipe2[0]) == -1)
				perror("close(pipe2[0]))");

			/* redirections */
			dup2(pipe0[0], STDIN_FILENO);   /* dup stdin */
			dup2(pipe1[1], STDOUT_FILENO);  /* dup stdout */
			dup2(pipe2[1], STDERR_FILENO);  /* dup stderr */
		}

		/* Close all the fd's in the child */
		{
			int fdlimit = sysconf(_SC_OPEN_MAX);
			int fd = 3;

			while (fd < fdlimit)
				close(fd++);
		}

		setpgid(getpid(), getpid());

		if (envp[0] == NULL) {
			execv(full_path, argv);
		} else {
			execve(full_path, argv, envp);
		}

		_exit(127);

	} else if (childpid != 0) { /* parent */

		char b;

		if (channels != NULL) {
			/* close the read end of pipe1 */
			if (close(pipe0[0]) == -1)
				perror("close(pipe0[0])");
 
			/* close the write end of pipe2 */
			if (close(pipe1[1]) == -1) 
				perror("close(pipe1[1])");

			/* close the write end of pipe2 */
			if (close(pipe2[1]) == -1) 
				perror("close(pipe2[1])");

			channels[0] = pipe0[1]; /* Output Stream. */
			channels[1] = pipe1[0]; /* Input Stream.  */
			channels[2] = pipe2[0]; /* Input Stream.  */
		}

		free(full_path);
		return childpid;
	}

	free(full_path);
	return -1;                  /*NOT REACHED */
}


int wait0(pid_t pid)
{
	int status;
	int val = -1;

	if (pid < 0 || waitpid(pid, &status, 0) < 0)
		return -1;

	if (WIFEXITED(status)) {
		val = WEXITSTATUS(status);
	}

	return val;
}
