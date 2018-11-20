namespace A 
{ 
	class ForwardA;
	ForwardA * tmp;
	int something(void);
	namespace B 
	{ 
		enum e1{dude1,dude2};
		int x;  
		class C 
		{	static int y = 5; 
			static int bar(void);
		}; 
	} 
} 
using namespace A::B;
using A::B::x;
using A::B::C;
using A::B::C::y;
using A::B::C::bar;
using A::something;
using A::B::e1;