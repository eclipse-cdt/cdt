class ManyOverloaded {
public:
	void qux();
	void qux(int i);
	void qux(int i, char c);
	void qux(ManyOverloaded* ptr);
	void qux(ManyOverloaded nptr);
};

void quux();
void quux(int i);
void quux(int i, char c);
void quux(ManyOverloaded* ptr);
void quux(ManyOverloaded nptr);

namespace corge {	
	void grault();
	void grault(int i);
	void grault(int i, char c);
	void grault(ManyOverloaded* ptr);
	void grault(ManyOverloaded nptr);
}

namespace ns2 {
	void quux();
	void quux(int i);
	void quux(int i, char c);
	void quux(ManyOverloaded* ptr);
	void quux(ManyOverloaded nptr);
}
