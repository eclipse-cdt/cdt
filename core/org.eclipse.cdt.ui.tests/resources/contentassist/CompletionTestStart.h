#define DEBUG 1
#define AMacro(x) x+1
#define XMacro(x,y) x+y

int aVariable;
int xVariable;

bool aFunction();
bool xFunction();

enum anEnumeration {
	aFirstEnum,
	aSecondEnum, 
	aThirdEnum
};

enum xEnumeration {
	xFirstEnum,
	xSecondEnum, 
	xThirdEnum
};

struct AStruct{
	int aStructField;
};

struct XStruct{
	int xStructField;
};

void anotherFunction(){
   int aLocalDeclaration = 1;
}

void xOtherFunction(){
   int xLocalDeclaration = 1;
}

class aClass {
public:
	int aField;
	float xAClassField;
	int aMethod();
	void xAClassMethod(int x);
};

class anotherClass {
public:
	int anotherField;
	void anotherMethod();
};

class xOtherClass {
public:
	xOtherClass(char*);
	xOtherClass(int);
	int xOtherField;
	void xOtherMethod();
	void xOtherMethod(int);
};

namespace aNamespace {
    void aNamespaceFunction(){
    }
};

namespace xNamespace {
    void xNamespaceFunction(){
    }
};


