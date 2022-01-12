/* ---------- Test 9 ---------- 
 * write global
 */
int var_v09; // def_v09
static void bar_v09(){
	var_v09=1; // ref_v09
}

/* ---------- Test 10 ---------- 
 * read global
 */
int var_v10 = 1; // def_v10
static void bar_v0(){
	int a = var_v10; // ref_v10
}
/* ---------- Test 11 ---------- 
 * read global in expr
 */
int var_v11 = 1; // def_v11
static void bar_v11(){
	int a = 1 + 
	        var_v11; // ref_v11
}
/* ---------- Test _v12 ---------- 
 * def and decl
 */
extern int var_v12; // decl_v12
static void bar_v12(){
	int a = var_v12; // ref_v12
}
int var_v12 = 1; // def_v12

/* ---------- Test _v13 ---------- 
 * def and decl
 */

extern int var_v13; // decl_v13
int var_v13 = 1; // def_v13

static void bar_v13(){
	int a = var_v13; // ref_v13
}
/* ---------- Test _v14 ---------- 
 * def and decl
 */


int var_v14 = 1; // def_v14
extern int var_v14; // decl_v14

static void bar_v14(){
	int a = var_v14; // ref_v14
}
