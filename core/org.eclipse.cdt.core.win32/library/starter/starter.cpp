/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
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
 *******************************************************************************/

#define STRICT
#define _WIN32_WINNT 0x0500
#include <windows.h>
#include <process.h>
#include <tchar.h>
#include <stdio.h>
#include <psapi.h>

//#define DEBUG_MONITOR 
#define MAX_CMD_LINE_LENGTH (2049)
#define PIPE_NAME_LENGTH 100

int copyTo(wchar_t * target, const wchar_t * source, int cpyLength,
		int availSpace);
void DisplayErrorMessage();

//BOOL KillProcessEx(DWORD dwProcessId);  // Handle of the process 

///////////////////////////////////////////////////////////////////////////////
BOOL WINAPI HandlerRoutine( DWORD dwCtrlType) //  control signal type
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

// The default here means we haven't checked yet
// i.e. cygwin is true but the bin dir hasn't been captured
wchar_t * cygwinBin = NULL;
bool _isCygwin = true;

bool isCygwin(HANDLE process) {
	// Have we checked before?
	if (cygwinBin != NULL || !_isCygwin)
		return _isCygwin;
	
	// See if this process loaded cygwin, need a different SIGINT for them
	HMODULE mods[1024];
	DWORD needed;
	if (EnumProcessModules(process, mods, sizeof(mods), &needed)) {
		int i;
		needed /= sizeof(HMODULE);
		for (i = 0; i < needed; ++i ) {
			wchar_t modName[MAX_PATH];
			if (GetModuleFileNameEx(process, mods[i], modName, MAX_PATH)) {
				wchar_t * p = wcsrchr(modName, L'\\');
				if (p) {
					*p = 0; // Null terminate there for future reference
					if (!wcscmp(++p, L"cygwin1.dll")) {
						_isCygwin = true;
						// Store away the bind dir
						cygwinBin = wcsdup(modName);
						return _isCygwin;
					}
				}
			}
		}
	}
	
	_isCygwin = false;
	return _isCygwin;
}

bool runCygwinCommand(wchar_t * command) {
	wchar_t cygcmd[1024];
	swprintf(cygcmd, L"%s\\%s", cygwinBin, command);
	
	STARTUPINFO si;
	ZeroMemory(&si, sizeof(si));
	si.cb = sizeof(si);
	PROCESS_INFORMATION pi;
	ZeroMemory(&pi, sizeof(pi));
	if (CreateProcess(NULL, cygcmd, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi)) {
		WaitForSingleObject(pi.hProcess, INFINITE);
		CloseHandle(pi.hThread);
		CloseHandle(pi.hProcess);
		return true;
	} else if (CreateProcess(NULL, command, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi)) {
		WaitForSingleObject(pi.hProcess, INFINITE);
		CloseHandle(pi.hThread);
		CloseHandle(pi.hProcess);
		return true;
	}
	return false;
}

void ensureSize(wchar_t** ptr, int* psize, int requiredLength) {
	int size= *psize;
	if (requiredLength > size) {
		size= 2*size;
		if (size < requiredLength) {
			size= requiredLength;
		}
		*ptr= (wchar_t *)realloc(*ptr, size * sizeof(wchar_t));
		if (NULL == *ptr) {
			*psize= 0;
		} else {
			*psize= size;
		}
	}
}

int main() {

	int argc;
	wchar_t ** argv = CommandLineToArgvW(GetCommandLine(), &argc);

	// Make sure that we've been passed the right number of arguments
	if (argc < 8) {
		_tprintf(_T("Usage: %s (four inheritable event handles) (CommandLineToSpawn)\n"),
				argv[0]);
		return(0);
	}

	// Construct the full command line
	int nCmdLineLength= MAX_CMD_LINE_LENGTH;
	wchar_t * szCmdLine= (wchar_t *)malloc(nCmdLineLength * sizeof(wchar_t));
	szCmdLine[0]= 0;
	int nPos = 0;

	for(int i = 8; i < argc; ++i)
	{
		int nCpyLen;
		int len= wcslen(argv[i]);
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

	STARTUPINFOW si = {sizeof(si)};
	PROCESS_INFORMATION pi = {0};
	DWORD dwExitCode = 0;
#ifdef DEBUG_MONITOR
	int currentPID = GetCurrentProcessId();
	wchar_t buffer[MAX_CMD_LINE_LENGTH];
#endif

	BOOL exitProc = FALSE;
	HANDLE waitEvent = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[4]);
	HANDLE h[5];
	h[0] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[3]);	// simulated SIGINT (CTRL-C or Cygwin 'kill -SIGINT')
//  h[1] we reserve for the process handle
	h[2] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[5]); // simulated SIGTERM
	h[3] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[6]); // simulated SIGKILL
	h[4] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[7]); // CTRL-C, in all cases
	
	SetConsoleCtrlHandler(HandlerRoutine, TRUE);

	int parentPid = wcstol(argv[1], NULL, 10);
	int nCounter = wcstol(argv[2], NULL, 10);
	wchar_t inPipeName[PIPE_NAME_LENGTH];
	wchar_t outPipeName[PIPE_NAME_LENGTH];
	wchar_t errPipeName[PIPE_NAME_LENGTH];

	swprintf(inPipeName, L"\\\\.\\pipe\\stdin%08i%010i", parentPid, nCounter);
	swprintf(outPipeName, L"\\\\.\\pipe\\stdout%08i%010i", parentPid, nCounter);
	swprintf(errPipeName, L"\\\\.\\pipe\\stderr%08i%010i", parentPid, nCounter);
#ifdef DEBUG_MONITOR
	swprintf(buffer, _T("Pipes: %s, %s, %s\n"), inPipeName, outPipeName, errPipeName);
	OutputDebugStringW(buffer);
#endif

	HANDLE stdHandles[3];

	SECURITY_ATTRIBUTES sa;
	sa.nLength = sizeof(SECURITY_ATTRIBUTES);
	sa.bInheritHandle = TRUE;
	sa.lpSecurityDescriptor = NULL;

	if((INVALID_HANDLE_VALUE == (stdHandles[0] = CreateFileW(inPipeName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, &sa))) ||
			(INVALID_HANDLE_VALUE == (stdHandles[1] = CreateFileW(outPipeName, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, &sa))) ||
			(INVALID_HANDLE_VALUE == (stdHandles[2] = CreateFileW(errPipeName, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, &sa))))
	{
#ifdef DEBUG_MONITOR
		swprintf(buffer, _T("Failed to open pipe %i, %i, %i: %i\n"), stdHandles[0], stdHandles[1], stdHandles[2], GetLastError());
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

	if(!SetStdHandle(STD_INPUT_HANDLE, stdHandles[0]) ||
			!SetStdHandle(STD_OUTPUT_HANDLE, stdHandles[1]) ||
			!SetStdHandle(STD_ERROR_HANDLE, stdHandles[2])) {
#ifdef DEBUG_MONITOR
		swprintf(buffer, _T("Failed to reassign standard streams: %i\n"), GetLastError());
		OutputDebugStringW(buffer);
#endif
		CloseHandle(stdHandles[0]);
		CloseHandle(stdHandles[1]);
		CloseHandle(stdHandles[2]);
		return -1;;
	}

#ifdef DEBUG_MONITOR
	wchar_t * lpvEnv = GetEnvironmentStringsW();

	// If the returned pointer is NULL, exit.
	if (lpvEnv == NULL)
	OutputDebugStringW(_T("Cannot Read Environment\n"));
	else {
		// Variable strings are separated by NULL byte, and the block is 
		// terminated by a NULL byte. 

		OutputDebugStringW(_T("Starter: Environment\n"));
		for (wchar_t * lpszVariable = (wchar_t *) lpvEnv; *lpszVariable; lpszVariable+=wcslen(lpszVariable) + 1) {
			swprintf(buffer, _T("%s\n"), lpszVariable);
			OutputDebugStringW(buffer);
		}

		FreeEnvironmentStringsW(lpvEnv);
	}
#endif
#ifdef DEBUG_MONITOR
	swprintf(buffer, _T("Starting: %s\n"), szCmdLine);
	OutputDebugStringW(buffer);
#endif
	// Create job object
	HANDLE hJob = CreateJobObject(NULL, NULL);
	
	// Spawn the other processes as part of this Process Group
	BOOL f = CreateProcessW(NULL, szCmdLine, NULL, NULL, TRUE,
			0, NULL, NULL, &si, &pi);

	// We don't need them any more
	CloseHandle(stdHandles[0]);
	CloseHandle(stdHandles[1]);
	CloseHandle(stdHandles[2]);
	
	if (f) {
#ifdef DEBUG_MONITOR
		swprintf(buffer, _T("Process %i started\n"), pi.dwProcessId);
		OutputDebugStringW(buffer);
#endif
		SetEvent(waitEvent); // Means thar process has been spawned
		CloseHandle(pi.hThread);
		h[1] = pi.hProcess;

		if(NULL != hJob) {
			if(!AssignProcessToJobObject(hJob, pi.hProcess)) {
#ifdef DEBUG_MONITOR
				swprintf(buffer, _T("Cannot assign process %i to a job\n"), pi.dwProcessId);
				OutputDebugStringW(buffer);
				DisplayErrorMessage();
#endif
			}
		}

		while(!exitProc)
		{
			// Wait for the spawned-process to die or for the event
			// indicating that the processes should be forcibly killed.
			DWORD event = WaitForMultipleObjects(5, h, FALSE, INFINITE);
			switch (event)
			{
			case WAIT_OBJECT_0 + 0: // SIGINT
			case WAIT_OBJECT_0 + 4: // CTRL-C
#ifdef DEBUG_MONITOR
				swprintf(buffer, _T("starter (PID %i) received CTRL-C event\n"), currentPID);
				OutputDebugStringW(buffer);
#endif
				if ((event == (WAIT_OBJECT_0 + 0)) && isCygwin(h[1])) {
					// Need to issue a kill command
					wchar_t kill[1024];
					swprintf(kill, L"kill -SIGINT %d", pi.dwProcessId);
					if (!runCygwinCommand(kill)) {
						// fall back to console event
						GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
					}
				} else {
					GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
				}

				SetEvent(waitEvent);
				break;

			case WAIT_OBJECT_0 + 1: // App terminated normally
				// Make it's exit code our exit code
#ifdef DEBUG_MONITOR
				swprintf(buffer, _T("starter: launched process has been terminated(PID %i)\n"),
						pi.dwProcessId);
				OutputDebugStringW(buffer);
#endif
				GetExitCodeProcess(pi.hProcess, &dwExitCode);
				exitProc = TRUE;
				break;

			// Terminate and Kill behavior differ only for cygwin processes, where
			// we use the cygwin 'kill' command. We send a SIGKILL in one case,
			// SIGTERM in the other. For non-cygwin processes, both requests
			// are treated exactly the same
			case WAIT_OBJECT_0 + 2:	// TERM
			case WAIT_OBJECT_0 + 3:	// KILL
			{
				const wchar_t* signal = (event == WAIT_OBJECT_0 + 2) ? L"TERM" : L"KILL";
#ifdef DEBUG_MONITOR
				swprintf(buffer, _T("starter received %s event (PID %i)\n"), signal, currentPID);
				OutputDebugStringW(buffer);
#endif
				if (isCygwin(h[1])) {
					// Need to issue a kill command
					wchar_t kill[1024];
					swprintf(kill, L"kill -%s %d", signal, pi.dwProcessId);
					if (!runCygwinCommand(kill)) {
						// fall back to console event
						GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
					}
				} else {				
					GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
				}
				
				SetEvent(waitEvent);
				
				if(NULL != hJob) {
					if(!TerminateJobObject(hJob, (DWORD)-1)) {
#ifdef DEBUG_MONITOR
						OutputDebugStringW(_T("Cannot terminate job\n"));
						DisplayErrorMessage();
#endif
					}
				}

				// Note that we keep trucking until the child process terminates (case WAIT_OBJECT_0 + 1)
				break;
			}

			default:
				// Unexpected code
#ifdef DEBUG_MONITOR
				DisplayErrorMessage();
#endif
				exitProc = TRUE;
				break;
			}

		}
	} else {
#ifdef DEBUG_MONITOR
		swprintf(buffer, _T("Cannot start: %s\n"), szCmdLine);
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
	CloseHandle(h[3]);
	CloseHandle(h[4]);

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
int copyTo(wchar_t * target, const wchar_t * source, int cpyLength,
		int availSpace) {
	BOOL bSlash = FALSE;
	int i = 0, j = 0;
	int totCpyLength = cpyLength;

#define QUOTATION_DO   0
#define QUOTATION_DONE 1
#define QUOTATION_NONE 2

	int nQuotationMode = 0;
	if (availSpace <= cpyLength) // = to reserve space for '\0'
		return -1;

	if ((_T('\"') == *source) && (_T('\"') == *(source + cpyLength - 1))) {
		// Already done
		nQuotationMode = QUOTATION_DONE;
	} else if (wcschr(source, _T(' '))== NULL) {
		// No reason to quotate term becase it doesn't have embedded spaces
		nQuotationMode = QUOTATION_NONE;
	} else {
		// Needs to be quotated
		nQuotationMode = QUOTATION_DO;
		*target = _T('\"');
		++j;
	}

	for (; i < cpyLength; ++i, ++j) {
		if (source[i] == _T('\\'))
			bSlash = TRUE;
		else
		// Don't escape embracing quotation marks
		if ((source[i] == _T('\"')) && !((nQuotationMode == QUOTATION_DONE) && ((i == 0) || (i == (cpyLength - 1))) )) {
			if (!bSlash) {
				if (j == availSpace)
					return -1;
				target[j] = _T('\\');
				++j;
			}
			bSlash = FALSE;
		} else
			bSlash = FALSE;

		if (j == availSpace)
			return -1;
		target[j] = source[i];
	}

	if (nQuotationMode == QUOTATION_DO) {
		if (j == availSpace)
			return -1;
		target[j] = _T('\"');
		++j;
	}
	return j;
}

void DisplayErrorMessage() {
	wchar_t * lpMsgBuf;
	FormatMessageW(
			FORMAT_MESSAGE_ALLOCATE_BUFFER |FORMAT_MESSAGE_FROM_SYSTEM |FORMAT_MESSAGE_IGNORE_INSERTS,
			NULL, GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
			(wchar_t *) &lpMsgBuf, 0, NULL);
	OutputDebugStringW(lpMsgBuf);
	// Free the buffer.
	LocalFree(lpMsgBuf);
}

//////////////////////////////// End of File //////////////////////////////////
