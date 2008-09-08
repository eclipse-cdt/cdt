/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 * Scanner2Tests ported to use the CPreprocessor
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
}
