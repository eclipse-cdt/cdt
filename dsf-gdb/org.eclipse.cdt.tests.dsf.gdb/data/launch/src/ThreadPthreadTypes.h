#ifndef THREADPTHREADTYPES_H
#define THREADPTHREADTYPES_H

#include <pthread.h>
#include <semaphore.h>

/* Type definitions */

typedef pthread_t ThreadHandle;
typedef pthread_barrier_t ThreadBarrier;
typedef sem_t ThreadSemaphore;
typedef void *ThreadRet;
static void *THREAD_DEFAULT_RET = NULL;
#define THREAD_CALL_CONV

#endif // THREADPTHREADTYPES_H
