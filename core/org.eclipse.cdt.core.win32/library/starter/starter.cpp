/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
/*
 *  starter.c
 *
 *  This is a small utility for windows spawner
 */


//#define UNICODE
//#define _UNICODE

#define STRICT
#include <Windows.h>
#include <process.h>
#include <tchar.h>
#include <stdio.h>

// #define DEBUG_MONITOR
#define MAX_CMD_LINE_LENGTH (2049)

int copyTo(char * target, const char * source, int cpyLength, int availSpace);

///////////////////////////////////////////////////////////////////////////////
BOOL WINAPI HandlerRoutine(  DWORD dwCtrlType)   //  control signal type
{
	BOOL ret = TRUE;
	switch(dwCtrlType)
	{
		case CTRL_C_EVENT:
			break;
		case CTRL_BREAK_EVENT:
			break;
		case CTRL_CLOSE_EVENT:
			ret = FALSE;
			break;
		case CTRL_LOGOFF_EVENT:
			ret = FALSE;
			break;
		case CTRL_SHUTDOWN_EVENT:
			ret = FALSE;
			break;
		default:
			break;
	}
	return ret;
}



extern "C" int  _tmain(int argc, TCHAR* argv[]) {

   // Make sure that we've been passed the right number of arguments
   if (argc < 5) {
      _tprintf(__TEXT("Usage: %s (Three InheritableEventHandles) (CommandLineToSpawn)\n"), 
         argv[0]);
      return(0);
   }

   // Construct the full command line
   TCHAR szCmdLine[MAX_CMD_LINE_LENGTH] = { 0 };
   int nPos = 0;

   for(int i = 4; i < argc; ++i) 
		{
		int nCpyLen;
		if(0 > (nCpyLen = copyTo(szCmdLine + nPos, argv[i], _tcslen(argv[i]), MAX_CMD_LINE_LENGTH - nPos)))
			{
#ifdef DEBUG_MONITOR
			OutputDebugString("Not enough space to build command line\n");
#endif
			return 0;
			}
		nPos += nCpyLen;
		szCmdLine[nPos] = _T(' ');
		++nPos;
		}   
   szCmdLine[nPos] = _T('\0');

   STARTUPINFO         si = { sizeof(si) };
   PROCESS_INFORMATION pi = { 0 };
   DWORD dwExitCode = 0;
#ifdef DEBUG_MONITOR
   int currentPID = GetCurrentProcessId();
   char buffer[MAX_CMD_LINE_LENGTH];
#endif
   
   BOOL exitProc = FALSE;
   HANDLE waitEvent = OpenEvent(EVENT_ALL_ACCESS, TRUE, argv[2]);
   HANDLE h[3];
   h[0] = OpenEvent(EVENT_ALL_ACCESS, TRUE, argv[1]);
   h[2] = OpenEvent(EVENT_ALL_ACCESS, TRUE, argv[3]); // This is a terminate event
   SetConsoleCtrlHandler(HandlerRoutine, TRUE);

#ifdef DEBUG_MONITOR
	sprintf(buffer, "starter start command: %s\n", szCmdLine);
	OutputDebugString(buffer);
#endif

//   OutputDebugString(szCmdLine);
   // Spawn the other processes as part of this Process Group
   BOOL f = CreateProcess(NULL, szCmdLine, NULL, NULL, TRUE, 
      0, NULL, NULL, &si, &pi);

   if (f) 
   {
   	  SetEvent(waitEvent); // Means thar process has been spawned
      CloseHandle(pi.hThread);
      h[1] = pi.hProcess;

	  while(!exitProc)
		  {
		  // Wait for the spawned-process to die or for the event
		  // indicating that the processes should be forcibly killed.
		  switch (WaitForMultipleObjects(3, h, FALSE, INFINITE)) 
			{
			 case WAIT_OBJECT_0 + 0:  // Send Ctrl-C
#ifdef DEBUG_MONITOR
				sprintf(buffer, "starter (PID %i) received CTRL-C event\n", currentPID);
				OutputDebugString(buffer);
#endif
   				GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
				SetEvent(waitEvent);
				break;

			 case WAIT_OBJECT_0 + 1:  // App terminated normally
				// Make it's exit code our exit code
#ifdef DEBUG_MONITOR
				sprintf(buffer, "starter: launched process has been terminated(PID %i)\n", currentPID);
				OutputDebugString(buffer);
#endif
				GetExitCodeProcess(pi.hProcess, &dwExitCode);
				exitProc = TRUE;
				break;
			 case WAIT_OBJECT_0 + 2:  // Kill
#ifdef DEBUG_MONITOR
				sprintf(buffer, "starter received KILL event (PID %i)\n", currentPID);
				OutputDebugString(buffer);
#endif
   				GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
				TerminateProcess(h[1], 0);
				exitProc = TRUE;
				break;
			 default:
			 // Unexpected code
				LPTSTR lpMsgBuf;

				FormatMessage( 
					FORMAT_MESSAGE_ALLOCATE_BUFFER | 
					FORMAT_MESSAGE_FROM_SYSTEM | 
					FORMAT_MESSAGE_IGNORE_INSERTS,
					NULL,
					GetLastError(),
					MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
					(LPTSTR) &lpMsgBuf,
					0,
					NULL 
				);
				OutputDebugString(lpMsgBuf);
				// Free the buffer.
				LocalFree( lpMsgBuf );
				exitProc = TRUE;
				break;
			 }
			
		  }
      CloseHandle(pi.hProcess);
	}

   CloseHandle(waitEvent);
   CloseHandle(h[0]);
   CloseHandle(h[2]);
 
   return(dwExitCode);
}

// Return number of bytes in target or -1 in case of error
int copyTo(LPTSTR target, LPCTSTR source, int cpyLength, int availSpace)
{
	BOOL bSlash = FALSE;
	int i = 0, j = 0;
	int totCpyLength = cpyLength;

#define QUOTATION_DO   0
#define QUOTATION_DONE 1
#define QUOTATION_NONE 2

	int nQuotationMode = 0;
	if(availSpace <= cpyLength)  // = to reserve space for '\0'
		return -1;

	if((_T('\"') == *source) && (_T('\"') == *(source + cpyLength - 1)))
		{
		// Already done
		nQuotationMode = QUOTATION_DONE;
		}
	else
	if(_tcschr(source, _T(' ')) == NULL)
		{
		// No reason to quotate term becase it doesn't have embedded spaces
		nQuotationMode = QUOTATION_NONE;
		}
	else
		{
		// Needs to be quotated
		nQuotationMode = QUOTATION_DO;
		*target = _T('\"');
		++j;
		}

	for(; i < cpyLength; ++i, ++j) 
		{
		if(source[i] == _T('\\'))
			bSlash = TRUE;
		else
		// Don't escape embracing quotation marks
		if((source[i] == _T('\"')) && !((nQuotationMode == QUOTATION_DONE) && ((i == 0) || (i == (cpyLength - 1))) ) )
			{
			if(!bSlash)
				{
				if(j == availSpace)
					return -1;
				target[j] = _T('\\');
				++j;
				}
			bSlash = FALSE;
			}
		else
			bSlash = FALSE;

		if(j == availSpace)
			return -1;
		target[j] = source[i];
		}

	if(nQuotationMode == QUOTATION_DO)
		{
		if(j == availSpace)
			return -1;
		target[j] = _T('\"');
		++j;
		}
	return j;
}



//////////////////////////////// End of File //////////////////////////////////
