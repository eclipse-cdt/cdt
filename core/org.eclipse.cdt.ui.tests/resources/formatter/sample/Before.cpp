//#include <Simple.h>
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


const SimpleStruct simpleStruct =
{
	1
  , "mySimple"
  , 0.1232
};

#define SIZEOF( A, B ) sizeof( A.B )

              #define FOREVER \
            for(;;)\
{\
        \
                }

const SimpleStruct array[] =
{
	{
		  SIZEOF( simpleStruct, num ),
#if FOO
				"foo"
   #  else
		"bar"
#endif
	  , 0.5
	}
  , {
	  SIZEOF( simpleStruct, floatNum )
, "name"
	  , 1.1
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
