#include <stdio.h>
#include "simple.h"

int main() {
	printf("Hello, World!\n");
	printf("v%d.%d\n", SimpleProject_VERSION_MAJOR, SimpleProject_VERSION_MINOR);
	return 0;
}
