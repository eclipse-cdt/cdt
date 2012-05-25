class Class1 {
public:
	double normalMethod(int p1, char p2, float p3);
	virtual void inheritedMethod();
	virtual void pureVirtualMethod() = 0;
	virtual void overriddenMethod();

	void noExceptionSpecMethod();
	void emptyExceptionSpecMethod() throw();
	void nonEmptyExceptionSpecMethod() throw(int);

	inline int inlineMethod();
	static int staticMethod();
	int varArgsMethod(...);
	int constMethod() const;
	int volatileMethod() volatile;
	int constVolatileMethod() const volatile;

	// Here, const/volatile applies to the return value, not the method
	const int *notConstMethod();
	volatile int *notVolatileMethod();
	const volatile int *notConstVolatileMethod();

	Class1();
	virtual ~Class1() = 0;
};

struct A {
	A();
	A(const A&) throw();
	~A() throw(int);
};

struct B {
	B() throw();
	B(const B&) throw();
	~B() throw(double);
};

struct D : public A, public B {};

struct E {
	virtual void virtualMemberFunction(){}
};

struct F : public E {
	void virtualMemberFunction() override{}
};

struct G : public F {
	void virtualMemberFunction() override final{}
};

class Class2 : public Class1 {
public:
	void pureVirtualMethod();
	void overriddenMethod();
	void overloadedMethod();
	void overloadedMethod(int p1);

	Class2();
	~Class2();
};

double Class1::normalMethod(int p1, char p2, float p3) {
}

void Class1::inheritedMethod() {
}

void Class1::overriddenMethod() {
}

void Class2::pureVirtualMethod() {
}

void Class2::overriddenMethod() {
}

void Class2::overloadedMethod() {
}

void Class2::overloadedMethod(int p1) {
}

Class1::Class1() {
}

Class1::~Class1() {
}

Class2::Class2() {
}

Class2::~Class2() {
}

class Class3 {
	int defaultMethod();
private:
	void privateMethod();
protected:
	char protectedMethod();
public:
	double publicMethod();
};

int main() {
	Class2 c2;

	Class1 *pc1 = &c2;
	Class2 *pc2 = &c2;

	pc1->inheritedMethod();

	pc1->pureVirtualMethod();
	pc1->pureVirtualMethod();

	pc1->overriddenMethod();
	pc1->overriddenMethod();
	pc1->overriddenMethod();

	c2.inheritedMethod();
	pc2->inheritedMethod();

	c2.pureVirtualMethod();
	c2.pureVirtualMethod();
	pc2->pureVirtualMethod();

	c2.overriddenMethod();
	c2.overriddenMethod();
	c2.overriddenMethod();
	pc2->overriddenMethod();

	c2.overloadedMethod();
	pc2->overloadedMethod();

	c2.overloadedMethod(1);
	c2.overloadedMethod(1);
	pc2->overloadedMethod(1);

	return 0;
}
