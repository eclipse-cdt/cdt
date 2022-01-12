// inclusion begins and ends on line 2
#include <stdio.h>				

// simple macro begins and ends on line 5; ANOTHER on line 6
#define SIMPLE_MACRO simple
#define ANOTHER
// namespace begins on line 7, ends on line 22
namespace MyPackage{
	// class specification begins on line 10, ends on line 21
	class Hello{
	protected:
		// simple declaration begins and ends on line 13
		int x;
		// simple declaration begins and ends on line 15
		void setX(int X);
	public:
		// simple declaration begins on line 18 and ends on line 20
		Hello( void ) : x 
			( 5 ) {
		}
	};
}
  
// simple declaration begins on line 25 and ends on line 27
int *
	y =
	0; 

// complex macro begins on line 30 and ends on line 31
#define COMPLEX_MACRO 33 \
	+ 44

// template declaration begins on line 34 and ends on line 35
template <class A > 
	A createA( void ); 

// enumeration begins on line 38 and ends on line 43
enum {
	one,  // enumerator begins and ends on line 39
	two,  // enumerator begins and ends on line 40
	three // enumerator begins on line 41, ends on line 42
		= 4
};