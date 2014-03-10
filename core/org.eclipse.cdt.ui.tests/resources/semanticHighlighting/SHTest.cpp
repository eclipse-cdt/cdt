#define INT      int
#define FUNCTION_MACRO(arg) globalFunc(arg)
#define EMPTY_MACRO(arg) 

enum Enumeration {
    enumerator
};

const int globalConstant = 0;
int globalVariable = 0;
static int globalStaticVariable = 0;

void globalFunc(int a);
static void globalStaticFunc() {
    EMPTY_MACRO(n);
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
        return globalStaticVariable;
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

template<class T1, class T2> class TemplateClass {
    T1 tArg1;
    T2 tArg2;
    TemplateClass(T1 arg1, T2 arg2) {
        tArg1 = arg1;
        tArg2 = arg2;
    }
};

template<class T1> class PartialInstantiatedClass : TemplateClass<T1, Base1> {};


struct CppStruct {
    int structField;
};

union CppUnion {
    int unionField;
    CppUnion operator+(CppUnion);
    CppUnion operator[](int);
};

typedef CppUnion TUnion;

namespace ns {
    int namespaceVar = 0;
    int namespaceFunc() {
	globalStaticFunc();
	return namespaceVar;
    }
}

INT ClassContainer::protMethod() {
    return protField;
}

INT ClassContainer::pubMethod() {
    int localVar = 0;
    return pubField + localVar;
}

INT ClassContainer::staticPrivMethod() {
    CppStruct* st= new CppStruct();
    st->structField= 1;
    CppUnion un;
    un.unionField= 2;
    staticPubMethod(staticPrivField);
    un + un[6];
label:
    FUNCTION_MACRO(0);
    if (un.unionField < st->structField) goto label;
    problemMethod();
    // external SDK
    SDKClass sdkClass;
    sdkClass.SDKMethod();
    SDKFunction();
    return 0;
}

//http://bugs.eclipse.org/209203
template <int n>
int f()
{
  return n;
}

//http://bugs.eclipse.org/220392
#define EMPTY
EMPTY int f();

//http://bugs.eclipse.org/340492
template< template<class> class U > class myClass {};

//http://bugs.eclipse.org/372004
void g() {
    extern int globalVariable;  // declared as global near top
}
