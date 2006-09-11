/*
 * header comment
 */

#define MULTI_LINE_MACRO(x) \
    if (DBG) { \
    	printf(x); \
    }

#if 0
# if 1
 //
# endif
#elif X
// X
#else
# if 1
#  if 0
#   if 1
 //
#   endif
#  else
#   if 1
 //
#   endif
#  endif
# endif
#endif

/*
 * comment
 */
int y;

#if 1
int func() {
#else
int func2() {
#endif
	return 0;
}

// multiple single line comments
// multiple single line comments
// multiple single line comments
// multiple single line comments
// multiple single line comments

class Class {
public:
	int pubField;
    static int staticPubMethod(int arg) {
        return 0;
    }
    int pubMethod();
};

int Class::pubMethod() {
	return pubField;
}

struct CppStruct {
    int structField;
};

union CppUnion {
    int unionField;
};
