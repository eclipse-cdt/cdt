#include "include.h"

#define FOO bar

class Heal{};

class A {
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
		
		using namespace NS2;
		
		a aStruct;
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
NS::B b2;

union u{
};