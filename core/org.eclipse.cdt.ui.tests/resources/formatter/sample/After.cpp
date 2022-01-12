//#include <Simple.h>
#ifndef SIMPLE_H
#define SIMPLE_H

struct SimpleStruct {
	int num;
	char name[];
	float floatNum;
};

void SimpleStruct_construct(struct SimpleStruct *const s);

int SimpleStruct_doSomething(const struct SimpleStruct *const s);

#endif /* SIMPLE_H */

const SimpleStruct simpleStruct = { 1, "mySimple", 0.1232 };

#define SIZEOF( A, B ) sizeof( A.B )

#define FOREVER \
            for(;;)\
{\
        \
                }

const SimpleStruct array[] = { { SIZEOF(simpleStruct, num),
#if FOO
				"foo"
   #  else
		"bar"
#endif
		, 0.5 }, { SIZEOF(simpleStruct, floatNum), "name", 1.1 } };

//          single line outside scope

void SimpleStruct_construct(struct SimpleStruct *const s) {
	// single line
	s->num = 1;
	s->name = "boo";
	s->floatNum = 1.5;
}

int ConnectParams_doSomething(const struct SimpleStruct *const s) {
	/*
	 * multiline
	 */
	return 1;
}
