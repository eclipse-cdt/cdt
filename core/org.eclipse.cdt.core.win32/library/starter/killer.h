/*******************************************************************************
 * Copyright (c) 2002 - 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *
 *  StdAfx.h
 *
 *  This is a header file for helper function for the process killing
 *  Implementation based on the article "Terminating Windows Processes"
 *  see http://www.alexfedotov.com/articles/killproc.asp and
 *  http://www.alexfedotov.com/samples/threads.asp
***********************************************************************/

#if !defined(_KILLER_H_INCLUDED_)
#define _KILLER_H_INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <Ntsecapi.h>


typedef LONG KPRIORITY; // From ntddk.h

//
// Process Virtual Memory Counters
//  NtQueryInformationProcess using ProcessVmCounters
// From ntddk.h

typedef struct _VM_COUNTERS {
    SIZE_T PeakVirtualSize;
    SIZE_T VirtualSize;
    ULONG PageFaultCount;
    SIZE_T PeakWorkingSetSize;
    SIZE_T WorkingSetSize;
    SIZE_T QuotaPeakPagedPoolUsage;
    SIZE_T QuotaPagedPoolUsage;
    SIZE_T QuotaPeakNonPagedPoolUsage;
    SIZE_T QuotaNonPagedPoolUsage;
    SIZE_T PagefileUsage;
    SIZE_T PeakPagefileUsage;
} VM_COUNTERS;
typedef VM_COUNTERS *PVM_COUNTERS;

//
// ClientId
//

typedef struct _CLIENT_ID {
    HANDLE UniqueProcess;
    HANDLE UniqueThread;
} CLIENT_ID;
typedef CLIENT_ID *PCLIENT_ID;

typedef struct _SYSTEM_THREAD_INFORMATION {
    LARGE_INTEGER   KernelTime;             // time spent in kernel mode
    LARGE_INTEGER   UserTime;               // time spent in user mode
    LARGE_INTEGER   CreateTime;             // thread creation time
    ULONG           WaitTime;               // wait time
    PVOID           StartAddress;           // start address
    CLIENT_ID       ClientId;               // thread and process IDs
    KPRIORITY       Priority;               // dynamic priority
    KPRIORITY       BasePriority;           // base priority
    ULONG           ContextSwitchCount;     // number of context switches
    LONG            State;                  // current state
    LONG            WaitReason;             // wait reason
} SYSTEM_THREAD_INFORMATION, * PSYSTEM_THREAD_INFORMATION;

typedef struct _SYSTEM_PROCESS_INFORMATION {
    ULONG           NextEntryDelta;         // offset to the next entry
    ULONG           ThreadCount;            // number of threads
    ULONG           Reserved1[6];           // reserved
    LARGE_INTEGER   CreateTime;             // process creation time
    LARGE_INTEGER   UserTime;               // time spent in user mode
    LARGE_INTEGER   KernelTime;             // time spent in kernel mode
    UNICODE_STRING  ProcessName;            // process name
    KPRIORITY       BasePriority;           // base process priority
    ULONG           ProcessId;              // process identifier
    ULONG           InheritedFromProcessId; // parent process identifier
    ULONG           HandleCount;            // number of handles
    ULONG           Reserved2[2];           // reserved
    VM_COUNTERS     VmCounters;             // virtual memory counters
#if _WIN32_WINNT >= 0x500
    IO_COUNTERS     IoCounters;             // i/o counters
#endif
    SYSTEM_THREAD_INFORMATION Threads[1];   // threads
} SYSTEM_PROCESS_INFORMATION, * PSYSTEM_PROCESS_INFORMATION;


static BOOL KillProcessTreeNtHelper(
    IN PSYSTEM_PROCESS_INFORMATION pInfo,
    IN DWORD dwProcessId);

static BOOL KillProcessTreeWinHelper(
    IN DWORD dwProcessId);

// From ntstatus.h
// MessageId: STATUS_INFO_LENGTH_MISMATCH
//
// MessageText:
//
//  The specified information record length does not match the length required for the specified information class.
//
#define STATUS_INFO_LENGTH_MISMATCH      ((NTSTATUS)0xC0000004L)

// From ntstatus.h
// Generic test for success on any status value (non-negative numbers
// indicate success).
//

#define NT_SUCCESS(Status) ((NTSTATUS)(Status) >= 0)




#endif // _KILLER_H_INCLUDED_