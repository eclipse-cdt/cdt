#ifdef __MINGW32__
 #include <process.h>	// MinGW has no POSIX support; use MSVC runtime
#else
 #include <pthread.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include "Sleep.h"

#ifdef __MINGW32__
typedef unsigned int TID;
#else
typedef pthread_t TID;
#endif

// Set a breakpoint here so that both threads stop.
void firstBreakpoint(int id)
{
  printf("First breakpoint method from thread %d\n", id);
}
  

#ifdef __MINGW32__
unsigned int __stdcall PrintHello(void *threadid)
#else
void *PrintHello(void *threadId)
#endif
{
   int tId = (int)threadId;
   firstBreakpoint(tId);  // Stop a first time

   SLEEP(1);  // Keep state running a little
   
   firstBreakpoint(tId);  // Stop a second time
   
   SLEEP(3); // Resuming past this will give us a running thread

#ifdef __MINGW32__
   return 0;
#else
   pthread_exit(NULL);
#endif
}


int main(int argc, char *argv[])
{
	TID thread;
	int tId = 1;   // Break at main will stop here: we have a single thread stopped

	SLEEP(1);  // When resuming past here, we have a single thread running

#ifdef __MINGW32__
	uintptr_t rc = _beginthreadex(NULL, 0, PrintHello, (void*)tId, 0, &thread);
    if (rc == 0)
    {
		printf("ERROR; _beginthreadex() failed. errno = %d\n", errno);
		exit(-1);
    }
#else
    int rc = pthread_create(&thread, NULL, PrintHello, (void *)tId);
    if (rc)
    {
		printf("ERROR; return code from pthread_create() is %d\n", rc);
		exit(-1);
    }
#endif
    
    firstBreakpoint(0);
    
	SLEEP(1);  // Resuming past this will make this thread run, while we stop the second thread

	SLEEP(3);  // Resuming past this will make this thread run, while we also run the second thread

	return 0;
}
