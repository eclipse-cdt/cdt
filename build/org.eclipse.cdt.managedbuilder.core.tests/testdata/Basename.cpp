/*
============================================================================
 Name        : $(baseName).cpp
 Author      : $(author)
 Version     :
 Copyright   : $(copyright)
 Description : Exe source file
============================================================================
*/

//  Include Files  

#include "$(baseName).h"

//  Defined Constants

#define Constant "CONSTANT"


//  Global Variables

static int globalValue;

//  Local Functions

void printMessage(char* message) {
    printf(message);
}

// Main Function

int main(int nArgs, char **args) {
    printMessage("Hello, world!\n");
    printMessage("$(baseName)");
}


