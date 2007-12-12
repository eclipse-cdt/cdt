//============================================================================
// Name        : HelloCDT.cpp
// Author      : Francois Chouinard
// Version     :
// Copyright   : Ericsson Research Canada
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <pthread.h>
using namespace std;

int   i = 0;
char  c = 'a';
char* s = "abcdef";

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
void print_values(char* ptr)
{
	char* header = ptr;
    printf("%s\n", header);
    printf("&i = 0x%x, i = %d\n",   (int) &i, ++i);
    printf("&c = 0x%x, c = %c\n",   (int) &c, c++);
    printf("&s = 0x%x, s = %s\n\n", (int) &s, s);
}

void *thread_print(void *ptr)
{
	char* cptr = (char *) ptr;
    print_values(cptr);
    return NULL;
}

void test_threads(void)
{
	pthread_t thread1, thread2;
	char *message1 = "Thread 2";
	char *message2 = "Thread 3";
	int iret1, iret2;

	// Create independent threads each of which will execute function
	iret1 = pthread_create( &thread1, NULL, thread_print, (void*) message1);
	iret2 = pthread_create( &thread2, NULL, thread_print, (void*) message2);

	/* Wait till threads are complete before main continues. Unless we  */
	/* wait we run the risk of executing an exit which will terminate   */
	/* the process and all threads before the threads have completed.   */

	pthread_join(thread1, NULL);
	printf("Thread 1 returns: %d\n", iret1);

	pthread_join(thread2, NULL);
	printf("Thread 2 returns: %d\n", iret2);
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
void test_stack2(int* k)
{
	bool j = true;
	int* l = k;
	*k += 10;

	// Modifying memory updates the monitors
	// but not the Variables view :-(
	printf("%d\n", *k);
}

void test_stack(void)
{
	// Add a memory monitor for 'j'
	int j = 5;
	//  Step into
	test_stack2(&j);
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
void countdown(int m)
{
	int x = m;
	if (x == 0) {
		printf("We have a lift-off!\n");
		return;
	}
	printf("%d, ", x);
	countdown(--x);
}

void test_recursion(void)
{
	// Add a monitor on l
	int l = 3;
	countdown(l);
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

void test_overlap2(void)
{
	int a = 2;
}

void test_overlap3(void)
{
	int b = 3;
}

void test_overlap(void)
{
	test_overlap2();
	test_overlap3();
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
int main()
{
	// If we don't step this instruction then the initial content of cptr will
	// be cached and the Variables view will display the wrong value.
	char* cptr = "Thread 1";

	// char* interpreted as C-string in Variables view
	char* cp = (char*) malloc(1);
	*cp = 'a';
	
	test_stack();
	test_threads();
	test_recursion();
	test_overlap();

	return 0;
}
