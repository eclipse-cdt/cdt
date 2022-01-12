#ifndef SIMPLE_H
#define SIMPLE_H

struct SimpleStruct
{
	int   num;
	char  name[ ];
	float floatNum;
};


void SimpleStruct_construct( struct SimpleStruct * const this );

int SimpleStruct_doSomething( const struct SimpleStruct * const this );

#endif /* SIMPLE_H */

