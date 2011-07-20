#ifdef __MINGW32__
  #include <process.h>  // MinGW has no POSIX support; use MSVC runtime
#else
  #include <pthread.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include "Sleep.h"
#define NUM_THREADS    5

int gIntVar = 543;
double gDoubleVar = 543.543;
char gCharVar = 'g';
bool gBoolVar = false;

int gIntArray[2] = {987, 654};
double gDoubleArray[2] = {987.654, 654.321};
char gCharArray[2] = {'g', 'd'};
bool gBoolArray[2] = {true, false};

int *gIntPtr = &gIntVar;
double *gDoublePtr = &gDoubleVar;
char *gCharPtr = &gCharVar;
bool *gBoolPtr = &gBoolVar;

int *gIntPtr2 = (int*)0x8;
double *gDoublePtr2 = (double*)0x5432;
char *gCharPtr2 = (char*)0x4321;
bool *gBoolPtr2 = (bool*)0x12ABCDEF;

class bar {
public: 
	int d;
private:
	int e[2];
};

class bar2 {
public: 
	int f;
private:
	int g[2];
};

class foo: public bar, bar2 {
public:
	int a[2];
	bar b;
private:
	int c;
};

struct Z {
public:
	int x;
	int y;
};
struct childStruct {
public:
	Z z;	
};

#ifdef __MINGW32__
typedef unsigned int TID;
#else
typedef pthread_t TID;
#endif


#ifdef __MINGW32__
unsigned int __stdcall testTracepoints(void *threadid)
#else
void *testTracepoints(void *threadid)
#endif
{
    int tid = (int)threadid;
    printf("Hello World! It's me, thread #%d!\n", tid);

    int lIntVar = 12345;
    double lDoubleVar = 12345.12345;
    char lCharVar = 'm';
    bool lBoolVar = false;

    int lIntArray[2] = {6789, 12345};
    double lDoubleArray[2] = {456.789, 12345.12345};
    char lCharArray[2] = {'i', 'm'};
    bool lBoolArray[2] = {true, false};
    
    int *lIntPtr = &lIntVar;
    double *lDoublePtr = &lDoubleVar;
    char *lCharPtr = &lCharVar;
    bool *lBoolPtr = &gBoolVar;

    int *lIntPtr2 = (int*)0x1;
    double *lDoublePtr2 = (double*)0x2345;
    char *lCharPtr2 = (char*)0x1234;
    bool *lBoolPtr2 = (bool*)0x123ABCDE;

	int counter = 0;
	// Small loop
    for (counter=0; counter<10;) {
        counter++;
    }

	printf("counter is now #%d!\n", counter);

	// Large loop
    for (; counter<10000;) {
        counter++;
    }

    SLEEP(2); // keep this thread around for a bit; the tests will check for its existence while the main thread is stopped at a breakpoint

#ifdef __MINGW32__
    return 0;
#else
    pthread_exit(NULL);
#endif
}

int main()
{
	TID threads[NUM_THREADS];
	int t;
	for(t=0; t < NUM_THREADS; t++)
	{
		printf("In main: creating thread %d\n", t);
#ifdef __MINGW32__
		{
			uintptr_t rc = _beginthreadex(NULL, 0, testTracepoints, (void*)t, 0, &threads[t]);
			SLEEP(1); // debugger should for sure receive thread creation event after stepping over this sleep; not guaranteed to happen simply stepping over the thread creation call
			if (rc == 0)
			{
				printf("ERROR; _beginthreadex() failed. errno = %d\n", errno);
				exit(-1);
			}
		}
#else
		{
			int rc = pthread_create(&threads[t], NULL, testTracepoints, (void *)t);
			SLEEP(1); // debugger should for sure receive thread creation event after stepping over this sleep; not guaranteed to happen simply stepping over the thread creation call
			if (rc)
			{
				printf("ERROR; return code from pthread_create() is %d\n", rc);
				exit(-1);
			}
		}
#endif
	}

	SLEEP(2); // keep this thread around for a bit
	return 0;
}

