namespace pr147903 {
	
	class testRef
	{
	};

	class test3 {
	public:
		void foo(testRef[]);
		void bar(testRef*);
	};

	void (* aFPtr)(testRef[]);

	void (* aFPtr1)(testRef*);

	namespace n {
		extern void (* aFPtr1)(testRef[]);
	};

}
