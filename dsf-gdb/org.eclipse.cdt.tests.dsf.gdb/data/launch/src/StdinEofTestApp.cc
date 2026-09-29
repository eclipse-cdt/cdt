#include <cstdio>

// Reads standard input until EOF and returns the number of bytes read,
// so a test can check that the inferior received end of input.
int main() {
	int count = 0;
	while (getchar() != EOF) {
		count++;
	}
	return count;
}
