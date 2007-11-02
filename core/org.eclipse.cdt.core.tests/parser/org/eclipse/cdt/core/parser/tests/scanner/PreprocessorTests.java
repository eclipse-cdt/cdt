/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
public class PreprocessorTests extends PreprocessorTestsBase {
	
	public static TestSuite suite() {
		return suite(PreprocessorTests.class);
	}

	// #define f(x) x+x
	// #define obj_f f
	// #define obj_fx f x
	// #define obj_fopen f (
	// obj_f 
	// (y)
	// obj_f
	// y
	// obj_fx
	// (y)
	// obj_fopen y)
	public void testParenthesisOnNextLine() throws Exception {
		initializeScanner();
		validateIdentifier("y");
		validateToken(IToken.tPLUS);
		validateIdentifier("y");

		validateIdentifier("f");
		validateIdentifier("y");

		validateIdentifier("f");
		validateIdentifier("x");
		validateToken(IToken.tLPAREN);
		validateIdentifier("y");
		validateToken(IToken.tRPAREN);
		
		validateIdentifier("y");
		validateToken(IToken.tPLUS);
		validateIdentifier("y");
		validateEOF();
	}
	
	// #define f(x) x
	// f(f(x));
	// f(f);
	// f(f)(x);
	public void testRecursiveInArgument() throws Exception {
		initializeScanner();
		validateIdentifier("x");
		validateToken(IToken.tSEMI);

		validateIdentifier("f");
		validateToken(IToken.tSEMI);
		
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateIdentifier("x");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
	}
	
	
	// #define f(x) x
	// f(f(
	public void testMissingParenthesis() throws Exception {
		initializeScanner();
		validateEOF();
	}
	
	// #define b(x) ok
	// #define step1 b
	// #define step2 step1 (x)
	// step2
	public void testSpaceBeforeParenthesis() throws Exception {
		initializeScanner();
		validateIdentifier("ok");
		validateEOF();
	}
	
	// #define m1(x) a1
	// #define m2(x...) a2
	// m1(1,2);
	// m2(1,2);
	public void testSuperfluousComma() throws Exception {
		initializeScanner();
		validateIdentifier("a1");
		validateInteger("2");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateIdentifier("a2");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(1);
		validateProblem(0, IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, "m1");
	}

	// #define str(x,y) #x#y
	//	str(a,b );
	//	str( a,b);
	//	str(a a,b);
	//	str(a, b);
	public void testSpaceInArgs() throws Exception {
		initializeScanner();
		validateString("ab");
		validateToken(IToken.tSEMI);

		validateString("ab");
		validateToken(IToken.tSEMI);

		validateString("a ab");
		validateToken(IToken.tSEMI);

		validateString("ab");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}

	// #define str(x) #x
	// #define m0( )    a0
	// #define m1(x)    str( .x. )
	// #define m2(x,y)  str( .x.y. )
	// #define open0 m0(
	// #define open1 m1(
	// #define open2 m2(
	// open0 );
	// open1 a );
	// open2 a , b c );
	public void testSpaceInArgsViaOpenMacro() throws Exception {
		initializeScanner();
		validateIdentifier("a0");
		validateToken(IToken.tSEMI);

		validateString(".a.");
		validateToken(IToken.tSEMI);

		validateString(".a.b c.");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
	}

	// #define str(x) #x
	// #define m0( )    a0
	// #define m1(x)    str(.x.)
	// #define m2(x,y)  str(.x.y.)
	// #define _a a
	// #define _b b
	// #define _c c
	// #define use0 m0( )
	// #define use1 m1( _a )
	// #define use2 m2( _a , _b _c )
	// use0;
	// use1;
	// use2;
	public void testSpaceInArgsViaExpansion() throws Exception {
		initializeScanner();
		validateIdentifier("a0");
		validateToken(IToken.tSEMI);

		validateString(".a.");
		validateToken(IToken.tSEMI);

		validateString(".a.b c.");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
	}
	
	// #define m0() a
	// m0;
	// m0();
	// m0( );
	// m0(x);
	public void testFunctionStyleWithoutArgs() throws Exception {
		initializeScanner();
		validateIdentifier("m0");
		validateToken(IToken.tSEMI);

		validateIdentifier("a");
		validateToken(IToken.tSEMI);

		validateIdentifier("a");
		validateToken(IToken.tSEMI);

		validateIdentifier("a");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(1);
		validateProblem(0, IProblem.PREPROCESSOR_MACRO_USAGE_ERROR, "m0");
	}
	
	// #define tp(x,y) #x##y
	// tp(a, );
	// tp(a,b);
	public void testStringifyAndPaste() throws Exception {
		initializeScanner();
		validateString("a");
		validateToken(IToken.tSEMI);

		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(1);
		validateProblem(0, IProblem.PREPROCESSOR_MACRO_PASTING_ERROR, "tp");
	}
	
	// #define tp(x,y) x##y
	// tp(a, b c);
	// tp(a b,c);
	public void testPasteMultipleTokens() throws Exception {
		initializeScanner();
		validateIdentifier("ab");
		validateIdentifier("c");
		validateToken(IToken.tSEMI);

		validateIdentifier("a");
		validateIdentifier("bc");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}

	// #define obj a b ## c ## d e
	// obj;
	public void testObjectStyleTokenPaste() throws Exception {
		initializeScanner();
		validateIdentifier("a");
		validateIdentifier("bcd");
		validateIdentifier("e");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	// #define variadic(x...) (a, ##x)
	// variadic();
	// variadic(b);
	// variadic(c,d);
	public void testGccVariadicMacroExtensions() throws Exception {
		initializeScanner();
		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		
		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tCOMMA);
		validateIdentifier("b");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tCOMMA);
		validateIdentifier("c");
		validateToken(IToken.tCOMMA);
		validateIdentifier("d");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}

	// #define str(x) #x
	// str();
	public void testEmptyStringify() throws Exception {
		initializeScanner();
		validateString("");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	// #define tp(x,y) x##y
	// #define _p p
	// tp(_p,);
	// tp(_p, a);
	public void testRescanAfterTokenPaste() throws Exception {
		initializeScanner();
		validateIdentifier("p");
		validateToken(IToken.tSEMI);

		validateIdentifier("_pa");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	// #define vararg(a, ...) (__VA_ARGS__)
	// vararg();
	// vararg( );
	// vararg(a);
	// vararg(a,b);
	// vararg(a, ,c);
	public void testVaargs() throws Exception {
		initializeScanner();
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.tLPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.tLPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.tLPAREN);
		validateIdentifier("b");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.tLPAREN);
		validateToken(IToken.tCOMMA);
		validateIdentifier("c");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
	}
	
	// #define OBJ __VA_ARGS__
	// #define func(x) __VA_ARGS__
	// OBJ;
	// func(a);
	public void testVaargsWarning() throws Exception {
		initializeScanner();
		validateIdentifier("__VA_ARGS__");
		validateToken(IToken.tSEMI);

		validateIdentifier("__VA_ARGS__");
		validateToken(IToken.tSEMI);
		validateEOF();
		// gcc actually warns about using __VA_ARGS__ in object-style macros too. 
		validateProblemCount(1);
		validateProblem(0, IProblem.PREPROCESSOR_INVALID_VA_ARGS, null);
	}
	
	// #define str(x) #x
	// #define _p p
	// #define obj str(_p)  // str is expanded before _p is rescanned.
	// obj;
	public void testRescanOrder() throws Exception {
		initializeScanner();
		validateString("_p");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
	}
	
	// #define obj #str
	// obj;
	public void testStringifyOperatorInObject() throws Exception { 
		initializeScanner();
		validateToken(IToken.tPOUND);
		validateIdentifier("str");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
	}
	
	// #define str(x) #x
	// #define open_str() str(a
	// open_str()b);
	public void testOpenStringify() throws Exception { 
		initializeScanner();
		validateString("ab");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
	}
	
    // #define ONE(a, ...) int x
    // #define TWO(b, args...) int y
    // ONE("string"); 
    // TWO("string"); 
    public void testSkippingVarags() throws Exception {
		initializeScanner();
		validateToken(IToken.t_int);
		validateIdentifier("x");
		validateToken(IToken.tSEMI);
		
		validateToken(IToken.t_int);
		validateIdentifier("y");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
    }
    
    // #define eval(f,x) f(x)
    // #define m(x) m[x]
    // eval(m,y);
    public void testReconsiderArgsForExpansion() throws Exception {
		initializeScanner();
		validateIdentifier("m");
		validateToken(IToken.tLBRACKET);
		validateIdentifier("y");
		validateToken(IToken.tRBRACKET);
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
    }
    
    //#define f\
    //(x) ok
    // f(x)
    public void testLineSpliceInMacroDefinition() throws Exception {
		initializeScanner();
		validateIdentifier("ok");
		validateEOF();
		validateProblemCount(0);
    }
    
    // #define f() fval
    // #define nospace f()f()
    // #define space f() f()
    // #define str(x) #x
    // #define xstr(x) str(x)
    // #define tp1(x,y,z) [x ## y ## z]
    // #define tp2(x,y,z) [ x ## y ## z ]
    // #define tstr1(x,y) [#x#y]
    // #define tstr2(x,y) [ #x #y ]
    // xstr(nospace);
    // xstr(space);
    // xstr(tp1(a b, c d , e f));
    // xstr(tp2(a b, c d , e f));
    // xstr(tp1(a-b, c-d , e-f));
    // xstr(tp2(a-b, c-d , e-f));
    // xstr(tstr1(a b, c d));
    // xstr(tstr2(a b, c d));
    public void testSpaceInStringify() throws Exception {
		initializeScanner();
		validateString("fvalfval");
		validateToken(IToken.tSEMI);
		
		validateString("fval fval");
		validateToken(IToken.tSEMI);
		
		validateString("[a bc de f]");
		validateToken(IToken.tSEMI);
		
		validateString("[ a bc de f ]");
		validateToken(IToken.tSEMI);

		validateString("[a-bc-de-f]");
		validateToken(IToken.tSEMI);
		
		validateString("[ a-bc-de-f ]");
		validateToken(IToken.tSEMI);

		validateString("[\\\"a b\\\"\\\"c d\\\"]");
		validateToken(IToken.tSEMI);

		validateString("[ \\\"a b\\\" \\\"c d\\\" ]");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
    }
}
