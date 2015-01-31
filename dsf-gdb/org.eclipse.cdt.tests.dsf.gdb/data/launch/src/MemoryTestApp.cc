//============================================================================
// Name        : MemoryTestApp.cpp
// Author      : Francois Chouinard
// Version     : 1.0
// Copyright   : Ericsson AB
// Description : Memory test application
//============================================================================

#include <iostream>
using namespace std;

const int ARRAY_SIZE = 256;

char charBlock[ARRAY_SIZE];
int integerBlock[ARRAY_SIZE];

void zeroBlocks()
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

int main()
{
	zeroBlocks(); // LINE_NUMBER
	setBlocks();
	return 0;
}
