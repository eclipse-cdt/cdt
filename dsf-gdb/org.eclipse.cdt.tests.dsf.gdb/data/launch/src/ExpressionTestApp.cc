#include <stdio.h>

int gIntVar = 543;
double gDoubleVar = 543.543;
char gCharVar = 'g';
bool gBoolVar = false;

int gIntArray[2] = {987, 654};
double gDoubleArray[2] = {987.654, 654.321};
char gCharArray[2] = {'g', 'd'};
bool gBoolArray[2] = {true, false};

int *gIntPtr = &gIntVar;
double *gDoublePtr = &gDoubleVar;
char *gCharPtr = &gCharVar;
bool *gBoolPtr = &gBoolVar;

int *gIntPtr2 = (int*)0x8;
double *gDoublePtr2 = (double*)0x5432;
char *gCharPtr2 = (char*)0x4321;
bool *gBoolPtr2 = (bool*)0x12ABCDEF;

class bar {
public:
	bar() {
		d = 8;
		e[0] = 18;
		e[1] = 28;
	}
	int d;
private:
	int e[2];
};

class bar2 {
public:
	bar2() { 
    	f = 318;
    	g[0] = 228;
    	g[1] = 138;
    }
	int f;
private:
	int g[2];
};

class foo: public bar, bar2 {
public:
    foo() {
    	c = 8;
    	a[0] = 1000;
    	a[1] = 23;	
    }
	int a[2];
	bar b;
private:
	int c;
};

struct Z {
public:
	int x;
	int y;
};
struct childStruct {
public:
	Z z;	
};

// For bug 320277
class Base {
public:
  int nested;
  int* pNested;
};
class BaseTest: public Base {
public:
  BaseTest() {}
  void test() { 
    nested = 8;
    return; 
  }
  Base Base; // make sure we don't get confused by the same name
};
// End bug 320277

void locals2() {
	// Check that we get the content of local variables with 
	// the same name as the calling method
	int lIntVar = 6789;
	double lDoubleArray[2] = {123.456, 6789.6789};
	char lCharVar = 'i';
	char *lCharPtr = &lCharVar;
	bool *lBoolPtr2 = (bool*)0xABCDE123;
	lBoolPtr2 = 0;	// step up to this line to ensure all locals are in scope
	return;
}

void testLocals() {

    int lIntVar = 12345;
    double lDoubleVar = 12345.12345;
    char lCharVar = 'm';
    bool lBoolVar = false;

    int lIntArray[2] = {6789, 12345};
    double lDoubleArray[2] = {456.789, 12345.12345};
    char lCharArray[2] = {'i', 'm'};
    bool lBoolArray[2] = {true, false};
    
    int *lIntPtr = &lIntVar;
    double *lDoublePtr = &lDoubleVar;
    char *lCharPtr = &lCharVar;
    bool *lBoolPtr = &gBoolVar;

    int *lIntPtr2 = (int*)0x1;
    double *lDoublePtr2 = (double*)0x2345;
    char *lCharPtr2 = (char*)0x1234;
    bool *lBoolPtr2 = (bool*)0x123ABCDE;

    locals2();
	lBoolPtr2 = (bool*)0;	// step-out from locals2() will land here; ensures our vars are still visible
    return;
}

int testChildren() {
    foo f;

	f.d = 1;
	
	return 0;
}

int testWrite() {
	typedef int *intPtr;

	int a[2] = {3, 456};
	intPtr ptr = 0;
	
	return 0;
}

int testName1(int newVal) {
	int a = newVal;
	return a;
}

int testName2(int newVal) {
	int a = newVal;
	return a;
}

int testSameName1(int newVal) {
	int a = newVal;
	Z z;
	z.x = newVal;
	z.x = newVal; // this redundant line is here to ensure 3 steps after running to this func leaves locals visible
	return a;
}
int testSameName1(int newVal, int ignore) {
	int a = newVal;
	Z z;
	z.x = newVal;
	a = newVal; // this redundant line is here to ensure 3 steps after running to this func leaves locals visible
	return a;
}


int testSameName() {
	testSameName1(1);
	testSameName1(2, 0);
	testSameName1(3);

	return 0;
}

int testConcurrent() {
	int a[2] = {28, 32};
	return a[0];
}

int testSubblock() {
	int a = 8;
	int b = 1;
	if (a) {
		int a = 12;
		b = a;
	}
	return b;
}

int testAddress() {
	int a = 8;
	int* a_ptr = &a;
	
	return a;
}



int testUpdateChildren(int val) {
	childStruct a;
	a.z.x = val + 10;
	a.z.y = val + 11;
	
	a.z.x = val + 20;	
	a.z.y = val + 21;
	
	return a.z.x;
}
int testUpdateChildren2(int val) {
	childStruct a;
	a.z.x = val + 10;
	a.z.y = val + 11;
	
	a.z.x = val + 20;	
	a.z.y = val + 21;
	
	return a.z.x;
}

int testDeleteChildren() {
	foo f;
	int a[1111];
	a[0] = 0; // this line is here to ensure a step-over after running to this function leaves our locals visible	
	return 1;
}

int testUpdateGDBBug() {
	// GDB 6.7 has a bug which will cause var-update not to show
	// the new value of 'a' if we switch the format to binary,
	// since binary of 3 is 11 which is the same as the old value
	// in natural format
	int a = 11;
	a = 3;
	return 0;
}

int testUpdateIssue() {
	double a = 1.99;
	a = 1.22;
	a = 1.22; // this redundant line is here to ensure 3 steps after running to this func leaves locals visible
}

int testUpdateIssue2() {
	struct {
		double d;
	} z;
	
	z.d = 1.0;
	z.d = 1.22;
	z.d = 1.22; // this redundant line is here to ensure 3 steps after running to this func leaves locals visible	
}

int testConcurrentReadAndUpdateChild() {
	struct {
		int d;
	}z;
	
	z.d = 1;
	z.d = 2;
}

int testConcurrentUpdateOutOfScopeChildThenParent1() {
	struct {
		int d;
	}z;

	z.d = 1;
	z.d = 1; // this redundant line is here to ensure 2 steps after running to this func leaves locals visible
}

int testConcurrentUpdateOutOfScopeChildThenParent2() {
	struct {
		int d;
	}z;

	z.d = 2;
	z.d = 2; // this redundant line is here to ensure 2 steps after running to this func leaves locals visible
}

int testConcurrentUpdateOutOfScopeChildThenParent() {
	testConcurrentUpdateOutOfScopeChildThenParent1();
	testConcurrentUpdateOutOfScopeChildThenParent2();
}

int testUpdateOfPointer() {
	struct {
		int value;
		int* ptr;
	} z;
	
	int otherValue = 3;
	
	z.ptr = &z.value;
	z.value = 1;

	/* testUpdateOfPointer_1 */

	z.ptr = &otherValue;
	z.value = 2;

	/* testUpdateOfPointer_2 */

	return 0;
}

int testUpdateOfPointerTypedef() {
	typedef int *intPtr;

	int int1 = 1;
	int int2 = 2;
	int int3 = 3;
	int int4 = 4;

	struct {
		intPtr ptr;
	} s;

	intPtr ptr = &int1;
	s.ptr = &int2;

	/* testUpdateOfPointerTypedef_1 */

	ptr = &int3;
	s.ptr = &int4;

	/* testUpdateOfPointerTypedef_2 */

	return 0;
}

int testCanWrite() {
	int a = 1;
	int* b = &a;
	struct {
		int in;
	} c;
	int d[2];
	
	return 1;
}

int testArrays() {
	int array_simple[10];
	int array_int[24321];
	foo array_foo[1200];
	int array_double_small[11][21];
	char array_double_large[111][210];

	return 1;
}

int testCasting() {
	int array_large[111] = {65, 0x41424344, 0x45464748}; // Decimal: 65, 1094861636, 1162233672, Char: A, ABCD, EFGH
	int array_small[4] = {65, 0x41424344, 0x45464748}; // Decimal: 65, 1094861636, 1162233672, Char: A, ABCD, EFGH

	int* int_ptr = &array_small[0];

	return 1;
}

// For bug 376901 RTTI tests
class VirtualBase {
public:
   virtual ~VirtualBase() {}  // Necessary to force RTTI generation for the base class
   int a;
private:
   bool b;
};

class Derived: public VirtualBase {
public:
    int c;    
    VirtualBase* ptr;
private:
    bool d;
    int e[4];
};

class OtherDerived: public VirtualBase {
public:
    int d;
private:
    bool c;
    int f[4];
};
int testRTTI() {
    Derived derived;
    Derived child1;
    OtherDerived child2;
    
    derived.ptr = &child1;  // here derived.b is of type bar
    
    derived.ptr = &child2;  // here derived.b is of type foo   
    
    return 1;   // here derived.b is of type Derived
}
// End of bug 376901 RTTI tests

void noReturnValue() {
    int a = 0;
    a++;
    return;
}
    
int testSimpleReturn(int a) {
	int b = 0;
	b = a;
	return b;
}

foo testComplexReturn() {
	foo f;
	int a = 8;
	
	return f;
}

void testReturn() {
	int a = 10;
	bool b = false;
	
	testSimpleReturn(6);
	testComplexReturn();
	noReturnValue();
	a = 0;
}

void testExistingChild() {
	bar b;
	int a = 10;
	return;
}

int main() {
    printf("Running ExpressionTest App\n");

    testLocals();
    testChildren();
    testWrite();
    testName1(1);
    testName2(2);
    testName1(3);
    testSameName();
    testConcurrent();
    testSubblock();
    testAddress();
    testUpdateChildren(0);
    testUpdateChildren(100);
    testUpdateChildren2(200);
    testDeleteChildren();
    testUpdateGDBBug();
    testUpdateIssue();
    testUpdateIssue2();
    testConcurrentReadAndUpdateChild();
    testConcurrentUpdateOutOfScopeChildThenParent();
    testUpdateOfPointer();
    testUpdateOfPointerTypedef();
    testCanWrite();
    testArrays();
    testRTTI();
    testCasting();
    testReturn();
    testExistingChild();
    
    // For bug 320277
    BaseTest b; b.test();
    
 
    return 0;
}

