class A {
public:
	void f() {
	}
};

class B : public A {
};

int main() {
	B b;
	b.f();
}
