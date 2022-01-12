#include <stdio.h>
#include "Class1.h"
#include "Class2.h"

int var;

//function
void foo2()
{
	printf("foo2\n");
}

int main()
{	
	namespace1::Class1 element;
	printf("%d\n", element.class1x);
	foo2();
	Class2 class2;
	class2.foo();
	var = 0;
	return 0;
}
