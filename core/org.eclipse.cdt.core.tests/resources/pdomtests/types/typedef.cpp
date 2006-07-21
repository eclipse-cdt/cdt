class A {
public:
	void f() {
	}
};

typedef A * X;

void g() {
	X x;
	x->f();
}
