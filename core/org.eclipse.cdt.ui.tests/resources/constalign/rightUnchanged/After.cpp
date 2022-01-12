
void f(int const * const);

void f(int const * const) {

}

int main(int argc, char **argv) {

	int const &dsa { 2 };

	int const j { 8 };

	int const * const klz;

	int const l { 2 };

	bool yes = false;

	int const k { 42 };

	int const volatile m = 21;

	using volatile_int = int;
	volatile_int const volatile v = 89;

	using const_int = int;
	const_int const w = 73;
}