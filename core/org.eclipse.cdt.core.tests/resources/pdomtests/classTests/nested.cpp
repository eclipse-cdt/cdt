class NestedA {
public:
	class NestedB {
	public:
		int x;
	};
};

int f() {
	NestedA::NestedB x;
	return x.x;
}
