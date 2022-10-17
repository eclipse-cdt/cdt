#include <iostream>
#include "config.h"

int main(int argc, char **argv) {
	std::cout << "Hello World" << std::endl;
	std::cout << "Version " << ${projectName?replace(" ", "_")}_VERSION_MAJOR << "." << ${projectName?replace(" ", "_")}_VERSION_MINOR << std::endl;
	return 0;
}
