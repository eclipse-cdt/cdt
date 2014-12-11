#include "Thread.h"
#include <stdio.h>
#include "Sleep.h"

// Set a breakpoint here so that both threads stop.
void firstBreakpoint(long id)
{
	printf("First breakpoint method from thread %ld\n", id);
}

struct PrintHelloArgs {
	int thread_id;
};

static ThreadRet THREAD_CALL_CONV PrintHello(void *void_arg) {
	struct PrintHelloArgs *args = (struct PrintHelloArgs *) void_arg;
	int thread_id = args->thread_id;

	firstBreakpoint(thread_id);  // Stop a first time

	SLEEP(1);  // Keep state running a little

	firstBreakpoint(thread_id);  // Stop a second time

	SLEEP(3); // Resuming past this will give us a running thread

	return THREAD_DEFAULT_RET;
}

int main(int argc, char *argv[])
{
	ThreadHandle thread;
	struct PrintHelloArgs args;
	args.thread_id = 1; // Break at main will stop here: we have a single thread stopped

	SLEEP(1);  // When resuming past here, we have a single thread running

	int ret = StartThread(PrintHello, &args, &thread);
	if (!ret) {
		printf("Error: failed to start thread.\n");
		return 1;
	}

	firstBreakpoint(0);

	SLEEP(1);  // Resuming past this will make this thread run, while we stop the second thread

	SLEEP(3);  // Resuming past this will make this thread run, while we also run the second thread

	return 0;
}
