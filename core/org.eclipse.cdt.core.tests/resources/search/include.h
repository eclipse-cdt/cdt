#ifndef INCLUDE_H
#define INCLUDE_H

class Head {
	Head * operator *= ( int index );
	Head * operator *  ( int index ){ return array[ index ]; }
	Head * operator += ( int index );
	
	operator const short & ();
	operator short         ();
	operator short int     ();
	
	Head ** array;
};

class DeclsAndDefns{
	static int staticField;
	int nonStaticField;
	
	void forwardMethod();
	void inlineMethod() {}
};

void forwardFunction();


#endif