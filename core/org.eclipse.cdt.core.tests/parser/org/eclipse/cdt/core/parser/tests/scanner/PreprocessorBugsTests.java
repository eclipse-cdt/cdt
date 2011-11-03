/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;


/**
 * Scanner2Tests ported to use the CPreprocessor plus additional bugs fixed in 
 * the CPreprocessor, afterwards.
 */
public class PreprocessorBugsTests extends PreprocessorTestsBase {
	
	public static TestSuite suite() {
		return suite(PreprocessorBugsTests.class);
	}

	// #define NOP(x)        x
	// #define CPUINC(cpu)   <NOP(reg)NOP(cpu).sfr>
	// #include CPUINC(xag4)
	public void testMacroInInclusion_Bug122891() throws Exception {
		initializeScanner();
		validateEOF();
		validateProblem(0, IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, "<regxag4.sfr>");
		validateProblemCount(1);
	}
	
	//	#define FUNKY(x) __##x##__
	//	#define __foo__ 127
	//
	//	#if FUNKY(foo) == 0x7f
	//	#define MSG "hello"
	//	#else
	//	#define MSG "goodbye"
	//	#endif
	//  MSG
	public void testTokenPaste_Bug210344() throws Exception {
		initializeScanner();
		validateString("hello");
		validateEOF();
		validateProblemCount(0);
	}
	
	// #ifndef PREFIX
	// #define PREFIX
	// #endif
	// #define STRING(x) #x
	// #define CONCAT(x,y) STRING(x##y)
	// #define EXPAND(x,y) CONCAT(x,y)
	// #define PREFIXED(x) EXPAND(PREFIX,x)
	// #include PREFIXED(bar.h)
	public void testEmptyStringInMacroInInclusion_Bug145270() throws Exception {
		initializeScanner();
		validateEOF();
		validateProblem(0, IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, "\"bar.h\"");
		validateProblemCount(1);
	}
	
	// #define D
	// #if defined D 
	//     x;
	// #endif 
	// #if defined(D) 
	//     y;
	// #endif 
	public void testBug186047() throws Exception {
		initializeScanner();
		
		validateIdentifier("x");
		validateToken(IToken.tSEMI);
		validateIdentifier("y");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	// __CDT_PARSER__
	public void testPredefinedCDTMacro_Bug173848() throws Exception {
		initializeScanner();
		validateInteger("1");
		validateEOF();
		validateProblemCount(0);
	}
	
	//	#define FOO(ARG) defined(ARG##_BAZ)
	//	#define BAR_BAZ UNDEFINED
	//	#if FOO(BAR)
	//	    juhuu
	//	#else
	//	    ojeh
	//	#endif
	//  FOO(BAR) // here expansion has to take place
	//
	//  #define PLATFORM(WTF_FEATURE) (defined( WTF_PLATFORM_##WTF_FEATURE ) && WTF_PLATFORM_##WTF_FEATURE)
	//  #define WTF_PLATFORM_FOO 1
    //  #if PLATFORM(FOO)
	//  ok
	//  #endif

	
	public void testIndirectDefined_Bug225562() throws Exception {
		initializeScanner();
		validateIdentifier("juhuu");
		validateIdentifier("defined");
		validateToken(IToken.tLPAREN);
		validateIdentifier("UNDEFINED"); // here the expansion has to take place
		validateToken(IToken.tRPAREN);
		
		validateIdentifier("ok");
		validateEOF();
		validateProblemCount(0);
	}
	
	// "unintentionally unbounded
	// "
	//
	public void testUnboundedEmptyStringLiteral_Bug190884() throws Exception {
		initializeScanner();
		validateString("unintentionally unbounded");
		validateEOF();
		validateProblemCount(2);
	}
	
	// #if true
	// yes
	// #else
	// no
	// #endif
	// #if false
	// no
	// #else
	// yes
	// #endif
	public void testTrueInConditionalExpression_Bug246369() throws Exception {
		initializeScanner();
		validateIdentifier("yes");
		validateIdentifier("yes");
		validateEOF();
		validateProblemCount(0);
	}

	// #if if
	// no
	// #else
	// yes
	// #endif
	// #if or
	// no
	// #endif
	public void testKeywordsInConditionalExpression_Bug246369() throws Exception {
		initializeScanner();
		validateIdentifier("yes");
		validateEOF();
		validateProblemCount(1);
		validateProblem(0, IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR, null);
	}
	
	//	#define BAR1_RX_BLOCK_SIZE 1
	//	#define MAX(__x,__y) ((__x)>(__y)?(__x):(__y))
	//	#define BAR_BLOCK_SIZE    (MAX(BAR1_RX_BLOCK_SIZE, 
	//	int main(void) {
	//	   BAR_BLOCK_SIZE;
	//	}
	public void testMissingClosingParenthesis_Bug251734() throws Exception {
		initializeScanner();
		validateToken(IToken.t_int);
		validateIdentifier("main");
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_void);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tLBRACE);
		validateToken(IToken.tLPAREN);
		validateEOF();
		validateProblemCount(1);
		validateProblem(0, IProblem.PREPROCESSOR_MISSING_RPAREN_PARMLIST, null);
	}

	//  /**/ #if 0
	//  bug
	//  /**/ #endif
	//  passed
	//
	//  /*
	//   */ #if 0
	//  bug
	//  /**/ #endif
	//  passed
	//
	//	#if 0
	//	/**/ #else
	//	OK1
	//	#endif
	//
	//	#if 0
	//	/*
	//   */ #else
	//	OK2
	//	#endif
	//
	//	#if 0
	//	a /**/ #else
	//	bug
	//	#endif
	//  passed
	//
	//	#if 0
	//	a /*
	//     */ #else   // interesting, gcc ignores this directive, we mimic the behavior
	//	bug
	//	#endif
	//  passed
	public void testCommentBeforeDirective_Bug255318() throws Exception {
		initializeScanner();
		validateIdentifier("passed");
		validateIdentifier("passed");
		validateIdentifier("OK1");
		validateIdentifier("OK2");
		validateIdentifier("passed");
		validateIdentifier("passed");
		validateEOF();
		validateProblemCount(0);
	}
	
	//	#define ID(x) x
	//	ID(
	//	#include "bbb"
	//	) 
	//  passed1
	//
	//  ID(
	//	#if ID(b)
	//	#elif ID(c)
	//	#else
	//		d
	//	#endif
	//	)
	//  passed2
	//  ID(
	//	#if 0
	//	#include "bbb"
	//	#endif
	//	)
	//  passed3
	public void testDirectiveInExpansion_Bug240194() throws Exception {
		initializeScanner();
		validateIdentifier("passed1");
		validateIdentifier("d");
		validateIdentifier("passed2");
		validateIdentifier("passed3");
		validateEOF();
		validateProblemCount(2);  // the inclusions
	}
	
	// #if 0xe000
	// ok
	// #endif
	// 0x1p2 0xe0
	public void testHexConstant_Bug265927() throws Exception {
		initializeScanner();
		validateIdentifier("ok");
		validateFloatingPointLiteral("0x1p2");
		validateInteger("0xe0");
		validateEOF();
		validateProblemCount(0); 
	}
	
	// #error // 
	// #warning // 
	// #pragma  // not marked as problem
	// #define //
	// #include //
	// #undef //
	// #if //
	// #endif
	// #ifdef //
	// #endif
	// #ifndef //
	// #endif
	// #if 0
	// #elif //
	// #endif
	// a
	public void testMissingArgument_Bug303969() throws Exception {
		initializeScanner();
		validateIdentifier("a");
		validateEOF();
		validateProblemCount(9); 
	}
	
	//	#define str(x) #x
	//	#define xstr(x) str(x)
	//  #define MY_MACROS(Type) unsigned ##Type f();
	//	xstr(MY_MACROS(int))
	public void testStringify_Bug282418() throws Exception {
		initializeScanner();
		validateString("unsignedint f();");
		validateEOF();
	}
	
	// #if '\0'
	// no
	// #else
	// yes
	// #endif
	// #if '\1'
	// yes
	// #else
	// no
	// #endif
	public void testOcatalCharConstant_Bug330747() throws Exception {
		initializeScanner();
		validateIdentifier("yes");
		validateIdentifier("yes");
		validateEOF();
		validateProblemCount(0);
	}
	
	//	#define foo(x) (## x)
	//	void test foo(void);  // Valid for Microsoft's compiler, expands to (void)
	public void testInvalidTokenPasting_Bug354553() throws Exception {
		initializeScanner();
		validateToken(IToken.t_void);
		validateIdentifier("test");
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_void);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblem(0, IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, "foo");
		validateProblemCount(1);
	}
	
	//	#define PR ""
	//	A
	//	#ifdef _DEBUG
	//	        PR"";
	//	#endif
	//	B
	public void testRawString_Bug362562() throws Exception {
		initializeScanner();
		validateIdentifier("A");
		validateIdentifier("B");
		validateProblemCount(0);
	}
	
	// __COUNTER__
	// __COUNTER__
	public void testCounter_Bug362148() throws Exception {
		initializeScanner();
		validateInteger("0");
		validateInteger("1");
		validateProblemCount(0);
	}
}
