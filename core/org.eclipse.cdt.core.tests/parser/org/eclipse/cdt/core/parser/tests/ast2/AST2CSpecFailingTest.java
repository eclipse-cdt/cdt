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

/**
 * @author dsteffle
 */
public class AST2CSpecFailingTest extends AST2SpecBaseTest {

	/**
	 [--Start Example(C 6.10.3.5-6):
	#define str(s) # s
	#define xstr(s) str(s)
	#define debug(s, t) printf("x" # s "= %d, x" # t "= %s", \
	x ## s, x ## t)
	#define INCFILE(n) vers ## n
	#define glue(a, b) a ## b
	#define xglue(a, b) glue(a, b)
	#define HIGHLOW "hello"
	#define LOW LOW ", world"
	int f() {
	debug(1, 2);
	fputs(str(strncmp("abc\0d", "abc", '\4') // this goes away
	== 0) str(: @\n), s);
	//#include xstr(INCFILE(2).h)
	glue(HIGH, LOW);
	xglue(HIGH, LOW)
	}
	 --End Example]
	 */
	public void test6_10_3_5s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define str(s) # s\n"); //$NON-NLS-1$
		buffer.append("#define xstr(s) str(s)\n"); //$NON-NLS-1$
		buffer.append("#define debug(s, t) printf(\"x\" # s \"= %d, x\" # t \"= %s\", \\n"); //$NON-NLS-1$
		buffer.append("x ## s, x ## t)\n"); //$NON-NLS-1$
		buffer.append("#define INCFILE(n) vers ## n\n"); //$NON-NLS-1$
		buffer.append("#define glue(a, b) a ## b\n"); //$NON-NLS-1$
		buffer.append("#define xglue(a, b) glue(a, b)\n"); //$NON-NLS-1$
		buffer.append("#define HIGHLOW \"hello\"\n"); //$NON-NLS-1$
		buffer.append("#define LOW LOW \", world\"\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("debug(1, 2);\n"); //$NON-NLS-1$
		buffer.append("fputs(str(strncmp(\"abc\0d\", \"abc\", '\4') // this goes away\n"); //$NON-NLS-1$
		buffer.append("== 0) str(: @\n), s);\n"); //$NON-NLS-1$
		buffer.append("//#include xstr(INCFILE(2).h)\n"); //$NON-NLS-1$
		buffer.append("glue(HIGH, LOW);\n"); //$NON-NLS-1$
		buffer.append("xglue(HIGH, LOW)\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parseCandCPP(buffer.toString(), false, 0);
		assertTrue(false);
		} catch (Exception e) {}
	}
	
	/**
	 [--Start Example(C 6.10.3.5-5):
	#define x 3
	#define f(a) f(x * (a))
	#undef x
	#define x 2
	#define g f
	#define z z[0]
	#define h g(~
	#define m(a) a(w)
	#define w 0,1
	#define t(a) a
	#define p() int
	#define q(x) x
	#define r(x,y) x ## y
	#define str(x) # x
	int foo() {
	p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) };
	char c[2][6] = { str(hello), str() };
	}
	 --End Example]
	 */
	public void test6_10_3_5s5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define x 3\n"); //$NON-NLS-1$
		buffer.append("#define f(a) f(x * (a))\n"); //$NON-NLS-1$
		buffer.append("#undef x\n"); //$NON-NLS-1$
		buffer.append("#define x 2\n"); //$NON-NLS-1$
		buffer.append("#define g f\n"); //$NON-NLS-1$
		buffer.append("#define z z[0]\n"); //$NON-NLS-1$
		buffer.append("#define h g(~\n"); //$NON-NLS-1$
		buffer.append("#define m(a) a(w)\n"); //$NON-NLS-1$
		buffer.append("#define w 0,1\n"); //$NON-NLS-1$
		buffer.append("#define t(a) a\n"); //$NON-NLS-1$
		buffer.append("#define p() int\n"); //$NON-NLS-1$
		buffer.append("#define q(x) x\n"); //$NON-NLS-1$
		buffer.append("#define r(x,y) x ## y\n"); //$NON-NLS-1$
		buffer.append("#define str(x) # x\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append("p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) };\n"); //$NON-NLS-1$
		buffer.append("char c[2][6] = { str(hello), str() };\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parseCandCPP(buffer.toString(), true, 0);
		assertTrue(false);
		} catch (Exception e) {}
	}
	
	/**
	 [--Start Example(C 6.10.3.5-7):
	#define t(x,y,z) x ## y ## z
	int j[] = { t(1,2,3), t(,4,5), t(6,,7), t(8,9,),
	t(10,,), t(,11,), t(,,12), t(,,) };
	 --End Example]
	 */
	public void test6_10_3_5s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define t(x,y,z) x ## y ## z\n"); //$NON-NLS-1$
		buffer.append("int j[] = { t(1,2,3), t(,4,5), t(6,,7), t(8,9,),\n"); //$NON-NLS-1$
		buffer.append("t(10,,), t(,11,), t(,,12), t(,,) };\n"); //$NON-NLS-1$
		try {
		parseCandCPP(buffer.toString(), true, 0);
		assertTrue(false);
		} catch (Exception e) {}
	}
	
	/**
	 [--Start Example(C 6.10.3.5-9):
	#define debug(...) fprintf(stderr, _ _VA_ARGS_ _)
	#define showlist(...) puts(#_ _VA_ARGS_ _)
	#define report(test, ...) ((test)?puts(#test):\
	printf(_ _VA_ARGS_ _))
	int f() {
	debug("Flag");
	debug("X = %d\n", x);
	showlist(The first, second, and third items.);
	report(x>y, "x is %d but y is %d", x, y);
	}
	 --End Example]
	 */
	public void test6_10_3_5s9() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define debug(...) fprintf(stderr, _ _VA_ARGS_ _)\n"); //$NON-NLS-1$
		buffer.append("#define showlist(...) puts(#_ _VA_ARGS_ _)\n"); //$NON-NLS-1$
		buffer.append("#define report(test, ...) ((test)?puts(#test):\\n"); //$NON-NLS-1$
		buffer.append("printf(_ _VA_ARGS_ _))\n"); //$NON-NLS-1$
		buffer.append("int f() {\n"); //$NON-NLS-1$
		buffer.append("debug(\"Flag\");\n"); //$NON-NLS-1$
		buffer.append("debug(\"X = %d\n\", x);\n"); //$NON-NLS-1$
		buffer.append("showlist(The first, second, and third items.);\n"); //$NON-NLS-1$
		buffer.append("report(x>y, \"x is %d but y is %d\", x, y);\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		try {
		parseCandCPP(buffer.toString(), false, 0);
		assertTrue(false);
		} catch (Exception e) {}
	}
}
