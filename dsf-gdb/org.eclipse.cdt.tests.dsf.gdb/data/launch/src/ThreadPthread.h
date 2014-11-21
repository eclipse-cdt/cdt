#ifndef THREADPTHREAD_H
#define THREADPTHREAD_H

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

/* Thread functions */

static int StartThread(ThreadFunc func, void *arg, ThreadHandle *handle) {
	return pthread_create(handle, NULL, func, arg) == 0;
}

static int JoinThread(ThreadHandle handle, ThreadRet *ret)
{
	return pthread_join(handle, ret) == 0;
}


/* Barrier functions */

static int ThreadBarrierInit(ThreadBarrier *barrier, unsigned int count)
{
	return pthread_barrier_init(barrier, NULL, count) == 0;
}

static int ThreadBarrierDestroy(ThreadBarrier *barrier)
{
	return pthread_barrier_destroy(barrier) == 0;
}

static int ThreadBarrierWait(ThreadBarrier *barrier)
{
	int ret;
	ret = pthread_barrier_wait(barrier);
	return ret == 0 || ret == PTHREAD_BARRIER_SERIAL_THREAD;
}

static int ThreadSetName(ThreadHandle thread, const char *name)
{
	return pthread_setname_np(thread, name) == 0;
}

static ThreadHandle ThreadSelf(void)
{
	return pthread_self();
}

#endif // THREADPTHREAD_H
