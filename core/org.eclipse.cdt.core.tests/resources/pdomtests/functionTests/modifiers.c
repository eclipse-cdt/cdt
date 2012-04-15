double normalCFunction(int p1, char p2, float p3);
static int staticCFunction(double p1);
extern float externCFunction(int p1);
inline void inlineCFunction(long p1);
void varArgsCFunction(int p1, ...);
const void constCFunction();
volatile void volatileCFunction();
void storageClassCFunction(register int p1, int p2);
void noReturnCFunction() __attribute__((noreturn));

void voidCFunction();
int intCFunction();
double doubleCFunction();
char charCFunction();
float floatCFunction();


struct S {
	struct D {
		int a;
	};
};
