#define DEBUG 1
enum {
	aFirstEnum,
	aSecondEnum, 
	aThirdEnum
};

enum {
	xFirstEnum,
	xSecondEnum, 
	xThirdEnum
};

int notAnonymous;
enum notAnonymousEnum {};
class notAnonymousClass {};

struct {
	int aStructField;
};

struct {
	int xStructField;
};

union {
	int aUnionMember1, aUnionMember2;
}; 

class {
public:
	int aField;
	float xAClassField;
	int aMethod();
	void xAClassMethod(int x);
};

class {
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

// namespace {
//    void aNamespaceFunction(){
//    }
// };

// namespace {
//    void xNamespaceFunction(){
//    }
// };
