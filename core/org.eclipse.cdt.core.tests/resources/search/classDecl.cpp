class A {
	class B {
		void f( A );
	};
};

namespace NS {
	namespace NS2{
		struct a{};
	}
	class B: public A {
		struct A {};
		enum e {};
		
		using namespace NS2;
		
		a aStruct;
		A anotherStruct;
	};
	union u{ } ;
}

namespace NS3{
	class C : public NS::B {
		e eE;
	};
}

A::B b1;
NS::B b2;

union u{
};