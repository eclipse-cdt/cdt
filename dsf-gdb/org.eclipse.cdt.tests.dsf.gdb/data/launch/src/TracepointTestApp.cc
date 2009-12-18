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
	int d;
private:
	int e[2];
};

class bar2 {
public: 
	int f;
private:
	int g[2];
};

class foo: public bar, bar2 {
public:
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

void testTracepoints() {	
    printf("Running TracepointTest App\n");

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

	int counter = 0;
	// Small loop
    for (counter=0; counter<10;) {
        counter++;
    }

	counter = 185;

	// Large loop
    for (counter=0; counter<10000;) {
        counter++;
    }
}

int main() {
	testTracepoints();    
    return 0;
}

