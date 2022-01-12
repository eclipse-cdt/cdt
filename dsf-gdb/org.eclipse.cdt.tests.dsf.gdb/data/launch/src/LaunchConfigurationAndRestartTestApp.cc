#include <stdio.h>
#include <stdlib.h>

int stopAtOther() {
    return 0;
}

int reverseTest() {
    int i = 0;
    i++;
    i++;
    i++;
    i++;
    i++;
    return 0;
}

int envTest() {
    char *home, *launchTest;
    home = getenv("HOME");
    launchTest = getenv("LAUNCHTEST");
    return 0;
}

int main (int argc, char *argv[])
{    int dummy = 1; // FIRST_LINE_IN_MAIN (make the line of code the same as opening brace to account for different gdb/gcc combinations)
	int var = 1; // GDB has incomplete support for reverse debugging - so for the purpose of tests reverse debug these simple assignment statements
	var = 2;
	var = 3;
	var = 4; // three_steps_back_from_b_stopAtOther
	var = 5;
	stopAtOther(); // main_init
    reverseTest(); // tests assume that every line between first and last
    envTest(); // is steppable, so no blank lines allowed.
    return 36; // LAST_LINE_IN_MAIN
    // Return special value to allow
    // testing exit code feature
}

