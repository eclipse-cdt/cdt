// IStructure
struct testStruct1 {
	char m_field1;
	char* m_field2;
	unsigned char m_field3;
	int m_field4;
	unsigned m_field5;
	void* m_field6;
	
	void method1();
	struct testStruct1 method2( char* in_field2, int in_field4 ) {}
	// this is very C++:
	testStruct1( char* in_field2, int in_field4 ) {}
	~testStruct1() {}
};

struct testStruct2 {
};

struct testStruct3 {
} aTestStruct3;

// no semicolon, parser should recover
struct testStruct4NoSemicolon {
}

// forward declaration
struct testStruct5;

// variable declaration using predefined struct.
struct testStruct6 aTestStruct6;

struct {
	int x;
} testAnonymousStructObject1;

struct {
	int x;
} testAnonymousStructObject2= {1};

// to resync the parser if necessary
struct testStruct7 {
};

// an inner struct
struct testStruct8 {
	struct testStruct9Inner {
		int x;
	};
	struct testStruct10Inner {
		int y;
		struct testStruct11Inner {
			int z;
		};
	};
};

union testUnion1 {
	char m_field1;
	char* m_field2;
	unsigned char m_field3;
	int m_field4;
	unsigned m_field5;
	void* m_field6;	
};

class testClass1 {
};

class testClass2NoSemicolon {
}

class catchTheSyntaxError;

class testClass3 {
};

class testClass4Abstract {
	void aNonVirtual();
	virtual void aVirtual();
	virtual void aPureVirtual()=0;
};

class testClass5
: public testClass1, protected testClass3, private testClass4Abstract {
};

// to resync the parser if necessary
class testClass6 {
};
