#ifndef INCLUDE_H
#define INCLUDE_H

class Head {
	Head ** array;

	Head * operator *= ( int index );
	Head * operator *  ( int index ){ return array[ index ]; }
	Head * operator += ( int index );
	
	operator const short & ();
	operator short         ();
	operator short int     ();	

};

class DeclsAndDefns{
	static int staticField;
	int nonStaticField;
	
	void forwardMethod();
	void inlineMethod() {}
};

void forwardFunction();


class Direction{
   void turn();
   void turn(int);
   void turnAgain(void);
};
class Right : public Direction  {
   void turn() { }
};
#endif