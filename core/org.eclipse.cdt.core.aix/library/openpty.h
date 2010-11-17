/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *******************************************************************************/

#ifndef _OPENPTY_H
#define _OPENPTY_H
int ptym_open (char *pts_name);
int ptys_open (int fdm, char * pts_name);
void set_noecho(int fd);
#endif
