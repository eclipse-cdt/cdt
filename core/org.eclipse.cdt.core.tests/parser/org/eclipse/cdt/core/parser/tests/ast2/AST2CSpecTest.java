/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anders Dahlberg (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author dsteffle
 */
public class AST2CSpecTest extends AST2SpecTestBase {

	public AST2CSpecTest() {
	}

	public AST2CSpecTest(String name) {
		super(name);
	}

	// /* Start Example(C 4-6) */
	// #ifdef _ _STDC_IEC_559_ _ // FE_UPWARD defined
	// fesetround(FE_UPWARD);
	// #endif
	public void test4s6() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 5.1.1.3-2) */
	// char i;
	// int i;
	public void test5_1_1_3s2() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 5.1.2.3-10) */
	// int f() {
	// char c1, c2;
	// c1 = c1 + c2;
	// }
	public void test5_1_2_3s10() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 5.1.2.3-11) */
	// int f() {
	// float f1, f2;
	// double d;
	// f1 = f2 * d;
	// }
	public void test5_1_2_3s11() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 5.1.2.3-12) */
	// int f() {
	// double d1, d2;
	// float f;
	// d1 = f = 1;
	// d2 = (float) 1;
	// }
	public void test5_1_2_3s12() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 5.1.2.3-13) */
	// int f() {
	// double x, y, z;
	// x = (x * y) * z; // not equivalent tox *= y * z;
	// z = (x - y) + y ; // not equivalent toz = x;
	// z = x + x * y; // not equivalent toz = x * (1.0 + y);
	// y = x / 5.0; // not equivalent toy = x * 0.2;
	// }
	public void test5_1_2_3s13() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 5.1.2.3-14) */
	// int f() {
	// int a, b;
	// a = a + 32760 + b + 5;
	// a = (((a + 32760) + b) + 5);
	// a = ((a + b) + 32765);
	// a = ((a + 32765) + b);
	// a = (a + (b + 32765));
	// }
	public void test5_1_2_3s14() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 5.1.2.3-15) */
	// //#include <stdio.h>
	// int f() {
	// int sum;
	// char *p;
	// sum = sum * 10 - '0' + (*p++ = getchar());
	// sum = (((sum * 10) - '0') + ((*(p++)) = (getchar())));
	// }
	public void test5_1_2_3s15() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.2.5-28) */
	// struct tag (* a[5])(float);
	public void test6_2_5s28() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.2.7-5) */
	// int f(int (*)(), double (*)[3]);
	// int f(int (*)(char *), double (*)[]);
	// int f(int (*)(char *), double (*)[3]);
	public void test6_2_7s5() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.4.4.4-12) */
	// char x='\023';
	// char y='\0';
	// char z='\x13';
	public void test6_4_4_4s12() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.5.2.2-12) */
	// int f1() {}
	// int f2() {}
	// int f3() {}
	// int f4() {}
	// int (*pf[5])(int a, int b);
	// int foo() {
	// int x=(*pf[f1()]) (f2(), f3() + f4());
	// }
	public void test6_5_2_2s12() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.5.2.3-7) */
	// struct s { int i; const int ci; };
	// struct s s;
	// const struct s cs;
	// volatile struct s vs;
	// int f() {
	// s.i; // int
	// s.ci; // const int
	// cs.i; // const int
	// cs.ci; // const int
	// vs.i; // volatile int
	// vs.ci; // volatile const int
	// }
	public void test6_5_2_3s7() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.5.2.3-8a) */
	// union {
	// struct {
	// int alltypes;
	// } n;
	// struct {
	// int type;
	// int intnode;
	// } ni;
	// struct {
	// int type;
	// double doublenode;
	// } nf;
	// } u;
	// int f() {
	// u.nf.type = 1;
	// u.nf.doublenode = 3.14;
	// if (u.n.alltypes == 1)
	// return 0;
	// if (sin(u.nf.doublenode) == 0.0)
	// return 0;
	// }
	public void test6_5_2_3s8a() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.5.2.3-8b) */
	// struct t1 { int m; };
	// struct t2 { int m; };
	// int f(struct t1 * p1, struct t2 * p2)
	// {
	// if (p1->m < 0)
	// p2->m = -p2->m;
	// return p1->m;
	// }
	// int g()
	// {
	// union {
	// struct t1 s1;
	// struct t2 s2;
	// } u;
	// return f(&u.s1, &u.s2);
	// }
	public void test6_5_2_3s8b() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.5.2.5-9) */
	// int *p = (int []){2, 4};
	public void test6_5_2_5s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.5.2.5-10) */
	// void f(void)
	// {
	// int *p;
	// p = (int [2]){*p};
	// }
	public void test6_5_2_5s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.5.2.5-11) */
	// int f(){
	// drawline((struct point){.x=1, .y=1},
	// (struct point){.x=3, .y=4});
	// drawline(&(struct point){.x=1, .y=1},
	// &(struct point){.x=3, .y=4});
	// }
	public void test6_5_2_5s11() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, false, 0);
	}

	// /* Start Example(C 6.5.2.5-12) */
	// int f() {
	// (const float []){1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6};
	// }
	public void test6_5_2_5s12() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.5.2.5-13) */
	// int f() {
	// "/tmp/fileXXXXXX";
	// (char []){"/tmp/fileXXXXXX"};
	// (const char []){"/tmp/fileXXXXXX"};
	// }
	public void test6_5_2_5s13() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.5.2.5-14) */
	// int f() {
	// (const char []){"abc"} == "abc";
	// }
	public void test6_5_2_5s14() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.5.2.5-15) */
	// int f() {
	// struct int_list { int car; struct int_list *cdr; };
	// struct int_list endless_zeros = {0, &endless_zeros};
	// eval(endless_zeros);
	// }
	public void test6_5_2_5s15() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.5.2.5-16) */
	// struct s { int i; };
	// int f (void)
	// {
	// struct s *p = 0, *q;
	// int j = 0;
	// again:
	// q = p, p = &((struct s){ j++ });
	// if (j < 2) goto again;
	// return p == q && q->i == 1;
	// }
	public void test6_5_2_5s16() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.5.3.4-5) */
	// extern void *alloc(size_t);
	// double *dp = alloc(sizeof *dp);
	public void test6_5_3_4s5() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.5.3.4-6) */
	// int f() {
	// int array[5];
	// int x = sizeof array / sizeof array[0];
	// }
	public void test6_5_3_4s6() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.5.6-10) */
	// int f() {
	// int n = 4, m = 3;
	// int a[n][m];
	// int (*p)[m] = a; // p == &a[0]
	// p += 1; // p == &a[1]
	// (*p)[2] = 99; // a[1][2] == 99
	// n = p - a; // n == 1
	// }
	public void test6_5_6s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.5.15-8) */
	//	int f(bool cond) {
	//		const void *c_vp;
	//		void *vp;
	//		const int *c_ip;
	//		volatile int *v_ip;
	//		int *ip;
	//		const char *c_cp;
	//
	//		cond ? c_vp : c_ip;
	//		cond ? v_ip : 0;
	//		cond ? c_ip : v_ip;
	//		cond ? vp : c_cp;
	//		cond ? ip : c_ip;
	//		cond ? vp : ip;
	//	}
	public void test6_5_15s8() throws Exception {
		BindingAssertionHelper helper = new AST2AssertionHelper(getAboveComment(), ParserLanguage.C);
		IASTExpression c1 = helper.assertNode("cond ? c_vp : c_ip");
		IASTExpression c2 = helper.assertNode("cond ? v_ip : 0");
		IASTExpression c3 = helper.assertNode("cond ? c_ip : v_ip");
		IASTExpression c4 = helper.assertNode("cond ? vp : c_cp");
		IASTExpression c5 = helper.assertNode("cond ? ip : c_ip");
		IASTExpression c6 = helper.assertNode("cond ? vp : ip");
		assertSameType(CommonCTypes.pointerToConstVoid, c1.getExpressionType());
		assertSameType(CommonCTypes.pointerToVolatileInt, c2.getExpressionType());
		assertSameType(CommonCTypes.pointerToConstVolatileInt, c3.getExpressionType());
		assertSameType(CommonCTypes.pointerToConstVoid, c4.getExpressionType());
		assertSameType(CommonCTypes.pointerToConstInt, c5.getExpressionType());
		assertSameType(CommonCTypes.pointerToVoid, c6.getExpressionType());
	}

	// /* Start Example(C 6.5.16.1-5) */
	// int f() {
	// char c;
	// int i;
	// long l;
	// l = (c = i);
	// }
	public void test6_5_16_1s5() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.5.16.1-6) */
	// int f() {
	// const char **cpp;
	// char *p;
	// const char c = 'A';
	// cpp = &p; // constraint violation
	// *cpp = &c; // valid
	// *p = 0; // valid
	// }
	public void test6_5_16_1s6() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.2.1-17) */
	// // offsetoff is a macro defined in stddef.h, using GNU definition
	// #define offsetof(TYPE, MEMBER) ((size_t) (&((TYPE *)0)->MEMBER))
	// struct s { int n; double d[]; };
	// struct ss { int n; double d[1]; };
	// int f() {
	// sizeof (struct s);
	// offsetof(struct s, d);
	// offsetof(struct ss, d);
	// }
	public void test6_7_2_1s17() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.7.2.1-18a) */
	// int f() {
	// struct s *s1;
	// struct s *s2;
	// s1 = malloc(sizeof (struct s) + 64);
	// s2 = malloc(sizeof (struct s) + 46);
	// }
	public void test6_7_2_1s18a() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.7.2.1-18b) */
	// struct { int n; double d[8]; } *s1;
	// struct { int n; double d[5]; } *s2;
	public void test6_7_2_1s18b() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.2.2-5) */
	// int f() {
	// enum hue { chartreuse, burgundy, claret=20, winedark };
	// enum hue col, *cp;
	// col = claret;
	// cp = &col;
	// if (*cp != burgundy)
	// return 0;
	// }
	public void test6_7_2_2s5() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.2.3-9) */
	// struct tnode {
	// int count;
	// struct tnode *left, *right;
	// };
	// struct tnode s, *sp;
	public void test6_7_2_3s9() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.2.3-10) */
	// typedef struct tnode TNODE;
	// struct tnode {
	// int count;
	// TNODE *left, *right;
	// };
	// TNODE s, *sp;
	public void test6_7_2_3s10() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.2.3-11) */
	// struct s2;
	// struct s1 { struct s2 *s2p; }; // D1
	// struct s2 { struct s1 *s1p; }; // D2
	public void test6_7_2_3s11() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.3-10) */
	// extern const volatile int real_time_clock;
	public void test6_7_3s10() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.3-11) */
	// int f() {
	// const struct s { int mem; } cs = { 1 };
	// struct s ncs; // the object ncs is modifiable
	// typedef int A[2][3];
	// const A a = {{4, 5, 6}, {7, 8, 9}}; // array of array of const int
	// int *pi;
	// const int *pci;
	// ncs = cs; // valid
	// cs = ncs; // violates modifiable lvalue constraint for =
	// pi = &ncs.mem; // valid
	// pi = &cs.mem; // violates type constraints for =
	// pci = &cs.mem; // valid
	// pi = a[0]; // invalid: a[0] has type ''const int *''
	// }
	public void test6_7_3s11() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.3.1-7) */
	// int * restrict a;
	// int * restrict b;
	// extern int c[];
	public void test6_7_3_1s7() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.3.1-8) */
	// void f(int n, int * restrict p, int * restrict q)
	// {
	// while (n-- > 0)
	// *p++ = *q++;
	// }
	public void test6_7_3_1s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.3.1-9) */
	// void f(int n, int * restrict p, int * restrict q)
	// {
	// while (n-- > 0)
	// *p++ = *q++;
	// }
	// void g(void)
	// {
	// extern int d[100];
	// f(50, d + 50, d); // valid
	// f(50, d + 1, d); // undefined behavior
	// }
	public void test6_7_3_1s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.3.1-10) */
	// void h(int n, int * restrict p, int * restrict q, int * restrict r)
	// {
	// int i;
	// for (i = 0; i < n; i++)
	// p[i] = q[i] + r[i];
	// }
	public void test6_7_3_1s10() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.3.1-11) */
	// int f()
	// {
	// int * restrict p1;
	// int * restrict q1;
	// p1 = q1; // undefined behavior
	// {
	// int * restrict p2 = p1; // valid
	// int * restrict q2 = q1; // valid
	// p1 = q2; // undefined behavior
	// p2 = q2; // undefined behavior
	// }
	// }
	public void test6_7_3_1s11() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.3.1-12) */
	// typedef struct { int n; float * restrict v; } vector;
	// vector new_vector(int n)
	// {
	// vector t;
	// t.n = n;
	// t.v = malloc(n * sizeof (float));
	// return t;
	// }
	public void test6_7_3_1s12() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, false, 0);
	}

	// /* Start Example(C 6.7.4-7) */
	// inline double fahr(double t)
	// {
	// return (9.0 * t) / 5.0 + 32.0;
	// }
	// inline double cels(double t)
	// {
	// return (5.0 * (t - 32.0)) / 9.0;
	// }
	// extern double fahr(double); // creates an external definition
	// double convert(int is_fahr, double temp)
	// {
	// return is_fahr ? cels(temp) : fahr(temp);
	// }
	public void test6_7_4s7() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.5.1-4) */
	// const int *ptr_to_constant;
	// int *const constant_ptr1;
	// typedef int *int_ptr;
	// const int_ptr constant_ptr2;
	public void test6_7_5_1s4() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.5.2-7) */
	// float fa[11], *afp[17];
	public void test6_7_5_2s7() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.5.2-8) */
	// extern int *x;
	// extern int y[];
	public void test6_7_5_2s8() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.5.2-9) */
	// extern int n;
	// extern int m;
	// void fcompat(void)
	// {
	// int a[n][6][m];
	// int (*p)[4][n+1];
	// int c[n][n][6][m];
	// int (*r)[n][n][n+1];
	// p = a; // invalid: not compatible because4 != 6
	// r = c; // compatible, but defined behavior only if
	// // n == 6 andm == n+1
	// }
	public void test6_7_5_2s9() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.5.2-10) */
	// extern int n;
	// int A[n]; // invalid: file scope VLA
	// extern int (*p2)[n]; // invalid: file scope VM
	// int B[100]; // valid: file scope but not VM
	// void fvla(int m, int C[m][m]); // valid: VLA with prototype scope
	// void fvla(int m, int C[m][m]) // valid: adjusted to auto pointer to VLA
	// {
	// typedef int VLA[m][m]; // valid: block scope typedef VLA
	// struct tag {
	// int (*y)[n]; // invalid: y not ordinary identifier
	// int z[n]; // invalid: z not ordinary identifier
	// };
	// int D[m]; // valid: auto VLA
	// static int E[m]; // invalid: static block scope VLA
	// extern int F[m]; // invalid: F has linkage and is VLA
	// int (*s)[m]; // valid: auto pointer to VLA
	// extern int (*r)[m]; // invalid: r has linkage and points to VLA
	// static int (*q)[m] = &B; // valid: q is a static block pointer to VLA
	// }
	public void test6_7_5_2s10() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.7.5.3-16) */
	// int f(void), *fip(), (*pfi)();
	public void test6_7_5_3s16() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.5.3-18) */
	// int (*apfi[3])(int *x, int *y);
	public void test6_7_5_3s18() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.5.3-19) */
	// int (*fpfi(int (*)(long), int))(int, ...);
	public void test6_7_5_3s19() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.5.3-20) */
	// void addscalar(int n, int m,
	// double a[n][n*m+300], double x);
	// int main()
	// {
	// double b[4][308];
	// addscalar(4, 2, b, 2.17);
	// return 0;
	// }
	// void addscalar(int n, int m,
	// double a[n][n*m+300], double x)
	// {
	// for (int i = 0; i < n; i++)
	// for (int j = 0, k = n*m+300; j < k; j++)
	// // a is a pointer to a VLA with n*m+300 elements
	// a[i][j] += x;
	// }
	public void test6_7_5_3s20() throws Exception {
		String code = getAboveComment();
		// no valid c++ code
		parse(code, ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.5.3-21) */
	// double maximum1(int n, int m, double a[n][m]);
	// double maximum2(int n, int m, double a[*][*]);
	// double maximum3(int n, int m, double a[ ][*]);
	// double maximum4(int n, int m, double a[ ][m]);
	// void f1(double (* restrict a)[5]);
	// void f2(double a[restrict][5]);
	// void f3(double a[restrict 3][5]);
	// void f4(double a[restrict static 3][5]);
	public void test6_7_5_3s21() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.7-4) */
	// typedef int MILES, KLICKSP();
	// typedef struct { double hi, lo; } range;
	// MILES distance;
	// extern KLICKSP *metricp;
	// range x;
	// range z, *zp;
	public void test6_7_7s4() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.7-5) */
	// typedef struct s1 { int x; } t1, *tp1;
	// typedef struct s2 { int x; } t2, *tp2;
	public void test6_7_7s5() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.7-7) */
	// typedef void fv(int), (*pfv)(int);
	// void (*signal(int, void (*)(int)))(int);
	// fv *signal(int, fv *);
	// pfv signal(int, pfv);
	public void test6_7_7s7() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.7-8) */
	// void copyt(int n)
	// {
	// typedef int B[n]; // B is n ints, n evaluated now
	// n += 1;
	// B a; // ais n ints, n without += 1
	// int b[n]; // a and b are different sizes
	// for (int i = 1; i < n; i++)
	// a[i-1] = b[i];
	// }
	public void test6_7_7s8() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-24) */
	// int i = 3.5;
	// complex c = 5 + 3 * I;
	public void test6_7_8s24() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.7.8-25) */
	// int x[] = { 1, 3, 5 };
	public void test6_7_8s25() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-26a) */
	// int y[4][3] = {
	// { 1, 3, 5 },
	// { 2, 4, 6 },
	// { 3, 5, 7 },
	// };
	public void test6_7_8s26a() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-26b) */
	// int y[4][3] = {
	// 1, 3, 5, 2, 4, 6, 3, 5, 7
	// };
	public void test6_7_8s26b() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-27) */
	// int z[4][3] = {
	// { 1 }, { 2 }, { 3 }, { 4 }
	// };
	public void test6_7_8s27() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-28) */
	// struct { int a[3], b; } w[] = { { 1 }, 2 };
	public void test6_7_8s28() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-29) */
	// short q1[4][3][2] = {
	// { 1 },
	// { 2, 3 },
	// { 4, 5, 6 }
	// };
	// short q2[4][3][2] = {
	// 1, 0, 0, 0, 0, 0,
	// 2, 3, 0, 0, 0, 0,
	// 4, 5, 6
	// };
	// short q3[4][3][2] = {
	// {
	// { 1 },
	// },
	// {
	// { 2, 3 },
	// },
	// {
	// { 4, 5 },
	// { 6 },
	// }
	// };
	public void test6_7_8s29() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-31) */
	// typedef int A[]; // OK - declared with block scope
	// A a1 = { 1, 2 }, b1 = { 3, 4, 5 };
	// int a2[] = { 1, 2 }, b2[] = { 3, 4, 5 };
	public void test6_7_8s31() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-32) */
	// int foo() {
	// char s1[] = "abc", t1[3] = "abc";
	// char s2[] = { 'a', 'b', 'c', '\0' },
	// t2[] = { 'a', 'b', 'c' };
	// char *p = "abc";
	// }
	public void test6_7_8s32() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.7.8-33) */
	// enum { member_one, member_two };
	// const char *nm[] = {
	// [member_two] = "member two",
	// [member_one] = "member one",
	// };
	public void test6_7_8s33() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.8-34) */
	// div_t answer = { .quot = 2, .rem = -1 };
	public void test6_7_8s34() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 1); // div_t (correctly) cannot be resolved
	}

	// /* Start Example(C 6.7.8-35) */
	// struct { int a[3], b; } w[] =
	// { [0].a = {1}, [1].a[0] = 2 };
	public void test6_7_8s35() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.8-36) */
	// int MAX=15;
	// int a[MAX] = {
	// 1, 3, 5, 7, 9, [MAX-5] = 8, 6, 4, 2, 0
	// };
	public void test6_7_8s36() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.7.8-38) */
	// union { int any_member; } u = { .any_member = 42 };
	public void test6_7_8s38() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.8.3-4) */
	// int p(int);
	// int f() {
	// (void)p(0);
	// }
	public void test6_8_3s4() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.8.3-5) */
	// int f() {
	// char *s;
	// while (*s++ != '\0')
	// ;
	// }
	public void test6_8_3s5() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.8.3-6) */
	// int f() {
	// int i=1;
	// while (i) {
	// while (1) {
	// i=0;
	// if (1)
	// goto end_loop1;
	// }
	// end_loop1: ;
	// }
	// }
	public void test6_8_3s6() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.8.4-7) */
	// int f(int a) {}
	// int g(int expr) {
	// switch (expr)
	// {
	// int i = 4;
	// f(i);
	// case 0:
	// i = 17;
	// default:
	// f(i+1);
	// }
	// }
	public void test6_8_4s7() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.8.6.1-3) */
	// int f() {
	// goto first_time;
	// for (;;) {
	// // determine next operation
	// if (1) {
	// // reinitialize-only code
	// first_time:
	// // general initialization code
	// continue;
	// }
	// // handle other operations
	// }
	// }
	public void test6_8_6_1s3() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.8.6.1-4) */
	// int f() {
	// goto lab3; // invalid: going INTO scope of VLA.
	// {
	// double a[n];
	// a[j] = 4.4;
	// lab3:
	// a[j] = 3.3;
	// goto lab4; // valid: going WITHIN scope of VLA.
	// a[j] = 5.5;
	// lab4:
	// a[j] = 6.6;
	// }
	// goto lab4; // invalid: going INTO scope of VLA.
	// }
	public void test6_8_6_1s4() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.8.6.4-4) */
	// struct s { double i; } f(void);
	// union {
	// struct {
	// int f1;
	// struct s f2;
	// } u1;
	// struct {
	// struct s f3;
	// int f4;
	// } u2;
	// } g;
	// struct s f(void)
	// {
	// return g.u1.f2;
	// }
	// int foo() {
	// g.u2.f3 = f();
	// }
	public void test6_8_6_4s4() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}

	// /* Start Example(C 6.9.1-13) */
	// extern int max(int a, int b)
	// {
	// return a > b ? a : b;
	// }
	public void test6_9_1s13() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.9.1-14) */
	// void g(int (*funcp)(void))
	// {
	// (*funcp)();
	// funcp();
	// }
	public void test6_9_1s14() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.9.2-4) */
	// int i1 = 1; // definition, external linkage
	// static int i2 = 2; // definition, internal linkage
	// extern int i3 = 3; // definition, external linkage
	// int i4; // tentative definition, external linkage
	// static int i5; // tentative definition, internal linkage
	// int i1; // valid tentative definition, refers to pre vious
	//int i2; // 6.2.2 renders undefined, linkage disagreement
	// int i3; // valid tentative definition, refers to pre vious
	// int i4; // valid tentative definition, refers to pre vious
	//int i5; // 6.2.2 renders undefined, linkage disagreement
	// extern int i1; // refers to pre vious, whose linkage is external
	// extern int i2; // refers to pre vious, whose linkage is internal
	// extern int i3; // refers to pre vious, whose linkage is external
	// extern int i4; // refers to pre vious, whose linkage is external
	// extern int i5; // refers to pre vious, whose linkage is internal
	public void test6_9_2s4() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.10.1-4) */
	// int g() {}
	// int f() {
	// #if 'z' - 'a' == 25
	// g();
	// #endif
	// if ('z' - 'a' == 25)
	// g();
	// }
	public void test6_10_1s4() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.10.3.3-4) */
	// #define hash_hash # ## #
	// #define mkstr(a) # a
	// #define in_between(a) mkstr(a)
	// #define join(c, d) in_between(c hash_hash d)
	// char p[] = join(x, y); // equivalent to
	// char p[] = "x ## y";
	public void test6_10_3_3s4() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.10.3.5-3) */
	// #define TABSIZE 100
	// int table[TABSIZE];
	public void test6_10_3_5s3() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.10.3.5-4) */
	// #define max(a, b) ((a) > (b) ? (a) : (b))
	public void test6_10_3_5s4() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.10.3.5-5) */
	// #define x 3
	// #define f(a) f(x * (a))
	// #undef x
	// #define x 2
	// #define g f
	// #define z z[0]
	// #define h g(~
	// #define m(a) a(w)
	// #define w 0,1
	// #define t(a) a
	// #define p() int
	// #define q(x) x
	// #define r(x,y) x ## y
	// #define str(x) # x
	// int foo() {
	// p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) };
	// char c[2][6] = { str(hello), str() };
	// }
	public void test6_10_3_5s5() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.10.3.5-6) */
	// #define str(s) # s
	// #define xstr(s) str(s)
	// #define debug(s, t) printf("x" # s "= %d, x" # t "= %s", \
	// x ## s, x ## t)
	// #define INCFILE(n) vers ## n
	// #define glue(a, b) a ## b
	// #define xglue(a, b) glue(a, b)
	// #define HIGHLOW "hello"
	// #define LOW LOW ", world"
	// void printf( char *, ...);
	// void fputs( char *, ... );
	// int x1, x2, s;
	// int f() {
	// debug(1, 2);
	// fputs(str(strncmp("abc\0d", "abc", '\4') // this goes away
	// == 0) str(: @\n), s);
	// char * c = glue(HIGH, LOW);
	// c = xglue(HIGH, LOW);
	// }
	public void test6_10_3_5s6() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.10.3.5-7) */
	// #define t(x,y,z) x ## y ## z
	// int j[] = { t(1,2,3), t(,4,5), t(6,,7), t(8,9,),
	// t(10,,), t(,11,), t(,,12), t(,,) };
	public void test6_10_3_5s7() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.10.3.5-8) */
	// #define OBJ_LIKE1 (1-1)
	// #define OBJ_LIKE2    \
	//     	(1-1)       \
	// #define FUNC_LIKE1(a) ( a )
	// #define FUNC_LIKE2( a )(              \
	//             	a                    \
	//                   	)
	public void test6_10_3_5s8() throws Exception {
		parseCandCPP(getAboveComment(), true, 0);
	}

	// /* Start Example(C 6.10.3.5-9) */
	// #define debug(...) fprintf(stderr, __VA_ARGS__)
	// #define showlist(...) puts(#__VA_ARGS__)
	// #define report(test, ...) ((test)?puts(#test):\
	// printf(__VA_ARGS__))
	// int f() {
	// debug("Flag");
	// debug("X = %d\n", x);
	// showlist(The first, second, and third items.);
	// report(x>y, "x is %d but y is %d", x, y);
	// }
	public void test6_10_3_5s9() throws Exception {
		parseCandCPP(getAboveComment(), false, 0);
	}

	// /* Start Example(C 6.7.7-6) */
	// typedef signed int t;
	// typedef int plain;
	// struct tag {
	// unsigned t:4;
	// const t:5;
	// plain r:5;
	// };
	// t f(t (t));
	// long t;
	public void test6_7_7s6() throws Exception {
		parse(getAboveComment(), ParserLanguage.C, true, 0);
	}
}
