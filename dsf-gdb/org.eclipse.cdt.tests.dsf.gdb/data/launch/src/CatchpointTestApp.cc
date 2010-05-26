#include <iostream>
#include "Sleep.h"
int g_i = 0;
int main() {
	for (; g_i < 8; g_i++) {
		try {
			std::cout << "Throwing exception" << std::endl;
			throw 1;
		}
		catch (int exc) {
			std::cout << "Exception caught" << std::endl;
		}
	}
	
	// For setting a catchpoint while target is running
	std::cout << "Sleeping..." << std::endl;
	SLEEP(2);
	std::cout << "...awake!" << std::endl;
	try {
		std::cout << "Throwing exception" << std::endl;
		throw 1;
	}
	catch (int exc) {
		std::cout << "Exception caught" << std::endl;
	}
		
	return 0;
}
