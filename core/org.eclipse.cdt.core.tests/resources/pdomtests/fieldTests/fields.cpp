class Class1 {
	int defaultField = 0;
	
	mutable int mutableField;
	static int staticField;
	
private:
	double privateField = 4.5;
protected:
	char protectedField = 'A';
public:
	long publicField = 20;

	int c1a;
	double c1b;
	
	Class1();
	~Class1();
};

class Class2 : public Class1 {
public:
	char c2a;
	float c2b;
	
	Class2();
	~Class2();
};

Class1::Class1() {
}

Class1::~Class1() {
}

Class2::Class2() {
}

Class2::~Class2() {
}

int main() {
	Class1 c1;
	Class1 *pc1 = &c1;
	
	Class2 c2;
	Class2 *pc2 = &c2;
	
	c1.c1a = 0;
	pc1->c1a = 1;
	
	c2.c1a = 2;
	pc2->c1a = 3;
	
	c2.c2a = 4;
	c2.c2a = 5;
	pc2->c2a = 6;

	return 0;
}
