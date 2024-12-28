#include "StepIntoSelection.h"

int foo()
{	int i = 0; // FOO_LINE
	return 1;
}

int bar(int i)
{	int b = 0; // BAR_LINE
	return i + b;
}

int add(int a)
{	return a + 1; // ADD_WITH_ARG_LINE
}

int add()
{	return 1; // ADD_NO_ARG_LINE
}

int recursiveTest(int a)
{	if (a == 1) return a;

	return a + recursiveTest(--a);  // RECURSIVE_LINE
}

int sameLineTest()
{	foo();
	return 0;
}

int sameLineBreakpointTest()
{	bar(foo());
	return 0;
}

int doubleMethodTest()
{	int a = 0;
	bar(foo());  // DOUBLE_METHOD_LINE
	return 0;
}

int laterLineTest()
{	int i = 0; // LATER_LINE_ENTRY_LINE
	i++;
	i++;
	foo();  // LATER_LINE_LINE
	i++;
	i++;
	return 0;
}

int laterLineNotHitTest()
{	int i = 0;
	if (i==100) {  // Won't hit
		foo();  // LATER_LINE_NOT_HIT_LINE
	}
	i++;
	i++;
	return 0;
}

int laterLineDifferentFileTest()
{   int b = 0;
    value();  // LATER_LINE_DIFFERENT_FILE_LINE
    return 0;
}

int differentFileTest()
{	return 0;
}

int methodWithDiffArgsNumberTest()
{	return add() + add(2);
}

int main()
{	sameLineTest();
	laterLineTest();
	laterLineNotHitTest();
	doubleMethodTest();
	recursiveTest(8);
	laterLineDifferentFileTest();
	differentFileTest();
	methodWithDiffArgsNumberTest();
	return 0;
}
