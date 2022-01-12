/* This is sample C++ code */
#include <cstdio>
#include <complex>
#define MACRO(x) x
using namespace std;
// This comment may span only this line
typedef unsigned int uint;
double operator""_d(unsigned long long i) {
  return static_cast<double>(i);
}
int static myfunc(uint parameter) {
  if (parameter == 0) fprintf(stdout, "zero\n");
  cout << "hello\n";
  using std::complex_literals;
  auto c = 13if;
  auto k = 13_d;
  return parameter - 1;
}
void mutator(int&);
template <typename Item>
class MyClass {
public:
  enum Number { ZERO, ONE, TWO };
  enum class NumberClass { ZERO, ONE, TWO };
  static char staticField;
  int field;
  virtual Number vmethod() const;
  void method(Number n) const {
    int local= (int)MACRO('\0');
label: myfunc(local);
    vmethod();
    staticMethod();
    problem();  // TODO: fix
    mutator(local);
  }
  static void staticMethod();
};
