// spawner.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"


CRITICAL_SECTION cs;

TCHAR path[MAX_PATH + 1] = {_T('\0') };


BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
    switch (ul_reason_for_call)
	{
		case DLL_PROCESS_ATTACH:
			{
			LPTSTR p;
			InitializeCriticalSection(&cs);
			GetModuleFileName(hModule, path, MAX_PATH);
			p = _tcsrchr(path, _T('\\'));
			if(NULL != p)
				*(p + 1) = _T('\0');
			else
				_tcscat(path, "\\"); 
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


