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
 *  Win32ProcessEx.c
 *
 *  This is a JNI implementation of spawner 
***********************************************************************/
 
#include "stdafx.h"
#include <string.h>
#include <stdlib.h>
#include <process.h>
#include "Spawner.h"


#include "jni.h"
#include "io.h"


#define PIPE_SIZE 512			// Size of pipe buffer
#define MAX_CMD_SIZE 2049		// Maximum size of command line
#define MAX_ENV_SIZE 4096		// Initial size of environment block
#define PIPE_NAME_LENGTH 100	// Size of pipe name buffer
#define PIPE_TIMEOUT 10000		// Default time-out value, in milliseconds.

#define MAX_PROCS (100)			// Maximum number of simultaneiously runnig processes


// Process description block. Should be created for each launched process
typedef struct _procInfo {
	int pid; // Process ID
	int uid; // quasi-unique process ID; we have to create it to avoid duplicated pid 
	         // (actually this impossible from OS point of view but it is still possible
			 // a clash of new created and already finished process with one and the same PID.
	// 3 events connected to this process (see starter)
	HANDLE eventBreak;
	HANDLE eventWait;
	HANDLE eventTerminate;
} procInfo_t, * pProcInfo_t;

static int procCounter = 0; // Number of running processes


// This is a VM helper
JNIEXPORT void JNICALL ThrowByName(JNIEnv *env, const char *name, const char *msg);

// Creates _procInfo block for every launched procss
pProcInfo_t createProcInfo(); 

// Find process description for this pid
pProcInfo_t findProcInfo(int pid); 

// We launch separate thread for each project to trap it termination
unsigned int _stdcall waitProcTermination(void* pv) ;

// This is a helper function to prevent losing of quotatin marks
static int copyTo(_TCHAR * target, const _TCHAR  * source, int cpyLenght, int availSpace);

// Use this function to clean project descriptor and return it to the pool of available blocks.
static void cleanUpProcBlock(pProcInfo_t pCurProcInfo);


// Signal codes
typedef enum {
	SIG_NOOP,
	SIG_HUP,
	SIG_INT,
	SIG_KILL = 9,
	SIG_TERM = 15,
} signals;

extern CRITICAL_SECTION cs;


extern _TCHAR path[MAX_PATH]; // Directory where spawner.dll is located

static HMODULE hVM = NULL;   // VM handler


static pProcInfo_t pInfo = NULL;

static int nCounter = 0; // We use it to build unique synchronisation object names

/////////////////////////////////////////////////////////////////////////////////////
// Launcher; launchess process and traps its termination
// Arguments: (see Spawner.java)
//			[in]  cmdarray - array of command line elements 
//			[in]  envp - array of environment variables
//			[in]  dir - working directory
//          [out] channels - streams handlers
/////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec2
  (JNIEnv * env, jobject process, jobjectArray cmdarray, jobjectArray envp, jstring dir, jintArray channels, jstring slaveName, jint fdm) 
{
	return -1;
}

JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec0
  (JNIEnv * env, jobject process, jobjectArray cmdarray, jobjectArray envp, jstring dir, jintArray channels) 
{
	HANDLE stdHandles[3];
    PROCESS_INFORMATION pi = {0};
    STARTUPINFOW si;
	DWORD flags = 0;
    const _TCHAR  * cwd = NULL;
	LPVOID envBlk = NULL;
    int ret = 0;
	_TCHAR  szCmdLine[MAX_CMD_SIZE];
	int nBlkSize = MAX_ENV_SIZE; 
	_TCHAR * szEnvBlock = NULL;
	jsize nCmdTokens = 0;
	jsize nEnvVars = 0;
	int i;
	DWORD pid = GetCurrentProcessId();
	int nPos;
	pProcInfo_t pCurProcInfo;
	DWORD dwThreadId;
	_TCHAR eventBreakName[20];
	_TCHAR eventWaitName[20];
	_TCHAR eventTerminateName[20];
#ifdef DEBUG_MONITOR
	_TCHAR buffer[1000];
#endif
	int nLocalCounter;
	_TCHAR inPipeName[PIPE_NAME_LENGTH];
	_TCHAR outPipeName[PIPE_NAME_LENGTH];
	_TCHAR errPipeName[PIPE_NAME_LENGTH];

	if((HIBYTE(LOWORD(GetVersion()))) & 0x80)
		{
		ThrowByName(env, "java/io/IOException", "Does not support Windows 3.1/95/98/Me");
		return 0;
		}

    if (cmdarray == 0) 
		{
		ThrowByName(env, "java/lang/NullPointerException", "No command line specified");
		return 0;
		}

   ZeroMemory(stdHandles, sizeof(stdHandles));

	// Create pipe names
   EnterCriticalSection(&cs);
   _stprintf(inPipeName,  _T("\\\\.\\pipe\\stdin%08i%010i"),  pid, nCounter); 
   _stprintf(outPipeName, _T("\\\\.\\pipe\\stdout%08i%010i"), pid, nCounter); 
   _stprintf(errPipeName, _T("\\\\.\\pipe\\stderr%08i%010i"), pid, nCounter); 
   nLocalCounter = nCounter;
   ++nCounter;
   LeaveCriticalSection(&cs);


   if ((INVALID_HANDLE_VALUE == (stdHandles[0] = CreateNamedPipeW(inPipeName, PIPE_ACCESS_OUTBOUND,
									  PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
									  PIPE_UNLIMITED_INSTANCES, PIPE_SIZE, PIPE_SIZE, PIPE_TIMEOUT, NULL))) ||
	   (INVALID_HANDLE_VALUE == (stdHandles[1] = CreateNamedPipeW(outPipeName, PIPE_ACCESS_INBOUND | FILE_FLAG_OVERLAPPED,
									  PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
									  PIPE_UNLIMITED_INSTANCES, PIPE_SIZE, PIPE_SIZE, PIPE_TIMEOUT, NULL))) ||
	   (INVALID_HANDLE_VALUE == (stdHandles[2] = CreateNamedPipeW(errPipeName, PIPE_ACCESS_INBOUND | FILE_FLAG_OVERLAPPED,
									  PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
									  PIPE_UNLIMITED_INSTANCES, PIPE_SIZE, PIPE_SIZE, PIPE_TIMEOUT, NULL))))		{
		CloseHandle(stdHandles[0]);
        CloseHandle(stdHandles[1]);
        CloseHandle(stdHandles[2]);
		ThrowByName(env, "java/io/IOException", "CreatePipe");
		return 0;
   } 

#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("Opened pipes: %s, %s, %s\n"), inPipeName, outPipeName, errPipeName);
	OutputDebugStringW(buffer);
#endif
	

	nCmdTokens = (*env) -> GetArrayLength(env, cmdarray);
    nEnvVars   = (*env) -> GetArrayLength(env, envp);

	pCurProcInfo = createProcInfo();

	if(NULL == pCurProcInfo)
		{
		ThrowByName(env, "java/io/IOException", "Too many processes");
		return 0;
		}

	// Construct starter's command line
	_stprintf(eventBreakName, _T("SABreak%p"), pCurProcInfo);
	_stprintf(eventWaitName, _T("SAWait%p"), pCurProcInfo);
	_stprintf(eventTerminateName, _T("SATerm%p"), pCurProcInfo);
	pCurProcInfo -> eventBreak = CreateEventW(NULL, TRUE, FALSE, eventBreakName);
	ResetEvent(pCurProcInfo -> eventBreak);   
	pCurProcInfo -> eventWait = CreateEventW(NULL, TRUE, FALSE, eventWaitName);
	ResetEvent(pCurProcInfo -> eventWait);   
	pCurProcInfo -> eventTerminate = CreateEventW(NULL, TRUE, FALSE, eventTerminateName);
	ResetEvent(pCurProcInfo -> eventTerminate);   

	nPos = _stprintf(szCmdLine, _T("%sstarter.exe %i %i %s %s %s "), path, pid, nLocalCounter, eventBreakName, eventWaitName, eventTerminateName);

	// Prepare command line
	for(i = 0; i < nCmdTokens; ++i) 
		{
		jobject item = (*env) -> GetObjectArrayElement(env, cmdarray, i);
		jsize    len = (*env) -> GetStringLength(env, item);
		int nCpyLen;
		const _TCHAR *  str = (*env) -> GetStringChars(env, item, 0);	
		if(NULL != str)
			{
			if(0 > (nCpyLen = copyTo(szCmdLine + nPos, str, len, MAX_CMD_SIZE - nPos)))
				{
				ThrowByName(env, "java/io/IOException", "Too long command line");
				return 0;
				}
			nPos += nCpyLen;
			szCmdLine[nPos] = _T(' ');
			++nPos;
			(*env) -> ReleaseStringChars(env, item, str);
			}
		}

	szCmdLine[nPos] = _T('\0');


#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("There are  %i environment variables \n"), nEnvVars);
	OutputDebugStringW(buffer);
#endif
	// Prepare environment block
    if (nEnvVars > 0) 
		{
		nPos = 0;
		szEnvBlock = (_TCHAR *)malloc(nBlkSize * sizeof(_TCHAR));
		for(i = 0; i < nEnvVars; ++i) 
			{
			jobject item = (*env) -> GetObjectArrayElement(env, envp, i);
			jsize    len = (*env) -> GetStringLength(env, item);
			const _TCHAR *  str = (*env) -> GetStringChars(env, item, 0);	
			if(NULL != str)
				{
				while((nBlkSize - nPos) <= (len + 2)) // +2 for two '\0'
					{
					nBlkSize += MAX_ENV_SIZE;
					szEnvBlock = (_TCHAR *)realloc(szEnvBlock, nBlkSize * sizeof(_TCHAR));
					if(NULL == szEnvBlock) 
						{
						ThrowByName(env, "java/io/IOException", "Not enough memory");
						return 0;
						}
#ifdef DEBUG_MONITOR
					_stprintf(buffer, _T("Realloc environment block; new length is  %i \n"), nBlkSize);
					OutputDebugStringW(buffer);
#endif

					}
#ifdef DEBUG_MONITOR
				_stprintf(buffer, _T("%s\n"), str);
				OutputDebugStringW(buffer);
#endif
				_tcsnccpy(szEnvBlock + nPos, str, len);
				nPos += len;
				szEnvBlock[nPos] = _T('\0');
				++nPos;
				(*env) -> ReleaseStringChars(env, item, str);
				}
			}
			szEnvBlock[nPos] = _T('\0');
		}



    if (dir != 0) 
		{ 
		const _TCHAR * str = (*env) -> GetStringChars(env, dir, 0);
		if(NULL != str)
			{
			cwd = _tcsdup(str);
			(*env) -> ReleaseStringChars(env, dir, str);
			}
		}


    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    si.dwFlags |= STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE;  // Processes in the Process Group are hidden



    SetHandleInformation(stdHandles[0], HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(stdHandles[1],  HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(stdHandles[2],  HANDLE_FLAG_INHERIT, FALSE);

	flags = CREATE_NEW_CONSOLE;
	flags |= CREATE_NO_WINDOW;
	flags |= CREATE_UNICODE_ENVIRONMENT;

#ifdef DEBUG_MONITOR
	OutputDebugStringW(szCmdLine);
#endif
	// launches starter; we need it to create another console group to correctly process 
	// emulation of SYSint signal (Ctrl-C)
    ret = CreateProcessW(0,                /* executable name */
                        szCmdLine,        /* command line */
                        0,                /* process security attribute */
                        0,                /* thread security attribute */
                        FALSE,            /* inherits system handles */
                        flags,            /* normal attached process */
                        szEnvBlock,		  /* environment block */
                        cwd,              /* change to the new current directory */
                        &si,              /* (in)  startup information */
                        &pi);             /* (out) process information */

    

	if(NULL != cwd)
		free((void *)cwd);

	if(NULL != szEnvBlock)
		free(szEnvBlock);

    if (!ret) // Launching error
		{
		char * lpMsgBuf;

		CloseHandle(stdHandles[0]);
		CloseHandle(stdHandles[1]);
		CloseHandle(stdHandles[2]); 
		FormatMessage( 
			FORMAT_MESSAGE_ALLOCATE_BUFFER | 
			FORMAT_MESSAGE_FROM_SYSTEM | 
			FORMAT_MESSAGE_IGNORE_INSERTS,
			NULL,
			GetLastError(),
			MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
			(char *)&lpMsgBuf,
			0,
			NULL 
		);
		ThrowByName(env, "java/io/IOException", lpMsgBuf);
		// Free the buffer.
		LocalFree( lpMsgBuf );
		cleanUpProcBlock(pCurProcInfo);
		ret = -1;
		}
    else
		{
    	int file_handles[3];
		HANDLE h[2];
		int what;

		CloseHandle(pi.hThread);
		CloseHandle(pi.hProcess);

		EnterCriticalSection(&cs);

		pCurProcInfo -> pid = pi.dwProcessId;
        h[0] = pCurProcInfo -> eventWait;
		h[1] = (HANDLE)_beginthreadex(NULL, 0, waitProcTermination, 
			(void *) pi.dwProcessId, 0, (UINT*) &dwThreadId);
		
		what = WaitForMultipleObjects(2, h, FALSE, INFINITE); 
		if((what != WAIT_OBJECT_0) && (pCurProcInfo -> pid > 0)) // CreateProcess failed
			{
#ifdef DEBUG_MONITOR
			_stprintf(buffer, _T("Process %i failed\n"), pi.dwProcessId);
			OutputDebugStringW(buffer);
#endif
			cleanUpProcBlock(pCurProcInfo);
			ThrowByName(env, "java/io/IOException", "Launching failed");
#ifdef DEBUG_MONITOR
			OutputDebugStringW(_T("Process failed\n"));
#endif
			}
		else 
			{
			ret = (long)(pCurProcInfo -> uid);
			
			// Prepare stream handlers to return to java program
			file_handles[0] = (int)stdHandles[0];
			file_handles[1] = (int)stdHandles[1];
			file_handles[2] = (int)stdHandles[2];
			(*env) -> SetIntArrayRegion(env, channels, 0, 3, file_handles);
#ifdef DEBUG_MONITOR
			OutputDebugStringW(_T("Process started\n"));
#endif
			}				
		CloseHandle(h[1]);
		LeaveCriticalSection(&cs);

		}


    return ret;

}


/////////////////////////////////////////////////////////////////////////////////////
// Launcher; just launches process and don't care about it any more
// Arguments: (see Spawner.java)
//			[in]  cmdarray - array of command line elements 
//			[in]  envp - array of environment variables
//			[in]  dir - working directory
/////////////////////////////////////////////////////////////////////////////////////
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec1
  (JNIEnv * env, jobject process, jobjectArray cmdarray, jobjectArray envp, jstring dir) 
{

    SECURITY_ATTRIBUTES sa;
    PROCESS_INFORMATION pi = {0};
    STARTUPINFOW si;
	DWORD flags = 0;
    _TCHAR * cwd = NULL;
	_TCHAR * envBlk = NULL;
    int ret = 0;
	jsize nCmdTokens = 0;
	jsize nEnvVars = 0;
	int i;
	int nPos;
	_TCHAR  szCmdLine[MAX_CMD_SIZE];
	int nBlkSize = MAX_ENV_SIZE; 
	_TCHAR * szEnvBlock = NULL;


    sa.nLength = sizeof(sa);
    sa.lpSecurityDescriptor = 0;
    sa.bInheritHandle = TRUE;


	nCmdTokens = (*env) -> GetArrayLength(env, cmdarray);
    nEnvVars   = (*env) -> GetArrayLength(env, envp);

	nPos = 0;

	// Prepare command line
	for(i = 0; i < nCmdTokens; ++i) 
		{
		jobject item = (*env) -> GetObjectArrayElement(env, cmdarray, i);
		jsize    len = (*env) -> GetStringLength(env, item);
		int nCpyLen;
		const _TCHAR *  str = (*env) -> GetStringChars(env, item, 0);	
		if(NULL != str)
			{
			if(0 > (nCpyLen = copyTo(szCmdLine + nPos, str, len, MAX_CMD_SIZE - nPos)))
				{
				ThrowByName(env, "java/io/Exception", "Too long command line");
				return 0;
				}
			nPos += nCpyLen;
			szCmdLine[nPos] = _T(' ');
			++nPos;
			(*env) -> ReleaseStringChars(env, item, str);
			}
		}

	szCmdLine[nPos] = _T('\0');

	// Prepare environment block
    if (nEnvVars > 0) 
		{
		szEnvBlock = (_TCHAR *)malloc(nBlkSize * sizeof(_TCHAR));
		nPos = 0;
		for(i = 0; i < nEnvVars; ++i) 
			{
			jobject item = (*env) -> GetObjectArrayElement(env, envp, i);
			jsize    len = (*env) -> GetStringLength(env, item);
			const _TCHAR *  str = (*env) -> GetStringChars(env, item, 0);	
			if(NULL != str)
				{
				while((nBlkSize - nPos) <= (len + 2)) // +2 for two '\0'
					{
					nBlkSize += MAX_ENV_SIZE;
					szEnvBlock = (_TCHAR *)realloc(szEnvBlock, nBlkSize * sizeof(_TCHAR));
					if(NULL == szEnvBlock) 
						{
						ThrowByName(env, "java/io/Exception", "Not enough memory");
						return 0;
						}
					}
				_tcsnccpy(szEnvBlock + nPos, str, len);
				nPos += len;
				szEnvBlock[nPos] = _T('\0');
				++nPos;
				(*env) -> ReleaseStringChars(env, item, str);
				}
			}
    	szEnvBlock[nPos] = _T('\0');
		envBlk = szEnvBlock;
		}



    if (dir != 0) 
		{ 
		const _TCHAR * str = (*env) -> GetStringChars(env, dir, 0);
		if(NULL != str) 
			{
			cwd = _tcsdup(str);
			(*env) -> ReleaseStringChars(env, dir, str);
			}
		}


    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);





	flags = CREATE_NEW_CONSOLE;
	flags |= CREATE_UNICODE_ENVIRONMENT;
	
    ret = CreateProcessW(0,                /* executable name */
                        szCmdLine,              /* command line */
                        0,                /* process security attribute */
                        0,                /* thread security attribute */
                        TRUE,             /* inherits system handles */
                        flags,            /* normal attached process */
                        envBlk,       /* environment block */
                        cwd,              /* change to the new current directory */
                        &si,              /* (in)  startup information */
                        &pi);             /* (out) process information */

    

	if(NULL != cwd)
		free(cwd);
	if(NULL != szEnvBlock)
		free(szEnvBlock);

    if (!ret)  // error
		{
		char * lpMsgBuf;

		FormatMessage( 
			FORMAT_MESSAGE_ALLOCATE_BUFFER | 
			FORMAT_MESSAGE_FROM_SYSTEM | 
			FORMAT_MESSAGE_IGNORE_INSERTS,
			NULL,
			GetLastError(),
			MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
				(char *)&lpMsgBuf,
				0,
				NULL 
		);
		ThrowByName(env, "java/io/IOException", lpMsgBuf);
		// Free the buffer.
		LocalFree( lpMsgBuf );		
		ret = -1;
		}
    else
		{
		// Clean-up
		CloseHandle(pi.hThread);
		CloseHandle(pi.hProcess);
		ret = (long)pi.dwProcessId; //hProcess;
		}


    return ret;

}


/////////////////////////////////////////////////////////////////////////////////////
// Emulation of the signal raising
// Arguments: (see Spawner.java)
//			[in]  uid - unique process ID
//			[in]  signal - signal to raise
/////////////////////////////////////////////////////////////////////////////////////
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_raise
  (JNIEnv * env, jobject process, jint uid, jint signal) 
{
	jint ret = 0;

	HANDLE hProc;
	pProcInfo_t pCurProcInfo = findProcInfo(uid);
#ifdef DEBUG_MONITOR
	_TCHAR buffer[100];
#endif
	
	if(NULL == pCurProcInfo) {
		if(SIG_INT == signal) { // Try another way
			return interruptProcess(uid) ;
		}
		return -1;
	}

#ifdef DEBUG_MONITOR
	_stprintf(buffer, _T("Spawner received signal %i for process %i\n"), signal, pCurProcInfo -> pid);
	OutputDebugStringW(buffer);
#endif
	
	hProc = OpenProcess(PROCESS_ALL_ACCESS, 0, pCurProcInfo -> pid);

	if(NULL == hProc)
		return -1;

	switch(signal)
		{
		case SIG_NOOP:
			// Wait 0 msec -just check if the process has been still running
			ret = ((WAIT_TIMEOUT == WaitForSingleObject(hProc, 0)) ? 0 : -1);
			break;
		case SIG_HUP:
			// Temporary do nothing
			ret = 0;
			break;
		case SIG_KILL:
		case SIG_TERM:
#ifdef DEBUG_MONITOR
			_stprintf(buffer, _T("Spawner received KILL or TERM signal for process %i\n"), 
				pCurProcInfo -> pid);
			OutputDebugStringW(buffer);
#endif
		    SetEvent(pCurProcInfo -> eventTerminate);
#ifdef DEBUG_MONITOR
			OutputDebugStringW(_T("Spawner signalled KILL event\n"));
#endif
			ret = 0;
			break;
		case SIG_INT:
		    ResetEvent(pCurProcInfo -> eventWait);
			PulseEvent(pCurProcInfo -> eventBreak);
			ret = (WaitForSingleObject(pCurProcInfo -> eventWait, 100) == WAIT_OBJECT_0);
			break;
		default:
			break;
		}

	CloseHandle(hProc);
	return ret;


}



/////////////////////////////////////////////////////////////////////////////////////
// Wait for process termination
// Arguments: (see Spawner.java)
//			[in]  uid - unique process ID
/////////////////////////////////////////////////////////////////////////////////////
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_waitFor
  (JNIEnv * env, jobject process, jint uid) 
{
    long exit_code;
    int what=0;
	HANDLE hProc;
	pProcInfo_t pCurProcInfo = findProcInfo(uid);

	if(NULL == pCurProcInfo)
		return -1;
	
    hProc = OpenProcess(PROCESS_ALL_ACCESS, 0, pCurProcInfo -> pid);
	
	if(NULL == hProc)
		return -1;

    what = WaitForSingleObject(hProc, INFINITE);


    if (what == WAIT_OBJECT_0)
		{
		GetExitCodeProcess(hProc, &exit_code);
		}


	if(hProc)
		CloseHandle(hProc);

    return exit_code;
}





// Utilities

/////////////////////////////////////////////////////////////////////////////////////
// Throws Java exception (will be trapped by VM).
// Arguments: 
//			[in]  name - name of exception class
//			[in]  message to assign thi event
/////////////////////////////////////////////////////////////////////////////////////
JNIEXPORT void JNICALL 
ThrowByName(JNIEnv *env, const char *name, const char *msg)
{
    jclass cls = (*env)->FindClass(env, name);

    if (cls != 0) /* Otherwise an exception has already been thrown */
        (*env)->ThrowNew(env, cls, msg);

    /* It's a good practice to clean up the local references. */
    (*env)->DeleteLocalRef(env, cls);
}




/////////////////////////////////////////////////////////////////////////////////////
// Create process description block.
// Arguments:  no
// Return : pointer to the process descriptor			
/////////////////////////////////////////////////////////////////////////////////////
pProcInfo_t createProcInfo()
{
	int i;
	pProcInfo_t p = NULL;

	EnterCriticalSection(&cs);

	if(NULL == pInfo)
		{
		pInfo = malloc(sizeof(procInfo_t) * MAX_PROCS);
		ZeroMemory(pInfo, sizeof(procInfo_t) * MAX_PROCS);
		}

	for(i = 0; i < MAX_PROCS; ++i)
		{
		if(pInfo[i].pid == 0)
			{
			pInfo[i].pid = -1;
			pInfo[i].uid = ++procCounter;
			p = pInfo + i;
			break;
			}
		}
	
	LeaveCriticalSection(&cs);
	
	return p;
}

/////////////////////////////////////////////////////////////////////////////////////
// Using unique process ID finds process descriptor
// Arguments:  no
// Return : pointer to the process descriptor			
/////////////////////////////////////////////////////////////////////////////////////
pProcInfo_t findProcInfo(int uid)
{
	int i;
	pProcInfo_t p = NULL;
	if(NULL == pInfo)
		return NULL;

	for(i = 0; i < MAX_PROCS; ++i)
		{
		if(pInfo[i].uid == uid)
			{
			p = pInfo + i;
			break;
			}
		}

	return p;
}

/////////////////////////////////////////////////////////////////////////////////////
// Cleans up vacant process descriptor
// Arguments: 
//				pCurProcInfo - pointer to descriptor to clean up
// Return : no
void cleanUpProcBlock(pProcInfo_t pCurProcInfo)
{
	if(0 != pCurProcInfo -> eventBreak) 
		{
		CloseHandle(pCurProcInfo -> eventBreak);
		pCurProcInfo -> eventBreak = 0;
		}
	if(0 != pCurProcInfo -> eventWait) 
		{
		CloseHandle(pCurProcInfo -> eventWait);
		pCurProcInfo -> eventWait = 0;
		}
	if(0 != pCurProcInfo -> eventTerminate) 
		{
		CloseHandle(pCurProcInfo -> eventTerminate);
		pCurProcInfo -> eventTerminate = 0;
		}

	pCurProcInfo -> pid = 0;
}

/////////////////////////////////////////////////////////////////////////////////////
// Running in separae thread and waiting for the process termination
// Arguments:  
//			pv - (int)pv is a pid
// Return : always 0
/////////////////////////////////////////////////////////////////////////////////////
unsigned int _stdcall waitProcTermination(void* pv) 
{
	int i;
	int pid = (int)pv;
#ifdef DEBUG_MONITOR
	_TCHAR buffer[1000];
#endif

	HANDLE hProc = OpenProcess(PROCESS_ALL_ACCESS, 0, pid);
	
	if(NULL == hProc) 
		{
#ifdef DEBUG_MONITOR
		_stprintf(buffer, _T("waitProcTermination: cannot get handler for PID %i (error %i)\n"), 
			pid, 
			GetLastError());
		OutputDebugStringW(buffer);
#endif
		}
	else
		{
		WaitForSingleObject(hProc, INFINITE);
#ifdef DEBUG_MONITOR
		_stprintf(buffer, _T("Process PID %i terminated\n"), pid);
		OutputDebugStringW(buffer);
#endif
		}
	
	for(i = 0; i < MAX_PROCS; ++i)
		{
		if(pInfo[i].pid == pid)
			{
			if(WaitForSingleObject(pInfo[i].eventWait, 1) == WAIT_OBJECT_0)  // Correct finish
				{
#ifdef DEBUG_MONITOR
				_stprintf(buffer, _T("waitProcTermination: set PID %i to 0\n"), 
					pid, 
					GetLastError());
				OutputDebugStringW(buffer);
#endif
				cleanUpProcBlock(pInfo + i);
				}
			break;
			} // Otherwise failed because was not started
		}

	CloseHandle(hProc);


	return 0;
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



	if(availSpace <= cpyLength) // = to reserve space for final '\0'
		return -1;

	if((_T('\"') == *source) && (_T('\"') == *(source + cpyLength - 1)))
		{
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
			{
			// Don't escape embracing quotation marks
			if((source[i] == _T('\"')) && !((nQuotationMode == QUOTATION_DONE) && ((i == 0) || (i == (cpyLength - 1))) ) )
				{
				if(!bSlash) // If still not escaped
					{
					if(j == availSpace)
						return -1;
					target[j] = _T('\\');
					++j;
					}
				}
			bSlash = FALSE;
			}

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

