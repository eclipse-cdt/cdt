/* Copyright, 2002, QNX Software Systems Ltd.  All Rights Reserved

 * This source code has been published by QNX Software Systems
 * Ltd. (QSSL). However, any use, reproduction, modification, distribution
 * or transfer of this software, or any software which includes or is based
 * upon any of this code, is only permitted if expressly authorized by a
 * written license agreement from QSSL. Contact the QNX Developer's Network
 * or contact QSSL's legal department for more information.
 *
 *
 *  Win32ProcessEx.c
 *
 *  This is a JNI implementation of spawner 
 */
#include "stdafx.h"
#include <string.h>
#include <stdlib.h>
#include <process.h>
#include "Spawner.h"


#include "jni.h"
#include "io.h"

// #define DEBUG_MONITOR

#define PIPE_SIZE 512
#define MAX_CMD_SIZE 1024
#define MAX_ENV_SIZE 4096

#define MAX_PROCS (100)

typedef JNIEXPORT void * (JNICALL * JVM_GetThreadInterruptEvent)();
typedef JNIEXPORT char * (JNICALL * JVM_NativePath)(const char *);

typedef struct _procInfo {
	int pid; // Process ID
	int uid; // quasi-unique process ID
	HANDLE eventBreak;
	HANDLE eventWait;
	HANDLE eventTerminate;
} procInfo_t, * pProcInfo_t;

static int procCounter = 0;


JNIEXPORT void JNICALL ThrowByName(JNIEnv *env, const char *name, const char *msg);
pProcInfo_t createProcInfo();
pProcInfo_t findProcInfo(int pid);
unsigned int _stdcall waitProcTermination(void* pv) ;
static int copyTo(char * target, const char * source, int cpyLenght, int availSpace);
static void cleanUpProcBlock(pProcInfo_t pCurProcInfo);



typedef enum {
	SIG_NOOP,
	SIG_HUP,
	SIG_INT,
	SIG_KILL = 9,
	SIG_TERM = 15,
} signals;

extern CRITICAL_SECTION cs;


extern TCHAR path[MAX_PATH];

static HMODULE hVM = NULL;


static pProcInfo_t pInfo = NULL;


JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec0
  (JNIEnv * env, jobject process, jobjectArray cmdarray, jobjectArray envp, jstring dir, jintArray channels) 
{

    HANDLE hread[3], hwrite[3];
    SECURITY_ATTRIBUTES sa;
    PROCESS_INFORMATION pi = {0};
    STARTUPINFO si;
	DWORD flags = 0;
    char * cwd = NULL;
	LPVOID envBlk = NULL;
    int ret = 0;
	char  szCmdLine[MAX_CMD_SIZE];
	int nBlkSize = MAX_ENV_SIZE; 
	char  * szEnvBlock = (char *)malloc(nBlkSize);
	jsize nCmdTokens = 0;
	jsize nEnvVars = 0;
	int i;
	int nPos;
	pProcInfo_t pCurProcInfo;
	DWORD dwThreadId;
	char eventBreakName[20];
	char eventWaitName[20];
	char eventTerminateName[20];
#ifdef DEBUG_MONITOR
	char buffer[1000];
#endif

	if((HIBYTE(LOWORD(GetVersion()))) & 0x80)
		{
		ThrowByName(env, "java/lang/IOException", "Does not support Windows 3.1/95/98/Me");
		return 0;
		}

    if (cmdarray == 0) 
		{
		ThrowByName(env, "java/lang/NullPointerException", "No command line specified");
		return 0;
		}

    sa.nLength = sizeof(sa);
    sa.lpSecurityDescriptor = 0;
    sa.bInheritHandle = TRUE;

    memset(hread, 0, sizeof(hread));
    memset(hwrite, 0, sizeof(hwrite));
    if (!(CreatePipe(&hread[0], &hwrite[0], &sa, PIPE_SIZE) &&
          CreatePipe(&hread[1], &hwrite[1], &sa, PIPE_SIZE) &&
	      CreatePipe(&hread[2], &hwrite[2], &sa, PIPE_SIZE))) 
		{
        CloseHandle(hread[0]);
        CloseHandle(hread[1]);
        CloseHandle(hread[2]);
		CloseHandle(hwrite[0]);
		CloseHandle(hwrite[1]);
		CloseHandle(hwrite[2]);
		ThrowByName(env, "java/io/IOException", "CreatePipe");
		return 0;
		}

	nCmdTokens = (*env) -> GetArrayLength(env, cmdarray);
    nEnvVars   = (*env) -> GetArrayLength(env, envp);

	pCurProcInfo = createProcInfo();

	if(NULL == pCurProcInfo)
		{
		ThrowByName(env, "java/io/IOException", "Too many processes");
		return 0;
		}


	sprintf(eventBreakName, "SABreak%p", pCurProcInfo);
	sprintf(eventWaitName, "SAWait%p", pCurProcInfo);
	sprintf(eventTerminateName, "SATerm%p", pCurProcInfo);
	pCurProcInfo -> eventBreak = CreateEvent(NULL, TRUE, FALSE, eventBreakName);
	ResetEvent(pCurProcInfo -> eventBreak);   
	pCurProcInfo -> eventWait = CreateEvent(NULL, TRUE, FALSE, eventWaitName);
	pCurProcInfo -> eventTerminate = CreateEvent(NULL, TRUE, FALSE, eventTerminateName);
	ResetEvent(pCurProcInfo -> eventTerminate);   

	nPos = sprintf(szCmdLine, "%sstarter.exe %s %s %s ", path, eventBreakName, eventWaitName, eventTerminateName);

	// Prepare command line
	for(i = 0; i < nCmdTokens; ++i) 
		{
		jobject item = (*env) -> GetObjectArrayElement(env, cmdarray, i);
		jsize    len = (*env) -> GetStringUTFLength(env, item);
		int nCpyLen;
		const char *  str = (*env) -> GetStringUTFChars(env, item, 0);	
		if(NULL != str)
			{
			if(0 > (nCpyLen = copyTo(szCmdLine + nPos, str, len, MAX_CMD_SIZE - nPos)))
				{
				ThrowByName(env, "java/lang/Exception", "Too long command line");
				return 0;
				}
			nPos += nCpyLen;
			szCmdLine[nPos] = ' ';
			++nPos;
			(*env) -> ReleaseStringUTFChars(env, item, str);
			}
		}

	szCmdLine[nPos] = '\0';

	// Prepare environment block
    if (nEnvVars > 0) 
		{
		nPos = 0;
		for(i = 0; i < nEnvVars; ++i) 
			{
			jobject item = (*env) -> GetObjectArrayElement(env, envp, i);
			jsize    len = (*env) -> GetStringUTFLength(env, item);
			const char *  str = (*env) -> GetStringUTFChars(env, item, 0);	
			if(NULL != str)
				{
				while((nBlkSize - nPos) <= (len + 2)) // +2 for two '\0'
					{
					nBlkSize += MAX_ENV_SIZE;
					szEnvBlock = (char *)realloc(szEnvBlock, nBlkSize);
					if(NULL == szEnvBlock) 
						{
						ThrowByName(env, "java/lang/Exception", "Not enough memory");
						return 0;
						}
#ifdef DEBUG_MONITOR
					sprintf(buffer, "Realloc environment block; new length is  %i \n", nBlkSize);
					OutputDebugString(buffer);
#endif

					}
				strncpy(szEnvBlock + nPos, str, len);
				nPos += len;
				szEnvBlock[nPos] = '\0';
				++nPos;
				(*env) -> ReleaseStringUTFChars(env, item, str);
				}
			}
    	szEnvBlock[nPos] = '\0';
		envBlk = szEnvBlock;
		}



    if (dir != 0) 
		{ 
		const char * str = (*env) -> GetStringUTFChars(env, dir, 0);
		if(NULL != str)
			{
			cwd = strdup(str);
			(*env) -> ReleaseStringUTFChars(env, dir, str);
			}
		}


    memset(&si, 0, sizeof(si));
    si.cb = sizeof(si);
    si.dwFlags |= STARTF_USESTDHANDLES;
    si.dwFlags |= STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE;  // Processes in the Process Group are hidden
    si.hStdInput  = hread[0];
    si.hStdOutput = hwrite[1];
    si.hStdError  = hwrite[2];




    SetHandleInformation(hwrite[0], HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(hread[1],  HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(hread[2],  HANDLE_FLAG_INHERIT, FALSE);

	flags = CREATE_NEW_CONSOLE;
	flags |= CREATE_NO_WINDOW;

#ifdef DEBUG_MONITOR
	OutputDebugString(szCmdLine);
#endif
	
    ret = CreateProcess(0,                /* executable name */
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
	
	free(szEnvBlock);

    CloseHandle(hread[0]);
    CloseHandle(hwrite[1]);
    CloseHandle(hwrite[2]);


    if (!ret) 
		{
		LPTSTR lpMsgBuf;

		CloseHandle(hwrite[0]);
		CloseHandle(hread[1]);
		CloseHandle(hread[2]); 
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
			sprintf(buffer, "Process %i failed\n", pi.dwProcessId);
			OutputDebugString(buffer);
#endif
			cleanUpProcBlock(pCurProcInfo);
			ThrowByName(env, "java/io/IOException", "Launching failed");
			}
		else 
			{
#ifdef DEBUG_MONITOR
			sprintf(buffer, "Process %i created\n", pi.dwProcessId);
			OutputDebugString(buffer);
#endif
			ret = (long)(pCurProcInfo -> uid);
			file_handles[0] = (int)hwrite[0];
			file_handles[1] = (int)hread[1];
			file_handles[2] = (int)hread[2];
			(*env) -> SetIntArrayRegion(env, channels, 0, 3, file_handles);
			}				
		CloseHandle(h[1]);
		LeaveCriticalSection(&cs);

		}


    return ret;

}


JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_exec1
  (JNIEnv * env, jobject process, jobjectArray cmdarray, jobjectArray envp, jstring dir) 
{

    SECURITY_ATTRIBUTES sa;
    PROCESS_INFORMATION pi = {0};
    STARTUPINFO si;
	DWORD flags = 0;
    char * cwd = NULL;
	LPVOID envBlk = NULL;
    int ret = 0;
	jsize nCmdTokens = 0;
	jsize nEnvVars = 0;
	int i;
	int nPos;
	char  szCmdLine[MAX_CMD_SIZE];
	int nBlkSize = MAX_ENV_SIZE; 
	char * szEnvBlock = (char *)malloc(nBlkSize);


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
		jsize    len = (*env) -> GetStringUTFLength(env, item);
		int nCpyLen;
		const char *  str = (*env) -> GetStringUTFChars(env, item, 0);	
		if(NULL != str)
			{
			if(0 > (nCpyLen = copyTo(szCmdLine + nPos, str, len, MAX_CMD_SIZE - nPos)))
				{
				ThrowByName(env, "java/lang/Exception", "Too long command line");
				return 0;
				}
			nPos += nCpyLen;
			szCmdLine[nPos] = ' ';
			++nPos;
			(*env) -> ReleaseStringUTFChars(env, item, str);
			}
		}

	szCmdLine[nPos] = '\0';

	// Prepare environment block
    if (nEnvVars > 0) 
		{
		nPos = 0;
		for(i = 0; i < nEnvVars; ++i) 
			{
			jobject item = (*env) -> GetObjectArrayElement(env, envp, i);
			jsize    len = (*env) -> GetStringUTFLength(env, item);
			const char *  str = (*env) -> GetStringUTFChars(env, item, 0);	
			if(NULL != str)
				{
				while((nBlkSize - nPos) <= (len + 2)) // +2 for two '\0'
					{
					nBlkSize += MAX_ENV_SIZE;
					szEnvBlock = (char *)realloc(szEnvBlock, nBlkSize);
					if(NULL == szEnvBlock) 
						{
						ThrowByName(env, "java/lang/Exception", "Not enough memory");
						return 0;
						}
					}
				strncpy(szEnvBlock + nPos, str, len);
				nPos += len;
				szEnvBlock[nPos] = '\0';
				++nPos;
				(*env) -> ReleaseStringUTFChars(env, item, str);
				}
			}
    	szEnvBlock[nPos] = '\0';
		envBlk = szEnvBlock;
		}



    if (dir != 0) 
		{ 
		const char * str = (*env) -> GetStringUTFChars(env, dir, 0);
		if(NULL != str) 
			{
			cwd = strdup(str);
			(*env) -> ReleaseStringUTFChars(env, dir, str);
			}
		}


    memset(&si, 0, sizeof(si));
    si.cb = sizeof(si);





	flags = CREATE_NEW_CONSOLE;
	
    ret = CreateProcess(0,                /* executable name */
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
	free(szEnvBlock);

    if (!ret) 
		{
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
		ThrowByName(env, "java/io/IOException", lpMsgBuf);
		// Free the buffer.
		LocalFree( lpMsgBuf );		
		ret = -1;
		}
    else
		{
		CloseHandle(pi.hThread);
		CloseHandle(pi.hProcess);
		ret = (long)pi.dwProcessId; //hProcess;
		}


    return ret;

}


JNIEXPORT jint JNICALL Java_org_eclipse_cdt_utils_spawner_Spawner_raise
  (JNIEnv * env, jobject process, jint uid, jint signal) 
{
	jint ret = 0;

	HANDLE hProc;
	pProcInfo_t pCurProcInfo = findProcInfo(uid);
#ifdef DEBUG_MONITOR
	char buffer[100];
#endif
	
	if(NULL == pCurProcInfo) {
		if(SIG_INT == signal) { // Try another way
			return interruptProcess(uid) ;
		}
		return -1;
	}

#ifdef DEBUG_MONITOR
	sprintf(buffer, "Spawner received signal %i for process %i\n", signal, pCurProcInfo -> pid);
	OutputDebugString(buffer);
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
			sprintf(buffer, "Spawner received KILL or TERM signal for process %i\n", pCurProcInfo -> pid);
			OutputDebugString(buffer);
#endif
		    SetEvent(pCurProcInfo -> eventTerminate);
#ifdef DEBUG_MONITOR
			OutputDebugString("Spawner signalled KILL event\n");
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

JNIEXPORT void JNICALL 
ThrowByName(JNIEnv *env, const char *name, const char *msg)
{
    jclass cls = (*env)->FindClass(env, name);

    if (cls != 0) /* Otherwise an exception has already been thrown */
        (*env)->ThrowNew(env, cls, msg);

    /* It's a good practice to clean up the local references. */
    (*env)->DeleteLocalRef(env, cls);
}




pProcInfo_t createProcInfo()
{
	int i;
	pProcInfo_t p = NULL;

	EnterCriticalSection(&cs);

	if(NULL == pInfo)
		{
		pInfo = malloc(sizeof(procInfo_t) * MAX_PROCS);
		memset(pInfo, 0, sizeof(procInfo_t) * MAX_PROCS);
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

unsigned int _stdcall waitProcTermination(void* pv) 
{
	int i;
	int pid = (int)pv;
	DWORD rc = 0;
#ifdef DEBUG_MONITOR
	char buffer[1000];
#endif

	HANDLE hProc = OpenProcess(PROCESS_ALL_ACCESS, 0, pid);
	
	if(NULL == hProc) 
		{
#ifdef DEBUG_MONITOR
		sprintf(buffer, "waitProcTermination: cannot get handler for PID %i (error %i)\n", pid, GetLastError());
		OutputDebugString(buffer);
#endif
		}
	else
		{
		WaitForSingleObject(hProc, INFINITE);
#ifdef DEBUG_MONITOR
		sprintf(buffer, "Process PID %i terminated\n", pid);
		OutputDebugString(buffer);
#endif
		}
	
	for(i = 0; i < MAX_PROCS; ++i)
		{
		if(pInfo[i].pid == pid)
			{
			if(WaitForSingleObject(pInfo[i].eventWait, 1) == WAIT_OBJECT_0)  // Correct finish
				{
#ifdef DEBUG_MONITOR
				sprintf(buffer, "waitProcTermination: set PID %i to 0\n", pid, GetLastError());
				OutputDebugString(buffer);
#endif
				cleanUpProcBlock(pInfo + i);
				}
			break;
			} // Otherwise failed because was not started
		}

	CloseHandle(hProc);


	return 0;
}

// Return number of bytes in target or -1 in case of error
int copyTo(char * target, const char * source, int cpyLength, int availSpace)
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

	if(('\"' == *source) && ('\"' == *(source + cpyLength - 1)))
		{
		nQuotationMode = QUOTATION_DONE;
		}
	else
	if(strchr(source, ' ') == NULL)
		{
		// No reason to quotate term becase it doesn't have embedded spaces
		nQuotationMode = QUOTATION_NONE;
		}
	else
		{
		// Needs to be quotated
		nQuotationMode = QUOTATION_DO;
		*target = '\"';
		++j;
		}


	for(; i < cpyLength; ++i, ++j) 
		{
		if(source[i] == '\\')
			bSlash = TRUE;
		else
			{
			// Don't escape embracing quotation marks
			if((source[i] == '\"') && !((nQuotationMode == QUOTATION_DONE) && ((i == 0) || (i == (cpyLength - 1))) ) )
				{
				if(!bSlash) // If still not escaped
					{
					if(j == availSpace)
						return -1;
					target[j] = '\\';
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
		target[j] = '\"';
		++j;
		}

	return j;
}

