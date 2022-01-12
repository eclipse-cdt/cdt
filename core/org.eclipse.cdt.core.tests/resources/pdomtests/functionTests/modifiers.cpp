double normalCPPFunction(int p1, char p2, float p3);
void storageClassCPPFunction(register int p1, auto int p2);
static int staticCPPFunction(double p1);
extern float externCPPFunction(int p1);
inline void inlineCPPFunction(long p1);
void varArgsCPPFunction(int p1, ...);
void noReturnCPPFunction() __attribute__((noreturn));
[[noreturn]] void trailingNoReturnStdAttributeDecl();
void leadingNoReturnStdAttributeDecl() [[noreturn]];
[[noreturn]] void trailingNoReturnStdAttributeDef(){}
void leadingNoReturnStdAttributeDef() [[noreturn]]{}

int noDiscardCPPFunction() __attribute__((warn_unused_result));
[[nodiscard]] int trailingNoDiscardStdAttributeDecl();
int leadingNoDiscardStdAttributeDecl() [[nodiscard]];
[[nodiscard]] int trailingNoDiscardStdAttributeDef(){return 0;}
int leadingNoDiscardStdAttributeDef() [[nodiscard]]{return 0;}

void voidCPPFunction();
int intCPPFunction();
double doubleCPPFunction();
char charCPPFunction();
float floatCPPFunction();
