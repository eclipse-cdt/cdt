#include <stdio.h>

int testMemoryChanges() {
	int i = 8;

	return i;
}

int main() {
    printf("Running ConsoleSyncTestApp\n");

    testMemoryChanges();
    
    return 0;
}
