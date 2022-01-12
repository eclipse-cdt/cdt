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

#include <org_eclipse_cdt_utils_spawner_Spawner.h>

#define PIPE_SIZE 512        // Size of pipe buffer
#define MAX_CMD_SIZE 2049    // Initial size of command line
#define MAX_ENV_SIZE 4096    // Initial size of environment block
#define PIPE_NAME_LENGTH 100 // Size of pipe name buffer
#define PIPE_TIMEOUT 10000   // Default time-out value, in milliseconds.

#define MAX_PROCS (100) // Maximum number of simultaneously running processes

typedef struct _eventInfo {
    HANDLE handle;
    wchar_t *name;
} EventInfo_t;

// Process description block. Should be created for each launched process
typedef struct _procInfo {
    int pid; // Process ID
    int uid; // quasi-unique process ID; we have to create it to avoid duplicated pid
             // (actually this impossible from OS point of view but it is still possible
             // a clash of new created and already finished process with one and the same PID.
    // 4 events connected to this process (see starter)
    EventInfo_t eventBreak; // signaled when Spawner.interrupt() is called; mildest of the terminate requests (SIGINT
                            // signal in UNIX world)
    EventInfo_t eventWait;
    EventInfo_t eventTerminate; // signaled when Spawner.terminate() is called; more forceful terminate request (SIGTERM
                                // signal in UNIX world)
    EventInfo_t eventKill; // signaled when Spawner.kill() is called; most forceful terminate request (SIGKILL signal in
                           // UNIX world)
    EventInfo_t eventCtrlc; // signaled when Spawner.interruptCTRLC() is called; like interrupt() but sends CTRL-C in
                            // all cases, even when inferior is a Cygwin program
} procInfo_t, *pProcInfo_t;

static int procCounter = 0; // Number of running processes

// This is a VM helper
void ThrowByName(JNIEnv *env, const char *name, const wchar_t *msg);

// Creates _procInfo block for every launched process
pProcInfo_t createProcInfo();

// Find process description for this pid
pProcInfo_t findProcInfo(int pid);

// We launch separate thread for each project to trap it termination
void _cdecl waitProcTermination(void *pv);

// Use this function to clean project descriptor and return it to the pool of available blocks.
static void cleanUpProcBlock(pProcInfo_t pCurProcInfo);

int interruptProcess(int pid);

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

static bool createStandardNamedPipe(HANDLE *handle, DWORD stdHandle, int pid, int counter) {
    wchar_t pipeName[PIPE_NAME_LENGTH];
    DWORD dwOpenMode;

    switch (stdHandle) {
    case STD_INPUT_HANDLE:
        BUILD_PIPE_NAME(pipeName, L"stdin", pid, counter);
        dwOpenMode = PIPE_ACCESS_OUTBOUND;
        break;
    case STD_OUTPUT_HANDLE:
        BUILD_PIPE_NAME(pipeName, L"stdout", pid, counter);
        dwOpenMode = PIPE_ACCESS_INBOUND | FILE_FLAG_OVERLAPPED;
        break;
    case STD_ERROR_HANDLE:
        BUILD_PIPE_NAME(pipeName, L"stderr", pid, counter);
        dwOpenMode = PIPE_ACCESS_INBOUND | FILE_FLAG_OVERLAPPED;
        break;
    default:
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Invalid STD handle given %i", stdHandle);
        }
        return false;
    }

    HANDLE pipe = CreateNamedPipeW(pipeName, dwOpenMode, PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
                                   PIPE_UNLIMITED_INSTANCES, PIPE_SIZE, PIPE_SIZE, PIPE_TIMEOUT, NULL);
    if (INVALID_HANDLE_VALUE == pipe) {
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Failed to create named pipe: %s\n", pipeName);
        }
        return false;
    }

    SetHandleInformation(pipe, HANDLE_FLAG_INHERIT, TRUE);

    if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(L"Successfully created pipe %s -> %p\n", pipeName, pipe);
    }

    *handle = pipe;
    return true;
}

static bool createNamedEvent(EventInfo_t *eventInfo, BOOL manualReset, const wchar_t *prefix, int pid, int counter) {
    wchar_t eventName[50];
    swprintf(eventName, sizeof(eventName) / sizeof(eventName[0]), L"%s%04x%08x", prefix, pid, counter);

    HANDLE event = CreateEventW(NULL, manualReset, FALSE, eventName);
    if (!event) {
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Failed to create event %s -> %i\n", eventName, GetLastError());
        }
        return false;
    } else if (GetLastError() == ERROR_ALREADY_EXISTS) {
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Event %s already exist -> %p\n", eventName, event);
        }
        return false;
    }

    eventInfo->handle = event;
    eventInfo->name = wcsdup(eventName);

    if (!eventInfo->name) {
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Failed to allocate memory for event %s -> %p\n", eventName, event);
        }
        return false;
    }

    if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(L"Successfully created event %s -> %p\n", eventName, event);
    }

    return true;
}

static bool createCommandLine(JNIEnv *env, jobjectArray cmdarray, wchar_t **cmdLine, const wchar_t *fmt, ...) {
    va_list ap;

    va_start(ap, fmt);

    wchar_t *buffer = NULL;
    int size = MAX_CMD_SIZE;
    int required = 0;
    do {
        // Free previous buffer
        free(buffer);

        size *= 2;
        buffer = (wchar_t *)malloc(size * sizeof(wchar_t));

        if (buffer) {
            // Try to format the string
            required = vswprintf(buffer, size, fmt, ap);
        } else {
            // malloc failed, clean up and return
            va_end(ap);
            ThrowByName(env, "java/io/IOException", L"Not enough memory");
            return false;
        }
    } while (size <= required);
    va_end(ap);

    int nPos = wcslen(buffer);
    int nCmdTokens = (*env)->GetArrayLength(env, cmdarray);
    for (int i = 0; i < nCmdTokens; ++i) {
        jstring item = (jstring)(*env)->GetObjectArrayElement(env, cmdarray, i);
        jsize len = (*env)->GetStringLength(env, item);
        const jchar *str = (*env)->GetStringChars(env, item, NULL);
        if (str) {
            required = nPos + len + 2; // 2 => space + \0
            if (required > 32 * 1024) {
                free(buffer);
                ThrowByName(env, "java/io/IOException", L"Command line too long");
                return false;
            }

            while (1) {
                // Ensure enough space in buffer
                if (required > size) {
                    size *= 2;
                    if (size < required) {
                        size = required;
                    }

                    wchar_t *tmp = (wchar_t *)realloc(buffer, size * sizeof(wchar_t));
                    if (tmp) {
                        // Allocation successful
                        buffer = tmp;
                    } else {
                        // Failed to realloc memory
                        free(buffer);
                        ThrowByName(env, "java/io/IOException", L"Not enough memory");
                        return false;
                    }
                }

                int nCpyLen = copyTo(buffer + nPos, (const wchar_t *)str, len, size - nPos);
                if (nCpyLen < 0) { // Buffer too small
                    // Do a real count of number of chars required
                    required = nPos + copyTo(NULL, (const wchar_t *)str, len, INT_MAX) + 2; // 2 => space + \0
                    continue;
                }

                // Buffer was big enough.
                nPos += nCpyLen;
                break;
            }

            buffer[nPos++] = _T(' ');
            buffer[nPos] = _T('\0');
            (*env)->ReleaseStringChars(env, item, str);
        } else {
            free(buffer);
            ThrowByName(env, "java/io/IOException", L"Command line contained null string");
            return false;
        }
    }

    *cmdLine = buffer;
    return true;
}

static bool createEnvironmentBlock(JNIEnv *env, jobjectArray envp, wchar_t **block) {
    int nEnvVars = (*env)->GetArrayLength(env, envp);

    if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(L"There are %i environment variables \n", nEnvVars);
    }

    if (nEnvVars == 0) {
        *block = NULL;
        return true;
    }

    int nPos = 0;
    int bufferSize = MAX_ENV_SIZE;
    wchar_t *buffer = (wchar_t *)malloc(bufferSize * sizeof(wchar_t));
    for (int i = 0; i < nEnvVars; ++i) {
        jstring item = (jstring)(*env)->GetObjectArrayElement(env, envp, i);
        const jchar *str = (*env)->GetStringChars(env, item, 0);
        if (str) {
            int len = wcslen(str);
            while (bufferSize - nPos <= len + 2) { // +2 for two '\0'
                bufferSize += MAX_ENV_SIZE;
                wchar_t *tmp = (wchar_t *)realloc(buffer, bufferSize * sizeof(wchar_t));
                if (tmp) {
                    buffer = tmp;
                } else {
                    free(buffer);
                    (*env)->ReleaseStringChars(env, item, str);
                    ThrowByName(env, "java/io/IOException", L"Not enough memory");
                    return false;
                }
                if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                    cdtTrace(L"Realloc environment block; new length is  %i \n", bufferSize);
                }
            }
            if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                cdtTrace(L"%s\n", (const wchar_t *)str);
            }
            wcsncpy(buffer + nPos, str, len);
            nPos += len;
            buffer[nPos++] = _T('\0');

            (*env)->ReleaseStringChars(env, item, str);
        }
    }

    buffer[nPos] = _T('\0');
    *block = buffer;

    return true;
}

#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT jint JNICALL
    Java_org_eclipse_cdt_utils_spawner_Spawner_exec0(JNIEnv *env, jobject process, jobjectArray cmdarray,
                                                     jobjectArray envp, jstring dir, jobjectArray channels) {
    if (!channels) {
        ThrowByName(env, "java/io/IOException", L"Channels can't be null");
        return 0;
    }

    jclass channelClass = (*env)->FindClass(env, "org/eclipse/cdt/utils/spawner/Spawner$WinChannel");
    if (!channelClass) {
        ThrowByName(env, "java/io/IOException", L"Unable to find channel class");
        return 0;
    }

    jmethodID channelConstructor = (*env)->GetMethodID(env, channelClass, "<init>", "(J)V");
    if (!channelConstructor) {
        ThrowByName(env, "java/io/IOException", L"Unable to find channel constructor");
        return 0;
    }

    if ((HIBYTE(LOWORD(GetVersion()))) & 0x80) {
        ThrowByName(env, "java/io/IOException", L"Does not support Windows 3.1/95/98/Me");
        return 0;
    }

    if (!cmdarray) {
        ThrowByName(env, "java/lang/NullPointerException", L"No command line specified");
        return 0;
    }

    DWORD pid = GetCurrentProcessId();

    // Create pipe names
    EnterCriticalSection(&cs);
    int nLocalCounter = nCounter++;
    LeaveCriticalSection(&cs);

    HANDLE stdHandles[] = {INVALID_HANDLE_VALUE, INVALID_HANDLE_VALUE, INVALID_HANDLE_VALUE};
    if (!createStandardNamedPipe(&stdHandles[0], STD_INPUT_HANDLE, pid, nLocalCounter) ||
        !createStandardNamedPipe(&stdHandles[1], STD_OUTPUT_HANDLE, pid, nLocalCounter) ||
        !createStandardNamedPipe(&stdHandles[2], STD_ERROR_HANDLE, pid, nLocalCounter)) {
        CLOSE_HANDLES(stdHandles);
        ThrowByName(env, "java/io/IOException", L"CreatePipe");
        return 0;
    }

    pProcInfo_t pCurProcInfo = createProcInfo();
    if (!pCurProcInfo) {
        CLOSE_HANDLES(stdHandles);
        ThrowByName(env, "java/io/IOException", L"Too many processes");
        return 0;
    }

    // Create events
    if (!createNamedEvent(&pCurProcInfo->eventBreak, FALSE, L"SABreak", pid, nLocalCounter) ||
        !createNamedEvent(&pCurProcInfo->eventWait, TRUE, L"SAWait", pid, nLocalCounter) ||
        !createNamedEvent(&pCurProcInfo->eventTerminate, FALSE, L"SATerm", pid, nLocalCounter) ||
        !createNamedEvent(&pCurProcInfo->eventKill, FALSE, L"SAKill", pid, nLocalCounter) ||
        !createNamedEvent(&pCurProcInfo->eventCtrlc, FALSE, L"SACtrlc", pid, nLocalCounter)) {
        cleanUpProcBlock(pCurProcInfo);
        CLOSE_HANDLES(stdHandles);
        ThrowByName(env, "java/io/IOException", L"Cannot create event");
        return 0;
    }

    // Prepare command line
    wchar_t *cmdLine = NULL;
    if (!createCommandLine(env, cmdarray, &cmdLine, L"\"%sstarter.exe\" %i %i %s %s %s %s %s %i ", path, //
                           pid,                                                                          //
                           nLocalCounter,                                                                //
                           pCurProcInfo->eventBreak.name,                                                //
                           pCurProcInfo->eventWait.name,                                                 //
                           pCurProcInfo->eventTerminate.name,                                            //
                           pCurProcInfo->eventKill.name,                                                 //
                           pCurProcInfo->eventCtrlc.name,                                                //
                           isTraceEnabled(CDT_TRACE_SPAWNER_STARTER))) {
        // Exception already thrown, just clean up
        cleanUpProcBlock(pCurProcInfo);
        CLOSE_HANDLES(stdHandles);
        return 0;
    }

    // Prepare environment block
    wchar_t *envBlock = NULL;
    if (!createEnvironmentBlock(env, envp, &envBlock)) {
        // Exception already thrown, just clean up
        free(cmdLine);
        cleanUpProcBlock(pCurProcInfo);
        CLOSE_HANDLES(stdHandles);
        return 0;
    }

    wchar_t *cwd = NULL;
    if (dir) {
        const jchar *str = (*env)->GetStringChars(env, dir, NULL);
        if (str) {
            cwd = wcsdup((const wchar_t *)str);
            (*env)->ReleaseStringChars(env, dir, str);
        }
    }

    STARTUPINFOW si;
    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    si.dwFlags |= STARTF_USESHOWWINDOW;
    si.wShowWindow = SW_HIDE; // Processes in the Process Group are hidden

    DWORD flags = CREATE_NEW_CONSOLE;
    flags |= CREATE_NO_WINDOW;
    flags |= CREATE_UNICODE_ENVIRONMENT;

    if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(cmdLine);
    }

    // launches starter; we need it to create another console group to correctly process
    // emulation of SYSint signal (Ctrl-C)
    PROCESS_INFORMATION pi = {0};
    int ret = CreateProcessW(NULL,     /* executable name */
                             cmdLine,  /* command line */
                             0,        /* process security attribute */
                             0,        /* thread security attribute */
                             FALSE,    /* inherits system handles */
                             flags,    /* normal attached process */
                             envBlock, /* environment block */
                             cwd,      /* change to the new current directory */
                             &si,      /* (in)  startup information */
                             &pi);     /* (out) process information */

    const DWORD error_CreateProcessW = GetLastError();

    free(cwd);
    free(envBlock);
    free(cmdLine);

    if (ret) {
        HANDLE h[2];

        EnterCriticalSection(&cs);

        pCurProcInfo->pid = pi.dwProcessId;
        h[0] = pCurProcInfo->eventWait.handle;
        h[1] = pi.hProcess;

        int what = WaitForMultipleObjects(2, h, FALSE, INFINITE);
        if (what != WAIT_OBJECT_0) { // CreateProcess failed
            if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                cdtTrace(L"Process %i failed\n", pi.dwProcessId);
            }
            cleanUpProcBlock(pCurProcInfo);
            CLOSE_HANDLES(stdHandles);
            ThrowByName(env, "java/io/IOException", L"Launching failed");
            if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
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
            PROCESS_INFORMATION *piCopy = (PROCESS_INFORMATION *)malloc(sizeof(PROCESS_INFORMATION));
            memcpy(piCopy, &pi, sizeof(PROCESS_INFORMATION));
            _beginthread(waitProcTermination, 0, (void *)piCopy);

            if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                cdtTrace(L"Process started\n");
            }
        }
        LeaveCriticalSection(&cs);
    } else { // Launching error
        wchar_t *lpMsgBuf;
        CLOSE_HANDLES(stdHandles);
        lpMsgBuf = formatWinErrorCode(error_CreateProcessW);
        ThrowByName(env, "java/io/IOException", lpMsgBuf);
        // Free the buffer.
        free(lpMsgBuf);
        cleanUpProcBlock(pCurProcInfo);
        ret = -1;
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

    // Prepare command line
    wchar_t *cmdLine = NULL;
    if (!createCommandLine(env, cmdarray, &cmdLine, L"")) {
        // Exception already thrown
        return 0;
    }

    // Prepare environment block
    wchar_t *envBlock = NULL;
    if (!createEnvironmentBlock(env, envp, &envBlock)) {
        free(cmdLine);
        return 0;
    }

    wchar_t *cwd = NULL;
    if (dir) {
        const jchar *str = (*env)->GetStringChars(env, dir, NULL);
        if (str) {
            cwd = wcsdup((const wchar_t *)str);
            (*env)->ReleaseStringChars(env, dir, str);
        }
    }

    STARTUPINFOW si;
    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);

    DWORD flags = CREATE_NEW_CONSOLE;
    flags |= CREATE_UNICODE_ENVIRONMENT;

    PROCESS_INFORMATION pi = {0};
    int ret = CreateProcessW(NULL,     /* executable name */
                             cmdLine,  /* command line */
                             0,        /* process security attribute */
                             0,        /* thread security attribute */
                             TRUE,     /* inherits system handles */
                             flags,    /* normal attached process */
                             envBlock, /* environment block */
                             cwd,      /* change to the new current directory */
                             &si,      /* (in)  startup information */
                             &pi);     /* (out) process information */
    const DWORD error_CreateProcessW = GetLastError();

    free(cwd);
    free(cmdLine);
    free(envBlock);

    if (ret) {
        // Clean-up
        CloseHandle(pi.hThread);
        CloseHandle(pi.hProcess);
        ret = (long)pi.dwProcessId; // hProcess;
    } else {                        // error
        wchar_t *lpMsgBuf = formatWinErrorCode(error_CreateProcessW);
        ThrowByName(env, "java/io/IOException", lpMsgBuf);
        // Free the buffer.
        free(lpMsgBuf);
        ret = -1;
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
        if (org_eclipse_cdt_utils_spawner_Spawner_SIG_INT == signal) { // Try another way
            return interruptProcess(uid);
        }
        return -1;
    }

    if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(L"Spawner received signal %i for process %i\n", signal, pCurProcInfo->pid);
    }

    hProc = OpenProcess(SYNCHRONIZE, 0, pCurProcInfo->pid);

    if (!hProc) {
        return -1;
    }

    switch (signal) {
    case org_eclipse_cdt_utils_spawner_Spawner_SIG_NOOP:
        // Wait 0 msec -just check if the process has been still running
        ret = ((WAIT_TIMEOUT == WaitForSingleObject(hProc, 0)) ? 0 : -1);
        break;
    case org_eclipse_cdt_utils_spawner_Spawner_SIG_HUP:
        // Temporary do nothing
        ret = 0;
        break;
    case org_eclipse_cdt_utils_spawner_Spawner_SIG_TERM:
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Spawner received TERM signal for process %i\n", pCurProcInfo->pid);
        }
        SetEvent(pCurProcInfo->eventTerminate.handle);
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Spawner signaled TERM event\n");
        }
        ret = 0;
        break;

    case org_eclipse_cdt_utils_spawner_Spawner_SIG_KILL:
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Spawner received KILL signal for process %i\n", pCurProcInfo->pid);
        }
        SetEvent(pCurProcInfo->eventKill.handle);
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Spawner signaled KILL event\n");
        }
        ret = 0;
        break;
    case org_eclipse_cdt_utils_spawner_Spawner_SIG_INT:
        ResetEvent(pCurProcInfo->eventWait.handle);
        SetEvent(pCurProcInfo->eventBreak.handle);
        ret = (WaitForSingleObject(pCurProcInfo->eventWait.handle, 100) == WAIT_OBJECT_0);
        break;
    case org_eclipse_cdt_utils_spawner_Spawner_SIG_CTRLC:
        ResetEvent(pCurProcInfo->eventWait.handle);
        SetEvent(pCurProcInfo->eventCtrlc.handle);
        ret = (WaitForSingleObject(pCurProcInfo->eventWait.handle, 100) == WAIT_OBJECT_0);
        break;
    default:
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Spawner does not support custom signals on Windows\n");
        }
        ret = -1;
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
//			[in]  message to assign the event
/////////////////////////////////////////////////////////////////////////////////////
void ThrowByName(JNIEnv *env, const char *name, const wchar_t *msg) {
    jclass cls = (*env)->FindClass(env, name);

    if (cls) { /* Otherwise an exception has already been thrown */
        size_t msgLen = wcslen(msg);
        int nChars = WideCharToMultiByte(CP_UTF8, 0, msg, msgLen, NULL, 0, NULL, NULL);
        if (nChars == 0) {
            (*env)->ThrowNew(env, cls, "");
        } else {
            // ThrowNew expects message to be encoded in "modified UTF-8"
            char *buf = (char *)calloc(nChars + 1, sizeof(char));
            WideCharToMultiByte(CP_UTF8, 0, msg, msgLen, buf, nChars, NULL, NULL);
            (*env)->ThrowNew(env, cls, buf);
            free(buf);
        }
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
    EventInfo_t *eventInfos[] = {
        &pCurProcInfo->eventBreak, &pCurProcInfo->eventWait,  &pCurProcInfo->eventTerminate,
        &pCurProcInfo->eventKill,  &pCurProcInfo->eventCtrlc,
    };

    for (int i = 0; i < sizeof(eventInfos) / sizeof(eventInfos[0]); i++) {
        EventInfo_t *p = eventInfos[i];
        if (p->handle) {
            CloseHandle(p->handle);
            p->handle = NULL;
        }

        free(p->name);
        p->name = NULL;
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
            if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                cdtTrace(L"waitProcTermination: set PID %i to 0\n", pi->dwProcessId);
            }
        }
    }
    CloseHandle(pi->hProcess);

    free(pi);
}

#ifdef __cplusplus
extern "C"
#endif
    JNIEXPORT void JNICALL
    Java_org_eclipse_cdt_utils_spawner_Spawner_configureNativeTrace(JNIEnv *env, jclass cls, jboolean spawner,
                                                                    jboolean spawnerDetails, jboolean starter,
                                                                    jboolean readReport) {

    if (spawner) {
        enableTraceFor(CDT_TRACE_SPAWNER);
    }

    if (spawnerDetails) {
        enableTraceFor(CDT_TRACE_SPAWNER_DETAILS);
    }

    if (starter) {
        enableTraceFor(CDT_TRACE_SPAWNER_STARTER);
    }

    if (readReport) {
        enableTraceFor(CDT_TRACE_SPAWNER_READ_REPORT);
    }
}
