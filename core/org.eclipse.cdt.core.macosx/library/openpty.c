/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
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
#include <termios.h>

#include <stdlib.h>

/**
 * This is taken from R. W. Stevens book.
 * Alain Magloire.
 */

int ptym_open (char *pts_name);
int ptys_open (int fdm, char * pts_name);

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

int
ptym_open(char * pts_name)
{
	char *ptr1, *ptr2;
	int fdm;
	
	strcpy(pts_name, "/dev/ptyXY");
	/* array index: 012345689 (for references in following code) */
	for (ptr1 = "pqrstuvwxyzPQRST"; *ptr1 != 0; ptr1++) {
		pts_name[8] = *ptr1;
		for (ptr2 = "0123456789abcdef"; *ptr2 != 0; ptr2++) {
			pts_name[9] = *ptr2;
			/* try to open master */
			fdm = open(pts_name, O_RDWR);
			if (fdm < 0) {
				if (errno == ENOENT) {/* different from EIO */
					return -1;  /* out of pty devices */
				} else {
					continue;  /* try next pty device */
				}
			}
			pts_name[5] = 't'; /* chage "pty" to "tty" */
			return fdm;   /* got it, return fd of master */
		}
	}
	return -1; /* out of pty devices */
}

int
ptys_open(int fdm, char * pts_name)
{
	int gid, fds;
	struct group *grptr;

	grptr = getgrnam("tty");
	if (grptr != NULL) {
		gid = grptr->gr_gid;
	} else {
		gid = -1;  /* group tty is not in the group file */
	}

	/* following two functions don't work unless we're root */
	chown(pts_name, getuid(), gid);
	chmod(pts_name, S_IRUSR | S_IWUSR | S_IWGRP);
	fds = open(pts_name, O_RDWR);
	if (fds < 0) {
		close(fdm);
		return -1;
	}
	return fds;
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
