#include <stdio.h>

void func1()
{
	int x,y,z;
	x=1;
	y=2;
	z=3;
	printf("Hello world\n");
	printf("Hello world\n");
	printf("Hello world\n");
}

int main()
{
	int a,b,c;
	a=b=c=10;
	a=12;
	b=124;
	c=1;
	func1();
	for (a=0;a<100;a++) {
		c++;
	}
	return(1);
}
