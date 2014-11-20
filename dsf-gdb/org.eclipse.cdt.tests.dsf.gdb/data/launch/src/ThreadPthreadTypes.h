#ifndef THREADPTHREADTYPES_H
#define THREADPTHREADTYPES_H

#include <pthread.h>

/* Type definitions */

typedef pthread_t ThreadHandle;
typedef pthread_barrier_t ThreadBarrier;
typedef void *ThreadRet;
static void *THREAD_DEFAULT_RET = NULL;

#endif // THREADPTHREADTYPES_H
