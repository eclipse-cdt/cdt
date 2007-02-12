// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169382
struct x {};
struct x getX() {}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171520
int bug=sizeof(int);
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=173837
class ABaseClass {protected:ABaseClass(int x);};
class AClass : public ABaseClass {AClass(int x) throw(int);};
AClass::AClass(int x)throw(int):ABaseClass(x){for (int i=0;i < 12;i++) {}}
