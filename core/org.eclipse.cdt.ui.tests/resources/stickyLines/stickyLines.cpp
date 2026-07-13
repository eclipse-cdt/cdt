/* CSourceStickyLinesProcessorTest source file */

/* source line numbers are critical */
/* append any further tests to the end of this file */

/* TEST1 - interleaved preprocessor */
#define TEST1
void test1(int n) { // sticky
#ifdef TEST1 // sticky
	while (n) { // sticky
		// TEST1 - line 11
	}
#endif
}

/* TEST2 - if else */
void test2(int n) { // sticky
	if (n) { // sticky
		// TEST2 - line 19
	} else { // sticky
		// TEST2 - line 21
	}
}

/* TEST3 - switch case */
void test3(int n) { // sticky
	switch (n) { // sticky
		case 1:
			break;
		// TEST3 - line 30
		case 2: // sticky
		case 3: // sticky
			// TEST3 - line 33
			break;
	}
}
