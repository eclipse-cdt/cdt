#ifndef THREADWINDOWS_H
#define THREADWINDOWS_H

#include <process.h>
#include <assert.h>
#include <limits.h>

/* Thread functions */

static int StartThread(ThreadFunc func, void *arg, ThreadHandle *handle) {
	*handle = (HANDLE) _beginthreadex(NULL, 0, func, arg, 0, NULL);

	return *handle != 0;
}

static int JoinThread(ThreadHandle handle, ThreadRet *ret)
{
	DWORD result = WaitForSingleObject(handle, INFINITE);
	if (result != WAIT_OBJECT_0)
		return 0;

	BOOL result_b = GetExitCodeThread(handle, (DWORD *) ret);
	if (!result)
		return 0;

	return 1;
}


/* Barrier functions */

struct WindowsThreadBarrier
{
	LONG num_threads_to_wait;
	// InterlockedIncrement requires the LONG variable to be aligned on a 4-bytes boundary.
	LONG num_threads_waiting __attribute__ ((aligned (4)));
	HANDLE semaphore;
};

static int ThreadBarrierInit(ThreadBarrier *barrier, unsigned int count)
{
	const LONG max_threads = LONG_MAX;

	barrier->semaphore = CreateSemaphore(NULL, 0, max_threads, NULL);
	if (!barrier->semaphore) {
		return 0;
	}

	barrier->num_threads_to_wait = count;
	barrier->num_threads_waiting = 0;

	/* Make sure that the 4-bytes alignment directive works properly. */
	assert(((intptr_t) &barrier->num_threads_waiting & 0x3) == 0);

	return 1;
}

static int ThreadBarrierDestroy(ThreadBarrier *barrier)
{
	CloseHandle(barrier->semaphore);

	return 1;
}

static int ThreadBarrierWait(ThreadBarrier *barrier)
{
	LONG new_value = InterlockedIncrement(&barrier->num_threads_waiting);

	if (new_value == barrier->num_threads_to_wait) {
		// We are the last thread to hit the barrier, release everybody else (count - 1 threads).
		BOOL ret = ReleaseSemaphore(barrier->semaphore, barrier->num_threads_to_wait - 1, NULL);
		if (!ret)
			return 0;
	} else {
		// We are not the last thread to hit the barrier, wait to get released.
		DWORD ret = WaitForSingleObject(barrier->semaphore, INFINITE);
		if (ret != WAIT_OBJECT_0)
			return 0;
	}

	return 1;
}

static int ThreadSemaphoreInit(ThreadSemaphore *sem, unsigned int initial_count)
{
	*sem = CreateSemaphore(NULL, initial_count, LONG_MAX, NULL);
	return *sem != NULL;
}

static int ThreadSemaphoreTake(ThreadSemaphore *sem)
{
	DWORD result = WaitForSingleObject(*sem, INFINITE);

	return result == WAIT_OBJECT_0;
}

static int ThreadSemaphorePut(ThreadSemaphore *sem)
{
	return ReleaseSemaphore(*sem, 1, NULL) != 0;
}

static int ThreadSemaphoreDestroy(ThreadSemaphore *sem)
{
	return CloseHandle(*sem) != 0;
}

static int ThreadSetName(const char *name)
{
	/* Not supported> */
	return 0;
}

#endif // THREADWINDOWS_H
