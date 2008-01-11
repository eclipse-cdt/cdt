#define INT      int
#define FUNCTION_MACRO(arg) globalFunc(arg)

enum Enumeration {
	ONE, TWO, THREE
};

const int globalConstant = 0;
int globalVariable = 0;
static int globalStaticVariable = 0;

void globalFunc(int a);
static void globalStaticFunc() {
	globalVariable = 1;
}
;

class Base1 {
};
class Base2 {
};

class ClassContainer : Base1, Base2 {
public:
	static int staticPubField;
	const int constPubField;
	const static int constStaticPubField;
	int pubField;

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
};

template<class T1> class PartialInstantiatedClass : TemplateClass<T1, Base1> {
};

struct CppStruct {
	CppStruct() {}
	int structField;
};

union CppUnion {
	int unionField;
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
	return namespaceVar;
}
}

INT ClassContainer::pubMethod() {
	int localVar = 0;
	ns::namespaceVar= 1;
	return pubField + localVar;
}

INT ClassContainer::staticPrivMethod() {
	CppStruct* st= new CppStruct();
	st->structField= 1;
	CppUnion un;
	un.unionField= 2;
	staticPubMethod(staticPubField);
label:
	FUNCTION_MACRO(0);
	if (un.unionField < st->structField) 
		goto label;
	return globalConstant;
}

template<int X>
class ConstantTemplate {
public:
	int foo(int y) {
		return X;
	}
};

ConstantTemplate<5> c5;
ConstantTemplate<5> c52;
ConstantTemplate<4> c4;
