class ClassA {
public:
friend class ClassC;
};

class ClassC {
public:
friend void functionB();
};