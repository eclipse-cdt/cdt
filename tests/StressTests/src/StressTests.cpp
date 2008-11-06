/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

#include <iostream>
using namespace std;


void step_loop() {
	int i = 0;
	int values[1000];
	while (i < 1000) {
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
		values[i] = i++;
	}
}

void descend_stack(int depth) {
	if (depth == 0) {
		step_loop();
	} else {
		descend_stack(--depth);
	}
}

void variables() {
	class bar {
	public:
		int d;
	private:
		int e[2];
	};

	class bar2 {
	public:
		int f;
	private:
		int g[2];
	};

	class foo: public bar, bar2 {
	public:
		int a[2];
		bar b;
	private:
		int c;
	};

	// Test children
    foo f;
	f.d = 1;

    int lIntVar = 12345;
    double lDoubleVar = 12345.12345;
    char lCharVar = 'm';
    bool lBoolVar = false;

    int lIntArray[2] = {6789, 12345};
    double lDoubleArray[2] = {456.789, 12345.12345};
    char lCharArray[2] = {'i', 'm'};
    bool lBoolArray[2] = {true, false};

    int *lIntPtr = &lIntVar;
    double *lDoublePtr = &lDoubleVar;
    char *lCharPtr = &lCharVar;

    int *lIntPtr2 = (int*)0x1;
    double *lDoublePtr2 = (double*)0x2345;
    char *lCharPtr2 = (char*)0x1234;
    bool *lBoolPtr2 = (bool*)0x123ABCDE;
}

int step_over_sleep() {
        int a = 1;
        sleep(1);
        return 1;
}

int main() {
	step_loop();
	descend_stack(50);
	variables();
	step_over_sleep();
	return 0;
}

