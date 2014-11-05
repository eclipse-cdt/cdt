#include <stdio.h>
#include "somehdr.h"

int main(int argc, char **argv) {
  int k = SOME_VALUE; /* comment */
  int j = x(k);
  int i;
  for (i = 0; i < argc; ++i) {
     printf("argv %d is %s\n", i, argv[i]);
  }
  printf ("hello world %d\n", j);
  return 0;
}
