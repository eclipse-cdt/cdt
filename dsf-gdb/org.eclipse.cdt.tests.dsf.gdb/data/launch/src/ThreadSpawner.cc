// Starts and joins threads in a loop, so that GDB sees new threads all the
// time while the program runs.
#include <stddef.h>
#include "Thread.h"

static ThreadRet THREAD_CALL_CONV DoNothing(void *arg)
{
	return THREAD_DEFAULT_RET;
}

int main(int argc, char *argv[])
{
	for (;;) {
		ThreadHandle thread;
		ThreadRet ret;
		if (StartThread(DoNothing, NULL, &thread)) {
			JoinThread(thread, &ret);
		}
	}
	return 0;
}
