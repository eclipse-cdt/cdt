#ifndef CLASS1_H_
#define CLASS1_H_

class Class1 {
};

namespace namespace1 {
    class Class1 {
    public:
    	Class1();
    	~Class1();
    	
    	int class1x;
    	int class1y;
        class Class2 { //namespace1::Class1::Class2
        };
    };
    
    namespace namespace2 {
    	class Class1 {
    	};
    };

    class Class2 {

    };
};

#endif /*CLASS1_H_*/
