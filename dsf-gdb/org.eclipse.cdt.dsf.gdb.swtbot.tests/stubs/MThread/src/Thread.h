#ifndef Thread_h
#define Thread_h

/*
 * This file provides a simple and incomplete platform compatibility
 * layer for thread-related operations. This should help avoiding
 * platform dependent code in tests.
 */


/* First, include type defintions necessary to define the public API. */
#ifndef __MINGW32__
#include "ThreadPthreadTypes.h"
#else //  __MINGW32__
#include "ThreadWindowsTypes.h"
#endif


/*
 * Type of callback functions for threads
 *
 * Note: if you don't care about the return value of the thread, you can
 * return THREAD_DEFAULT_RET instead, which is defined according to the
 * platform type.
 *
 * Otherwise, you'll need to have some #ifdefs in your test code in order
 * to return different value types depending on the platform.
 */
typedef ThreadRet (THREAD_CALL_CONV *ThreadFunc)(void *);


/* Public API */
static int StartThread(ThreadFunc func, void *arg, ThreadHandle *handle);
static int JoinThread(ThreadHandle handle, ThreadRet *ret);
static int ThreadBarrierInit(ThreadBarrier *barrier, unsigned int count);
static int ThreadBarrierDestroy(ThreadBarrier *barrier);
static int ThreadBarrierWait(ThreadBarrier *barrier);
static int ThreadSemaphoreInit(ThreadSemaphore *sem, unsigned int count);
static int ThreadSemaphoreTake(ThreadSemaphore *sem);
static int ThreadSemaphorePut(ThreadSemaphore *sem);
static int ThreadSemaphoreDestroy(ThreadSemaphore *sem);
static int ThreadSetName(const char *name);


/* Then, include the implemention of the API. */
#ifndef __MINGW32__
#include "ThreadPthread.h"
#else //  __MINGW32__
#include "ThreadWindows.h"
#endif

#endif // Thread_h
