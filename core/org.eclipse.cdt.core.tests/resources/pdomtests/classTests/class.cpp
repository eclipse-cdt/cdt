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

class Z {
  public:
  Z (*f)(Z);
};

Z zzz= *new Z();

class C {
  public:
     C(int a) {}
};

class D {
  public:
     D(D &) {}
};
