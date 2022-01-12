/*
 ============================================================================
 Name        : exampleProgram.c
 Author      : $(author)
 Version     :
 Copyright   : $(copyright)
 Description : Uses shared library to print greeting
               To run the resulting executable the LD_LIBRARY_PATH must be
               set to ${project_loc}/$(libName)/.libs
               Alternatively, libtool creates a wrapper shell script in the
               build directory of this program which can be used to run it.
               Here the script will be called exampleProgram.
 ============================================================================
 */

#include "$(libName).h"

int main(void) {
  print_hello();
  return 0;
}
