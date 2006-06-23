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
#include <termios.h>
#include <stropts.h>
#include <sys/ioctl.h>

/* from pfind.c */
extern char *pfind(const char *name);

pid_t
exec_pty(const char *path, char *const argv[], char *const envp[],
      const char *dirpath, int channels[3], const char *pts_name, int fdm)
{
	int pipe2[2];
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
		if (pipe(pipe2) < 0) { 
			fprintf(stderr, "%s(%d): returning due to error: %s\n", __FUNCTION__, __LINE__, strerror(errno));
			free(full_path);
			return -1;
		}
	}

	childpid = fork();

	if (childpid < 0) {
		fprintf(stderr, "%s(%d): returning due to error: %s\n", __FUNCTION__, __LINE__, strerror(errno));
		free(full_path);
		return -1;
	} else if (childpid == 0) { /* child */

		chdir(dirpath);

		if (channels != NULL) {
			int fds;

			fds = ptys_open(fdm, pts_name);
			if (fds < 0) {
				fprintf(stderr, "%s(%d): returning due to error: %s\n", __FUNCTION__, __LINE__, strerror(errno));
				return -1;
			}

			/* Close the read end of pipe2 */
			if (close(pipe2[0]) == -1)
				perror("close(pipe2[0]))");

			/* close the master, no need in the child */
			close(fdm);

			set_noecho(fds);
			/* redirections */
			dup2(fds, STDIN_FILENO);   /* dup stdin */
			dup2(fds, STDOUT_FILENO);  /* dup stdout */
			dup2(pipe2[1], STDERR_FILENO);  /* dup stderr */
			close(fds);  /* done with 	fds. */
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

		ioctl(fdm, I_PUSH, "ptem");
		set_noecho(fdm);
		if (channels != NULL) {
			/* close the write end of pipe1 */
			if (close(pipe2[1]) == -1)
				perror("close(pipe2[1])");
 
			channels[0] = fdm; /* Input Stream. */
			channels[1] = fdm; /* Output Stream.  */
			channels[2] = pipe2[0]; /* stderr Stream.  */
			//channels[2] = fdm; /* Input Stream.  */
		}

		free(full_path);
		return childpid;
	}

	free(full_path);
	return -1;                  /*NOT REACHED */
}
#ifdef __STAND_ALONE__
int main(int argc, char **argv, char **envp) {
	const char *path = "./bufferring_test";
	int channels[3] = { -1, -1, -1};
	int status;	
	FILE *app_stdin;
	FILE *app_stdout;
	FILE *app_stderr;
	char pts_name[32];
	int fdm;	
	char buffer[32];

	fdm = ptym_open(pts_name);
	status =  exec_pty(path, argv, envp, ".", channels, pts_name, fdm);
	if (status >= 0) {
		//app_stdin = fdopen(channels[0], "w");	
		app_stdout = fdopen(channels[1], "r");	
		app_stderr = fdopen(channels[2], "r");	
		if (app_stdout == NULL || app_stderr == NULL /*|| app_stdin == NULL*/) {
			fprintf(stderr, "PROBLEMS\n");
		} else {
			printf("PID %d\n", status);
			if (isatty(fdm)) {
				printf("Is atty\n");
			}
			write(fdm, "foo\n", 4);
			write(fdm, "bar\n", 4);
			//fputs("foo\n", app_stdin);
			//fputs("bar\n", app_stdin);
			//fflush(app_stdin);
			while(fgets(buffer, sizeof buffer, app_stdout) != NULL) {
				fprintf(stdout, "STDOUT: %s\n", buffer);
			}
			while(fgets(buffer, sizeof buffer, app_stderr) != NULL) {
				fprintf(stdout, "STDERR: %s\n", buffer);
			}
		}
	}
	fputs("bye\n", stdout);
	close(channels[0]);
	close(channels[1]);
	close(channels[2]);
	return 0;	
}
#endif
