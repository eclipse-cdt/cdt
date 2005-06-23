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
#include <windows.h>

#ifndef __LISTTASKS_H
#define __LISTTASKS_H

typedef BOOL (CALLBACK *PROCENUMPROC)( DWORD, WORD, LPSTR,
      LPARAM ) ;

BOOL WINAPI EnumProcs( PROCENUMPROC lpProc, LPARAM lParam ) ;


#endif