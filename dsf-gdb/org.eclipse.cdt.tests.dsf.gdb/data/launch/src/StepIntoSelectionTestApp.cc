#include "StepIntoSelection.h"
//
//
//
//    
//
//
//
// The first line of the below method must be at line 10
int foo() {
	int i = 0;  // The tests expect this to be at line 11
	return 1;
}
//
//
//
//
//
int bar(int i) {
	int b = 0;  // The tests expect this to be at line 20
	return i + b;
}
//
//
//
//
//
//
int add(int a) {
	return a + 1;  // The tests expect this to be at line 30
}
//
//
int add() {
	return 1;   // The tests expect this to be at line 35
}

int recursiveTest(int a) {
	if (a == 1) return a;
	
	return a + recursiveTest(--a);  // The test expects this line to be exactly 2 lines below the first line of the method
}

int sameLineTest() {
	foo();
	return 0;
}

int sameLineBreakpointTest() {
	bar(foo());
	return 0;
}

int doubleMethodTest() {
	int a = 0;
	bar(foo());  // The test expects this line to be one line below the star of the method
	return 0;
}

int laterLineTest() {
	int i = 0;
	i++;
	i++;
	foo();  // The test expects this line to be exactly 3 lines below the first line of the method
	i++;
	i++;
	return 0;
}

int laterLineNotHitTest() {
	int i = 0;
	if (i==100) {  // Won't hit
		foo();  // The test expects this line to be exactly 2 lines below the first line of the method
	}
	i++;
	i++;
	return 0;
}

int laterLineDifferentFileTest() {
    int b = 0;
    value();  // Must be one line below start of the method
              // value() is from .h header file
}

int differentFileTest() {

	return 0;
}

int methodWithDiffArgsNumberTest() {
	return add() + add(2);
}

int main() {
	sameLineTest();
	laterLineTest();
	laterLineNotHitTest();
	doubleMethodTest();
	recursiveTest(8);
	laterLineDifferentFileTest();
	differentFileTest();
	methodWithDiffArgsNumberTest();
	return 0;
}
