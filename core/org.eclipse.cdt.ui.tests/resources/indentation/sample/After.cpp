#include <Simple.h>

const SimpleStruct simpleStruct =
{
		1,
		"mySimple",
		0.1232
};

#define SIZEOF( A, B ) sizeof( A.B )

#define FOREVER \
		for(;;)\
		{\
			\
		}

const OtherStruct array[] =
{
		{
#if FOO
				"foo"
#  else
				"bar"
#endif
				,	SIZEOF( simpleStruct, num )
				, &t_int
				, 0
		}
		, {
				"name"
				, SIZEOF( simpleStruct, floatnum )
				, &t_float
				, 1
		}
};

//          single line outside scope

void SimpleStruct_construct( 
		struct SimpleStruct * const this )
{
	// single line
	this->num = 1;
	this->name = "boo";
	this->floatNum = 1.5;
}

int ConnectParams_doSomething( const struct SimpleStruct * const this )
{
	/*
	 * multiline
	 */
	return 1;
}
