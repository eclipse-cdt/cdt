class Foo {
public:
	Foo() {}
	void foo() {}
	void foo(int a) {}
	void foo(Foo f) {}
};

void bar() {printf("bar()\n");}
void bar(int a) {printf("bar(int)\n");}
void bar(int a, int b) {printf("bar(int,int)\n");}
void bar(Foo f, int z) {
	Foo a,b,c;
	printf("bar(Foo,int)\n");
}

void baz() {}

namespace X {
	class Foo {
	public:
		Foo(void) {}
		void m() {}
	};

	void bar() {printf("X::bar()\n");}
	void bar(int a) {printf("X::bar(int)\n");}
	void bar(int a, int b) {printf("X::bar(int,int)\n");}
	void bar(Foo f, int z) {
		Foo a,b,c;
		printf("X::bar(X::Foo,int)\n");
	}

	namespace Y {
		void qux() {}
	}
}

void references(Foo f, X::Foo h) {
    X::bar(); X::bar();
	X::bar(3); X::bar(3); X::bar(3);
	X::bar(4,4); X::bar(4,4); X::bar(4,4); X::bar(4,4);
	X::bar(h, 5); X::bar(h, 5); X::bar(h, 5); X::bar(h, 5); X::bar(h, 5);

    bar(); bar(); bar(); bar();
    bar(3); bar(3); bar(3);
    bar(2,2); bar(2,2);
    bar(f, 1);
    
    Foo y = new Foo();
    X::Foo z = new X::Foo();
}
