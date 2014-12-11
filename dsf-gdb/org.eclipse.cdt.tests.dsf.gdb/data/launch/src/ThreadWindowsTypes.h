#ifndef THREADWINDOWSTYPES_H
#define THREADWINDOWSTYPES_H

#include <stdlib.h>
#include <x86intrin.h>
#include <malloc.h>
#include <windows.h>
#include <process.h>
#include <limits.h>


/* Type definitions */

struct WindowsThreadBarrier;

typedef uintptr_t ThreadHandle;
typedef struct WindowsThreadBarrier ThreadBarrier;
typedef HANDLE ThreadSemaphore;
typedef unsigned ThreadRet;
static ThreadRet THREAD_DEFAULT_RET = 0;
#define THREADFUNC __stdcall

#endif // THREADWINDOWSTYPES_H
