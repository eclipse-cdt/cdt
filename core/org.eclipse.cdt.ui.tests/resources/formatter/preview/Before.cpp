/*
 * Indentation
 */
#include <math.h>
class Point {public:Point(double xc, double yc) : x(xc), y(yc) {}double distance(const Point& other) const;int compareX(const Point& other) const;double x;double y;};double Point::distance(const Point& other) const {double dx = x - other.x;double dy = y - other.y;return sqrt(dx * dx + dy * dy);}int Point::compareX(const Point& other) const {if (x < other.x) {return -1;} else if (x > other.x) {return 1;} else {return 0;}}namespace FOO {int foo(int bar) const {switch (bar) {case 0:++bar;break;case 1:--bar;default: {bar += bar;break;}}}} // end namespace FOO
