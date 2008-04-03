/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.tests.c99;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.BaseExtensibleLanguage;
import org.eclipse.cdt.core.dom.lrparser.c99.C99Language;
import org.eclipse.cdt.core.dom.lrparser.cpp.ISOCPPLanguage;
import org.eclipse.cdt.core.lrparser.tests.ParseHelper;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.tests.ast2.AST2CSpecTest;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class C99SpecTests extends AST2CSpecTest {

	public C99SpecTests() { } 
	public C99SpecTests(String name) { super(name); }

	
	@Override
	protected void parseCandCPP( String code, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		parse(code, ParserLanguage.C,   checkBindings, expectedProblemBindings);
		parse(code, ParserLanguage.CPP, checkBindings, expectedProblemBindings);
	}
		
	@Override
	protected IASTTranslationUnit parse( String code, ParserLanguage lang, boolean checkBindings, int expectedProblemBindings ) throws ParserException {
		ILanguage language = lang.isCPP() ? getCPPLanguage() : getCLanguage();
		return ParseHelper.parse(code, language, true, checkBindings, expectedProblemBindings );
    }
	
	protected BaseExtensibleLanguage getCLanguage() {
		return C99Language.getDefault();
	}
	
	protected BaseExtensibleLanguage getCPPLanguage() {
		return ISOCPPLanguage.getDefault();
	}

	//Assignment statements cannot exists outside of a function body
	@Override
	public void test5_1_2_3s15() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("//#include <stdio.h>\n"); //$NON-NLS-1$
		buffer.append("int foo() { \n"); //$NON-NLS-1$
		buffer.append("int sum;\n"); //$NON-NLS-1$
		buffer.append("char *p;\n"); //$NON-NLS-1$
		buffer.append("sum = sum * 10 - '0' + (*p++ = getchar());\n"); //$NON-NLS-1$
		buffer.append("sum = (((sum * 10) - '0') + ((*(p++)) = (getchar())));\n"); //$NON-NLS-1$
		buffer.append("} \n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}	

	
	
	// Tests from AST2CSpecFailingTests
	
	/**
	 * TODO: This one fails, it can't resolve one of the bindings (const t) I think
	 * 
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

		try {
			parse(buffer.toString(), ParserLanguage.C, true, 0);
		} catch(AssertionFailedError _) {
			// there should be an error
		}
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
	@Override
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

		//parseCandCPP(buffer.toString(), true, 0);
		// TODO: this only works on the C99 parser for now
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	
	/**
	 [--Start Example(C 6.10.3.5-7):
	#define t(x,y,z) x ## y ## z
	int j[] = { t(1,2,3), t(,4,5), t(6,,7), t(8,9,),
	t(10,,), t(,11,), t(,,12), t(,,) };
	 --End Example]
	 */
	@Override
	public void test6_10_3_5s7() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define t(x,y,z) x ## y ## z\n"); //$NON-NLS-1$
		buffer.append("int j[] = { t(1,2,3), t(,4,5), t(6,,7), t(8,9,),\n"); //$NON-NLS-1$
		buffer.append("t(10,,), t(,11,), t(,,12), t(,,) };\n"); //$NON-NLS-1$

		// TODO: this only works on the C99 parser for now
		parse(buffer.toString(), ParserLanguage.C, true, 0);
	}
	
	/**
	 * This test seems to be incorrect in AST2SpecTests
	 */
	@Override
	public void test4s6() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifdef __STDC_IEC_559__ /* FE_UPWARD defined */\n"); //$NON-NLS-1$
		buffer.append("fesetround(FE_UPWARD);\n"); //$NON-NLS-1$
		buffer.append("#endif\n"); //$NON-NLS-1$
		parseCandCPP(buffer.toString(), false, 0);
	}
	

//	@Override
//	public void test6_7_8s24() throws Exception { // complex isn't declared as a typedef
//		try {
//			super.test6_7_8s24();
//			fail();
//		} catch(AssertionFailedError _) { }
//	}
//	
//	
//	@Override
//	public void test6_7_8s34() throws Exception { // div_t isn't declared as a typedef
//		try {
//			super.test6_7_8s34();
//			fail();
//		} catch(AssertionFailedError _) { }
//	}
	
	@Override
	public void test6_7_2_1s17() throws Exception { // what the heck is offsetof
		try {
			super.test6_7_2_1s17();
			fail();
		} catch(AssertionFailedError _) { }
	}
	
	
}
