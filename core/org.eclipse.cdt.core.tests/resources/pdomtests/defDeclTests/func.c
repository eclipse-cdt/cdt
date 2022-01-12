
/* ---------- Test 1 ---------- 
 * simple 
 */
void foo01(); // decl01
void bar01(){
	foo01(); // ref01
}

/* ---------- Test 2 ---------- 
 * K&R declrations
 */
void foo02(); // decl02
void bar02(){
	foo02('a'); // ref02
}

/* ---------- Test 3 ---------- 
 * post-declaration
 */

void bar03(){
	foo03(); // ref03
}
void foo03(); // decl03

/* ---------- Test 4 ---------- 
 * post-definition
 */

void bar04(){
	foo04(); // ref04
}
void foo04() { // def04
}
/* ---------- Test 5 ---------- 
 * no decl/def
 */

void bar05(){
	foo05(); // ref05
}
/* ---------- Test 6 ---------- 
 * function foo06 defined in second.c
 */
void foo06(); // decl06
void bar06(){
	foo06(); // ref06
}
/* ---------- Test 7 ---------- 
 * static function foo07 defined in second.c
 */
void foo07(); // decl07
void bar07(){
	foo07(); // ref07
}

/* ---------- Test 8 ---------- 
 * static function foo08 defined in second.c
 */
static void bar08(){
	foo08(); // ref08
}
static void foo08() { // def08
}
