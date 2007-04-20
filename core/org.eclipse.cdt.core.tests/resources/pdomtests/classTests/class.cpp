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

class C {
  public:
     C(int a) {}
};

class D {
  public:
     D(D &) {}
};
