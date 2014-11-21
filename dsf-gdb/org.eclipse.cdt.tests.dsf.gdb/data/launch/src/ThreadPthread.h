#ifndef THREADPTHREAD_H
#define THREADPTHREAD_H

#include <pthread.h>
#include <sys/prctl.h>

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
	int ret = pthread_barrier_wait(barrier);
	return ret == 0 || ret == PTHREAD_BARRIER_SERIAL_THREAD;
}

static int ThreadSemaphoreInit(ThreadSemaphore *sem, unsigned int count)
{
	return sem_init(sem, 0, count) == 0;
}

static int ThreadSemaphoreTake(ThreadSemaphore *sem)
{
	return sem_wait(sem) == 0;
}

static int ThreadSemaphorePut(ThreadSemaphore *sem)
{
	return sem_post(sem) == 0;
}

static int ThreadSemaphoreDestroy(ThreadSemaphore *sem)
{
	return sem_destroy(sem) == 0;
}

static int ThreadSetName(const char *name)
{
	return prctl(PR_SET_NAME, name, 0, 0, 0) == 0;
}

#endif // THREADPTHREAD_H
