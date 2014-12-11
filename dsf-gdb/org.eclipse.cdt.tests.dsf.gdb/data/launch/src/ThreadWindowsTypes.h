#ifndef THREADWINDOWSTYPES_H
#define THREADWINDOWSTYPES_H

#include <windows.h>

/* Type definitions */

struct WindowsThreadBarrier;

typedef HANDLE ThreadHandle;
typedef struct WindowsThreadBarrier ThreadBarrier;
typedef HANDLE ThreadSemaphore;
typedef unsigned ThreadRet;
static ThreadRet THREAD_DEFAULT_RET = 0;
#define THREAD_CALL_CONV __stdcall

#endif // THREADWINDOWSTYPES_H
