namespace ns4 {
	
	class Class1 {
		friend void function2(Class1);
	};
	Class1 element;
	void function2(Class1){};
}
using ns4::element;

void Z()
{
	ns4::function2(element);
	ns4::Class1::function2(element);  //error!
}
