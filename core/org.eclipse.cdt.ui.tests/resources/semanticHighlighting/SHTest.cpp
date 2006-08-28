#define SIMPLE_MACRO
#define FUNCTION_MACRO(arg) globalFunc(arg)

enum Enumeration {
    enumerator
};

const int globalConstant = 0;
int globalVariable = 0;
static int globalStaticVariable;

void globalFunc(int a);
static void globalStaticFunc() {
};

class Base1 {};
class Base2 {};

class ClassContainer : Base1, Base2 {
    friend void friendFunc();
    friend class FriendClass;    
    
public:
    static int staticPubField;
    const int constPubField;
    const static int constStaticPubField;
    int pubField;

    static int staticPubMethod(int arg) {
        FUNCTION_MACRO(arg);
        globalFunc(arg);
    }
    int pubMethod();

    enum pubEnumeration {pubEnumerator};
    class pubClass{};
    class pubStruct{};
    class pubUnion{};
    typedef pubClass pubTypedef;
    
protected:
    static const int constStaticProtField = 12; 
    static int staticProtField;
    const  int constProtField;
    int protField;

    static int staticProtMethod();
    int protMethod();

    enum protEnumeration {protEnumerator};
    class protClass{};
    class protStruct{};
    class protUnion{};
    typedef protClass protTypedef;
    
private:
    static const int constStaticPrivField = 12; 
    static int staticPrivField;
    const  int constPrivField;
    int privField;    

    static int staticPrivMethod();
    int privMethod();

    enum privEnumeration {privEnumerator};
    class privClass{};
    class privStruct{};
    class privUnion{};
    typedef privClass privTypedef;


};

template<T1,T2> class TemplateClass {
    T1 tArg1;
    T2 tArg2;
    TemplateClass(T1 arg1, T2 arg2) {
        tArg1 = arg1;
        tArg2 = arg2;
    }
};

template<T1> class PartialInstantiatedClass : TemplateClass<T1,Base1> {};


struct CppStruct {
    int structField;
};

union CppUnion {
    int unionField;
};

typedef CppUnion TUnion;

namespace ns {
    int namespaceField = 0;
    int namespaceFunc() {
    }
}

int ClassContainer::protMethod() {
    return protField;
}

int ClassContainer::pubMethod() {
    int localVar;
    return pubField;
}

int ClassContainer::staticPrivMethod() {
    CppStruct st= new CppStruct();
    st.structField= 1;
    CppUnion un= new CppUnion();
    un.unionField= 2;
    staticPubMethod(staticPrivField);
label:
    FUNCTION_MACRO(0);
    return 0;
}
