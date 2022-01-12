/* ---------- Test 6 ---------- 
 * function foo06 used in func.c
 */
void foo06() { // def06
}
/* ---------- Test 7 ---------- 
 * function foo06 fake usage in func.c
 */
static void foo07() { // def07
}

/* ---------- Test 8 ---------- 
 * static function foo08 defined in second.c
 */
static void foo08() { // defS08
}
static void bar08(){
	foo08(); // refS08
}
