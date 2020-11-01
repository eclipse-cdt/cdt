/*******************************************************************************
 * Copyright (c) 2002, 2015 QNX Software Systems and others.
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
 *     Wind River Systems, Inc.
 *
 *  Win32ProcessEx.c
 *
 *  This is a JNI implementation of spawner
 *******************************************************************************/

#include <string.h>
#include <stdlib.h>
#include <process.h>
#include <tchar.h>
#include <windows.h>
#include <jni.h>

#include "util.h"

#include "org_eclipse_cdt_utils_spawner_Spawner.h"

#define PIPE_SIZE 512        // Size of pipe buffer
#define MAX_CMD_SIZE 2049    // Initial size of command line
#define MAX_ENV_SIZE 4096    // Initial size of environment block
#define PIPE_NAME_LENGTH 100 // Size of pipe name buffer
#define PIPE_TIMEOUT 10000   // Default time-out value, in milliseconds.

#define MAX_PROCS (100) // Maximum number of simultaneously running processes

// Process description block. Should be created for each launched process
typedef struct _procInfo {
    int pid; // Process ID
    int uid; // quasi-unique process ID; we have to create it to avoid duplicated pid
             // (actually this impossible from OS point of view but it is still possible
             // a clash of new created and already finished process with one and the same PID.
    // 4 events connected to this process (see starter)
    HANDLE eventBreak; // signaled when Spawner.interrupt() is called; mildest of the terminate requests (SIGINT signal
                       // in UNIX world)
    HANDLE eventWait;
    HANDLE eventTerminate; // signaled when Spawner.terminate() is called; more forceful terminate request (SIGTERM
                           // signal in UNIX world)
    HANDLE eventKill; // signaled when Spawner.kill() is called; most forceful terminate request (SIGKILL signal in UNIX
                      // world)
    HANDLE eventCtrlc; // signaled when Spawner.interruptCTRLC() is called; like interrupt() but sends CTRL-C in all
                       // cases, even when inferior is a Cygwin program
} procInfo_t, *pProcInfo_t;

static int procCounter = 0; // Number of running processes

// This is a VM helper
void ThrowByName(JNIEnv *env, const char *name, const char *msg);

// Creates _procInfo block for every launched process
pProcInfo_t createProcInfo();

// Find process description for this pid
pProcInfo_t findProcInfo(int pid);

// We launch separate thread for each project to trap it termination
void _cdecl waitProcTermination(void *pv);

// This is a helper function to prevent losing of quotation marks
static int copyTo(wchar_t *target, const wchar_t *source, int cpyLenght, int availSpace);

// Use this function to clean project descriptor and return it to the pool of available blocks.
static void cleanUpProcBlock(pProcInfo_t pCurProcInfo);

int interruptProcess(int pid);

// Signal codes
typedef enum {
    SIG_NOOP,      //
    SIG_HUP,       //
    SIG_INT,       //
    SIG_KILL = 9,  //
    SIG_TERM = 15, //
    CTRLC = 1000   // special, Windows only. Sends CTRL-C in all cases, even when inferior is a Cygwin program
} signals;

extern CRITICAL_SECTION cs;

extern wchar_t path[MAX_PATH]; // Directory where spawner.dll is located

static pProcInfo_t pInfo = NULL;

static int nCounter = 0; // We use it to build unique synchronization object names

/////////////////////////////////////////////////////////////////////////////////////
// Launcher; launchess process and traps its termination
// Arguments: (see Spawner.java)
//			[in]  cmdarray - array of command line elements
//			[in]  envp - array of environment variables
//			[in]  dir - working directory
//          [out] channels - streams handlers
/////////////////////////////////////////////////////////////////////////////////////

#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_Spawner_exec2(JNIEnv *env, jobject process, jobjectArray cmdarray,
                                                     jobjectArray envp, jstring dir, jobjectArray channels,
                                                     jstring slaveName, jint fdm, jboolean console) {
    return -1;
}

void ensureSize(wchar_t **ptr, int *psize, int requiredLength) {
    int size = *psize;
    if (requiredLength > size) {
        size = 2 * size;
        if (size < requiredLength) {
            size = requiredLength;
        }
        *ptr = (wchar_t *)realloc(*ptr, size * sizeof(wchar_t));
        if (*ptr) {
            *psize = size;
        } else {
            *psize = 0;
        }
    }
}

#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_Spawner_exec0(JNIEnv *env, jobject process, jobjectArray cmdarray,
                                                     jobjectArray envp, jstring dir, jobjectArray channels) {
    HANDLE stdHandles[3];
    PROCESS_INFORMATION pi = {0}, *piCopy;
    STARTUPINFOW si;
    DWORD flags = 0;
    wchar_t *cwd = NULL;
    int ret = 0;
    int nCmdLineLength = 0;
    wchar_t *szCmdLine = 0;
    int nBlkSize = MAX_ENV_SIZE;
    wchar_t *szEnvBlock = NULL;
    jsize nCmdTokens = 0;
    jsize nEnvVars = 0;
    DWORD pid = GetCurrentProcessId();
    int nPos;
    pProcInfo_t pCurProcInfo;

    // This needs to be big enough to contain the name of the event used when calling CreateEventW bellow.
    // It is made of a prefix (7 characters max) plus the value of a pointer that gets output in characters.
    // This will be bigger in the case of 64 bit.
    static const int MAX_EVENT_NAME_LENGTH = 50;
    wchar_t eventBreakName[MAX_EVENT_NAME_LENGTH];
    wchar_t eventWaitName[MAX_EVENT_NAME_LENGTH];
    wchar_t eventTerminateName[MAX_EVENT_NAME_LENGTH];
    wchar_t eventKillName[MAX_EVENT_NAME_LENGTH];
    wchar_t eventCtrlcName[MAX_EVENT_NAME_LENGTH];
    int nLocalCounter;
    wchar_t inPipeName[PIPE_NAME_LENGTH];
    wchar_t outPipeName[PIPE_NAME_LENGTH];
    wchar_t errPipeName[PIPE_NAME_LENGTH];
    jclass channelClass = NULL;
    jmethodID channelConstructor = NULL;

    if (!channels) {
        ThrowByName(env, "java/io/IOException", "Channels can't be null");
        return 0;
    }

    channelClass = (*env)->FindClass(env, "org/eclipse/cdt/utils/spawner/Spawner$WinChannel");
    if (!channelClass) {
        ThrowByName(env, "java/io/IOException", "Unable to find channel class");
        return 0;
    }

    channelConstructor = (*env)->GetMethodID(env, channelClass, "<init>", "(J)V");
    if (!channelConstructor) {
        ThrowByName(env, "java/io/IOException", "Unable to find channel constructor");
        return 0;
    }

    nCmdLineLength = MAX_CMD_SIZE;
    szCmdLine = (wchar_t *)malloc(nCmdLineLength * sizeof(wchar_t));
    szCmdLine[0] = _T('\0');
    if ((HIBYTE(LOWORD(GetVersion()))) & 0x80) {
        ThrowByName(env, "java/io/IOException", "Does not support Windows 3.1/95/98/Me");
        return 0;
    }

    if (cmdarray == 0) {
        ThrowByName(env, "java/lang/NullPointerException", "No command line specified");
        return 0;
    }

    ZeroMemory(stdHandles, sizeof(stdHandles));

    // Create pipe names
    EnterCriticalSection(&cs);
    swprintf(inPipeName, sizeof(inPipeName) / sizeof(inPipeName[0]), L"\\\\.\\pipe\\stdin%08i%010i", pid, nCounter);
    swprintf(outPipeName, sizeof(outPipeName) / sizeof(outPipeName[0]), L"\\\\.\\pipe\\stdout%08i%010i", pid, nCounter);
    swprintf(errPipeName, sizeof(errPipeName) / sizeof(errPipeName[0]), L"\\\\.\\pipe\\stderr%08i%010i", pid, nCounter);
    nLocalCounter = nCounter;
    ++nCounter;
    LeaveCriticalSection(&cs);

    if ((INVALID_HANDLE_VALUE == (stdHandles[0] = CreateNamedPipeW(
                                      inPipeName, PIPE_ACCESS_OUTBOUND, PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
                                      PIPE_UNLIMITED_INSTANCES, PIPE_SIZE, PIPE_SIZE, PIPE_TIMEOUT, NULL))) ||
        (INVALID_HANDLE_VALUE ==
         (stdHandles[1] = CreateNamedPipeW(outPipeName, PIPE_ACCESS_INBOUND | FILE_FLAG_OVERLAPPED,
                                           PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT, PIPE_UNLIMITED_INSTANCES,
                                           PIPE_SIZE, PIPE_SIZE, PIPE_TIMEOUT, NULL))) ||
        (INVALID_HANDLE_VALUE ==
         (stdHandles[2] = CreateNamedPipeW(errPipeName, PIPE_ACCESS_INBOUND | FILE_FLAG_OVERLAPPED,
                                           PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT, PIPE_UNLIMITED_INSTANCES,
                                           PIPE_SIZE, PIPE_SIZE, PIPE_TIMEOUT, NULL)))) {
        CloseHandle(stdHandles[0]);
        CloseHandle(stdHandles[1]);
        CloseHandle(stdHandles[2]);
        ThrowByName(env, "java/io/IOException", "CreatePipe");
        return 0;
    }

    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
        cdtTrace(L"Opened pipes: %s, %s, %s\n", inPipeName, outPipeName, errPipeName);
    }

    nCmdTokens = (*env)->GetArrayLength(env, cmdarray);
    nEnvVars = (*env)->GetArrayLength(env, envp);

    pCurProcInfo = createProcInfo();

    if (!pCurProcInfo) {
        ThrowByName(env, "java/io/IOException", "Too many processes");
        return 0;
    }

    // Construct starter's command line
    swprintf(eventBreakName, sizeof(eventBreakName) / sizeof(eventBreakName[0]), L"SABreak%04x%08x", pid,
             nLocalCounter);
    swprintf(eventWaitName, sizeof(eventWaitName) / sizeof(eventWaitName[0]), L"SAWait%04x%08x", pid, nLocalCounter);
    swprintf(eventTerminateName, sizeof(eventTerminateName) / sizeof(eventTerminateName[0]), L"SATerm%04x%08x", pid,
             nLocalCounter);
    swprintf(eventKillName, sizeof(eventKillName) / sizeof(eventKillName[0]), L"SAKill%04x%08x", pid, nLocalCounter);
    swprintf(eventCtrlcName, sizeof(eventCtrlcName) / sizeof(eventCtrlcName[0]), L"SACtrlc%04x%08x", pid,
             nLocalCounter);

    pCurProcInfo->eventBreak = CreateEventW(NULL, FALSE, FALSE, eventBreakName);
    if (!pCurProcInfo->eventBreak || GetLastError() == ERROR_ALREADY_EXISTS) {
        ThrowByName(env, "java/io/IOException", "Cannot create event");
        return 0;
    }
    pCurProcInfo->eventWait = CreateEventW(NULL, TRUE, FALSE, eventWaitName);
    pCurProcInfo->eventTerminate = CreateEventW(NULL, FALSE, FALSE, eventTerminateName);
    pCurProcInfo->eventKill = CreateEventW(NULL, FALSE, FALSE, eventKillName);
    pCurProcInfo->eventCtrlc = CreateEventW(NULL, FALSE, FALSE, eventCtrlcName);

    swprintf(szCmdLine, nCmdLineLength, L"\"%sstarter.exe\" %i %i %s %s %s %s %s ", path, pid, nLocalCounter,
             eventBreakName, eventWaitName, eventTerminateName, eventKillName, eventCtrlcName);
    nPos = wcslen(szCmdLine);

    // Prepare command line
    for (int i = 0; i < nCmdTokens; ++i) {
        jstring item = (jstring)(*env)->GetObjectArrayElement(env, cmdarray, i);
        jsize len = (*env)->GetStringLength(env, item);
        int nCpyLen;
        const wchar_t *str = (const wchar_t *)(*env)->GetStringChars(env, item, 0);
        if (str) {
            int requiredSize = nPos + len + 2;
            if (requiredSize > 32 * 1024) {
                ThrowByName(env, "java/io/IOException", "Command line too long");
                return 0;
            }
            ensureSize(&szCmdLine, &nCmdLineLength, requiredSize);
            if (!szCmdLine) {
                ThrowByName(env, "java/io/IOException", "Not enough memory");
                return 0;
            }

            if (0 > (nCpyLen = copyTo(szCmdLine + nPos, str, len, nCmdLineLength - nPos))) {
                ThrowByName(env, "java/io/IOException", "Command line too long");
                return 0;
            }
            nPos += nCpyLen;
            szCmdLine[nPos] = _T(' ');
            ++nPos;
            (*env)->ReleaseStringChars(env, item, (const jchar *)str);
        }
    }
    szCmdLine[nPos] = _T('\0');

    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
        cdtTrace(L"There are %i environment variables \n", nEnvVars);
    }

    // Prepare environment block
    if (nEnvVars > 0) {
        nPos = 0;
        szEnvBlock = (wchar_t *)malloc(nBlkSize * sizeof(wchar_t));
        for (int i = 0; i < nEnvVars; ++i) {
            jstring item = (jstring)(*env)->GetObjectArrayElement(env, envp, i);
            jsize len = (*env)->GetStringLength(env, item);
            const wchar_t *str = (const wchar_t *)(*env)->GetStringChars(env, item, 0);
            if (str) {
                while ((nBlkSize - nPos) <= (len + 2)) { // +2 for two '\0'
                    nBlkSize += MAX_ENV_SIZE;
                    szEnvBlock = (wchar_t *)realloc(szEnvBlock, nBlkSize * sizeof(wchar_t));
                    if (!szEnvBlock) {
                        ThrowByName(env, "java/io/IOException", "Not enough memory");
                        return 0;
                    }
                    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
                        cdtTrace(L"Realloc environment block; new length is  %i \n", nBlkSize);
                    }
                }
                if (isTraceEnabled(CDT_TRACE_MONITOR)) {
                    cdtTrace(L"%s\n", str);
                }
                wcsncpy(szEnvBlock + nPos, str, len);
                nPos += len;
                szEnvBlock[nPos] = _T('\0');
                ++nPos;
                (*env)->ReleaseStringChars(env, item, (const jchar *)str);
            }
        }
        szEnvBlock[nPos] = _T('\0');
    }

    if (dir) {
        const jchar *str = (*env)->GetStringChars(env, dir, NULL);
        if (str) {
            cwd = wcsdup((const wchar_t *)str);
            (*env)->ReleaseStringChars(env, dir, str);
        }
    }

    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    si.dwFlags |= STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE; // Processes in the Process Group are hidden

    SetHandleInformation(stdHandles[0], HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(stdHandles[1], HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(stdHandles[2], HANDLE_FLAG_INHERIT, FALSE);

    flags = CREATE_NEW_CONSOLE;
    flags |= CREATE_NO_WINDOW;
    flags |= CREATE_UNICODE_ENVIRONMENT;

    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
        cdtTrace(szCmdLine);
    }
    // launches starter; we need it to create another console group to correctly process
    // emulation of SYSint signal (Ctrl-C)
    ret = CreateProcessW(0,          /* executable name */
                         szCmdLine,  /* command line */
                         0,          /* process security attribute */
                         0,          /* thread security attribute */
                         FALSE,      /* inherits system handles */
                         flags,      /* normal attached process */
                         szEnvBlock, /* environment block */
                         cwd,        /* change to the new current directory */
                         &si,        /* (in)  startup information */
                         &pi);       /* (out) process information */

    free(cwd);
    free(szEnvBlock);
    free(szCmdLine);

    if (!ret) { // Launching error
        char *lpMsgBuf;
        CloseHandle(stdHandles[0]);
        CloseHandle(stdHandles[1]);
        CloseHandle(stdHandles[2]);
        FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
                       NULL, GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
                       (char *)&lpMsgBuf, 0, NULL);
        ThrowByName(env, "java/io/IOException", lpMsgBuf);
        // Free the buffer.
        LocalFree(lpMsgBuf);
        cleanUpProcBlock(pCurProcInfo);
        ret = -1;
    } else {
        HANDLE h[2];
        int what;

        EnterCriticalSection(&cs);

        pCurProcInfo->pid = pi.dwProcessId;
        h[0] = pCurProcInfo->eventWait;
        h[1] = pi.hProcess;

        what = WaitForMultipleObjects(2, h, FALSE, INFINITE);
        if (what != WAIT_OBJECT_0) { // CreateProcess failed
            if (isTraceEnabled(CDT_TRACE_MONITOR)) {
                cdtTrace(L"Process %i failed\n", pi.dwProcessId);
            }
            cleanUpProcBlock(pCurProcInfo);
            ThrowByName(env, "java/io/IOException", "Launching failed");
            if (isTraceEnabled(CDT_TRACE_MONITOR)) {
                cdtTrace(L"Process failed\n");
            }
        } else {
            ret = (long)(pCurProcInfo->uid);

            // Prepare stream handlers to return to java program
            for (jsize i = 0; i < 3; i++) {
                jobject chan = (*env)->NewObject(env, channelClass, channelConstructor, (jlong)stdHandles[i]);
                (*env)->SetObjectArrayElement(env, channels, i, chan);
            }

            // do the cleanup so launch the according thread
            // create a copy of the PROCESS_INFORMATION as this might get destroyed
            piCopy = (PROCESS_INFORMATION *)malloc(sizeof(PROCESS_INFORMATION));
            memcpy(piCopy, &pi, sizeof(PROCESS_INFORMATION));
            _beginthread(waitProcTermination, 0, (void *)piCopy);

            if (isTraceEnabled(CDT_TRACE_MONITOR)) {
                cdtTrace(L"Process started\n");
            }
        }
        LeaveCriticalSection(&cs);
    }

    CloseHandle(pi.hThread);

    return ret;
}

/////////////////////////////////////////////////////////////////////////////////////
// Launcher; just launches process and don't care about it any more
// Arguments: (see Spawner.java)
//			[in]  cmdarray - array of command line elements
//			[in]  envp - array of environment variables
//			[in]  dir - working directory
/////////////////////////////////////////////////////////////////////////////////////
#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_Spawner_exec1(JNIEnv *env, jobject process, jobjectArray cmdarray,
                                                     jobjectArray envp, jstring dir) {

    SECURITY_ATTRIBUTES sa;
    PROCESS_INFORMATION pi = {0};
    STARTUPINFOW si;
    DWORD flags = 0;
    wchar_t *cwd = NULL;
    wchar_t *envBlk = NULL;
    int ret = 0;
    jsize nCmdTokens = 0;
    jsize nEnvVars = 0;
    int i;
    int nPos;
    int nCmdLineLength = 0;
    wchar_t *szCmdLine = 0;
    int nBlkSize = MAX_ENV_SIZE;
    wchar_t *szEnvBlock = NULL;

    nCmdLineLength = MAX_CMD_SIZE;
    szCmdLine = (wchar_t *)malloc(nCmdLineLength * sizeof(wchar_t));
    szCmdLine[0] = 0;

    sa.nLength = sizeof(sa);
    sa.lpSecurityDescriptor = 0;
    sa.bInheritHandle = TRUE;

    nCmdTokens = (*env)->GetArrayLength(env, cmdarray);
    nEnvVars = (*env)->GetArrayLength(env, envp);

    nPos = 0;

    // Prepare command line
    for (i = 0; i < nCmdTokens; ++i) {
        jstring item = (jstring)(*env)->GetObjectArrayElement(env, cmdarray, i);
        jsize len = (*env)->GetStringLength(env, item);
        int nCpyLen;
        const wchar_t *str = (const wchar_t *)(*env)->GetStringChars(env, item, 0);
        if (str) {
            int requiredSize = nPos + len + 2;
            if (requiredSize > 32 * 1024) {
                ThrowByName(env, "java/io/IOException", "Command line too long");
                return 0;
            }
            ensureSize(&szCmdLine, &nCmdLineLength, requiredSize);
            if (!szCmdLine) {
                ThrowByName(env, "java/io/IOException", "Not enough memory");
                return 0;
            }

            if (0 > (nCpyLen = copyTo(szCmdLine + nPos, str, len, nCmdLineLength - nPos))) {
                ThrowByName(env, "java/io/Exception", "Command line too long");
                return 0;
            }
            nPos += nCpyLen;
            szCmdLine[nPos] = _T(' ');
            ++nPos;
            (*env)->ReleaseStringChars(env, item, (const jchar *)str);
        }
    }

    szCmdLine[nPos] = _T('\0');

    // Prepare environment block
    if (nEnvVars > 0) {
        szEnvBlock = (wchar_t *)malloc(nBlkSize * sizeof(wchar_t));
        nPos = 0;
        for (i = 0; i < nEnvVars; ++i) {
            jstring item = (jstring)(*env)->GetObjectArrayElement(env, envp, i);
            jsize len = (*env)->GetStringLength(env, item);
            const wchar_t *str = (const wchar_t *)(*env)->GetStringChars(env, item, 0);
            if (str) {
                while ((nBlkSize - nPos) <= (len + 2)) { // +2 for two '\0'
                    nBlkSize += MAX_ENV_SIZE;
                    szEnvBlock = (wchar_t *)realloc(szEnvBlock, nBlkSize * sizeof(wchar_t));
                    if (!szEnvBlock) {
                        ThrowByName(env, "java/io/Exception", "Not enough memory");
                        return 0;
                    }
                }
                wcsncpy(szEnvBlock + nPos, str, len);
                nPos += len;
                szEnvBlock[nPos] = _T('\0');
                ++nPos;
                (*env)->ReleaseStringChars(env, item, (const jchar *)str);
            }
        }
        szEnvBlock[nPos] = _T('\0');
        envBlk = szEnvBlock;
    }

    if (dir) {
        const jchar *str = (*env)->GetStringChars(env, dir, NULL);
        if (str) {
            cwd = wcsdup((const wchar_t *)str);
            (*env)->ReleaseStringChars(env, dir, str);
        }
    }

    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);

    flags = CREATE_NEW_CONSOLE;
    flags |= CREATE_UNICODE_ENVIRONMENT;
    ret = CreateProcessW(0,         /* executable name */
                         szCmdLine, /* command line */
                         0,         /* process security attribute */
                         0,         /* thread security attribute */
                         TRUE,      /* inherits system handles */
                         flags,     /* normal attached process */
                         envBlk,    /* environment block */
                         cwd,       /* change to the new current directory */
                         &si,       /* (in)  startup information */
                         &pi);      /* (out) process information */

    free(cwd);
    free(szEnvBlock);
    free(szCmdLine);

    if (!ret) { // error
        char *lpMsgBuf;

        FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS, NULL,
                      GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
                      (wchar_t *)&lpMsgBuf, 0, NULL);
        ThrowByName(env, "java/io/IOException", lpMsgBuf);
        // Free the buffer.
        LocalFree(lpMsgBuf);
        ret = -1;
    } else {
        // Clean-up
        CloseHandle(pi.hThread);
        CloseHandle(pi.hProcess);
        ret = (long)pi.dwProcessId; // hProcess;
    }

    return ret;
}

/////////////////////////////////////////////////////////////////////////////////////
// Emulation of the signal raising
// Arguments: (see Spawner.java)
//			[in]  uid - unique process ID
//			[in]  signal - signal to raise
/////////////////////////////////////////////////////////////////////////////////////
#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_Spawner_raise(JNIEnv *env, jobject process, jint uid, jint signal) {
    jint ret = 0;

    HANDLE hProc;
    pProcInfo_t pCurProcInfo = findProcInfo(uid);

    if (!pCurProcInfo) {
        if (SIG_INT == signal) { // Try another way
            return interruptProcess(uid);
        }
        return -1;
    }

    if (isTraceEnabled(CDT_TRACE_MONITOR)) {
        cdtTrace(L"Spawner received signal %i for process %i\n", signal, pCurProcInfo->pid);
    }

    hProc = OpenProcess(SYNCHRONIZE, 0, pCurProcInfo->pid);

    if (!hProc) {
        return -1;
    }

    switch (signal) {
    case SIG_NOOP:
        // Wait 0 msec -just check if the process has been still running
        ret = ((WAIT_TIMEOUT == WaitForSingleObject(hProc, 0)) ? 0 : -1);
        break;
    case SIG_HUP:
        // Temporary do nothing
        ret = 0;
        break;
    case SIG_TERM:
        if (isTraceEnabled(CDT_TRACE_MONITOR)) {
            cdtTrace(L"Spawner received TERM signal for process %i\n", pCurProcInfo->pid);
        }
        SetEvent(pCurProcInfo->eventTerminate);
        if (isTraceEnabled(CDT_TRACE_MONITOR)) {
            cdtTrace(L"Spawner signaled TERM event\n");
        }
        ret = 0;
        break;

    case SIG_KILL:
        if (isTraceEnabled(CDT_TRACE_MONITOR)) {
            cdtTrace(L"Spawner received KILL signal for process %i\n", pCurProcInfo->pid);
        }
        SetEvent(pCurProcInfo->eventKill);
        if (isTraceEnabled(CDT_TRACE_MONITOR)) {
            cdtTrace(L"Spawner signaled KILL event\n");
        }
        ret = 0;
        break;
    case SIG_INT:
        ResetEvent(pCurProcInfo->eventWait);
        SetEvent(pCurProcInfo->eventBreak);
        ret = (WaitForSingleObject(pCurProcInfo->eventWait, 100) == WAIT_OBJECT_0);
        break;
    case CTRLC:
        ResetEvent(pCurProcInfo->eventWait);
        SetEvent(pCurProcInfo->eventCtrlc);
        ret = (WaitForSingleObject(pCurProcInfo->eventWait, 100) == WAIT_OBJECT_0);
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
#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_Spawner_waitFor(JNIEnv *env, jobject process, jint uid) {
    DWORD exit_code = -1;
    int what = 0;
    HANDLE hProc;
    pProcInfo_t pCurProcInfo = findProcInfo(uid);

    if (!pCurProcInfo) {
        return -1;
    }

    hProc = OpenProcess(SYNCHRONIZE | PROCESS_QUERY_INFORMATION, 0, pCurProcInfo->pid);

    if (!hProc) {
        return -1;
    }

    what = WaitForSingleObject(hProc, INFINITE);

    if (what == WAIT_OBJECT_0) {
        GetExitCodeProcess(hProc, &exit_code);
    }

    if (hProc) {
        CloseHandle(hProc);
    }

    return exit_code;
}

// Utilities

/////////////////////////////////////////////////////////////////////////////////////
// Throws Java exception (will be trapped by VM).
// Arguments:
//			[in]  name - name of exception class
//			[in]  message to assign thi event
/////////////////////////////////////////////////////////////////////////////////////
void ThrowByName(JNIEnv *env, const char *name, const char *msg) {
    jclass cls = (*env)->FindClass(env, name);

    if (cls) { /* Otherwise an exception has already been thrown */
        (*env)->ThrowNew(env, cls, msg);
    }

    /* It's a good practice to clean up the local references. */
    (*env)->DeleteLocalRef(env, cls);
}

/////////////////////////////////////////////////////////////////////////////////////
// Create process description block.
// Arguments:  no
// Return : pointer to the process descriptor
/////////////////////////////////////////////////////////////////////////////////////
pProcInfo_t createProcInfo() {
    pProcInfo_t p = NULL;

    EnterCriticalSection(&cs);

    if (!pInfo) {
        pInfo = (pProcInfo_t)malloc(sizeof(procInfo_t) * MAX_PROCS);
        ZeroMemory(pInfo, sizeof(procInfo_t) * MAX_PROCS);
    }

    for (int i = 0; i < MAX_PROCS; ++i) {
        if (pInfo[i].pid == 0) {
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
pProcInfo_t findProcInfo(int uid) {
    if (pInfo) {
        for (int i = 0; i < MAX_PROCS; ++i) {
            if (pInfo[i].uid == uid) {
                return pInfo + i;
            }
        }
    }

    return NULL;
}

/////////////////////////////////////////////////////////////////////////////////////
// Cleans up vacant process descriptor
// Arguments:
//				pCurProcInfo - pointer to descriptor to clean up
// Return : no
void cleanUpProcBlock(pProcInfo_t pCurProcInfo) {
    if (0 != pCurProcInfo->eventBreak) {
        CloseHandle(pCurProcInfo->eventBreak);
        pCurProcInfo->eventBreak = 0;
    }
    if (0 != pCurProcInfo->eventWait) {
        CloseHandle(pCurProcInfo->eventWait);
        pCurProcInfo->eventWait = 0;
    }
    if (0 != pCurProcInfo->eventTerminate) {
        CloseHandle(pCurProcInfo->eventTerminate);
        pCurProcInfo->eventTerminate = 0;
    }

    if (0 != pCurProcInfo->eventKill) {
        CloseHandle(pCurProcInfo->eventKill);
        pCurProcInfo->eventKill = 0;
    }

    if (0 != pCurProcInfo->eventCtrlc) {
        CloseHandle(pCurProcInfo->eventCtrlc);
        pCurProcInfo->eventCtrlc = 0;
    }

    pCurProcInfo->pid = 0;
}

/////////////////////////////////////////////////////////////////////////////////////
// Running in separate thread and waiting for the process termination
// Arguments:
//			pv - pointer to PROCESS_INFORMATION struct
// Return : no
/////////////////////////////////////////////////////////////////////////////////////
void _cdecl waitProcTermination(void *pv) {
    PROCESS_INFORMATION *pi = (PROCESS_INFORMATION *)pv;

    // wait for process termination
    WaitForSingleObject(pi->hProcess, INFINITE);

    for (int i = 0; i < MAX_PROCS; i++) {
        if (pInfo[i].pid == pi->dwProcessId) {
            cleanUpProcBlock(pInfo + i);
            if (isTraceEnabled(CDT_TRACE_MONITOR)) {
                cdtTrace(L"waitProcTermination: set PID %i to 0\n", pi->dwProcessId);
            }
        }
    }
    CloseHandle(pi->hProcess);

    free(pi);
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
int copyTo(wchar_t *target, const wchar_t *source, int cpyLength, int availSpace) {
    bool bSlash = false;
    int i = 0, j = 0;

    enum { QUOTATION_DO, QUOTATION_DONE, QUOTATION_NONE } nQuotationMode = QUOTATION_DO;

    if (availSpace <= cpyLength) { // = to reserve space for final '\0'
        return -1;
    }

    if ((_T('\"') == *source) && (_T('\"') == *(source + cpyLength - 1))) {
        nQuotationMode = QUOTATION_DONE;
    } else if (wcschr(source, _T(' '))) {
        // Needs to be quoted
        nQuotationMode = QUOTATION_DO;
        *target = _T('\"');
        ++j;
    } else {
        // No reason to quote term because it doesn't have embedded spaces
        nQuotationMode = QUOTATION_NONE;
    }

    for (; i < cpyLength; ++i, ++j) {
        if (source[i] == _T('\\')) {
            bSlash = true;
        } else {
            // Don't escape embracing quotation marks
            if ((source[i] == _T('\"')) &&
                !((nQuotationMode == QUOTATION_DONE) && ((i == 0) || (i == (cpyLength - 1))))) {
                if (!bSlash) { // If still not escaped
                    if (j == availSpace) {
                        return -1;
                    }
                    target[j] = _T('\\');
                    ++j;
                }
            }
            bSlash = false;
        }

        if (j == availSpace) {
            return -1;
        }
        target[j] = source[i];
    }

    if (nQuotationMode == QUOTATION_DO) {
        if (j == availSpace) {
            return -1;
        }
        target[j] = _T('\"');
        ++j;
    }

    return j;
}
