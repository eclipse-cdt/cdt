/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Wind River Systems, Inc.
 *     Mikhail Zabaluev (Nokia) - bug 82744
 *******************************************************************************/
#include <unistd.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <signal.h>
#include <errno.h>

extern pid_t exec0(const char *path, char *const argv[],
                   char *const envp[], const char *dirpath,
                   int channels[3]);


extern pid_t exec_pty(const char *path, char *const argv[],
                      char *const envp[], const char *dirpath,
                      int channels[3], const char *pts_name, int fdm,
                      int console);

extern int wait0(pid_t pid);
