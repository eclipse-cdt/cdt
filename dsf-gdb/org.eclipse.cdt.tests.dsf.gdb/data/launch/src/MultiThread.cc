#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include "Sleep.h"
#include "Thread.h"

static const int NUM_THREADS = 5;

struct PrintHelloArgs {
	int thread_id;
	ThreadBarrier *barrier_start;
	ThreadBarrier *barrier_finish;
	ThreadSemaphore *sem_start;
	const char *name;
};

static ThreadRet THREAD_CALL_CONV PrintHello(void *void_arg)
{
	struct PrintHelloArgs *args = (struct PrintHelloArgs *) void_arg;
	int thread_id = args->thread_id;
	ThreadBarrier *barrier_start = args->barrier_start;
	ThreadBarrier *barrier_finish = args->barrier_finish;
	ThreadSemaphore *sem_start = args->sem_start;
	const char *name = args->name;

	/* Indicate to main thread that the thread is started. */
	ThreadSemaphorePut(sem_start);

	printf("Hello World! It's me, thread #%d!\n", thread_id);

	ThreadSetName(name);

	/* Make sure that all threads are started before the breakpoint in main hits. */
	ThreadBarrierWait(barrier_start);

	printf("Thread %d in the middle\n", thread_id);

	/* Make sure that the thread does not finish before the breakpoint in main hits. */
	ThreadBarrierWait(barrier_finish);

	printf("Goodbye World! From thread #%d\n", thread_id);

	return THREAD_DEFAULT_RET;
}

int main(int argc, char *argv[])
{
	ThreadHandle threads[NUM_THREADS];
	struct PrintHelloArgs args[NUM_THREADS];
	const char *thread_names[NUM_THREADS] = {"monday", "tuesday", "wednesday", "thursday", "friday"};

	/* Used to make rendez-vous points between all threads. */
	ThreadBarrier barrier_start;
	ThreadBarrier barrier_finish;

	/* Used to tell when a thread is started for real. */
	ThreadSemaphore sem_start;

	/* + 1 for main thread */
	ThreadBarrierInit(&barrier_start, NUM_THREADS + 1);
	ThreadBarrierInit(&barrier_finish, NUM_THREADS + 1);

	ThreadSemaphoreInit(&sem_start, 0);

	for (int t = 0; t < NUM_THREADS; t++)
	{
		printf("In main: creating thread #%d\n", t);

		args[t].thread_id = t;
		args[t].barrier_start = &barrier_start;
		args[t].barrier_finish = &barrier_finish;
		args[t].sem_start = &sem_start;
		args[t].name = thread_names[t];

		int ret = StartThread(PrintHello, &args[t], &threads[t]); /* Breakpoint LINE_MAIN_BEFORE_THREAD_START */

		if (!ret)
		{
				printf("Error: StartThread failed.\n");
				exit(-1);
		}

		/* Wait until the thread has really started. */
		ThreadSemaphoreTake(&sem_start);

		printf("In main: thread #%d has started\n", t); /* Breakpoint LINE_MAIN_AFTER_THREAD_START */
	}

	/* Let the threads continue to the 'critical' section> */
	ThreadBarrierWait(&barrier_start);

	printf("In main thread, all threads created.\n"); /* Breakpoint LINE_MAIN_ALL_THREADS_STARTED */

	SLEEP(30);

	/* Unlock the threads and let the program finish. */
	ThreadBarrierWait(&barrier_finish);

	for (int t = 0; t < NUM_THREADS; t++)
	{
		printf("In main, joining thread #%d\n", t);
		JoinThread(threads[t], NULL);
	}

	ThreadBarrierDestroy(&barrier_start);
	ThreadBarrierDestroy(&barrier_finish);
	ThreadSemaphoreDestroy(&sem_start);

	return 0;
}
