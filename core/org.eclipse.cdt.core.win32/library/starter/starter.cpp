/*******************************************************************************
 * Copyright (c) 2002 - 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Wind River Systems, Inc.  
 *
 *  starter.cpp
 *
 *  This is a small utility for windows spawner
***********************************************************************/



#define STRICT
#include <Windows.h>
#include <process.h>
#include <tchar.h>
#include <stdio.h>

//#define DEBUG_MONITOR 
#define MAX_CMD_LINE_LENGTH (2049)
#define PIPE_NAME_LENGTH 100

int copyTo(_TCHAR * target, const _TCHAR * source, int cpyLength, int availSpace);
void DisplayErrorMessage();

//BOOL KillProcessEx(DWORD dwProcessId);  // Handle of the process 

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

void ensureSize(_TCHAR** ptr, int* psize, int requiredLength) 
{ 
	int size= *psize;
	if (requiredLength > size) { 
	   size= 2*size;
	   if (size < requiredLength) {
	      size= requiredLength;
	   }
	   *ptr= (_TCHAR *)realloc(*ptr, size * sizeof(_TCHAR));
	   if (NULL == *ptr) {
	   	  *psize= 0;
	   }
	   else {
	   	  *psize= size;
	   }
    }
}


extern "C" int  _tmain(int argc, _TCHAR * argv[]) {

	// Make sure that we've been passed the right number of arguments
   if (argc < 7) {
      _tprintf(_T("Usage: %s (Three InheritableEventHandles) (CommandLineToSpawn)\n"), 
         argv[0]);
      return(0);
   }

   // Construct the full command line
   int nCmdLineLength= MAX_CMD_LINE_LENGTH;
   _TCHAR * szCmdLine= (_TCHAR *)malloc(nCmdLineLength * sizeof(_TCHAR));
   szCmdLine[0]= 0;
   int nPos = 0;

   for(int i = 6; i < argc; ++i) 
		{
		int nCpyLen;
		int len= _tcslen(argv[i]);
		int requiredSize= nPos+len+2;
		if (requiredSize > 32*1024) {
#ifdef DEBUG_MONITOR
			OutputDebugStringW(_T("Command line too long!\n"));
#endif
			return 0;
		}				
		ensureSize(&szCmdLine, &nCmdLineLength, requiredSize);
		if (NULL == szCmdLine) {
#ifdef DEBUG_MONITOR
			OutputDebugStringW(_T("Not enough memory to build cmd line!\n"));
#endif
			return 0;
		}
		if(0 > (nCpyLen = copyTo(szCmdLine + nPos, argv[i], len, nCmdLineLength - nPos)))
		   {
#ifdef DEBUG_MONITOR
		   OutputDebugStringW(_T("Not enough space to build command line\n"));
#endif
		   return 0;
		}
		nPos += nCpyLen;
		szCmdLine[nPos] = _T(' ');
		++nPos;
		}   
   szCmdLine[nPos] = _T('\0');

   STARTUPINFOW        si = { sizeof(si) };
   PROCESS_INFORMATION pi = { 0 };
   DWORD dwExitCode = 0;
#ifdef DEBUG_MONITOR
   int currentPID = GetCurrentProcessId();
   _TCHAR buffer[MAX_CMD_LINE_LENGTH];
#endif
   
   BOOL exitProc = FALSE;
   HANDLE waitEvent = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[4]);
   HANDLE h[3];
   h[0] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[3]);
   h[2] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[5]); // This is a terminate event
   SetConsoleCtrlHandler(HandlerRoutine, TRUE);


   int parentPid = _tcstol(argv[1], NULL, 10);
   int nCounter = _tcstol(argv[2], NULL, 10);
   _TCHAR inPipeName[PIPE_NAME_LENGTH];
   _TCHAR outPipeName[PIPE_NAME_LENGTH];
   _TCHAR errPipeName[PIPE_NAME_LENGTH];
 
   _stprintf(inPipeName,  _T("\\\\.\\pipe\\stdin%08i%010i"),  parentPid, nCounter); 
   _stprintf(outPipeName, _T("\\\\.\\pipe\\stdout%08i%010i"), parentPid, nCounter); 
   _stprintf(errPipeName, _T("\\\\.\\pipe\\stderr%08i%010i"), parentPid, nCounter);
#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("Pipes: %s, %s, %s\n"), inPipeName, outPipeName, errPipeName);
	OutputDebugStringW(buffer);
#endif
   
   HANDLE stdHandles[3];

   SECURITY_ATTRIBUTES sa;
   sa.nLength = sizeof(SECURITY_ATTRIBUTES); 
   sa.bInheritHandle = TRUE; 
   sa.lpSecurityDescriptor = NULL; 

   if((INVALID_HANDLE_VALUE  == (stdHandles[0] = CreateFileW(inPipeName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, &sa))) ||
	  (INVALID_HANDLE_VALUE  == (stdHandles[1] = CreateFileW(outPipeName, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, &sa))) ||
	  (INVALID_HANDLE_VALUE  == (stdHandles[2] = CreateFileW(errPipeName, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, &sa)))) 
		{ 
#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("Failed to open pipe %i, %i, %i: %i\n"), stdHandles[0], stdHandles[1], stdHandles[2], GetLastError());
	OutputDebugStringW(buffer);
#endif
		CloseHandle(stdHandles[0]);
		CloseHandle(stdHandles[1]);
		CloseHandle(stdHandles[2]);
		return -1;;
		}
   SetHandleInformation(stdHandles[0], HANDLE_FLAG_INHERIT, TRUE);
   SetHandleInformation(stdHandles[1], HANDLE_FLAG_INHERIT, TRUE);
   SetHandleInformation(stdHandles[2], HANDLE_FLAG_INHERIT, TRUE);

   if(!SetStdHandle(STD_INPUT_HANDLE, stdHandles[0])  ||
	  !SetStdHandle(STD_OUTPUT_HANDLE, stdHandles[1]) ||
	  !SetStdHandle(STD_ERROR_HANDLE, stdHandles[2])) {
#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("Failed to reassign standard streams: %i\n"), GetLastError());
	OutputDebugStringW(buffer);
#endif
		CloseHandle(stdHandles[0]);
		CloseHandle(stdHandles[1]);
		CloseHandle(stdHandles[2]);
		return -1;;
   }


 
#ifdef DEBUG_MONITOR
	_TCHAR * lpvEnv = GetEnvironmentStringsW();

	// If the returned pointer is NULL, exit.
	if (lpvEnv == NULL)
		OutputDebugStringW(_T("Cannot Read Environment\n"));
	else {
		// Variable strings are separated by NULL byte, and the block is 
		// terminated by a NULL byte. 
 
		OutputDebugStringW(_T("Starter: Environment\n"));
		for (_TCHAR * lpszVariable = (_TCHAR *) lpvEnv; *lpszVariable; lpszVariable+=_tcslen(lpszVariable) + 1) { 
			_stprintf(buffer, _T("%s\n"), lpszVariable);
			OutputDebugStringW(buffer);
		}

		FreeEnvironmentStringsW(lpvEnv);
	}
#endif
#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("Starting: %s\n"), szCmdLine);
	OutputDebugStringW(buffer);
#endif
	// Create job object if it is possible
	HMODULE hKernel = GetModuleHandle(L"kernel32.dll");
	HANDLE hJob = NULL;
    HANDLE (WINAPI * pCreateJobObject)(LPSECURITY_ATTRIBUTES lpJobAttributes,
										char * lpName);
	*(FARPROC *)&pCreateJobObject =
        GetProcAddress(hKernel, "CreateJobObjectA");

	if(NULL != pCreateJobObject)
		hJob = pCreateJobObject(NULL, NULL);
   // Spawn the other processes as part of this Process Group
   BOOL f = CreateProcessW(NULL, szCmdLine, NULL, NULL, TRUE, 
      0, NULL, NULL, &si, &pi);
   // We don't need them any more
   CloseHandle(stdHandles[0]);
   CloseHandle(stdHandles[1]);
   CloseHandle(stdHandles[2]);
   if (f)  {
#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("Process %i started\n"), pi.dwProcessId);
	OutputDebugStringW(buffer);
#endif
   	  SetEvent(waitEvent); // Means thar process has been spawned
      CloseHandle(pi.hThread);
      h[1] = pi.hProcess;

	  if(NULL != hJob) {
		HANDLE (WINAPI * pAssignProcessToJobObject)(HANDLE job, HANDLE process);
		*(FARPROC *)&pAssignProcessToJobObject =
			GetProcAddress(hKernel, "AssignProcessToJobObjectA");
		if(NULL != pAssignProcessToJobObject)
			if(!pAssignProcessToJobObject(hJob, pi.hProcess)) {
#ifdef DEBUG_MONITOR
				_stprintf(buffer, _T("Cannot assign process %i to a job\n"), pi.dwProcessId);
				OutputDebugStringW(buffer);
				DisplayErrorMessage();
#endif
			}
	  }

	  while(!exitProc)
		  {
		  // Wait for the spawned-process to die or for the event
		  // indicating that the processes should be forcibly killed.
		  switch (WaitForMultipleObjects(3, h, FALSE, INFINITE)) 
			{
			 case WAIT_OBJECT_0 + 0:  // Send Ctrl-C
#ifdef DEBUG_MONITOR
				_stprintf(buffer, _T("starter (PID %i) received CTRL-C event\n"), currentPID);
				OutputDebugStringW(buffer);
#endif
   				GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
				SetEvent(waitEvent);
				break;

			 case WAIT_OBJECT_0 + 1:  // App terminated normally
				// Make it's exit code our exit code
#ifdef DEBUG_MONITOR
				_stprintf(buffer, _T("starter: launched process has been terminated(PID %i)\n"), 
					pi.dwProcessId);
				OutputDebugStringW(buffer);
#endif
				GetExitCodeProcess(pi.hProcess, &dwExitCode);
				exitProc = TRUE;
				break;
			 case WAIT_OBJECT_0 + 2:  // Kill
#ifdef DEBUG_MONITOR
				_stprintf(buffer, _T("starter received KILL event (PID %i)\n"), currentPID);
				OutputDebugStringW(buffer);
#endif
   				GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
				if(NULL != hJob) {
					HANDLE (WINAPI * pTerminateJobObject)(HANDLE job, UINT uExitCode);
					*(FARPROC *)&pTerminateJobObject =
						GetProcAddress(hKernel, "TerminateJobObjectA");
					if(NULL != pTerminateJobObject) {
						if(!pTerminateJobObject(hJob, -1)) {
#ifdef DEBUG_MONITOR
							OutputDebugStringW(_T("Cannot terminate job\n"));
							DisplayErrorMessage();
#endif
						}
					}
				} else
				exitProc = TRUE;
				break;
			 default:
			 // Unexpected code
#ifdef DEBUG_MONITOR
				DisplayErrorMessage();
#endif
				exitProc = TRUE;
				break;
			 }
			
		  }
      CloseHandle(pi.hProcess);
   } else {
#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("Cannot start: %s\n"), szCmdLine);
	OutputDebugStringW(buffer);

	   DisplayErrorMessage();
#endif
   }
   
   if (NULL != szCmdLine) 
      {
      free(szCmdLine);
   }

   CloseHandle(waitEvent);
   CloseHandle(h[0]);
   CloseHandle(h[1]);
   CloseHandle(h[2]);
 
   return(dwExitCode);
}

/////////////////////////////////////////////////////////////////////////////////////
// Use this utility program to process correctly quotation marks in the command line
// Arguments:  
//			target - string to copy to
//			source - string to copy from
//			cpyLength - copy length
//			availSpace - size of the target buffer
// Return :number of bytes used in target, or -1 in case of error
/////////////////////////////////////////////////////////////////////////////////////
int copyTo(_TCHAR * target, const _TCHAR * source, int cpyLength, int availSpace)
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


void DisplayErrorMessage() {
	_TCHAR * lpMsgBuf;
	FormatMessageW( 
		FORMAT_MESSAGE_ALLOCATE_BUFFER | 
		FORMAT_MESSAGE_FROM_SYSTEM | 
		FORMAT_MESSAGE_IGNORE_INSERTS,
		NULL,
		GetLastError(),
		MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
		(_TCHAR *) &lpMsgBuf,
		0,
		NULL 
	);
	OutputDebugStringW(lpMsgBuf);
	// Free the buffer.
	LocalFree( lpMsgBuf );
}


//////////////////////////////// End of File //////////////////////////////////
