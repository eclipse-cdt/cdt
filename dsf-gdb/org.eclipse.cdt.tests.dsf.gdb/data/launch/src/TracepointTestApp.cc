/*
 * This program should be compiled with -g3 -O0 to try to always obtain
 * the proper instruction sizes at the expected location.
 * The instruction sizes matter for tests that set tracepoints
 *
 * There is a test, GDBRemoteTracepointsTest.checkInstructionsAreExpectedLength(), which
 * attempts to make sure the instructions are the size the comments say they are. Compiler
 * changes, whether version or platform, can affect these results.
 *
 * To experiment at setting the instructions to the correct length, a command like this
 * can be used:
     g++ -g3 -c -O0 -pthread -o TracepointTestApp.o TracepointTestApp.cc && gdb -batch -ex 'file TracepointTestApp.o' -ex 'disassemble /rs foo'
 * On the lines following the N_BYTE comment, the instruction should be N bytes long. 
 * for example, this is the output I had when writing (with the comment line munged as to not affect resolveLineTagLocations):
 
17			if (x != a) {  // *3*_BYTE
   0x0000000000000022 <+23>:	8b 45 fc	mov    -0x4(%rbp),%eax
   0x0000000000000025 <+26>:	3b 45 ec	cmp    -0x14(%rbp),%eax
   0x0000000000000028 <+29>:	74 0b	je     0x35 <foo(int)+42>

 * As you can see, the "mov" instruction above is 3-bytes (8b 45 fc) long.
 */

int bar() {
	return 0;
}

int foo(int a)
{
	int x = a;

	while(x < 5) {
		
		if (x != a) {  // 3_BYTE  // IF_X_NE_A
			
			++x;       // 4_BYTE
			
			bar();     // 5_BYTE
			
			goto end;   // 2_BYTE
		}
		x++;           // INCR_X
	}
end:
	return 0;

}                      // 1_BYTE

int gIntVar = 0;
bool gBoolVar = true;

void lastCall() {}

int main() {

	foo(0);

	lastCall(); // This method should be called last
	return 0;
}
