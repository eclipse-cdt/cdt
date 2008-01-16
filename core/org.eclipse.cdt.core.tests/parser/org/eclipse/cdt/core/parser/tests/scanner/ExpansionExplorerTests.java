/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.NullCodeReaderFactory;
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

		verifyStep(expander, original, Integer.MAX_VALUE, original, input[steps+1]);
		for (i= 0; i < steps; i++) {
			verifyStep(expander, original, i, input[i+1], input[i+2]);
		}
	}

	private void verifyStep(MacroExpander expander, String original, int step, String expectedPre,
			String expectedPost) {
		MacroExpansionTracker tracker= new MacroExpansionTracker(step);
		expander.expand(original, tracker);
		String pre = tracker.getCodeBeforeStep();
		ReplaceEdit replacement = tracker.getReplacement();
		assertNotNull(pre);
		assertNotNull(replacement);
		String post= apply(pre, replacement);
		
		assertEquals(expectedPre, pre);
		assertEquals(expectedPost, post);
	}

	private String apply(String pre, ReplaceEdit replacement) {
		StringBuilder buf= new StringBuilder();
		buf.append(pre, 0, replacement.getOffset());
		buf.append(replacement.getText());
		buf.append(pre, replacement.getExclusiveEnd(), pre.length());
		return buf.toString();
	}

	private MacroExpander createExpander(final String macrodefs) throws OffsetLimitReachedException {
		CPreprocessor cpp= new CPreprocessor(new CodeReader(macrodefs.toCharArray()),
				new ScannerInfo(), ParserLanguage.C, new NullLogService(), 
				new GCCScannerExtensionConfiguration(), NullCodeReaderFactory.getInstance());
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
		performTest(1);
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
    


}
