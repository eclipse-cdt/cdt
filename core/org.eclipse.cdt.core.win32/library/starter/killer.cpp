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
 *  starter.cpp
 *
 *  This is a helper function for the process killing
 *  Implementation based on the article "Terminating Windows Processes"
 *  see http://www.alexfedotov.com/articles/killproc.asp
***********************************************************************/



#define STRICT
#include <Windows.h>
#include <Tlhelp32.h>
#include <process.h>
#include <tchar.h>
#include <stdio.h>

#include "killer.h"

#define SystemProcessesAndThreadsInformation 5

#define MAX_CMD_LINE_LENGTH 512

//#define DEBUG_MONITOR

void DisplayErrorMessage();

BOOL KillProcessEx(
    IN DWORD dwProcessId  // Handle of the process 
    )
{

    OSVERSIONINFO osvi;
    DWORD dwError;
#ifdef DEBUG_MONITOR
   _TCHAR buffer[MAX_CMD_LINE_LENGTH];
#endif

    // determine operating system version
    osvi.dwOSVersionInfoSize = sizeof(osvi);
    GetVersionEx(&osvi);

    if (osvi.dwPlatformId == VER_PLATFORM_WIN32_NT)
    {
        HINSTANCE hNtDll;
        NTSTATUS (WINAPI * pZwQuerySystemInformation)(UINT, PVOID, 
                                                      ULONG, PULONG);

        // get NTDLL.DLL handle
        hNtDll = GetModuleHandleW(_T("ntdll.dll"));
        if(hNtDll == NULL) {
#ifdef DEBUG_MONITOR
	        _stprintf(buffer, _T("Failed to get ntdll.dll handle"));
	        OutputDebugStringW(buffer);
#endif
            return FALSE;
        }

        // find address of ZwQuerySystemInformation
        *(FARPROC *)&pZwQuerySystemInformation =
            GetProcAddress(hNtDll, "ZwQuerySystemInformation");
        if (pZwQuerySystemInformation == NULL)
            return SetLastError(ERROR_PROC_NOT_FOUND), NULL;

        // get default process heap handle
        HANDLE hHeap = GetProcessHeap();
    
        NTSTATUS Status;
        ULONG cbBuffer = 0x8000;
        PVOID pBuffer = NULL;

        // it is difficult to predict what buffer size will be
        // enough, so we start with 32K buffer and increase its
        // size as needed
        do
        {
            pBuffer = HeapAlloc(hHeap, 0, cbBuffer);
            if (pBuffer == NULL)
                return SetLastError(ERROR_NOT_ENOUGH_MEMORY), FALSE;

            Status = pZwQuerySystemInformation(
                            SystemProcessesAndThreadsInformation,
                            pBuffer, cbBuffer, NULL);

            if (Status == STATUS_INFO_LENGTH_MISMATCH)
            {
                HeapFree(hHeap, 0, pBuffer);
                cbBuffer *= 2;
            }
            else if (!NT_SUCCESS(Status))
            {
                HeapFree(hHeap, 0, pBuffer);
                return SetLastError(Status), NULL;
            }
        }
        while (Status == STATUS_INFO_LENGTH_MISMATCH);

        // call the helper
        dwError = KillProcessTreeNtHelper(
                          (PSYSTEM_PROCESS_INFORMATION)pBuffer, 
                          dwProcessId);
        
        HeapFree(hHeap, 0, pBuffer);
    }
    else
    {
        // call the helper
        dwError = KillProcessTreeWinHelper(dwProcessId);
    }

    SetLastError(dwError);
    return dwError == ERROR_SUCCESS;
}

// Heloer function for process killing

static BOOL KillProcess(
    IN DWORD dwProcessId
    )
{
    // get process handle
    HANDLE hProcess = OpenProcess(PROCESS_TERMINATE, FALSE, dwProcessId);
    if (hProcess == NULL)
        return FALSE;

    DWORD dwError = ERROR_SUCCESS;

    // try to terminate the process
    if (!TerminateProcess(hProcess, (DWORD)-1))
        dwError = GetLastError();

    // close process handle
    CloseHandle(hProcess);

    SetLastError(dwError);
#ifdef DEBUG_MONITOR
    if(dwError != ERROR_SUCCESS) {
	    _stprintf(buffer, _T("Process %i killed"), dwProcessId);
    	OutputDebugStringW(buffer);
    } else {
	    _stprintf(buffer, _T("Failed to kill process %i"), dwProcessId);
    	OutputDebugStringW(buffer);
        DisplayMessage();
    }
#endif
    return dwError == ERROR_SUCCESS;
}

// a helper function that walks a process tree recursively
// on Windows NT and terminates all processes in the tree
static BOOL KillProcessTreeNtHelper(
    IN PSYSTEM_PROCESS_INFORMATION pInfo,
    IN DWORD dwProcessId
    )
{
#ifdef DEBUG_MONITOR
   _TCHAR buffer[MAX_CMD_LINE_LENGTH];
#endif
        if(pInfo == NULL) {
#ifdef DEBUG_MONITOR
	        _stprintf(buffer, _T("KillProcessTreeNtHelper: wrong parameter"));
	        OutputDebugStringW(buffer);
#endif
            return FALSE;
        }


    // terminate all children first
    for (;;)
    {
        if (pInfo->InheritedFromProcessId == dwProcessId)
            KillProcessTreeNtHelper(pInfo, pInfo->ProcessId);

        if (pInfo->NextEntryDelta == 0)
            break;

        // find address of the next structure
        pInfo = (PSYSTEM_PROCESS_INFORMATION)(((PUCHAR)pInfo) 
                                          + pInfo->NextEntryDelta);
    }

    // terminate the specified process
    if (!KillProcess(dwProcessId))
        return GetLastError();

    return ERROR_SUCCESS;
}

// a helper function that walks a process tree recursively
// on Windows 9x and terminates all processes in the tree
static BOOL KillProcessTreeWinHelper(
    IN DWORD dwProcessId
    )
{
#ifdef DEBUG_MONITOR
   _TCHAR buffer[MAX_CMD_LINE_LENGTH];
#endif
    HINSTANCE hKernel;
    HANDLE (WINAPI * pCreateToolhelp32Snapshot)(DWORD, DWORD);
    BOOL (WINAPI * pProcess32First)(HANDLE, PROCESSENTRY32 *);
    BOOL (WINAPI * pProcess32Next)(HANDLE, PROCESSENTRY32 *);

    // get KERNEL32.DLL handle
    hKernel = GetModuleHandleW(_T("kernel32.dll"));
        if(hKernel == NULL) {
#ifdef DEBUG_MONITOR
	        _stprintf(buffer, _T("KillProcessTreeNtHelper: wrong parameter"));
	        OutputDebugStringW(buffer);
#endif
            return FALSE;
        }

    // find necessary entrypoints in KERNEL32.DLL
    *(FARPROC *)&pCreateToolhelp32Snapshot =
        GetProcAddress(hKernel, "CreateToolhelp32Snapshot");
    *(FARPROC *)&pProcess32First =
        GetProcAddress(hKernel, "Process32First");
    *(FARPROC *)&pProcess32Next =
        GetProcAddress(hKernel, "Process32Next");

    if (pCreateToolhelp32Snapshot == NULL ||
        pProcess32First == NULL ||
        pProcess32Next == NULL)
        return ERROR_PROC_NOT_FOUND;

    HANDLE hSnapshot;
    PROCESSENTRY32 Entry;

    // create a snapshot of all processes
    hSnapshot = pCreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    if (hSnapshot == INVALID_HANDLE_VALUE)
        return GetLastError();

    Entry.dwSize = sizeof(Entry);
    if (!pProcess32First(hSnapshot, &Entry))
    {
        DWORD dwError = GetLastError();
        CloseHandle(hSnapshot);
        return dwError;
    }

    // terminate children first
    do
    {
        if (Entry.th32ParentProcessID == dwProcessId)
            KillProcessTreeWinHelper(Entry.th32ProcessID);

        Entry.dwSize = sizeof(Entry);
    }
    while (pProcess32Next(hSnapshot, &Entry));

    CloseHandle(hSnapshot);

    // terminate the specified process
    if (!KillProcess(dwProcessId))
        return GetLastError();

    return ERROR_SUCCESS;
}

