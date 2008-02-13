//============================================================================
// Name        : BreakpointTestApp.cpp
// Author      : Francois Chouinard
// Version     : 1.0
// Copyright   : Ericsson AB
// Description : Breakpoint test application
//============================================================================

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
	int j;
	
	for (int i = 0; i < ARRAY_SIZE; i++)
		j = i;
}

int main()
{
	zeroBlocks(1);
	loop();
	setBlocks();
	return 0;
}
