#include "occurrences.h"

#define INT      int
#define FUNCTION_MACRO(arg) globalFunc(arg)
#define EMPTY_MACRO(arg) 

enum Enumeration {
	ONE, TWO, THREE
};

const int globalConstant = 0;
int globalVariable = 0;
static int globalStaticVariable = 0;

void globalFunc(int a);
static void globalStaticFunc() {
    EMPTY_MACRO(n);
	globalVariable = 1;
    EMPTY_MACRO(1);
    return;
}

class Base1 {
	Base1();
	~Base1();
};

Base1::~Base1() {}
Base1::Base1() {}

Base2::Base2() {}
void Base2::foo() {}

class ClassContainer : Base1, Base2 {
public:
	static int staticPubField;
	const int constPubField;
	const static int constStaticPubField;
	size_t pubField;

	static INT staticPubMethod(int arg) {
		FUNCTION_MACRO(arg);
		globalFunc(arg);
		return globalStaticVariable;
	}
	int pubMethod();

	typedef float pubTypedef;
	pubTypedef tdField;
private:
	static INT staticPrivMethod();
};

template<class T1, class T2> class TemplateClass {
	T1 tArg1;
	T2 tArg2;
	TemplateClass(T1 arg1, T2 arg2) {
		tArg1 = arg1;
		tArg2 = arg2;
	}
	void m(TemplateClass&);
};

template<class T1> class PartialInstantiatedClass : TemplateClass<T1, Base1> {
};

struct CppStruct {
	CppStruct() {}
	int structField;
};

union CppUnion {
	int unionField;
	CppUnion operator+(CppUnion);
};

typedef CppUnion TUnion;

namespace ns {
int namespaceVar = 0;
int namespaceFunc() {
	globalStaticFunc();
	TUnion tu;
	Enumeration e= TWO;
	switch (e) {
	case ONE: case THREE:
		return 1;
	}
	size_t size;
	return namespaceVar;
}
}

INT ClassContainer::pubMethod() {
	int localVar = 0;
	ns::namespaceVar= 1;
	return pubField + localVar;
}

using namespace ns;
//using ns::namespaceVar;

INT ClassContainer::staticPrivMethod() {
	CppStruct* st= new CppStruct();
	st->structField= namespaceVar;
	CppUnion un;
	un.unionField= 2;
	staticPubMethod(staticPubField);
	un + un;
label:
	FUNCTION_MACRO(0);
	if (un.unionField < st->structField) 
		goto label;
	return globalConstant;
}

template<int X>
class ConstantTemplate {
public:
	size_t getNumber(size_t y) {
		return X;
	}
};

ConstantTemplate<5> c5;
ConstantTemplate<5> c52;
ConstantTemplate<4> c4;

const int c= c5.getNumber(0);
