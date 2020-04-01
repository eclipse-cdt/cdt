enum TestCPPEnum {
	cppa,
	cppb,
	cppc
};

TestCPPEnum test() {
	return cppa;
}

enum [[nodiscard]] TestCPPEnumNoDis {
	e1,
	e2,
	e3
};
