/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.parser.scanner.MacroExpander;
import org.eclipse.cdt.internal.core.parser.scanner.MacroExpansionTracker;
import org.eclipse.text.edits.ReplaceEdit;


public class ExpansionExplorerTests extends BaseTestCase {
	
	public static TestSuite suite() {
		return suite(ExpansionExplorerTests.class);
	}
	
	private void performTest(int steps) throws Exception {
		StringBuffer[] bufs= TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), steps+2);
		String[] input= new String[steps+2];
		int i= -1;
		for (StringBuffer buf : bufs) {
			input[++i]= buf.toString().trim();
		}
		final MacroExpander expander= createExpander(input[0]);
		final String original= input[1];
		
		verifyStepCount(expander, original, steps);

		verifyStep(expander, original, Integer.MAX_VALUE, original, input[steps+1]);
		for (i= 0; i < steps; i++) {
			verifyStep(expander, original, i, input[i+1], input[i+2]);
		}
	}

	private void verifyStepCount(MacroExpander expander, String original, int steps) {
		MacroExpansionTracker tracker= new MacroExpansionTracker(Integer.MAX_VALUE);
		expander.expand(original, tracker, "", 1, false);
		assertEquals(steps, tracker.getStepCount());
	}

	private void verifyStep(MacroExpander expander, String original, int step, String expectedPre,
			String expectedPost) {
		MacroExpansionTracker tracker= new MacroExpansionTracker(step);
		expander.expand(original, tracker, "", 1, false);
		String pre = tracker.getCodeBeforeStep();
		ReplaceEdit replacement = tracker.getReplacement();
		assertNotNull(pre);
		assertNotNull(replacement);
		String post= apply(pre, replacement);
		
		assertEquals("incorrect value pre " + step, expectedPre, pre);
		assertEquals("incorrect value post " + step, expectedPost, post);
	}

	private String apply(String pre, ReplaceEdit replacement) {
		StringBuilder buf= new StringBuilder();
		buf.append(pre, 0, replacement.getOffset());
		buf.append(replacement.getText());
		buf.append(pre, replacement.getExclusiveEnd(), pre.length());
		return buf.toString();
	}

	private MacroExpander createExpander(final String macrodefs) throws OffsetLimitReachedException {
		CPreprocessor cpp= new CPreprocessor(FileContent.create("<macro-expander>", macrodefs.toCharArray()),
				new ScannerInfo(), ParserLanguage.C, new NullLogService(), 
				GCCScannerExtensionConfiguration.getInstance(), IncludeFileContentProvider.getEmptyFilesProvider());
		int type;
		do {
			type= cpp.nextTokenRaw().getType();
		} while (type != IToken.tEND_OF_INPUT);
		return (MacroExpander) cpp.getAdapter(MacroExpander.class);
	}
	
	// #define A
	
	// B
	
	// B
	public void testNoOp() throws Exception {
		performTest(0);
	}
	
	// #define A B
	
	// A
	
	// B
	public void testObject() throws Exception {
		performTest(1);
	}

	// #define A A1
	// #define A1 A2
	// #define A2 A
	
	// A
	
	// A1
	
	// A2
	
	// A
	public void testObjectChain() throws Exception {
		performTest(3);
	}

	// #define A(x) B+x
	
	// A(c)
	
	// B+c
	public void testFunction() throws Exception {
		performTest(1);
	}
	

	// #define A(x) x+x
	// #define _t t
	
	// A(_t)
	
	// A(t)
	
	// t+t
	public void testFunctionParam() throws Exception {
		performTest(2);
	}

	// #define A(x,y) x+y
	// #define _t t
	
	// A(_t, _t)
	
	// A(t, _t)

	// A(t, t)

	// t+t
	public void test2Params() throws Exception {
		performTest(3);
	}

	// #define A(x,y,z) x + y + z
	// #define _t t
	
	// A ( _t , , _t )
	
	// A ( t , , _t )

	// A ( t , , t )

	// t +  + t
	public void test3Params() throws Exception {
		performTest(3);
	}
	
	// #define m !(m)+n 
	// #define n(n) n(m)
	
	// m(m)
	
	// !(m)+n(m)
	
	// !(m)+n(!(m)+n)
	
	// !(m)+!(m)+n(m)
	
	// !(m)+!(m)+n(!(m)+n)
    public void testRecursiveExpansion() throws Exception {
    	performTest(4);
    }

    // #define f(x,y) (x + y)
    // #define g(x,y) (x*y)
    // #define _a a
    // #define _b b
    
    // f( g(_a,_b), g(_b,_a) )

    // f( g(a,_b), g(_b,_a) )

    // f( g(a,b), g(_b,_a) )

    // f( (a*b), g(_b,_a) )

    // f( (a*b), g(b,_a) )

    // f( (a*b), g(b,a) )
    
    // f( (a*b), (b*a) )

    // ((a*b) + (b*a))
    public void testNestedFunctions() throws Exception {
    	performTest(7);
    }
    
    // #define id(x) x
    
    // id(
    //    id(a))
    
    // id(
    //    a)
    
    // a
    public void testNewline() throws Exception {
    	performTest(2);
    }
    
    // #define f x  _a  _b  x
    // #define _a a
    // #define _b b
    
    // f
     
    // x  _a  _b  x
    
    // x  a  _b  x
    
    // x  a  b  x
    public void testSpace() throws Exception {
    	performTest(3);
    }
    
    // #define L __LINE__
    // #define x(a) a
    
    // x(L)

    // x(__LINE__)

    // x(1)
    
    // 1
    public void testLineNumber() throws Exception {
    	performTest(3);
    }
    
    // #define L __LINE__
    // #define x(a,b) a,b
    
    // x(L,
    //   L)

    // x(__LINE__,
    //   L)

    // x(2,
    //   L)

    // x(2,
    //   __LINE__)

    // x(2,
    //   2)
    
    // 2,2
    public void testLineNumber2() throws Exception {
    	performTest(5);
    }

    // #define str(x) #x
    // #define xstr(x) str(x)
    
    // xstr(__LINE__)
    
    // xstr(1)
    
    // str(1)
    
    // "1"
    public void testStringify() throws Exception {
    	performTest(3);
    }
    
    // #define vararg(x, y...) bla(x, y)
    // #define _p p
    
    // vararg( _p );

    // vararg( p );

    // bla(p, );
    public void testVararg1() throws Exception {
    	performTest(2);
    }

    // #define vararg(x, y...) bla(x, ##y)
    // #define _p p
    
    // vararg( _p );

    // vararg( p );

    // bla(p);
    public void testVararg1x() throws Exception {
    	performTest(2);
    }

    // #define vararg(x, y...) bla(x, y)
    // #define _p p
    
    // vararg( _p , _p );

    // vararg( p , _p );

    // vararg( p , p );

    // bla(p, p);
    public void testVararg2() throws Exception {
    	performTest(3);
    }

    // #define vararg(x, y...) bla(x,  ## y)
    // #define _p p
    
    // vararg( _p , _p );

    // vararg( p , _p );

    // vararg( p , p );

    // bla(p,  p);
    public void testVararg2x() throws Exception {
    	performTest(3);
    }

    // #define func2(x,y) (x,y)
    // #define _p p
    
    // func2(_p);
    
    // func2(p);
    
    // (p,);
    public void testTooFewArgs() throws Exception {
    	performTest(2);
    }
}
