/*
 * This program should be compiled with -g3 -O0 to try to always obtain
 * the proper instruction sizes at the expected location.
 * The instruction sizes matter for tests that set tracepoints
 */

int bar() {
	return 0;
}

int foo(int a)
{
	int x = a;

	while(x < 5) {     // 2-byte on both 32bit and 64bit
		
		if (x != a) {  // 3-byte on both 32bit and 64bit
			
			++x;       // 4-byte on both 32bit and 64bit
			
			bar();     // 5-byte on both 32bit and 64bit
			
		}
		x++;
	}
	return 0;

}                      // 1-byte on both 32bit and 64bit

int gIntVar = 0;
bool gBoolVar = true;

void lastCall() {}

int main() {

	foo(0);

	lastCall(); // This method should be called last
	return 0;
}
