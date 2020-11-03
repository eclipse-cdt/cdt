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
#include <stdbool.h>

#include "util.h"

#define MAX_CMD_LINE_LENGTH (2049)
#define PIPE_NAME_LENGTH 100

int copyTo(wchar_t *target, const wchar_t *source, int cpyLength, int availSpace);
void DisplayErrorMessage();

// BOOL KillProcessEx(DWORD dwProcessId);  // Handle of the process

bool configureTrace() {
    for (int i = 0; i < sizeof(ALL_TRACE_KINDS) / sizeof(ALL_TRACE_KINDS[0]); i++) {
        const wchar_t *envVar = getTraceEnvVarFor(ALL_TRACE_KINDS[i]);
        if (envVar) {
            if (!_wgetenv(envVar)) {
                enableTraceFor(ALL_TRACE_KINDS[i]);
            }
        }
    }

    return true;
}

///////////////////////////////////////////////////////////////////////////////
BOOL WINAPI HandlerRoutine(DWORD dwCtrlType) { //  control signal type
    BOOL ret = TRUE;
    switch (dwCtrlType) {
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
wchar_t *cygwinBin = NULL;
bool _isCygwin = true;

bool isCygwin(HANDLE process) {
    // Have we checked before?
    if (cygwinBin || !_isCygwin) {
        return _isCygwin;
    }

    // See if this process loaded cygwin, need a different SIGINT for them
    HMODULE mods[1024];
    DWORD needed;
    if (EnumProcessModules(process, mods, sizeof(mods), &needed)) {
        int i;
        needed /= sizeof(HMODULE);
        for (i = 0; i < needed; ++i) {
            wchar_t modName[MAX_PATH];
            if (GetModuleFileNameEx(process, mods[i], modName, MAX_PATH)) {
                wchar_t *p = wcsrchr(modName, L'\\');
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

bool runCygwinCommand(wchar_t *command) {
    wchar_t cygcmd[1024];
    swprintf(cygcmd, sizeof(cygcmd) / sizeof(cygcmd[0]), L"%s\\%s", cygwinBin, command);

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

static bool openNamedPipeAsStdHandle(HANDLE *handle, DWORD stdHandle, int parentPid, int counter,
                                     SECURITY_ATTRIBUTES *sa) {
    wchar_t pipeName[PIPE_NAME_LENGTH];
    DWORD dwDesiredAccess;
    DWORD dwShareMode;

    switch (stdHandle) {
    case STD_INPUT_HANDLE:
        BUILD_PIPE_NAME(pipeName, L"stdin", parentPid, counter);
        dwDesiredAccess = GENERIC_READ;
        dwShareMode = FILE_SHARE_READ;
        break;
    case STD_OUTPUT_HANDLE:
        BUILD_PIPE_NAME(pipeName, L"stdout", parentPid, counter);
        dwDesiredAccess = GENERIC_WRITE;
        dwShareMode = FILE_SHARE_WRITE;
        break;
    case STD_ERROR_HANDLE:
        BUILD_PIPE_NAME(pipeName, L"stderr", parentPid, counter);
        dwDesiredAccess = GENERIC_WRITE;
        dwShareMode = FILE_SHARE_WRITE;
        break;
    default:
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Invalid STD handle given %i", stdHandle);
        }
        return false;
    }

    *handle = CreateFileW(pipeName, dwDesiredAccess, dwShareMode, NULL, OPEN_EXISTING, 0, sa);
    if (INVALID_HANDLE_VALUE == *handle) {
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Failed to open pipe: %s -> %p\n", pipeName, handle);
        }
        return false;
    }

    SetHandleInformation(*handle, HANDLE_FLAG_INHERIT, TRUE);

    if (!SetStdHandle(stdHandle, *handle)) {
        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Failed to reassign standard stream to pipe %s: %i\n", pipeName, GetLastError());
        }
        return false;
    }

    if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(L"Successfully assigned pipe %s -> %p\n", pipeName, *handle);
    }

    return true;
}

bool createCommandLine(int argc, wchar_t **argv, wchar_t **cmdLine) {
    int size = MAX_CMD_LINE_LENGTH;
    wchar_t *buffer = (wchar_t *)malloc(size * sizeof(wchar_t));

    if (!buffer) {
        // malloc failed
        cdtTrace(L"Not enough memory to build cmd line!\n");
        return false;
    }

    int nPos = 0;
    for (int i = 0; i < argc; ++i) {
        wchar_t *str = *(argv + i);
        int len = wcslen(str);
        if (str) {
            int required = nPos + len + 2; // 2 => space + \0
            if (required > 32 * 1024) {
                free(buffer);
                if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                    cdtTrace(L"Command line too long!\n");
                }
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
                        // realloc successful
                        buffer = tmp;
                    } else {
                        // Failed to realloc memory
                        free(buffer);
                        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                            cdtTrace(L"Not enough memory to build cmd line!\n");
                        }
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
        } else {
            free(buffer);
            if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                cdtTrace(L"Invalid argument!\n");
            }
            return false;
        }
    }

    *cmdLine = buffer;
    return true;
}

int main() {

    int argc;
    wchar_t **argv = CommandLineToArgvW(GetCommandLine(), &argc);

    // Make sure that we've been passed the right number of arguments
    if (argc < 8) {
        wprintf(L"Usage: %s (parent pid) (counter) (four inheritable event handles) (CommandLineToSpawn)\n", argv[0]);
        return 0;
    }

    configureTrace();

    STARTUPINFOW si = {sizeof(si)};
    PROCESS_INFORMATION pi = {0};
    DWORD dwExitCode = 0;

    BOOL exitProc = FALSE;
    HANDLE waitEvent = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[4]);
    HANDLE h[5];
    h[0] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[3]); // simulated SIGINT (CTRL-C or Cygwin 'kill -SIGINT')
                                                        //  h[1] we reserve for the process handle
    h[2] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[5]); // simulated SIGTERM
    h[3] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[6]); // simulated SIGKILL
    h[4] = OpenEventW(EVENT_ALL_ACCESS, TRUE, argv[7]); // CTRL-C, in all cases

    SetConsoleCtrlHandler(HandlerRoutine, TRUE);

    int parentPid = wcstol(argv[1], NULL, 10);
    int nCounter = wcstol(argv[2], NULL, 10);

    HANDLE stdHandles[] = {
        INVALID_HANDLE_VALUE, // STDIN
        INVALID_HANDLE_VALUE, // STDOUT
        INVALID_HANDLE_VALUE  // STDERR
    };

    SECURITY_ATTRIBUTES sa;
    sa.nLength = sizeof(SECURITY_ATTRIBUTES);
    sa.bInheritHandle = TRUE;
    sa.lpSecurityDescriptor = NULL;

    if (!openNamedPipeAsStdHandle(&stdHandles[0], STD_INPUT_HANDLE, parentPid, nCounter, &sa) ||
        !openNamedPipeAsStdHandle(&stdHandles[1], STD_OUTPUT_HANDLE, parentPid, nCounter, &sa) ||
        !openNamedPipeAsStdHandle(&stdHandles[2], STD_ERROR_HANDLE, parentPid, nCounter, &sa)) {
        CLOSE_HANDLES(stdHandles);
        return -1;
    }

    if (isTraceEnabled(CDT_TRACE_SPAWNER_DETAILS)) {
        wchar_t *lpvEnv = GetEnvironmentStringsW();

        if (lpvEnv) {
            // Variable strings are separated by NULL byte, and the block is
            // terminated by a NULL byte.

            cdtTrace(L"Starter: Environment\n");
            for (wchar_t *lpszVariable = lpvEnv; *lpszVariable; lpszVariable += wcslen(lpszVariable) + 1) {
                cdtTrace(L"%s\n", lpszVariable);
            }

            FreeEnvironmentStringsW(lpvEnv);
        } else {
            // If the returned pointer is NULL, exit.
            cdtTrace(L"Cannot Read Environment\n");
        }
    }

    // Create job object
    HANDLE hJob = CreateJobObject(NULL, NULL);
    if (hJob) {
        // Configure job to
        // - terminate all associated processes when the last handle to it is closed
        // - allow child processes to break away from the job.
        JOBOBJECT_EXTENDED_LIMIT_INFORMATION jobInfo;
        ZeroMemory(&jobInfo, sizeof(jobInfo));
        jobInfo.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE | JOB_OBJECT_LIMIT_BREAKAWAY_OK;
        if (!SetInformationJobObject(hJob, JobObjectExtendedLimitInformation, &jobInfo, sizeof(jobInfo))) {
            if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                cdtTrace(L"Cannot set job information\n");
                DisplayErrorMessage();
            }
        }
    } else if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(L"Cannot create job object\n");
        DisplayErrorMessage();
    }

    // Construct the full command line
    wchar_t *cmdLine = NULL;
    if (!createCommandLine(argc - 8, &argv[8], &cmdLine)) {
        return 0;
    }

    if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(L"Starting: %s\n", cmdLine);
    }

    // Spawn the other processes as part of this Process Group
    // If this process is already part of a job, the flag CREATE_BREAKAWAY_FROM_JOB
    // makes the child process detach from the job, such that we can assign it
    // to our own job object.
    BOOL f = CreateProcessW(NULL, cmdLine, NULL, NULL, TRUE, CREATE_BREAKAWAY_FROM_JOB, NULL, NULL, &si, &pi);
    // If breaking away from job is not permitted, retry without breakaway flag
    if (!f) {
        f = CreateProcessW(NULL, cmdLine, NULL, NULL, TRUE, 0, NULL, NULL, &si, &pi);
    }

    // We don't need them any more
    CLOSE_HANDLES(stdHandles);

    if (f) {
        free(cmdLine);
        cmdLine = NULL;

        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
            cdtTrace(L"Process %i started\n", pi.dwProcessId);
        }
        SetEvent(waitEvent); // Means that process has been spawned
        CloseHandle(pi.hThread);
        h[1] = pi.hProcess;

        if (hJob) {
            if (!AssignProcessToJobObject(hJob, pi.hProcess)) {
                if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                    cdtTrace(L"Cannot assign process %i to a job\n", pi.dwProcessId);
                    DisplayErrorMessage();
                }
            }
        }

        while (!exitProc) {
            // Wait for the spawned-process to die or for the event
            // indicating that the processes should be forcibly killed.
            DWORD event = WaitForMultipleObjects(5, h, FALSE, INFINITE);
            switch (event) {
            case WAIT_OBJECT_0 + 0: // SIGINT
            case WAIT_OBJECT_0 + 4: // CTRL-C
                if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                    cdtTrace(L"starter (PID %i) received CTRL-C event\n", GetCurrentProcessId());
                }
                if ((event == (WAIT_OBJECT_0 + 0)) && isCygwin(h[1])) {
                    // Need to issue a kill command
                    wchar_t kill[1024];
                    swprintf(kill, sizeof(kill) / sizeof(kill[0]), L"kill -SIGINT %d", pi.dwProcessId);
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
                if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                    cdtTrace(L"starter: launched process has been terminated(PID %i)\n", pi.dwProcessId);
                }
                GetExitCodeProcess(pi.hProcess, &dwExitCode);
                exitProc = TRUE;
                break;

                // Terminate and Kill behavior differ only for cygwin processes, where
                // we use the cygwin 'kill' command. We send a SIGKILL in one case,
                // SIGTERM in the other. For non-cygwin processes, both requests
                // are treated exactly the same
            case WAIT_OBJECT_0 + 2: // TERM
            case WAIT_OBJECT_0 + 3: // KILL
            {
                const wchar_t *signal = (event == WAIT_OBJECT_0 + 2) ? L"TERM" : L"KILL";
                if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                    cdtTrace(L"starter received %s event (PID %i)\n", signal, GetCurrentProcessId());
                }
                if (isCygwin(h[1])) {
                    // Need to issue a kill command
                    wchar_t kill[1024];
                    swprintf(kill, sizeof(kill) / sizeof(kill[0]), L"kill -%s %d", signal, pi.dwProcessId);
                    if (!runCygwinCommand(kill)) {
                        // fall back to console event
                        GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
                    }
                } else {
                    GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
                }

                SetEvent(waitEvent);

                if (hJob) {
                    if (!TerminateJobObject(hJob, (DWORD)-1)) {
                        if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                            cdtTrace(L"Cannot terminate job\n");
                            DisplayErrorMessage();
                        }
                    }
                }

                // Note that we keep trucking until the child process terminates (case WAIT_OBJECT_0 + 1)
                break;
            }

            default:
                // Unexpected code
                if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
                    DisplayErrorMessage();
                }
                exitProc = TRUE;
                break;
            }
        }
    } else if (isTraceEnabled(CDT_TRACE_SPAWNER)) {
        cdtTrace(L"Cannot start: %s\n", cmdLine);
        free(cmdLine);

        DisplayErrorMessage();
    }

    CloseHandle(waitEvent);
    CLOSE_HANDLES(h);

    return dwExitCode;
}

void DisplayErrorMessage() {
    wchar_t *lpMsgBuf;
    FormatMessageW(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS, NULL,
                   GetLastError(), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
                   (wchar_t *)&lpMsgBuf, 0, NULL);
    OutputDebugStringW(lpMsgBuf);
    // Free the buffer.
    LocalFree(lpMsgBuf);
}
//////////////////////////////// End of File //////////////////////////////////
