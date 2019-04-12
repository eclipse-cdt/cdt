/*
 * Indentation
 */
#include <math.h>
class Point {
public:
	Point(double x, double y) :
			x(x), y(y) {
	}
	double distance(const Point &other) const;
	int compareX(const Point &other) const;
	double x;
	double y;
};
double Point::distance(const Point &other) const {
	double dx = x - other.x;
	double dy = y - other.y;
	return sqrt(dx * dx + dy * dy);
}
int Point::compareX(const Point &other) const {
	if (x < other.x) {
		return -1;
	} else if (x > other.x) {
		return 1;
	} else {
		return 0;
	}
}
namespace FOO {
int foo(int bar) const {
	switch (bar) {
	case 0:
		++bar;
		break;
	case 1:
		--bar;
	default: {
		bar += bar;
		break;
	}
	}
}
} // end namespace FOO
/*
 * Line Wrapping
 */
int array[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 1000,
		2000, 3000, 4000, 5000 };
int compare(int argument, int otherArg) {
	return argument + otherArg > argument * otherArg + 1000000 ?
			100000 + 50000 : 200000 - 30000;
}
class Other {
	static void bar(int arg1, int arg2, int arg3, int arg4, int arg5, int arg6,
			int arg7, int arg8, int arg9) {
	}
};
void foo() {
	Other::bar(100000, 200000, 300000, 400000, 500000, 600000, 700000, 800000,
			900000);
}
enum EEEE {
	ONE,
	TWO,
	THREE,
	FOUR,
	FIVE,
	SIX,
	SEVEN = 7,
	EIGHT,
	NINE,
	TEN,
	HUNDRED,
	THOUSAND,
	AMILLION
};
template<typename T1, typename T2> class map {
};
map<int, int> m;
