namespace namespace1 {
   
    namespace namespace2 {
    	void foo();
    	
    	namespace namespace3 {
    	}
    }
}

namespace namespaceNew = namespace1::namespace2;

void namespace1::namespace2::foo() {
	/* definition */
}
