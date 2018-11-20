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
 *     Mikhail Zabaluev (Nokia) - bug 82744
 *     Corey Ashford (IBM) - bug 272370, bug 272372
 *******************************************************************************/

/* _XOPEN_SOURCE is needed to bring in the header for ptsname */
#define _XOPEN_SOURCE
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <termios.h>
#include <errno.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <grp.h>

#include <stdlib.h>

/**
 * This is taken from R. W. Stevens book.
 * Alain Magloire.
 */

int ptym_open (char *pts_name);
int ptys_open (int fdm, const char * pts_name);
void set_noecho(int fd);

int
openpty(int *amaster, int *aslave, char *name, struct termios *termp, struct winsize *winp)
{
	char line[20];
	line[0]=0;
	*amaster = ptym_open(line);
	if (*amaster < 0)
		return -1;
	*aslave = ptys_open(*amaster, line);
	if (*aslave < 0) {
		close(*amaster);
		return -1;
	}

	if (name)
		strcpy(name, line);
#ifndef TCSAFLUSH
#define TCSAFLUSH TCSETAF
#endif
	if (termp)
		(void) tcsetattr(*aslave, TCSAFLUSH, termp);
#ifdef TIOCSWINSZ
	if (winp)
		(void) ioctl(*aslave, TIOCSWINSZ, (char *)winp);
#endif
	return 0;
}

void
set_noecho(int fd)
{
	struct termios stermios;
	if (tcgetattr(fd, &stermios) < 0) {
		return ;
	}

	/* turn off echo */
	stermios.c_lflag &= ~(ECHO | ECHOE | ECHOK | ECHONL);
	/* Turn off the NL to CR/NL mapping ou output.  */
	/*stermios.c_oflag &= ~(ONLCR);*/

	stermios.c_iflag |= (IGNCR);

	tcsetattr(fd, TCSANOW, &stermios);
}

int
ptym_open(char * pts_name)
{
	int fdm;
	char *ptr;

	strcpy(pts_name, "/dev/ptmx");
	fdm = getpt();
	if (fdm < 0)
		return -1;
	if (grantpt(fdm) < 0) { /* grant access to slave */
		close(fdm);
		return -2;
	}
	if (unlockpt(fdm) < 0) { /* clear slave's lock flag */
		close(fdm);
		return -3;
	}
	ptr = ptsname(fdm);
	if (ptr == NULL) { /* get slave's name */
		close (fdm);
		return -4;
	}
	strcpy(pts_name, ptr); /* return name of slave */
	return fdm;            /* return fd of master */
}

int
ptys_open(int fdm, const char * pts_name)
{
	int fds;
	/* following should allocate controlling terminal */
	fds = open(pts_name, O_RDWR);
	if (fds < 0) {
		close(fdm);
		return -5;
	}

#if	defined(TIOCSCTTY)
	/*  TIOCSCTTY is the BSD way to acquire a controlling terminal. */
	if (ioctl(fds, TIOCSCTTY, (char *)0) < 0) {
		// ignore error: this is expected in console-mode
	}
#endif
	return fds;
}
