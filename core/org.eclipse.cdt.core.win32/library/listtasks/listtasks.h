#include <windows.h>

#ifndef __LISTTASKS_H
#define __LISTTASKS_H

typedef BOOL (CALLBACK *PROCENUMPROC)( DWORD, WORD, LPSTR,
      LPARAM ) ;

BOOL WINAPI EnumProcs( PROCENUMPROC lpProc, LPARAM lParam ) ;


#endif