class A {
	struct A_Hidden {
		static void test() {}
	};

protected:
	using A_VisibleAlias = A_Hidden;
	typedef A_Hidden A_VisibleTypedef;
};