
#include <stdio.h>
#include "Sleep.h"


void PrintHello()
{
   printf("Hello World!\n");

   SLEEP(1);
    
}

void PrintHi()
{
   printf("Hi everybody!\n");

   SLEEP(1);
    
}

void PrintBonjour()
{
   printf("Bonjour!\n");   

   SLEEP(1);
    
}

int main(int argc, char *argv[])
{
	printf("In main\n");
	  
    SLEEP(1);
    
    SLEEP(1);
	
	PrintHello();
	
	PrintHi();
	
	PrintBonjour();
	
	return 0;
}
