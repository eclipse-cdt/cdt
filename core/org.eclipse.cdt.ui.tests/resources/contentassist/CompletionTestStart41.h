
class B {
public:
	void bar() {}
};

class A {
	public:
	B* operator->() { return new B(); }
	void foo() {}
};