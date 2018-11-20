// include
#include <stdio.h>
#include "whatever.h"
#include <src/slash.h>
#include <src\backslash.h>
#include "Program Files/space.h"
#include "../up1dir.h"
#include "./samedir.h"
#include "different_extension1.hpp"
#include "different_extension2.hh"
#include "different_extension3.x"
#include <no_extension>
# include "whitespace_after_hash"
	 #include "whitespace_before_hash"

// failure cases:
#include garbage
#include "resync_after_bad_parse_1"
#include
#include "resync_after_bad_parse_2"
#include "one" "two" "three"
#include "resync_after_bad_parse_3"

// from the Spec:

// from [C, 6.10.p8]
// should fail
#define EMPTY
EMPTY #include "invalid.h"

// from [C, 6.10.2.p8]:
// should equal #include "myInclude1.h"
#define MYINCFILE "myInclude1.h"
#include MYINCFILE

// from [C, 6.10.3.5.p6]:
// should equal #include "vers2.h"
#define INCFILE(x) vers ## x
#define xstr(x) str(x)
#define str(x) #x
#include xstr(INCFILE(2).h)

