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
    envTest(); // FIRST_LINE_IN_MAIN
    reverseTest();
    stopAtOther();
    return 0; // LAST_LINE_IN_MAIN
}

