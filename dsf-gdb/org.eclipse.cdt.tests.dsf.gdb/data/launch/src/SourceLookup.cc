#include <stdio.h>
volatile int i;

int func(void) {
	i = 0;
	i += 1;
	i += 2;
	return i;
}


int main() {
	int calc = func();

	printf("Calc: %d\n", calc);
	return 0;
}
