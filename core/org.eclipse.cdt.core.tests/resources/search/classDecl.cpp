#include "include.h"

#define FOO bar

class Heal{};

class A {
	A() {}
	~A(){}
	class B {
		void f( A );
		void f( A & );
		void f( A* );
		void f( int &, const char [], A ** );
	};
};

namespace NS {
	namespace NS2{
		struct a{};
	}
	class B: public A {
	public:
		struct AA {};
		enum e {
			One,
			Two,
			Three
		};
		
		void f(){
			using namespace NS2;
			a aStruct;
		}
		
		

		AA anotherStruct;
	};
	union u{ } ;
}

namespace NS3{
	class C : public NS::B {
		e eE = One;
	};
}

A::B b1;

typedef NS::B NS_B;
NS_B b2;

union u{
};

class AClassForFoo {};

AClassForFoo foo( AClassForFoo ){
	AClassForFoo b;
	return b;
}

Head * Head::operator *= ( int index ){
	return array[ index ];
}

Head * Head::operator += ( int index ){
	return array[ index ];
}

extern int externalInt;
extern int externalIntWithInitializer = 2;
extern "C" int externCInt;
extern "C" int externCIntWithInitializer = 3;

void forwardFunction() { }
void normalFunction()  { }

void DeclsAndDefns::forwardMethod(){ }

int DeclsAndDefns::staticField = 5;


namespace bug68235{
	struct xTag {	
		int x;
	};
	typedef xTag xType;
	
	typedef struct yTag {
		int x;
	} yType;			
			
	class C1{
	public:
		xType x;		
		yType y;		
		C1();
		~C1();
	};
}