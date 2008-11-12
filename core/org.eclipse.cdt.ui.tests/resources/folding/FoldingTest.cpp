/*
 * header comment
 */
#define ONE
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
# if ONE
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

// http://bugs.eclipse.org/214590
int
main(int argc,
     char *argv[])
{
	int MyI = 0,j = 0;
	if (0==0) {
		puts("Wow ");
	} else {
		j = j;
	}
	for (MyI = 0; MyI < 10; ++MyI) {
		printf("%d\n",MyI);
	}
	while (0) {
		puts("nothinghere");
	}
	switch (1) {
		case 1:
			puts("ab");
			break;
		case 2:
			puts("cd");
		default:
			puts("xy");
	}
	do {
		puts("tryagain");

	} while (0);
	if (MyI==0) 
	{
		return 1;
	}
    return 0;
}

enum E {
    e1,
    e2,
    e3
};

// http://bugs.eclipse.org/248613
jungle::Monkey_ptr
jungle::MonkeyImpl::
Initialize()
{
}
// http://bugs.eclipse.org/248716
void foo() {
	if (1
			&& 2)
	{
	} else if (3
			|| 4)
	{
	}
}
// http://bugs.eclipse.org/255018
#if 0
// #endif missing
