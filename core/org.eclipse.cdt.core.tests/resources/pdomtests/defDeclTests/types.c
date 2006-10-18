/* ---------- Test t02 ---------- 
 * tag struct
 */

struct type_t01 {int a;}; // def_t01
static void bar_t01(){
	struct type_t01 var; //ref_t01
} 

/* ---------- Test t02 ---------- 
 * struct pre-declaration
 */

struct type_t02; // decl_t02
static void bar_t02(){
	struct type_t02 * var; //ref_t02
} 

/* ---------- Test t03 ---------- 
 * struct pre-declaration and definiton
 */

struct type_t03; // decl_t03
static void bar_t03(){
	struct type_t03 * var; //ref_t03
} 
struct type_t03 {int a;}; // def_t03

/* ---------- Test t04 ---------- 
 * typedef with structs
 */

typedef struct type_t04 { // defS_t04
	struct type_t04 * next; // refS_t04
} type_t04; // def_t04
static void bar_t04(){
	type_t04 st; // ref_t04
} 
/* ---------- Test t05 ---------- 
 * typedef with anonimous struct
 */

typedef struct { 
	int a; 
} type_t05; // def_t05
static void bar_t05(){
	type_t05 st; // ref_t05
} 
