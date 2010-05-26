//============================================================================
// Name        : BreakpointTestApp.cpp
// Author      : Francois Chouinard
// Version     : 1.0
// Copyright   : Ericsson AB
// Description : Breakpoint test application
//============================================================================
#include "Sleep.h"
#include <iostream>
using namespace std;

const int ARRAY_SIZE = 256;

char charBlock[ARRAY_SIZE];
int integerBlock[ARRAY_SIZE];

void zeroBlocks(int abc)
{
	for (int i = 0; i < ARRAY_SIZE; i++) {
		charBlock[i] = '\0';
		integerBlock[i] = 0;
	}
}

void setBlocks()
{
	for (int i = 0; i < ARRAY_SIZE; i++) {
		charBlock[i] = (char) i;
		integerBlock[i] = i;
	}
}

void loop()
{
	int j = 10;
    int i = 0;
	for (i = 0; i < ARRAY_SIZE; i++)
		j = i;
}

int main()
{
    int a = 10;
    
	zeroBlocks(1);
	loop();
	setBlocks();
	SLEEP(1);
	a++;
	return 0;
}
