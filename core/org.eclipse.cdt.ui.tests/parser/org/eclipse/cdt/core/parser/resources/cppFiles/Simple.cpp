#include <Simple.h>

#include <iostream>

#define NULL (void *)0

SimpleClass::SimpleClass( void )
{
	init( NULL );
}

SimpleClass::~SimpleClass( void )
{
}

SimpleClass::SimpleClass( const SimpleClass & arg )
{
	//TODO: copy constructor
}

SimpleClass & SimpleClass::operator=( const SimpleClass & arg )
{
	if( this != &arg )
	{
	}
	return *this;
}


void SimpleClass::init( void * foo)
{
}

InnerStruct & SimpleClass::getInner( void )
{
	return inner;
}
