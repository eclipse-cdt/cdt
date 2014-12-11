#ifndef THREADWINDOWS_H
#define THREADWINDOWS_H

#include <assert.h>

static void *aligned_malloc(const size_t size, const size_t alignment);
static void aligned_free(void *ptr);

/* Thread functions */

static int StartThread(ThreadFunc func, void *arg, ThreadHandle *handle) {
	uintptr_t th;

	*handle = _beginthreadex(NULL, 0, func, arg, 0, NULL);
	
	return *handle != 0;
}

static int JoinThread(ThreadHandle handle, ThreadRet *ret)
{
	return 0;
}


/* Barrier functions */

struct WindowsThreadBarrier 
{
	LONG num_threads_to_wait;
	LONG *num_threads_waiting;
	HANDLE semaphore;
};

static int ThreadBarrierInit(ThreadBarrier *barrier, unsigned int count)
{
	const LONG max_threads = LONG_MAX;

	// InterlockedIncrement requires the LONG variable to be aligned on a 32-bits boundary.
	barrier->num_threads_waiting = (LONG *) aligned_malloc(sizeof(*(barrier->num_threads_waiting)), 4);
	if (!barrier->num_threads_waiting) {
		goto malloc_failed;
	}
		
	barrier->semaphore = CreateSemaphore(NULL, 0, max_threads, NULL);
	if (!barrier->semaphore) {
		goto semaphore_failed;
	}

	barrier->num_threads_to_wait = count;
	*barrier->num_threads_waiting = 0;

	return 1;

semaphore_failed:
	aligned_free(barrier->num_threads_waiting);

malloc_failed:

	return 0;
}

static int ThreadBarrierDestroy(ThreadBarrier *barrier)
{
	CloseHandle(barrier->semaphore);
	aligned_free(barrier->num_threads_waiting);

	return 1;
}

static int ThreadBarrierWait(ThreadBarrier *barrier)
{
	LONG new_value;
	DWORD ret;
	
	new_value = InterlockedIncrement(barrier->num_threads_waiting);
	
	if (new_value == barrier->num_threads_to_wait) {
		// We are the last thread to hit the barrier, release everybody else (count - 1 threads).
		ReleaseSemaphore(barrier->semaphore, barrier->num_threads_to_wait - 1, NULL);
	} else {
		// We are not the last thread to hit the barrier, wait to get released.
		WaitForSingleObject(barrier->semaphore, INFINITE);
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
	WaitForSingleObject(*sem, INFINITE);
	return 0;
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

// Implementation of an aligned malloc, because _aligned_malloc is somehow not available on MinGW.
static void *aligned_malloc(const size_t size, const size_t alignment)
{
	char *ptr, *orig_ptr;
	char **orig_ptr_storage;
	
	// Make sure alignment is a power of two.
	if ((alignment - 1) & alignment) {
		return NULL;
	}
	
	// Make sure alignment is bigger than sizeof(void*), so we have
	// room to store the original pointer.
	if (alignment < sizeof(orig_ptr)) {
		return NULL;
	}
	
	// Allocate enough space to be able to align.
	orig_ptr = (char *) malloc(size + alignment);
	
	if (!orig_ptr) {
		return NULL;
	}
	
	// Align the pointer.
	ptr = orig_ptr + alignment;
	ptr = (char*) ((uintptr_t) ptr & ~(alignment - 1));
	
	// Compute original pointer storage location.
	orig_ptr_storage = (char **) (ptr - sizeof(orig_ptr));
	
	// Check that the original pointer storage is in the malloced region.
	assert( (char *) orig_ptr_storage >= orig_ptr );
	
	// Check that the value we return is indeed aligned.
	assert( ((uintptr_t) ptr & (alignment - 1)) == 0 );
	
	// Check that the buffer we return is entirely within the malloced region.
	assert( ptr >= orig_ptr );
	assert( (ptr + size) <= (orig_ptr + size + alignment) );

	// Save the original pointer.
	*orig_ptr_storage = orig_ptr;

	return ptr;
}

static void aligned_free(void *ptr) {
	void **orig_ptr_storage;
	void *orig_ptr;
	
	if (!ptr) {
		return;
	}
	
	// Read original pointer.
	orig_ptr_storage = (void **) ((char *) ptr - sizeof(orig_ptr));
	orig_ptr = *orig_ptr_storage;
	
	free(orig_ptr);
}

#endif // THREADWINDOWS_H
