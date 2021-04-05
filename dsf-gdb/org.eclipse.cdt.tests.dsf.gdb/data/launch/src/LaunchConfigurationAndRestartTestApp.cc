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
{
    int dummy = 1; // this line is to make sure that for all versions of GDB/GCC the line below where we run the tests is a different line than b main is inserted
    envTest(); // FIRST_LINE_IN_MAIN
    reverseTest(); // tests assume that every line between first and last
    stopAtOther(); // is steppable, so no blank lines allowed.
    return 36; // LAST_LINE_IN_MAIN
    // Return special value to allow
    // testing exit code feature
}

