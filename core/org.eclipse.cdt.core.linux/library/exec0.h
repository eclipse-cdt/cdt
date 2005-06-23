/*******************************************************************************
 * Copyright (c) 2002 - 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
#include <unistd.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <signal.h>
#include <errno.h>

extern pid_t exec0(const char *path, char *const argv[],
                   char *const envp[], const char *dirpath,
                   int channels[3] );

extern int wait0(pid_t pid);
