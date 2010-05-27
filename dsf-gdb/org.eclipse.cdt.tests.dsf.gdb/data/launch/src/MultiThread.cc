#ifdef __MINGW32__
 #include <process.h>	// MinGW has no POSIX support; use MSVC runtime
#else
 #include <pthread.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include "Sleep.h"
#define NUM_THREADS	5

#ifdef __MINGW32__
typedef unsigned int TID;
#else
typedef pthread_t TID;
#endif


#ifdef __MINGW32__
unsigned int __stdcall PrintHello(void *threadid)
#else
void *PrintHello(void *threadid)
#endif
{
   int tid = (int)threadid;
   printf("Hello World! It's me, thread #%d!\n", tid);
   SLEEP(2); // keep this thread around for a bit; the tests will check for its existence while the main thread is stopped at a breakpoint

#ifdef __MINGW32__
   return 0;
#else
   pthread_exit(NULL);
#endif
}

int main(int argc, char *argv[])
{
	TID threads[NUM_THREADS];
	int t;
	for(t=0; t < NUM_THREADS; t++)
	{
		printf("In main: creating thread %d\n", t);
#ifdef __MINGW32__
		{
			uintptr_t rc = _beginthreadex(NULL, 0, PrintHello, (void*)t, 0, &threads[t]);
	    	SLEEP(1); // debugger should for sure receive thread creation event after stepping over this sleep; not guaranteed to happen simply stepping over the thread creation call   
		    if (rc == 0)
		    {
				printf("ERROR; _beginthreadex() failed. errno = %d\n", errno);
				exit(-1);
		    }
		}  
#else
		{
	        int rc = pthread_create(&threads[t], NULL, PrintHello, (void *)t);
	    	SLEEP(1); // debugger should for sure receive thread creation event after stepping over this sleep; not guaranteed to happen simply stepping over the thread creation call	        
		    if (rc)
		    {
				printf("ERROR; return code from pthread_create() is %d\n", rc);
				exit(-1);
		    }
		}
#endif
	}
	
	return 0;
}
