// dllmain.cpp : Defines the entry point for the DLL application.
#include <windows.h>
#include <delayimp.h>
#include <assert.h>
 
#pragma comment(lib, "delayimp")

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
					 )
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
	return TRUE;
}

static HMODULE getCurrentModule()
{
    HMODULE module;
    if (!GetModuleHandleEx(
                GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS |
                GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
                (LPCTSTR)getCurrentModule,
                &module)) {
        assert(false);
    }
    return module;
}

HMODULE PTYExplicitLoadLibrary( LPCSTR pszModuleName )
{
  if( lstrcmpiA( pszModuleName, "winpty.dll" ) == 0 )
  {
    CHAR szPath[MAX_PATH] = "";
    //_hdllInstance is the HMODULE of *this* module
    DWORD cchPath = GetModuleFileNameA(getCurrentModule(), szPath, MAX_PATH );
    while( cchPath > 0 )
    {
      switch( szPath[cchPath - 1] )
      {
        case '\\':
        case '/':
        case ':':
          break;
        default:
          --cchPath;
          continue;
      }
      break; //stop searching; found path separator
    }
    lstrcpynA( szPath + cchPath, pszModuleName, MAX_PATH - cchPath );
    return LoadLibraryA( szPath ); //call with full path to dependent DLL
  }
  return NULL;
}

FARPROC WINAPI PTYDliNotifyHook( unsigned dliNotify, PDelayLoadInfo pdli )
{
  if( dliNotify == dliNotePreLoadLibrary )
    return (FARPROC)PTYExplicitLoadLibrary( pdli->szDll );
  return NULL;
}

extern "C" PfnDliHook __pfnDliNotifyHook2 = PTYDliNotifyHook;
