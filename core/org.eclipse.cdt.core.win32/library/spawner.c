/**********************************************************************
 * Copyright (c) 2002-2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 *
 *  spawner.c
 *
 *  This is a part of JNI implementation of spawner 
***********************************************************************/

#include "stdafx.h"
#include "spawner.h"


CRITICAL_SECTION cs;


_TCHAR path[MAX_PATH + 1] = {_T('\0') };  // Directory where spawner.dll is located


BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
    switch (ul_reason_for_call)
	{
		case DLL_PROCESS_ATTACH:
			{
			_TCHAR * p;
			InitializeCriticalSection(&cs);
			GetModuleFileNameW(hModule, path, MAX_PATH);
			p = _tcsrchr(path, _T('\\'));
			if(NULL != p)
				*(p + 1) = _T('\0');
			else
				_tcscat(path, L"\\"); 
			}
			break;
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
			break;
		case DLL_PROCESS_DETACH:
			DeleteCriticalSection(&cs);
			break;
    }
    return TRUE;
}

