
void f(int const* const);

void f(int const* const) {

}

int main(int argc, char **argv) {

	int const &dsa { 2 };

	int const j { 8 };

	int const *const klz;

	int const l { 2 };

	bool yes = false;

	int const k { 42 };

	volatile int const q = 1;

	using int_const = int;
	int_const const r = 7;
}
