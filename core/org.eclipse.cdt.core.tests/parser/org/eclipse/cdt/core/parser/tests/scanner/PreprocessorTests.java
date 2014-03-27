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
 *    Richard Eames
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;


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
		validateProblemCount(1);
		validateProblem(0, IProblem.PREPROCESSOR_MISSING_RPAREN_PARMLIST, "f");
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
	public void testStringifyAndPasteCPP() throws Exception {
		initializeScanner(getAboveComment(), ParserLanguage.CPP);
		validateString("a");
		validateToken(IToken.tSEMI);

		validateUserDefinedLiteralString("a", "b");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	// #define tp(x,y) #x##y
	// tp(a, );
	// tp(a,b);
	public void testStringifyAndPasteC() throws Exception {
		initializeScanner(getAboveComment(), ParserLanguage.C);
		validateString("a");
		validateToken(IToken.tSEMI);

		validateString("a");
		validateIdentifier("b");
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

	// #define variadic(x...) (a, ##x)
	// #define _c c
	// variadic();
	// variadic(_c);
	// variadic(_c,_c);
	public void testGccVariadicMacroExtensions2() throws Exception {
		initializeScanner();
		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		
		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tCOMMA);
		validateIdentifier("c");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tCOMMA);
		validateIdentifier("c");
		validateToken(IToken.tCOMMA);
		validateIdentifier("c");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}

	// #define variadic(y, x...) (a, ##x)
	// #define _c c
	// variadic(1);
	// variadic(,_c);
	// variadic(,_c,_c);
	public void testGccVariadicMacroExtensions3() throws Exception {
		initializeScanner();
		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		
		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tCOMMA);
		validateIdentifier("c");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.tLPAREN);
		validateIdentifier("a");
		validateToken(IToken.tCOMMA);
		validateIdentifier("c");
		validateToken(IToken.tCOMMA);
		validateIdentifier("c");
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
    // #define spaceBeforeStr(x) a #x b
    // xstr(nospace);
    // xstr(space);
    // xstr(tp1(a b, c d , e f));
    // xstr(tp2(a b, c d , e f));
    // xstr(tp1(a-b, c-d , e-f));
    // xstr(tp2(a-b, c-d , e-f));
    // xstr(tstr1(a b, c d));
    // xstr(tstr2(a b, c d));
    // xstr(spaceBeforeStr(c));
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

		validateString("a \\\"c\\\" b");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);
    }

    // #define empty
    // #define paste1(y) x##y z
    // #define paste2(x) x##empty z
    // paste1();
    // paste1(empty);
    // paste2();
    // paste2(empty);
    // paste2(a);
    public void testTokenPasteWithEmptyParam() throws Exception {
		initializeScanner();
		validateIdentifier("x");
		validateIdentifier("z");
		validateToken(IToken.tSEMI);
		
		validateIdentifier("xempty");
		validateIdentifier("z");
		validateToken(IToken.tSEMI);
		
		validateIdentifier("z");
		validateToken(IToken.tSEMI);
		
		validateIdentifier("emptyempty");
		validateIdentifier("z");
		validateToken(IToken.tSEMI);

		validateIdentifier("aempty");
		validateIdentifier("z");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);    	
    }
    
    // #define empty
    // #define paste1(y) x##y z
    // #define paste2(x) x##empty z
    // paste1();
    // paste1(empty);
    // paste2();
    // paste2(empty);
    // paste2(a);
    public void testSpacesBeforeStringify() throws Exception {
		initializeScanner();
		validateIdentifier("x");
		validateIdentifier("z");
		validateToken(IToken.tSEMI);
		
		validateIdentifier("xempty");
		validateIdentifier("z");
		validateToken(IToken.tSEMI);
		
		validateIdentifier("z");
		validateToken(IToken.tSEMI);
		
		validateIdentifier("emptyempty");
		validateIdentifier("z");
		validateToken(IToken.tSEMI);

		validateIdentifier("aempty");
		validateIdentifier("z");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);    	
    }

    // #define paste(x,y,z) x##y##z
    // paste(a,b,c);
    // paste(1,2,3);
    public void testTokenPasteChain() throws Exception {
    	initializeScanner();
		validateIdentifier("abc");
		validateToken(IToken.tSEMI);

		validateInteger("123");
		validateToken(IToken.tSEMI);

		validateEOF();
		validateProblemCount(0);    	    	
    }
    
    
	// #define A(x,y,z) x + y + z
	// #define _t t
	// A ( _t , , _t )
    public void testEmptyToken() throws Exception {
    	initializeScanner();
    	validateIdentifier("t");
		validateToken(IToken.tPLUS);
		validateToken(IToken.tPLUS);
    	validateIdentifier("t");
    }

    
    // #define FOO 5
    // # define BAR 10
    // int x = FOO + BAR;
    public void testSimpleObjectLike1() throws Exception {
    	initializeScanner();
    	validateToken(IToken.t_int);
    	validateIdentifier("x");
    	validateToken(IToken.tASSIGN);
    	validateInteger("5");
    	validateToken(IToken.tPLUS);
    	validateInteger("10");
    	validateToken(IToken.tSEMI);
    	validateEOF();
		validateProblemCount(0);
	}
	
    // #define FOO BAR
    // # define BAR 10
    // int x = BAR;
	public void testSimpleObjectLike2() throws Exception {
		initializeScanner();
		validateToken(IToken.t_int);
		validateIdentifier("x");
		validateToken(IToken.tASSIGN);
		validateInteger("10");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	// #define MAX(a, b) (a) > (b) ? (a) : (b)
	// int max = MAX(x, y);
	public void testSimpleFunctionLike1() throws Exception {
		// int max = (x) > (y) ? (x) : (y);
		initializeScanner();

		validateToken(IToken.t_int);
		validateIdentifier("max");
		validateToken(IToken.tASSIGN);
		validateToken(IToken.tLPAREN);
		validateIdentifier("x");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tGT);
		validateToken(IToken.tLPAREN);
		validateIdentifier("y");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tQUESTION);
		validateToken(IToken.tLPAREN);
		validateIdentifier("x");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tCOLON);
		validateToken(IToken.tLPAREN);
		validateIdentifier("y");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	// #define ADD(a, b) (a) + (b)
	// #define ADDPART(a) ADD(a
	// int sum = ADDPART (x) , y);
	public void testSimpleFunctionLike2() throws Exception {
		// int sum = (x) + (y) ;
		initializeScanner();

		validateToken(IToken.t_int);
		validateIdentifier("sum");
		validateToken(IToken.tASSIGN);
		validateToken(IToken.tLPAREN);
		validateIdentifier("x");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tPLUS);
		validateToken(IToken.tLPAREN);
		validateIdentifier("y");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	// #define ADD(a, b) (a) + (b)
	// int sum = ADD(x+1,y+1);
	public void testSimpleFunctionLike3() throws Exception {
		// int sum = (x+1) + (y+1) ;
		initializeScanner();
		
		validateToken(IToken.t_int);
		validateIdentifier("sum");
		validateToken(IToken.tASSIGN);
		validateToken(IToken.tLPAREN);
		validateIdentifier("x");
		validateToken(IToken.tPLUS);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tPLUS);
		validateToken(IToken.tLPAREN);
		validateIdentifier("y");
		validateToken(IToken.tPLUS);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	// #define ADD(a, b) (a) + (b)
	// int sum = ADD(f(x,y),z+1);
	public void testSimpleFunctionLike4() throws Exception {
		// int sum = (f(x,y)) + (z+1) ;
		initializeScanner();
		
		validateToken(IToken.t_int);
		validateIdentifier("sum");
		validateToken(IToken.tASSIGN);
		validateToken(IToken.tLPAREN);
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateIdentifier("x");
		validateToken(IToken.tCOMMA);
		validateIdentifier("y");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tPLUS);
		validateToken(IToken.tLPAREN);
		validateIdentifier("z");
		validateToken(IToken.tPLUS);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	// #define hash_hash # ## #
	// #define mkstr(a) # a
	// #define in_between(a) mkstr(a)
	// #define join(c, d) in_between(c hash_hash d)
	// char p[] = join(x, y); 
	public void testSpecHashHashExample() throws Exception {
		// char p[] = "x ## y" ;
		initializeScanner();
		
		validateToken(IToken.t_char);
		validateIdentifier("p");
		validateToken(IToken.tLBRACKET);
		validateToken(IToken.tRBRACKET);
		validateToken(IToken.tASSIGN);
		validateString("x ## y");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	private static StringBuffer getExample3Defines() {
		return new StringBuffer()
			.append("#define x 3 \n")
			.append("#define f(a) f(x * (a)) \n")
			.append("#undef x \n")
			.append("#define x 2 \n")
			.append("#define g f \n")
			.append("#define z z[0] \n")
			.append("#define h g(~ \n")
			.append("#define m(a) a(w) \n")
			.append("#define w 0,1 \n")
			.append("#define t(a) a \n")
			.append("#define p() int \n")
			.append("#define q(x) x \n")
			.append("#define r(x,y) x ## y \n")
			.append("#define str(x) # x \n");
	}
	
	
	
	public void testSpecExample3_1() throws Exception {
		StringBuffer sb = getExample3Defines();
		sb.append("f(y+1) + f(f(z)) % t(t(g)(0) + t)(1); \n"); 
		
		// f(2 * (y+1)) + f(2 * (f(2 * (z[0])))) % f(2 * (0)) + t(1);
		initializeScanner(sb.toString());
		
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tSTAR);
		validateToken(IToken.tLPAREN);
		validateIdentifier("y");
		validateToken(IToken.tPLUS);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tPLUS);
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tSTAR);
		validateToken(IToken.tLPAREN);
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tSTAR);
		validateToken(IToken.tLPAREN);
		validateIdentifier("z");
		validateToken(IToken.tLBRACKET);
		validateInteger("0");
		validateToken(IToken.tRBRACKET);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tMOD);
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tSTAR);
		validateToken(IToken.tLPAREN);
		validateInteger("0");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tPLUS);
		validateIdentifier("t");
		validateToken(IToken.tLPAREN);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	public void testSpecExample3_2() throws Exception {
		StringBuffer sb = getExample3Defines();
		sb.append("g(x+(3,4)-w) | h 5) & m (f)^m(m); \n");
		
		// f(2 * (2+(3,4)-0,1)) | f(2 * (~ 5)) & f(2 * (0,1))^m(0,1); //47
		initializeScanner(sb.toString());
		
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tSTAR);
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tPLUS);
		validateToken(IToken.tLPAREN);
		validateInteger("3");
		validateToken(IToken.tCOMMA);
		validateInteger("4");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tMINUS);
		validateInteger("0");
		validateToken(IToken.tCOMMA);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tBITOR);
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tSTAR);
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tBITCOMPLEMENT);
		validateInteger("5");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tAMPER);
		validateIdentifier("f");
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tSTAR);
		validateToken(IToken.tLPAREN);
		validateInteger("0");
		validateToken(IToken.tCOMMA);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tXOR);
		validateIdentifier("m");
		validateToken(IToken.tLPAREN);
		validateInteger("0");
		validateToken(IToken.tCOMMA);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	
	public void testSpecExample3_3() throws Exception {
		StringBuffer sb = getExample3Defines();
		sb.append("p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) }; \n");
		
		// int i[] = { 1, 23, 4, 5, };
		initializeScanner(sb.toString());
		
		validateToken(IToken.t_int);
		validateIdentifier("i");
		validateToken(IToken.tLBRACKET);
		validateToken(IToken.tRBRACKET);
		validateToken(IToken.tASSIGN);
		validateToken(IToken.tLBRACE);
		validateInteger("1");
		validateToken(IToken.tCOMMA);
		validateInteger("23");
		validateToken(IToken.tCOMMA);
		validateInteger("4");
		validateToken(IToken.tCOMMA);
		validateInteger("5");
		validateToken(IToken.tCOMMA);
		validateToken(IToken.tRBRACE);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	public void testSpecExample3_4() throws Exception {
		StringBuffer sb = getExample3Defines();
		sb.append("char c[2][6] = { str(hello), str() }; \n");   //31
		
		// char c[2][6] = { "hello", "" }; //15
		initializeScanner(sb.toString());

		validateToken(IToken.t_char);
		validateIdentifier("c");
		validateToken(IToken.tLBRACKET);
		validateInteger("2");
		validateToken(IToken.tRBRACKET);
		validateToken(IToken.tLBRACKET);
		validateInteger("6");
		validateToken(IToken.tRBRACKET);
		validateToken(IToken.tASSIGN);
		validateToken(IToken.tLBRACE);
		validateString("hello");
		validateToken(IToken.tCOMMA);
		validateString("");
		validateToken(IToken.tRBRACE);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	private static StringBuffer getExample4Defines() {
		return new StringBuffer()
			.append("#define str(s) # s \n")
			.append("#define xstr(s) str(s) \n")
			.append("#define debug(s, t) printf(\"x\" # s \"= %d, x\" # t \"= %s\", \\ \n")
			.append("x ## s, x ## t) \n")
			.append("#define INCFILE(n) vers ## n \n")
			.append("#define glue(a, b) a ## b \n")
			.append("#define xglue(a, b) glue(a, b) \n")
			.append("#define HIGHLOW \"hello\" \n")
			.append("#define LOW LOW \", world\" \n");
	}
	

	public void testSpecExample4_1() throws Exception {
		StringBuffer sb = getExample4Defines();
		sb.append("debug(1, 2); \n");  //31
		
		// printf("x1= %d, x2= %s", x1, x2); // 9
		initializeScanner(sb.toString());
		
		validateIdentifier("printf");
		validateToken(IToken.tLPAREN);
		validateString("x1= %d, x2= %s");
		validateToken(IToken.tCOMMA);
		validateIdentifier("x1");
		validateToken(IToken.tCOMMA);
		validateIdentifier("x2");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	public void testSpecExample4_2() throws Exception {
		StringBuffer sb = getExample4Defines();
		sb.append("fputs(str(strncmp(\"abc\\0d\", \"abc\", '\\4') // this goes away   \n");
		sb.append("== 0) str(: @\\n), s); \n");
		
		// fputs( "strncmp(\"abc\\0d\", \"abc\", '\\4') == 0: @\n", s); // 7
		initializeScanner(sb.toString());
		
		validateIdentifier("fputs");
		validateToken(IToken.tLPAREN);
		validateString("strncmp(\\\"abc\\\\0d\\\", \\\"abc\\\", '\\\\4') == 0: @\\n");
		validateToken(IToken.tCOMMA);
		validateIdentifier("s");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	public void testSpecExample4_3() throws Exception {
		StringBuffer sb = getExample4Defines();
		sb.append("xglue(HIGH, LOW) \n");
		
		// "hello, world"
		initializeScanner(sb.toString());
		
		validateString("hello, world");
		validateEOF();
		validateProblemCount(0);
	}
	
	public void testSpecExample4_4() throws Exception {
		StringBuffer sb = getExample4Defines();
		sb.append("glue(HIGH, LOW); \n");
		
		// "hello";
		initializeScanner(sb.toString());

		validateString("hello");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	

	// #define t(x,y,z) x ## y ## z 
	// int j[] = { t(1,2,3), t(,4,5), t(6,,7), t(8,9,), t(10,,), t(,11,), t(,,12), t(,,) };
	public void testSpecExample5() throws Exception {
		// int j[] = {123, 45, 67, 89, 10, 11, 12, };
		initializeScanner();
		
		validateToken(IToken.t_int);
		validateIdentifier("j");
		validateToken(IToken.tLBRACKET);
		validateToken(IToken.tRBRACKET);
		validateToken(IToken.tASSIGN);
		validateToken(IToken.tLBRACE);
		validateInteger("123");
		validateToken(IToken.tCOMMA);
		validateInteger("45");
		validateToken(IToken.tCOMMA);
		validateInteger("67");
		validateToken(IToken.tCOMMA);
		validateInteger("89");
		validateToken(IToken.tCOMMA);
		validateInteger("10");
		validateToken(IToken.tCOMMA);
		validateInteger("11");
		validateToken(IToken.tCOMMA);
		validateInteger("12");
		validateToken(IToken.tCOMMA);
		validateToken(IToken.tRBRACE);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	public StringBuffer getExample7Defines() {
		return new StringBuffer()
			.append("#define debug(...) fprintf(stderr, __VA_ARGS__) \n ")
			.append("#define showlist(...) puts(#__VA_ARGS__)\n ")
			.append("#define report(test, ...) ((test)?puts(#test):\\ \n ")
			.append("printf(__VA_ARGS__))  \n ");
	}
	
	
	public void testSpecExample7_1() throws Exception {
		StringBuffer sb = getExample7Defines();
		sb.append("debug(\"Flag\"); \n");
		
		// fprintf(stderr, "Flag" ); //7
		initializeScanner(sb.toString());
		
		validateIdentifier("fprintf");
		validateToken(IToken.tLPAREN);
		validateIdentifier("stderr");
		validateToken(IToken.tCOMMA);
		validateString("Flag");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	public void testSpecExample7_2() throws Exception {
		StringBuffer sb = getExample7Defines();
		sb.append("debug(\"X = %d\\n\", x); \n");
		
		// fprintf(stderr, "X = %d\n", x ); //9
		initializeScanner(sb.toString());
		
		validateIdentifier("fprintf");
		validateToken(IToken.tLPAREN);
		validateIdentifier("stderr");
		validateToken(IToken.tCOMMA);
		validateString("X = %d\\n");
		validateToken(IToken.tCOMMA);
		validateIdentifier("x");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);	
		validateEOF();
		validateProblemCount(0);
	}
	
	
	public void testSpecExample7_3() throws Exception {
		StringBuffer sb = getExample7Defines();
		sb.append("showlist(The first, second, and third items.); \n");
		
		// puts( "The first, second, and third items." ); //5
		initializeScanner(sb.toString());
		
		validateIdentifier("puts");
		validateToken(IToken.tLPAREN);
		validateString("The first, second, and third items.");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	
	public void testSpecExample7_4() throws Exception {
		StringBuffer sb = getExample7Defines();
		sb.append("report(x>y, \"x is %d but y is %d\", x, y); \n");
		
		// ( (x>y) ? puts("x>y") : printf("x is %d but y is %d", x, y) ); //22
		initializeScanner(sb.toString());
		
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tLPAREN);
		validateIdentifier("x");
		validateToken(IToken.tGT);
		validateIdentifier("y");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tQUESTION);
		validateIdentifier("puts");
		validateToken(IToken.tLPAREN);
		validateString("x>y");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tCOLON);
		validateIdentifier("printf");
		validateToken(IToken.tLPAREN );
		validateString("x is %d but y is %d");
		validateToken(IToken.tCOMMA);
		validateIdentifier("x");
		validateToken(IToken.tCOMMA);
		validateIdentifier("y");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	
	// #define foo g g g
	// #define g f##oo
	// foo
	public void testRecursiveExpansion() throws Exception {
		initializeScanner();
		
		validateIdentifier("foo");
		validateIdentifier("foo");
		validateIdentifier("foo");
		validateEOF();
		validateProblemCount(0);
	}
	
	
	// #define m !(m)+n 
	// #define n(n) n(m)
	// m(m)
	public void testRecursiveExpansion2() throws Exception {
		// !(m)+ !(m)+n(!(m)+n)
		initializeScanner();
		
		validateToken(IToken.tNOT);
		validateToken(IToken.tLPAREN);
		validateIdentifier("m");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tPLUS);
		validateToken(IToken.tNOT);
		validateToken(IToken.tLPAREN);
		validateIdentifier("m");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tPLUS);
		validateIdentifier("n");
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tNOT);
		validateToken(IToken.tLPAREN);
		validateIdentifier("m");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tPLUS);
		validateIdentifier("n");
		validateToken(IToken.tRPAREN);
		validateEOF();
		validateProblemCount(0);
	}
	
	
	// #define f g
	// #define cat(a,b) a ## b
	// #define g bad
	// cat(f, f)
	public void testRecursiveExpansion3() throws Exception {
		// ff
		initializeScanner();
		
		validateIdentifier("ff");
		validateEOF();
		validateProblemCount(0);
	}
	
	
	//	f(2 * (y+1)) + f(2 * (f(2 * (z[0])))) % f(2 * (0)) + t(1);
	//	f(2 * (2+(3,4)-0,1)) | f(2 * (~ 5)) & f(2 * (0,1))^m(0,1);
	//	int i[] = { 1, 23, 4, 5, };
	//	char c[2][6] = { "hello", "" };
	//  --
	//	#define x 3
	//	#define f(a) f(x * (a))
	//	#undef x
	//	#define x 2
	//	#define g f
	//	#define z z[0]
	//	#define h g(~
	//	#define m(a) a(w)
	//	#define w 0,1
	//	#define t(a) a
	//	#define p() int
	//	#define q(x) x
	//	#define r(x,y) x ## y
	//	#define str(x) # x
	//
	//	f(y+1) + f(f(z)) % t(t(g)(0) + t)(1);
	//	g(x+(3,4)-w) | h 5) & m
	//	(f)^m(m);
	//	p() i[q()] = { q(1), r(2,3), r(4,), r(,5), r(,) };
	//	char c[2][6] = { str(hello), str() };
	public void testC99_6_7_5_3_5_Bug104869() throws Exception {
		initializeScanner();
		// read in expected tokens
		List<IToken> expect= new ArrayList<IToken>();
		IToken t= fScanner.nextToken();
		while(t.getType() != IToken.tDECR) {
			expect.add(t);
			t= fScanner.nextToken();
		}
		
		for (IToken et : expect) {
			t= fScanner.nextToken();
			assertEquals(et.getImage(), t.getImage());
			assertEquals(et.getType(), t.getType());
		}
		validateEOF();
		validateProblemCount(0);
	}
	
	//	#define hash_hash # ## #
	//	#define mkstr(a) # a
	//	#define in_between(a) mkstr(a)
	//	#define join(c, d) in_between(c hash_hash d)
	//  join(x, y)
	public void testC99_6_10_3_3_4_Bug84268() throws Exception {
		initializeScanner();
		validateString("x ## y");
		validateEOF();
		validateProblemCount(0);
	}
	
	// #define BIN 0b10101010
	// #define HEX 0xAA
	// #define OCT 0252
	// #define DEC 170
	// #if (BIN == HEX && HEX == OCT && OCT == DEC)
	// int foo = BIN;
	// #endif
	public void testGCC43BinaryNumbers() throws Exception {
		initializeScanner();
		validateToken(IToken.t_int);
		validateIdentifier("foo");
		validateToken(IToken.tASSIGN);
		validateInteger("0b10101010");
		validateToken(IToken.tSEMI);
		validateEOF();
		validateProblemCount(0);
	}
	
	public void testBadBinaryNumbersC() throws Exception {
		String badbinary = "{0b012, 0b01b, 0b1111e01, 0b1111p10, 0b10010.10010}";
		initializeScanner(badbinary, ParserLanguage.C);
		fullyTokenize();
		validateProblemCount(5);
		validateProblem(0, IProblem.SCANNER_BAD_BINARY_FORMAT, null);
		validateProblem(1, IProblem.SCANNER_CONSTANT_WITH_BAD_SUFFIX, "b");
		validateProblem(2, IProblem.SCANNER_FLOAT_WITH_BAD_PREFIX, "0b");
		validateProblem(3, IProblem.SCANNER_CONSTANT_WITH_BAD_SUFFIX, "p10");
		validateProblem(4, IProblem.SCANNER_FLOAT_WITH_BAD_PREFIX, "0b");
	}
	
	public void testBadBinaryNumbersCPP() throws Exception {
		// First, third, and fift are invalid in c++11
		String badbinary = "{0b012, 0b01b, 0b1111e01, 0b1111p10, 0b10010.10010}";
		initializeScanner(badbinary);
		fullyTokenize();
		validateProblemCount(3);
		validateProblem(0, IProblem.SCANNER_BAD_BINARY_FORMAT, null);
		validateProblem(1, IProblem.SCANNER_FLOAT_WITH_BAD_PREFIX, "0b");
		validateProblem(2, IProblem.SCANNER_FLOAT_WITH_BAD_PREFIX, "0b");
	}
	
	// #if 123ASDF
	// #endif
	// #if 0xU
	// #endif
	public void testUDLInPP() throws Exception {
		initializeScanner();
		validateEOF();
		validateProblemCount(2);
		validateProblem(0, IProblem.SCANNER_CONSTANT_WITH_BAD_SUFFIX, "ASDF");
		validateProblem(1, IProblem.SCANNER_CONSTANT_WITH_BAD_SUFFIX, "xU");
	}
}
