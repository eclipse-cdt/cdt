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
	const char *name;
};

ThreadRet PrintHello(void *void_arg)
{
	struct PrintHelloArgs *args = (struct PrintHelloArgs *) void_arg;
	int thread_id = args->thread_id;
	ThreadBarrier *barrier_start = args->barrier_start;
	ThreadBarrier *barrier_finish = args->barrier_finish;
	const char *name = args->name;

	printf("Hello World! It's me, thread #%d!\n", thread_id);

	pthread_setname_np(pthread_self(), name);

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
	ThreadBarrier barrier_start;
	ThreadBarrier barrier_finish;

	/* + 1 for main thread */
	ThreadBarrierInit(&barrier_start, NUM_THREADS + 1);
	ThreadBarrierInit(&barrier_finish, NUM_THREADS + 1);

	for (int t = 0; t < NUM_THREADS; t++)
	{
		int ret;
		printf("In main: creating thread #%d\n", t);

		args[t].thread_id = t;
		args[t].barrier_start = &barrier_start;
		args[t].barrier_finish = &barrier_finish;
		args[t].name = thread_names[t];

		ret = StartThread(PrintHello, &args[t], &threads[t]); /* Breakpoint LINE_MAIN_BEFORE_THREAD_START */

		if (!ret) /* Breakpoint LINE_MAIN_AFTER_THREAD_START */
		{
				printf("ERROR; _beginthreadex() failed. errno = %d\n", errno);
				exit(-1);
		}
	}

	/* Make sure that all threads are started before the breakpoint in main hits. */
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

	return 0;
}
