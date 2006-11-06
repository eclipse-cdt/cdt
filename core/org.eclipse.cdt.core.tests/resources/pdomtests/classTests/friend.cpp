class ClassA {
	int x,y;
	friend class ClassC; //ClassC is a friend class of ClassA
};

class ClassB {
public:
	void functionB();
	
};

class ClassC {
	friend void ClassB::functionB(); //functionB is a friend of ClassC
};
