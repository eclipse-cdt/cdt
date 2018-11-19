/*******************************************************************************
 * Copyright (c) 2002, 2007 QNX Software Systems and others.
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
 *
 *  spawner.c
 *
 *  This is a part of JNI implementation of spawner 
 *******************************************************************************/

#include "stdafx.h"
#include "spawner.h"


CRITICAL_SECTION cs;


wchar_t path[MAX_PATH + 1] = {_T('\0') };  // Directory where spawner.dll is located

extern "C"
BOOL APIENTRY DllMain( HINSTANCE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
    switch (ul_reason_for_call)
	{
		case DLL_PROCESS_ATTACH:
			{
			wchar_t * p;
			InitializeCriticalSection(&cs);
			GetModuleFileNameW(hModule, path, MAX_PATH);
			p = wcsrchr(path, _T('\\'));
			if(NULL != p)
				*(p + 1) = _T('\0');
			else
				wcscat(path, L"\\"); 
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

