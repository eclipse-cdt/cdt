/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author dsteffle
 */
public class AST2CSpecTest extends AST2SpecBaseTest {

	/**
	 [--Start Example(C 4-6):
	#ifdef _ _STDC_IEC_559_ _ // FE_UPWARD defined 
	fesetround(FE_UPWARD);
	#endif
	 --End Example]
	 */
	public void test4s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifdef _ _STDC_IEC_559_ _ /* FE_UPWARD defined */\n"); //$NON-NLS-1$
		buffer.append("fesetround(FE_UPWARD);\n"); //$NON-NLS-1$
		buffer.append("#endif\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 5.1.1.3-2):
	char i;
	int i;
	 --End Example]
	 */
	public void test5_1_1_3s2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("char i;\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 5.1.2.3-10):
	int f() {
	char c1, c2;
	c1 = c1 + c2;
	}
	 --End Example]
	 */
	public void test5_1_2_3s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("char c1, c2;\n"); //$NON-NLS-1$
		buffer.append("c1 = c1 + c2;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 5.1.2.3-11):
	int f() {
	float f1, f2;
	double d;
	f1 = f2 * d;
	}
	 --End Example]
	 */
	public void test5_1_2_3s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("float f1, f2;\n"); //$NON-NLS-1$
		buffer.append("double d;\n"); //$NON-NLS-1$
		buffer.append("f1 = f2 * d;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 5.1.2.3-12):
	int f() {
	double d1, d2;
	float f;
	d1 = f = 1;
	d2 = (float) 1;
	}
	 --End Example]
	 */
	public void test5_1_2_3s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("double d1, d2;\n"); //$NON-NLS-1$
		buffer.append("float f;\n"); //$NON-NLS-1$
		buffer.append("d1 = f = 1;\n"); //$NON-NLS-1$
		buffer.append("d2 = (float) 1;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$		
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 5.1.2.3-13):
	int f() {
	double x, y, z;
	x = (x * y) * z; // not equivalent tox *= y * z;
	z = (x - y) + y ; // not equivalent toz = x;
	z = x + x * y; // not equivalent toz = x * (1.0 + y);
	y = x / 5.0; // not equivalent toy = x * 0.2;
	}
	 --End Example]
	 */
	public void test5_1_2_3s13() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("double x, y, z;\n"); //$NON-NLS-1$
		buffer.append("x = (x * y) * z; // not equivalent tox *= y * z;\n"); //$NON-NLS-1$
		buffer.append("z = (x - y) + y ; // not equivalent toz = x;\n"); //$NON-NLS-1$
		buffer.append("z = x + x * y; // not equivalent toz = x * (1.0 + y);\n"); //$NON-NLS-1$
		buffer.append("y = x / 5.0; // not equivalent toy = x * 0.2;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 5.1.2.3-14):
	int f() {
	int a, b;
	a = a + 32760 + b + 5;
	a = (((a + 32760) + b) + 5);
	a = ((a + b) + 32765);
	a = ((a + 32765) + b);
	a = (a + (b + 32765));
	}
	 --End Example]
	 */
	public void test5_1_2_3s14() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("int a, b;\n"); //$NON-NLS-1$
		buffer.append("a = a + 32760 + b + 5;\n"); //$NON-NLS-1$
		buffer.append("a = (((a + 32760) + b) + 5);\n"); //$NON-NLS-1$
		buffer.append("a = ((a + b) + 32765);\n"); //$NON-NLS-1$
		buffer.append("a = ((a + 32765) + b);\n"); //$NON-NLS-1$
		buffer.append("a = (a + (b + 32765));\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 5.1.2.3-15):
	//#include <stdio.h>
	int f() {
	int sum;
	char *p;
	sum = sum * 10 - '0' + (*p++ = getchar());
	sum = (((sum * 10) - '0') + ((*(p++)) = (getchar())));
	}
	 --End Example]
	 */
	public void test5_1_2_3s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("//#include <stdio.h>\n"); //$NON-NLS-1$
		buffer.append("int sum;\n"); //$NON-NLS-1$
		buffer.append("char *p;\n"); //$NON-NLS-1$
		buffer.append("sum = sum * 10 - '0' + (*p++ = getchar());\n"); //$NON-NLS-1$
		buffer.append("sum = (((sum * 10) - '0') + ((*(p++)) = (getchar())));\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.2.5-28):
	struct tag (* a[5])(float);
	 --End Example]
	 */
	public void test6_2_5s28() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct tag (* a[5])(float);\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.2.7-5):
	int f(int (*)(), double (*)[3]);
	int f(int (*)(char *), double (*)[]);
	int f(int (*)(char *), double (*)[3]);
	 --End Example]
	 */
	public void test6_2_7s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(int (*)(), double (*)[3]);\n"); //$NON-NLS-1$
		buffer.append("int f(int (*)(char *), double (*)[]);\n"); //$NON-NLS-1$
		buffer.append("int f(int (*)(char *), double (*)[3]);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.4.4.4-12):
	char x='\023';
	char y='\0';
	char z='\x13';
	 --End Example]
	 */
	public void test6_4_4_4s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("char x='\\023';\n"); //$NON-NLS-1$
		buffer.append("char y='\\0';\n"); //$NON-NLS-1$
		buffer.append("char z='\\x13';\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.2-12):
	int f1() {}
	int f2() {}
	int f3() {}
	int f4() {}
	int (*pf[5])(int a, int b);
	int foo() {
	int x=(*pf[f1()]) (f2(), f3() + f4());
	}
	 --End Example]
	 */
	public void test6_5_2_2s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f1() {}\n"); //$NON-NLS-1$
		buffer.append("int f2() {}\n"); //$NON-NLS-1$
		buffer.append("int f3() {}\n"); //$NON-NLS-1$
		buffer.append("int f4() {}\n"); //$NON-NLS-1$
		buffer.append("int (*pf[5])(int a, int b);\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("int x=(*pf[f1()]) (f2(), f3() + f4());\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.3-7):
	struct s { int i; const int ci; };
	struct s s;
	const struct s cs;
	volatile struct s vs;
	int f() {
	s.i; // int
	s.ci; // const int
	cs.i; // const int
	cs.ci; // const int
	vs.i; // volatile int
	vs.ci; // volatile const int
	}
	 --End Example]
	 */
	public void test6_5_2_3s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s { int i; const int ci; };\n"); //$NON-NLS-1$
		buffer.append("struct s s;\n"); //$NON-NLS-1$
		buffer.append("const struct s cs;\n"); //$NON-NLS-1$
		buffer.append("volatile struct s vs;\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("s.i; // int\n"); //$NON-NLS-1$
		buffer.append("s.ci; // const int\n"); //$NON-NLS-1$
		buffer.append("cs.i; // const int\n"); //$NON-NLS-1$
		buffer.append("cs.ci; // const int\n"); //$NON-NLS-1$
		buffer.append("vs.i; // volatile int\n"); //$NON-NLS-1$
		buffer.append("vs.ci; // volatile const int\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.3-8a):
	union {
	struct {
	int alltypes;
	} n;
	struct {
	int type;
	int intnode;
	} ni;
	struct {
	int type;
	double doublenode;
	} nf;
	} u;
	int f() {
	u.nf.type = 1;
	u.nf.doublenode = 3.14;
	if (u.n.alltypes == 1)
	return 0;
	if (sin(u.nf.doublenode) == 0.0)
	return 0;
	}
	 --End Example]
	 */
	public void test6_5_2_3s8a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("union {\n"); //$NON-NLS-1$
		buffer.append("struct {\n"); //$NON-NLS-1$
		buffer.append("int alltypes;\n"); //$NON-NLS-1$
		buffer.append("} n;\n"); //$NON-NLS-1$
		buffer.append("struct {\n"); //$NON-NLS-1$
		buffer.append("int type;\n"); //$NON-NLS-1$
		buffer.append("int intnode;\n"); //$NON-NLS-1$
		buffer.append("} ni;\n"); //$NON-NLS-1$
		buffer.append("struct {\n"); //$NON-NLS-1$
		buffer.append("int type;\n"); //$NON-NLS-1$
		buffer.append("double doublenode;\n"); //$NON-NLS-1$
		buffer.append("} nf;\n"); //$NON-NLS-1$
		buffer.append("} u;\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("u.nf.type = 1;\n"); //$NON-NLS-1$
		buffer.append("u.nf.doublenode = 3.14;\n"); //$NON-NLS-1$
		buffer.append("if (u.n.alltypes == 1)\n"); //$NON-NLS-1$
		buffer.append("return 0;\n"); //$NON-NLS-1$
		buffer.append("if (sin(u.nf.doublenode) == 0.0)\n"); //$NON-NLS-1$
		buffer.append("return 0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.3-8b):
	struct t1 { int m; };
	struct t2 { int m; };
	int f(struct t1 * p1, struct t2 * p2)
	{
	if (p1->m < 0)
	p2->m = -p2->m;
	return p1->m;
	}
	int g()
	{
	union {
	struct t1 s1;
	struct t2 s2;
	} u;
	return f(&u.s1, &u.s2);
	}
	 --End Example]
	 */
	public void test6_5_2_3s8b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct t1 { int m; };\n"); //$NON-NLS-1$
		buffer.append("struct t2 { int m; };\n"); //$NON-NLS-1$
		buffer.append("int f(struct t1 * p1, struct t2 * p2)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("if (p1->m < 0)\n"); //$NON-NLS-1$
		buffer.append("p2->m = -p2->m;\n"); //$NON-NLS-1$
		buffer.append("return p1->m;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int g()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("union {\n"); //$NON-NLS-1$
		buffer.append("struct t1 s1;\n"); //$NON-NLS-1$
		buffer.append("struct t2 s2;\n"); //$NON-NLS-1$
		buffer.append("} u;\n"); //$NON-NLS-1$
		buffer.append("return f(&u.s1, &u.s2);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.5-9):
	int *p = (int []){2, 4};
	 --End Example]
	 */
	public void test6_5_2_5s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int *p = (int []){2, 4};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.5-10):
	void f(void)
	{
	int *p;
	p = (int [2]){*p};
	}
	 --End Example]
	 */
	public void test6_5_2_5s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f(void)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int *p;\n"); //$NON-NLS-1$
		buffer.append("p = (int [2]){*p};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.5-11):
	int f(){
	drawline((struct point){.x=1, .y=1},
	(struct point){.x=3, .y=4});
	drawline(&(struct point){.x=1, .y=1},
	&(struct point){.x=3, .y=4});
	}
	 --End Example]
	 */
	public void test6_5_2_5s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(){\n"); //$NON-NLS-1$
		buffer.append("drawline((struct point){.x=1, .y=1},\n"); //$NON-NLS-1$
		buffer.append("(struct point){.x=3, .y=4});\n"); //$NON-NLS-1$
		buffer.append("drawline(&(struct point){.x=1, .y=1},\n"); //$NON-NLS-1$
		buffer.append("&(struct point){.x=3, .y=4});\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, false, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.5-12):
	int f() {
	(const float []){1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6};
	}
	 --End Example]
	 */
	public void test6_5_2_5s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("(const float []){1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.5-13):
	int f() {
	"/tmp/fileXXXXXX";
	(char []){"/tmp/fileXXXXXX"};
	(const char []){"/tmp/fileXXXXXX"};
	}
	 --End Example]
	 */
	public void test6_5_2_5s13() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("\"/tmp/fileXXXXXX\";\n"); //$NON-NLS-1$
		buffer.append("(char []){\"/tmp/fileXXXXXX\"};\n"); //$NON-NLS-1$
		buffer.append("(const char []){\"/tmp/fileXXXXXX\"};\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.5-14):
	int f() {
	(const char []){"abc"} == "abc";
	}
	 --End Example]
	 */
	public void test6_5_2_5s14() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("(const char []){\"abc\"} == \"abc\";\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.5-15):
	int f() {
	struct int_list { int car; struct int_list *cdr; };
	struct int_list endless_zeros = {0, &endless_zeros};
	eval(endless_zeros);
	}
	 --End Example]
	 */
	public void test6_5_2_5s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("struct int_list { int car; struct int_list *cdr; };\n"); //$NON-NLS-1$
		buffer.append("struct int_list endless_zeros = {0, &endless_zeros};\n"); //$NON-NLS-1$
		buffer.append("eval(endless_zeros);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.5.2.5-16):
	struct s { int i; };
	int f (void)
	{
	struct s *p = 0, *q;
	int j = 0;
	again:
	q = p, p = &((struct s){ j++ });
	if (j < 2) goto again;
	return p == q && q->i == 1;
	}
	 --End Example]
	 */
	public void test6_5_2_5s16() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s { int i; };\n"); //$NON-NLS-1$
		buffer.append("int f (void)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("struct s *p = 0, *q;\n"); //$NON-NLS-1$
		buffer.append("int j = 0;\n"); //$NON-NLS-1$
		buffer.append("again:\n"); //$NON-NLS-1$
		buffer.append("q = p, p = &((struct s){ j++ });\n"); //$NON-NLS-1$
		buffer.append("if (j < 2) goto again;\n"); //$NON-NLS-1$
		buffer.append("return p == q && q->i == 1;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.3.4-5):
	extern void *alloc(size_t);
	double *dp = alloc(sizeof *dp);
	 --End Example]
	 */
	public void test6_5_3_4s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern void *alloc(size_t);\n"); //$NON-NLS-1$
		buffer.append("double *dp = alloc(sizeof *dp);\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.5.3.4-6):
	int f() {
	int array[5];
	int x = sizeof array / sizeof array[0];
	}
	 --End Example]
	 */
	public void test6_5_3_4s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("int array[5];\n"); //$NON-NLS-1$
		buffer.append("int x = sizeof array / sizeof array[0];\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.6-10):
	int f() {
	int n = 4, m = 3;
	int a[n][m];
	int (*p)[m] = a; // p == &a[0]
	p += 1; // p == &a[1]
	(*p)[2] = 99; // a[1][2] == 99
	n = p - a; // n == 1
	}
	 --End Example]
	 */
	public void test6_5_6s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("int n = 4, m = 3;\n"); //$NON-NLS-1$
		buffer.append("int a[n][m];\n"); //$NON-NLS-1$
		buffer.append("int (*p)[m] = a; // p == &a[0]\n"); //$NON-NLS-1$
		buffer.append("p += 1; // p == &a[1]\n"); //$NON-NLS-1$
		buffer.append("(*p)[2] = 99; // a[1][2] == 99\n"); //$NON-NLS-1$
		buffer.append("n = p - a; // n == 1\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.15-8):
	int f() {
	const void *c_vp;
	void *vp;
	const int *c_ip;
	volatile int *v_ip;
	int *ip;
	const char *c_cp;
	}
	 --End Example]
	 */
	public void test6_5_15s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("const void *c_vp;\n"); //$NON-NLS-1$
		buffer.append("void *vp;\n"); //$NON-NLS-1$
		buffer.append("const int *c_ip;\n"); //$NON-NLS-1$
		buffer.append("volatile int *v_ip;\n"); //$NON-NLS-1$
		buffer.append("int *ip;\n"); //$NON-NLS-1$
		buffer.append("const char *c_cp;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.16.1-5):
	int f() {
	char c;
	int i;
	long l;
	l = (c = i);
	}
	 --End Example]
	 */
	public void test6_5_16_1s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("char c;\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("long l;\n"); //$NON-NLS-1$
		buffer.append("l = (c = i);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.5.16.1-6):
	int f() {
	const char **cpp;
	char *p;
	const char c = 'A';
	cpp = &p; // constraint violation
	*cpp = &c; // valid
	*p = 0; // valid
	}
	 --End Example]
	 */
	public void test6_5_16_1s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("const char **cpp;\n"); //$NON-NLS-1$
		buffer.append("char *p;\n"); //$NON-NLS-1$
		buffer.append("const char c = 'A';\n"); //$NON-NLS-1$
		buffer.append("cpp = &p; // constraint violation\n"); //$NON-NLS-1$
		buffer.append("*cpp = &c; // valid\n"); //$NON-NLS-1$
		buffer.append("*p = 0; // valid\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.2.1-17):
	struct s { int n; double d[]; };
	struct ss { int n; double d[1]; };
	int f() {
	sizeof (struct s);
	offsetof(struct s, d);
	offsetof(struct ss, d);
	}
	 --End Example]
	 */
	public void test6_7_2_1s17() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s { int n; double d[]; };\n"); //$NON-NLS-1$
		buffer.append("struct ss { int n; double d[1]; };\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("sizeof (struct s);\n"); //$NON-NLS-1$
		buffer.append("offsetof(struct s, d);\n"); //$NON-NLS-1$
		buffer.append("offsetof(struct ss, d);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.7.2.1-18a):
	int f() {
	struct s *s1;
	struct s *s2;
	s1 = malloc(sizeof (struct s) + 64);
	s2 = malloc(sizeof (struct s) + 46);
	}
	 --End Example]
	 */
	public void test6_7_2_1s18a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("struct s *s1;\n"); //$NON-NLS-1$
		buffer.append("struct s *s2;\n"); //$NON-NLS-1$
		buffer.append("s1 = malloc(sizeof (struct s) + 64);\n"); //$NON-NLS-1$
		buffer.append("s2 = malloc(sizeof (struct s) + 46);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.7.2.1-18b):
	struct { int n; double d[8]; } *s1;
	struct { int n; double d[5]; } *s2;
	 --End Example]
	 */
	public void test6_7_2_1s18b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct { int n; double d[8]; } *s1;\n"); //$NON-NLS-1$
		buffer.append("struct { int n; double d[5]; } *s2;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.2.2-5):
	int f() {
	enum hue { chartreuse, burgundy, claret=20, winedark };
	enum hue col, *cp;
	col = claret;
	cp = &col;
	if (*cp != burgundy)
	return 0;
	}
	 --End Example]
	 */
	public void test6_7_2_2s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("enum hue { chartreuse, burgundy, claret=20, winedark };\n"); //$NON-NLS-1$
		buffer.append("enum hue col, *cp;\n"); //$NON-NLS-1$
		buffer.append("col = claret;\n"); //$NON-NLS-1$
		buffer.append("cp = &col;\n"); //$NON-NLS-1$
		buffer.append("if (*cp != burgundy)\n"); //$NON-NLS-1$
		buffer.append("return 0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.2.3-9):
	struct tnode {
	int count;
	struct tnode *left, *right;
	};
	struct tnode s, *sp;
	 --End Example]
	 */
	public void test6_7_2_3s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct tnode {\n"); //$NON-NLS-1$
		buffer.append("int count;\n"); //$NON-NLS-1$
		buffer.append("struct tnode *left, *right;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("struct tnode s, *sp;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.2.3-10):
	typedef struct tnode TNODE;
	struct tnode {
	int count;
	TNODE *left, *right;
	};
	TNODE s, *sp;
	 --End Example]
	 */
	public void test6_7_2_3s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef struct tnode TNODE;\n"); //$NON-NLS-1$
		buffer.append("struct tnode {\n"); //$NON-NLS-1$
		buffer.append("int count;\n"); //$NON-NLS-1$
		buffer.append("TNODE *left, *right;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("TNODE s, *sp;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.2.3-11):
	struct s2;
	struct s1 { struct s2 *s2p; }; // D1
	struct s2 { struct s1 *s1p; }; // D2
	 --End Example]
	 */
	public void test6_7_2_3s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s2;\n"); //$NON-NLS-1$
		buffer.append("struct s1 { struct s2 *s2p; }; // D1\n"); //$NON-NLS-1$
		buffer.append("struct s2 { struct s1 *s1p; }; // D2\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.3-10):
		extern const volatile int real_time_clock;
	 --End Example]
	 */
	public void test6_7_3s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern const volatile int real_time_clock;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.3-11):
	int f() {
	const struct s { int mem; } cs = { 1 };
	struct s ncs; // the object ncs is modifiable
	typedef int A[2][3];
	const A a = {{4, 5, 6}, {7, 8, 9}}; // array of array of const int
	int *pi;
	const int *pci;
	ncs = cs; // valid
	cs = ncs; // violates modifiable lvalue constraint for =
	pi = &ncs.mem; // valid
	pi = &cs.mem; // violates type constraints for =
	pci = &cs.mem; // valid
	pi = a[0]; // invalid: a[0] has type ‘‘const int *’’
	}
	 --End Example]
	 */
	public void test6_7_3s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("const struct s { int mem; } cs = { 1 };\n"); //$NON-NLS-1$
		buffer.append("struct s ncs; // the object ncs is modifiable\n"); //$NON-NLS-1$
		buffer.append("typedef int A[2][3];\n"); //$NON-NLS-1$
		buffer.append("const A a = {{4, 5, 6}, {7, 8, 9}}; // array of array of const int\n"); //$NON-NLS-1$
		buffer.append("int *pi;\n"); //$NON-NLS-1$
		buffer.append("const int *pci;\n"); //$NON-NLS-1$
		buffer.append("ncs = cs; // valid\n"); //$NON-NLS-1$
		buffer.append("cs = ncs; // violates modifiable lvalue constraint for =\n"); //$NON-NLS-1$
		buffer.append("pi = &ncs.mem; // valid\n"); //$NON-NLS-1$
		buffer.append("pi = &cs.mem; // violates type constraints for =\n"); //$NON-NLS-1$
		buffer.append("pci = &cs.mem; // valid\n"); //$NON-NLS-1$
		buffer.append("pi = a[0]; // invalid: a[0] has type ‘‘const int *’’\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.3.1-7):
	int * restrict a;
	int * restrict b;
	extern int c[];
	 --End Example]
	 */
	public void test6_7_3_1s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int * restrict a;\n"); //$NON-NLS-1$
		buffer.append("int * restrict b;\n"); //$NON-NLS-1$
		buffer.append("extern int c[];\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.3.1-8):
	void f(int n, int * restrict p, int * restrict q)
	{
	while (n-- > 0)
	*p++ = *q++;
	}
	 --End Example]
	 */
	public void test6_7_3_1s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f(int n, int * restrict p, int * restrict q)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("while (n-- > 0)\n"); //$NON-NLS-1$
		buffer.append("*p++ = *q++;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.3.1-9):
	void f(int n, int * restrict p, int * restrict q)
	{
	while (n-- > 0)
	*p++ = *q++;
	}
	void g(void)
	{
	extern int d[100];
	f(50, d + 50, d); // valid
	f(50, d + 1, d); // undefined behavior
	}
	 --End Example]
	 */
	public void test6_7_3_1s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void f(int n, int * restrict p, int * restrict q)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("while (n-- > 0)\n"); //$NON-NLS-1$
		buffer.append("*p++ = *q++;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void g(void)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("extern int d[100];\n"); //$NON-NLS-1$
		buffer.append("f(50, d + 50, d); // valid\n"); //$NON-NLS-1$
		buffer.append("f(50, d + 1, d); // undefined behavior\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.3.1-10):
	void h(int n, int * restrict p, int * restrict q, int * restrict r)
	{
	int i;
	for (i = 0; i < n; i++)
	p[i] = q[i] + r[i];
	}
	 --End Example]
	 */
	public void test6_7_3_1s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void h(int n, int * restrict p, int * restrict q, int * restrict r)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i;\n"); //$NON-NLS-1$
		buffer.append("for (i = 0; i < n; i++)\n"); //$NON-NLS-1$
		buffer.append("p[i] = q[i] + r[i];\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.3.1-11):
	int f()
	{
	int * restrict p1;
	int * restrict q1;
	p1 = q1; // undefined behavior
	{
	int * restrict p2 = p1; // valid
	int * restrict q2 = q1; // valid
	p1 = q2; // undefined behavior
	p2 = q2; // undefined behavior
	}
	}
	 --End Example]
	 */
	public void test6_7_3_1s11() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int * restrict p1;\n"); //$NON-NLS-1$
		buffer.append("int * restrict q1;\n"); //$NON-NLS-1$
		buffer.append("p1 = q1; // undefined behavior\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int * restrict p2 = p1; // valid\n"); //$NON-NLS-1$
		buffer.append("int * restrict q2 = q1; // valid\n"); //$NON-NLS-1$
		buffer.append("p1 = q2; // undefined behavior\n"); //$NON-NLS-1$
		buffer.append("p2 = q2; // undefined behavior\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.3.1-12):
	typedef struct { int n; float * restrict v; } vector;
	vector new_vector(int n)
	{
	vector t;
	t.n = n;
	t.v = malloc(n * sizeof (float));
	return t;
	}
	 --End Example]
	 */
	public void test6_7_3_1s12() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef struct { int n; float * restrict v; } vector;\n"); //$NON-NLS-1$
		buffer.append("vector new_vector(int n)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("vector t;\n"); //$NON-NLS-1$
		buffer.append("t.n = n;\n"); //$NON-NLS-1$
		buffer.append("t.v = malloc(n * sizeof (float));\n"); //$NON-NLS-1$
		buffer.append("return t;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, false, 0);
	}
	
	/**
	 [--Start Example(C 6.7.4-7):
	inline double fahr(double t)
	{
	return (9.0 * t) / 5.0 + 32.0;
	}
	inline double cels(double t)
	{
	return (5.0 * (t - 32.0)) / 9.0;
	}
	extern double fahr(double); // creates an external definition
	double convert(int is_fahr, double temp)
	{
	return is_fahr ? cels(temp) : fahr(temp);
	}
	 --End Example]
	 */
	public void test6_7_4s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("inline double fahr(double t)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("return (9.0 * t) / 5.0 + 32.0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("inline double cels(double t)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("return (5.0 * (t - 32.0)) / 9.0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("extern double fahr(double); // creates an external definition\n"); //$NON-NLS-1$
		buffer.append("double convert(int is_fahr, double temp)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("return is_fahr ? cels(temp) : fahr(temp);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.1-4):
	const int *ptr_to_constant;
	int *const constant_ptr1;
	typedef int *int_ptr;
	const int_ptr constant_ptr2;
	 --End Example]
	 */
	public void test6_7_5_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("const int *ptr_to_constant;\n"); //$NON-NLS-1$
		buffer.append("int *const constant_ptr1;\n"); //$NON-NLS-1$
		buffer.append("typedef int *int_ptr;\n"); //$NON-NLS-1$
		buffer.append("const int_ptr constant_ptr2;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.2-7):
	float fa[11], *afp[17];
	 --End Example]
	 */
	public void test6_7_5_2s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("float fa[11], *afp[17];\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.2-8):
	extern int *x;
	extern int y[];
	 --End Example]
	 */
	public void test6_7_5_2s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern int *x;\n"); //$NON-NLS-1$
		buffer.append("extern int y[];\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.2-9):
	extern int n;
	extern int m;
	void fcompat(void)
	{
	int a[n][6][m];
	int (*p)[4][n+1];
	int c[n][n][6][m];
	int (*r)[n][n][n+1];
	p = a; // invalid: not compatible because4 != 6
	r = c; // compatible, but defined behavior only if
	// n == 6 andm == n+1
	}
	 --End Example]
	 */
	public void test6_7_5_2s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern int n;\n"); //$NON-NLS-1$
		buffer.append("extern int m;\n"); //$NON-NLS-1$
		buffer.append("void fcompat(void)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int a[n][6][m];\n"); //$NON-NLS-1$
		buffer.append("int (*p)[4][n+1];\n"); //$NON-NLS-1$
		buffer.append("int c[n][n][6][m];\n"); //$NON-NLS-1$
		buffer.append("int (*r)[n][n][n+1];\n"); //$NON-NLS-1$
		buffer.append("p = a; // invalid: not compatible because4 != 6\n"); //$NON-NLS-1$
		buffer.append("r = c; // compatible, but defined behavior only if\n"); //$NON-NLS-1$
		buffer.append("// n == 6 andm == n+1\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.2-10):
	extern int n;
	int A[n]; // invalid: file scope VLA
	extern int (*p2)[n]; // invalid: file scope VM
	int B[100]; // valid: file scope but not VM
	void fvla(int m, int C[m][m]); // valid: VLA with prototype scope
	void fvla(int m, int C[m][m]) // valid: adjusted to auto pointer to VLA
	{
	typedef int VLA[m][m]; // valid: block scope typedef VLA
	struct tag {
	int (*y)[n]; // invalid: y not ordinary identifier
	int z[n]; // invalid: z not ordinary identifier
	};
	int D[m]; // valid: auto VLA
	static int E[m]; // invalid: static block scope VLA
	extern int F[m]; // invalid: F has linkage and is VLA
	int (*s)[m]; // valid: auto pointer to VLA
	extern int (*r)[m]; // invalid: r has linkage and points to VLA
	static int (*q)[m] = &B; // valid: q is a static block pointer to VLA
	}
	 --End Example]
	 */
	public void test6_7_5_2s10() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern int n;\n"); //$NON-NLS-1$
		buffer.append("int A[n]; // invalid: file scope VLA\n"); //$NON-NLS-1$
		buffer.append("extern int (*p2)[n]; // invalid: file scope VM\n"); //$NON-NLS-1$
		buffer.append("int B[100]; // valid: file scope but not VM\n"); //$NON-NLS-1$
		buffer.append("void fvla(int m, int C[m][m]); // valid: VLA with prototype scope\n"); //$NON-NLS-1$
		buffer.append("void fvla(int m, int C[m][m]) // valid: adjusted to auto pointer to VLA\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("typedef int VLA[m][m]; // valid: block scope typedef VLA\n"); //$NON-NLS-1$
		buffer.append("struct tag {\n"); //$NON-NLS-1$
		buffer.append("int (*y)[n]; // invalid: y not ordinary identifier\n"); //$NON-NLS-1$
		buffer.append("int z[n]; // invalid: z not ordinary identifier\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("int D[m]; // valid: auto VLA\n"); //$NON-NLS-1$
		buffer.append("static int E[m]; // invalid: static block scope VLA\n"); //$NON-NLS-1$
		buffer.append("extern int F[m]; // invalid: F has linkage and is VLA\n"); //$NON-NLS-1$
		buffer.append("int (*s)[m]; // valid: auto pointer to VLA\n"); //$NON-NLS-1$
		buffer.append("extern int (*r)[m]; // invalid: r has linkage and points to VLA\n"); //$NON-NLS-1$
		buffer.append("static int (*q)[m] = &B; // valid: q is a static block pointer to VLA\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.3-16):
	int f(void), *fip(), (*pfi)();
	 --End Example]
	 */
	public void test6_7_5_3s16() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(void), *fip(), (*pfi)();\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.3-18):
	int (*apfi[3])(int *x, int *y);
	 --End Example]
	 */
	public void test6_7_5_3s18() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int (*apfi[3])(int *x, int *y);\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.3-19):
	int (*fpfi(int (*)(long), int))(int, ...);
	 --End Example]
	 */
	public void test6_7_5_3s19() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int (*fpfi(int (*)(long), int))(int, ...);\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.3-20):
	void addscalar(int n, int m,
	double a[n][n*m+300], double x);
	int main()
	{
	double b[4][308];
	addscalar(4, 2, b, 2.17);
	return 0;
	}
	void addscalar(int n, int m,
	double a[n][n*m+300], double x)
	{
	for (int i = 0; i < n; i++)
	for (int j = 0, k = n*m+300; j < k; j++)
	// a is a pointer to a VLA with n*m+300 elements
	a[i][j] += x;
	}
	 --End Example]
	 */
	public void test6_7_5_3s20() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void addscalar(int n, int m,\n"); //$NON-NLS-1$
		buffer.append("double a[n][n*m+300], double x);\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("double b[4][308];\n"); //$NON-NLS-1$
		buffer.append("addscalar(4, 2, b, 2.17);\n"); //$NON-NLS-1$
		buffer.append("return 0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("void addscalar(int n, int m,\n"); //$NON-NLS-1$
		buffer.append("double a[n][n*m+300], double x)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("for (int i = 0; i < n; i++)\n"); //$NON-NLS-1$
		buffer.append("for (int j = 0, k = n*m+300; j < k; j++)\n"); //$NON-NLS-1$
		buffer.append("// a is a pointer to a VLA with n*m+300 elements\n"); //$NON-NLS-1$
		buffer.append("a[i][j] += x;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.5.3-21):
	double maximum1(int n, int m, double a[n][m]);
	double maximum2(int n, int m, double a[*][*]);
	double maximum3(int n, int m, double a[ ][*]);
	double maximum4(int n, int m, double a[ ][m]);
	void f1(double (* restrict a)[5]);
	void f2(double a[restrict][5]);
	void f3(double a[restrict 3][5]);
	void f4(double a[restrict static 3][5]);
	 --End Example]
	 */
	public void test6_7_5_3s21() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("double maximum1(int n, int m, double a[n][m]);\n"); //$NON-NLS-1$
		buffer.append("double maximum2(int n, int m, double a[*][*]);\n"); //$NON-NLS-1$
		buffer.append("double maximum3(int n, int m, double a[ ][*]);\n"); //$NON-NLS-1$
		buffer.append("double maximum4(int n, int m, double a[ ][m]);\n"); //$NON-NLS-1$
		buffer.append("void f1(double (* restrict a)[5]);\n"); //$NON-NLS-1$
		buffer.append("void f2(double a[restrict][5]);\n"); //$NON-NLS-1$
		buffer.append("void f3(double a[restrict 3][5]);\n"); //$NON-NLS-1$
		buffer.append("void f4(double a[restrict static 3][5]);\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.7-4):
	typedef int MILES, KLICKSP();
	typedef struct { double hi, lo; } range;
	MILES distance;
	extern KLICKSP *metricp;
	range x;
	range z, *zp;
	 --End Example]
	 */
	public void test6_7_7s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int MILES, KLICKSP();\n"); //$NON-NLS-1$
		buffer.append("typedef struct { double hi, lo; } range;\n"); //$NON-NLS-1$
		buffer.append("MILES distance;\n"); //$NON-NLS-1$
		buffer.append("extern KLICKSP *metricp;\n"); //$NON-NLS-1$
		buffer.append("range x;\n"); //$NON-NLS-1$
		buffer.append("range z, *zp;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.7-5):
	typedef struct s1 { int x; } t1, *tp1;
	typedef struct s2 { int x; } t2, *tp2;
	 --End Example]
	 */
	public void test6_7_7s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef struct s1 { int x; } t1, *tp1;\n"); //$NON-NLS-1$
		buffer.append("typedef struct s2 { int x; } t2, *tp2;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.7-6):
	typedef signed int t;
	typedef int plain;
	struct tag {
	unsigned t:4;
	const t:5;
	plain r:5;
	};
	t f(t (t));
	long t;
	 --End Example]
	 */
	public void test6_7_7s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef signed int t;\n"); //$NON-NLS-1$
		buffer.append("typedef int plain;\n"); //$NON-NLS-1$
		buffer.append("struct tag {\n"); //$NON-NLS-1$
		buffer.append("unsigned t:4;\n"); //$NON-NLS-1$
		buffer.append("const t:5;\n"); //$NON-NLS-1$
		buffer.append("plain r:5;\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("t f(t (t));\n"); //$NON-NLS-1$
		buffer.append("long t;\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.7-7):
	typedef void fv(int), (*pfv)(int);
	void (*signal(int, void (*)(int)))(int);
	fv *signal(int, fv *);
	pfv signal(int, pfv);
	 --End Example]
	 */
	public void test6_7_7s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef void fv(int), (*pfv)(int);\n"); //$NON-NLS-1$
		buffer.append("void (*signal(int, void (*)(int)))(int);\n"); //$NON-NLS-1$
		buffer.append("fv *signal(int, fv *);\n"); //$NON-NLS-1$
		buffer.append("pfv signal(int, pfv);\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.7-8):
	void copyt(int n)
	{
	typedef int B[n]; // B is n ints, n evaluated now
	n += 1;
	B a; // ais n ints, n without += 1
	int b[n]; // a and b are different sizes
	for (int i = 1; i < n; i++)
	a[i-1] = b[i];
	}
	 --End Example]
	 */
	public void test6_7_7s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void copyt(int n)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("typedef int B[n]; // B is n ints, n evaluated now\n"); //$NON-NLS-1$
		buffer.append("n += 1;\n"); //$NON-NLS-1$
		buffer.append("B a; // ais n ints, n without += 1\n"); //$NON-NLS-1$
		buffer.append("int b[n]; // a and b are different sizes\n"); //$NON-NLS-1$
		buffer.append("for (int i = 1; i < n; i++)\n"); //$NON-NLS-1$
		buffer.append("a[i-1] = b[i];\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-24):
	int i = 3.5;
	complex c = 5 + 3 * I;
	 --End Example]
	 */
	public void test6_7_8s24() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int i = 3.5;\n"); //$NON-NLS-1$
		buffer.append("complex c = 5 + 3 * I;\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-25):
	int x[] = { 1, 3, 5 };
	 --End Example]
	 */
	public void test6_7_8s25() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int x[] = { 1, 3, 5 };\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-26a):
	int y[4][3] = {
	{ 1, 3, 5 },
	{ 2, 4, 6 },
	{ 3, 5, 7 },
	};
	 --End Example]
	 */
	public void test6_7_8s26a() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int y[4][3] = {\n"); //$NON-NLS-1$
		buffer.append("{ 1, 3, 5 },\n"); //$NON-NLS-1$
		buffer.append("{ 2, 4, 6 },\n"); //$NON-NLS-1$
		buffer.append("{ 3, 5, 7 },\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-26b):
	int y[4][3] = {
	1, 3, 5, 2, 4, 6, 3, 5, 7
	};
	 --End Example]
	 */
	public void test6_7_8s26b() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int y[4][3] = {\n"); //$NON-NLS-1$
		buffer.append("1, 3, 5, 2, 4, 6, 3, 5, 7\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-27):
	int z[4][3] = {
	{ 1 }, { 2 }, { 3 }, { 4 }
	};
	 --End Example]
	 */
	public void test6_7_8s27() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int z[4][3] = {\n"); //$NON-NLS-1$
		buffer.append("{ 1 }, { 2 }, { 3 }, { 4 }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-28):
	struct { int a[3], b; } w[] = { { 1 }, 2 };
	 --End Example]
	 */
	public void test6_7_8s28() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct { int a[3], b; } w[] = { { 1 }, 2 };\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-29):
	short q1[4][3][2] = {
	{ 1 },
	{ 2, 3 },
	{ 4, 5, 6 }
	};
	short q2[4][3][2] = {
	1, 0, 0, 0, 0, 0,
	2, 3, 0, 0, 0, 0,
	4, 5, 6
	};
	short q3[4][3][2] = {
	{
	{ 1 },
	},
	{
	{ 2, 3 },
	},
	{
	{ 4, 5 },
	{ 6 },
	}
	};
	 --End Example]
	 */
	public void test6_7_8s29() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("short q1[4][3][2] = {\n"); //$NON-NLS-1$
		buffer.append("{ 1 },\n"); //$NON-NLS-1$
		buffer.append("{ 2, 3 },\n"); //$NON-NLS-1$
		buffer.append("{ 4, 5, 6 }\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("short q2[4][3][2] = {\n"); //$NON-NLS-1$
		buffer.append("1, 0, 0, 0, 0, 0,\n"); //$NON-NLS-1$
		buffer.append("2, 3, 0, 0, 0, 0,\n"); //$NON-NLS-1$
		buffer.append("4, 5, 6\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("short q3[4][3][2] = {\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("{ 1 },\n"); //$NON-NLS-1$
		buffer.append("},\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("{ 2, 3 },\n"); //$NON-NLS-1$
		buffer.append("},\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("{ 4, 5 },\n"); //$NON-NLS-1$
		buffer.append("{ 6 },\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-31):
	typedef int A[]; // OK - declared with block scope
	A a1 = { 1, 2 }, b1 = { 3, 4, 5 };
	int a2[] = { 1, 2 }, b2[] = { 3, 4, 5 };
	 --End Example]
	 */
	public void test6_7_8s31() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("typedef int A[]; // OK - declared with block scope\n"); //$NON-NLS-1$
		buffer.append("A a1 = { 1, 2 }, b1 = { 3, 4, 5 };\n"); //$NON-NLS-1$
		buffer.append("int a2[] = { 1, 2 }, b2[] = { 3, 4, 5 };\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-32):
	int foo() {
	char s1[] = "abc", t1[3] = "abc";
	char s2[] = { 'a', 'b', 'c', '\0' },
	t2[] = { 'a', 'b', 'c' };
	char *p = "abc";
	}
	 --End Example]
	 */
	public void test6_7_8s32() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("char s1[] = \"abc\", t1[3] = \"abc\";\n"); //$NON-NLS-1$
		buffer.append("char s2[] = { 'a', 'b', 'c', '\0' },\n"); //$NON-NLS-1$
		buffer.append("t2[] = { 'a', 'b', 'c' };\n"); //$NON-NLS-1$
		buffer.append("char *p = \"abc\";\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-33):
	enum { member_one, member_two };
	const char *nm[] = {
	[member_two] = "member two",
	[member_one] = "member one",
	};
	 --End Example]
	 */
	public void test6_7_8s33() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("enum { member_one, member_two };\n"); //$NON-NLS-1$
		buffer.append("const char *nm[] = {\n"); //$NON-NLS-1$
		buffer.append("[member_two] = \"member two\",\n"); //$NON-NLS-1$
		buffer.append("[member_one] = \"member one\",\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-34):
	div_t answer = { .quot = 2, .rem = -1 };
	 --End Example]
	 */
	public void test6_7_8s34() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("div_t answer = { .quot = 2, .rem = -1 };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-35):
	struct { int a[3], b; } w[] =
	{ [0].a = {1}, [1].a[0] = 2 };
	 --End Example]
	 */
	public void test6_7_8s35() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct { int a[3], b; } w[] =\n"); //$NON-NLS-1$
		buffer.append("{ [0].a = {1}, [1].a[0] = 2 };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-36):
	int MAX=15;
	int a[MAX] = {
	1, 3, 5, 7, 9, [MAX-5] = 8, 6, 4, 2, 0
	};
	 --End Example]
	 */
	public void test6_7_8s36() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int MAX=15;\n"); //$NON-NLS-1$
		buffer.append("int a[MAX] = {\n"); //$NON-NLS-1$
		buffer.append("1, 3, 5, 7, 9, [MAX-5] = 8, 6, 4, 2, 0\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.7.8-38):
	union { int any_member; } u = { .any_member = 42 };
	 --End Example]
	 */
	public void test6_7_8s38() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("union { int any_member; } u = { .any_member = 42 };\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.8.3-4):
	int p(int);
	int f() {
	(void)p(0);
	}
	 --End Example]
	 */
	public void test6_8_3s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int p(int);\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("(void)p(0);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.8.3-5):
	int f() {
	char *s;
	while (*s++ != '\0')
	;
	}
	 --End Example]
	 */
	public void test6_8_3s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("char *s;\n"); //$NON-NLS-1$
		buffer.append("while (*s++ != '\0')\n"); //$NON-NLS-1$
		buffer.append(";\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.8.3-6):
	int f() {
	int i=1;
	while (i) {
	while (1) {
	i=0;
	if (1)
	goto end_loop1;
	}
	end_loop1: ;
	}
	}
	 --End Example]
	 */
	public void test6_8_3s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("int i=1;\n"); //$NON-NLS-1$
		buffer.append("while (i) {\n"); //$NON-NLS-1$
		buffer.append("while (1) {\n"); //$NON-NLS-1$
		buffer.append("i=0;\n"); //$NON-NLS-1$
		buffer.append("if (1)\n"); //$NON-NLS-1$
		buffer.append("goto end_loop1;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("end_loop1: ;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.8.4-7):
	int f(int a) {}
	int g(int expr) {
	switch (expr)
	{
	int i = 4;
	f(i);
	case 0:
	i = 17;
	default:
	f(i+1);
	}
	}
	 --End Example]
	 */
	public void test6_8_4s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f(int a) {}\n"); //$NON-NLS-1$
		buffer.append("int g(int expr) {\n"); //$NON-NLS-1$
		buffer.append("switch (expr)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("int i = 4;\n"); //$NON-NLS-1$
		buffer.append("f(i);\n"); //$NON-NLS-1$
		buffer.append("case 0:\n"); //$NON-NLS-1$
		buffer.append("i = 17;\n"); //$NON-NLS-1$
		buffer.append("default:\n"); //$NON-NLS-1$
		buffer.append("f(i+1);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.8.6.1-3):
	int f() {
	goto first_time;
	for (;;) {
	// determine next operation
	if (1) {
	// reinitialize-only code
	first_time:
	// general initialization code
	continue;
	}
	// handle other operations
	}
	}
	 --End Example]
	 */
	public void test6_8_6_1s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("goto first_time;\n"); //$NON-NLS-1$
		buffer.append("for (;;) {\n"); //$NON-NLS-1$
		buffer.append("// determine next operation\n"); //$NON-NLS-1$
		buffer.append("if (1) {\n"); //$NON-NLS-1$
		buffer.append("// reinitialize-only code\n"); //$NON-NLS-1$
		buffer.append("first_time:\n"); //$NON-NLS-1$
		buffer.append("// general initialization code\n"); //$NON-NLS-1$
		buffer.append("continue;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("// handle other operations\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.8.6.1-4):
	int f() {
	goto lab3; // invalid: going INTO scope of VLA.
	{
	double a[n];
	a[j] = 4.4;
	lab3:
	a[j] = 3.3;
	goto lab4; // valid: going WITHIN scope of VLA.
	a[j] = 5.5;
	lab4:
	a[j] = 6.6;
	}
	goto lab4; // invalid: going INTO scope of VLA.
	}
	 --End Example]
	 */
	public void test6_8_6_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("goto lab3; // invalid: going INTO scope of VLA.\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("double a[n];\n"); //$NON-NLS-1$
		buffer.append("a[j] = 4.4;\n"); //$NON-NLS-1$
		buffer.append("lab3:\n"); //$NON-NLS-1$
		buffer.append("a[j] = 3.3;\n"); //$NON-NLS-1$
		buffer.append("goto lab4; // valid: going WITHIN scope of VLA.\n"); //$NON-NLS-1$
		buffer.append("a[j] = 5.5;\n"); //$NON-NLS-1$
		buffer.append("lab4:\n"); //$NON-NLS-1$
		buffer.append("a[j] = 6.6;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("goto lab4; // invalid: going INTO scope of VLA.\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	
	/**
	 [--Start Example(C 6.8.6.4-4):
	struct s { double i; } f(void);
	union {
	struct {
	int f1;
	struct s f2;
	} u1;
	struct {
	struct s f3;
	int f4;
	} u2;
	} g;
	struct s f(void)
	{
	return g.u1.f2;
	}
	int foo() {
	g.u2.f3 = f();
	}
	 --End Example]
	 */
	public void test6_8_6_4s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("struct s { double i; } f(void);\n"); //$NON-NLS-1$
		buffer.append("union {\n"); //$NON-NLS-1$
		buffer.append("struct {\n"); //$NON-NLS-1$
		buffer.append("int f1;\n"); //$NON-NLS-1$
		buffer.append("struct s f2;\n"); //$NON-NLS-1$
		buffer.append("} u1;\n"); //$NON-NLS-1$
		buffer.append("struct {\n"); //$NON-NLS-1$
		buffer.append("struct s f3;\n"); //$NON-NLS-1$
		buffer.append("int f4;\n"); //$NON-NLS-1$
		buffer.append("} u2;\n"); //$NON-NLS-1$
		buffer.append("} g;\n"); //$NON-NLS-1$
		buffer.append("struct s f(void)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("return g.u1.f2;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("g.u2.f3 = f();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 [--Start Example(C 6.9.1-13):
	extern int max(int a, int b)
	{
	return a > b ? a : b;
	}
	 --End Example]
	 */
	public void test6_9_1s13() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern int max(int a, int b)\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("return a > b ? a : b;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.9.1-14):
	void g(int (*funcp)(void))
	{
	(*funcp)();
	funcp();
	}
	 --End Example]
	 */
	public void test6_9_1s14() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("void g(int (*funcp)(void))\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("(*funcp)();\n"); //$NON-NLS-1$
		buffer.append("funcp();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.9.2-4):
	int i1 = 1; // definition, external linkage
	static int i2 = 2; // definition, internal linkage
	extern int i3 = 3; // definition, external linkage
	int i4; // tentative definition, external linkage
	static int i5; // tentative definition, internal linkage
	int i1; // valid tentative definition, refers to pre vious
	//int i2; // 6.2.2 renders undefined, linkage disagreement
	int i3; // valid tentative definition, refers to pre vious
	int i4; // valid tentative definition, refers to pre vious
	//int i5; // 6.2.2 renders undefined, linkage disagreement
	extern int i1; // refers to pre vious, whose linkage is external
	extern int i2; // refers to pre vious, whose linkage is internal
	extern int i3; // refers to pre vious, whose linkage is external
	extern int i4; // refers to pre vious, whose linkage is external
	extern int i5; // refers to pre vious, whose linkage is internal
	 --End Example]
	 */
	public void test6_9_2s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int i1 = 1; // definition, external linkage\n"); //$NON-NLS-1$
		buffer.append("static int i2 = 2; // definition, internal linkage\n"); //$NON-NLS-1$
		buffer.append("extern int i3 = 3; // definition, external linkage\n"); //$NON-NLS-1$
		buffer.append("int i4; // tentative definition, external linkage\n"); //$NON-NLS-1$
		buffer.append("static int i5; // tentative definition, internal linkage\n"); //$NON-NLS-1$
		buffer.append("int i1; // valid tentative definition, refers to pre vious\n"); //$NON-NLS-1$
		buffer.append("//int i2; // 6.2.2 renders undefined, linkage disagreement\n"); //$NON-NLS-1$
		buffer.append("int i3; // valid tentative definition, refers to pre vious\n"); //$NON-NLS-1$
		buffer.append("int i4; // valid tentative definition, refers to pre vious\n"); //$NON-NLS-1$
		buffer.append("//int i5; // 6.2.2 renders undefined, linkage disagreement\n"); //$NON-NLS-1$
		buffer.append("extern int i1; // refers to pre vious, whose linkage is external\n"); //$NON-NLS-1$
		buffer.append("extern int i2; // refers to pre vious, whose linkage is internal\n"); //$NON-NLS-1$
		buffer.append("extern int i3; // refers to pre vious, whose linkage is external\n"); //$NON-NLS-1$
		buffer.append("extern int i4; // refers to pre vious, whose linkage is external\n"); //$NON-NLS-1$
		buffer.append("extern int i5; // refers to pre vious, whose linkage is internal\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.10.1-4):
	int g() {}
	int f() {
	#if 'z' - 'a' == 25
	g();
	#endif
	if ('z' - 'a' == 25)
	g();
	}
	 --End Example]
	 */
	public void test6_10_1s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int g() {}\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("#if 'z' - 'a' == 25\n"); //$NON-NLS-1$
		buffer.append("g();\n"); //$NON-NLS-1$
		buffer.append("#endif\n"); //$NON-NLS-1$
		buffer.append("if ('z' - 'a' == 25)\n"); //$NON-NLS-1$
		buffer.append("g();\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.10.3.3-4):
	#define hash_hash # ## #
	#define mkstr(a) # a
	#define in_between(a) mkstr(a)
	#define join(c, d) in_between(c hash_hash d)
	char p[] = join(x, y); // equivalent to
	// char p[] = "x ## y";
	 --End Example]
	 */
	public void test6_10_3_3s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define hash_hash # ## #\n"); //$NON-NLS-1$
		buffer.append("#define mkstr(a) # a\n"); //$NON-NLS-1$
		buffer.append("#define in_between(a) mkstr(a)\n"); //$NON-NLS-1$
		buffer.append("#define join(c, d) in_between(c hash_hash d)\n"); //$NON-NLS-1$
		buffer.append("char p[] = join(x, y); // equivalent to\n"); //$NON-NLS-1$
		buffer.append("// char p[] = \"x ## y\";\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.10.3.5-3):
	#define TABSIZE 100
	int table[TABSIZE];
	 --End Example]
	 */
	public void test6_10_3_5s3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define TABSIZE 100\n"); //$NON-NLS-1$
		buffer.append("int table[TABSIZE];\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.10.3.5-4):
	#define max(a, b) ((a) > (b) ? (a) : (b))
	 --End Example]
	 */
	public void test6_10_3_5s4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define max(a, b) ((a) > (b) ? (a) : (b))\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
	/**
	 [--Start Example(C 6.10.3.5-8):
	#define OBJ_LIKE1 (1-1)
	#define OBJ_LIKE2    \
        	(1-1)       \
	#define FUNC_LIKE1(a) ( a )
	#define FUNC_LIKE2( a )(              \
                	a                    \
                      	)
	 --End Example]
	 */
	public void test6_10_3_5s8() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define OBJ_LIKE1 (1-1)\n"); //$NON-NLS-1$
		buffer.append("#define OBJ_LIKE2    \\n"); //$NON-NLS-1$
	    buffer.append("         (1-1)       \\n"); //$NON-NLS-1$
		buffer.append("#define FUNC_LIKE1(a) ( a )\n"); //$NON-NLS-1$
		buffer.append("#define FUNC_LIKE2( a )(              \\n"); //$NON-NLS-1$
	    buffer.append("                 a                    \\n"); //$NON-NLS-1$
		buffer.append("                                      )\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), true, 0);
	}
	
}