/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
#ifndef _OPENPTY_H
#define _OPENPTY_H
int ptym_open (char *pts_name);
int ptys_open (int fdm, char * pts_name);
void set_noecho(int fd);
#endif
