/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.IOException;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;

public class ASTNodeSelectorTest extends AST2BaseTest {

	static public TestSuite suite() {
		return suite(ASTNodeSelectorTest.class);
	}

	protected String fCode;
	protected IASTTranslationUnit fTu;
	protected IASTNodeSelector fSelector;
	
	public ASTNodeSelectorTest() {
	}

	public ASTNodeSelectorTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createTranslationUnit();
	}

	protected void createTranslationUnit() throws IOException {
		fCode= getContents(1)[0].toString();
        FileContent codeReader = FileContent.create("<test-code>", fCode.toCharArray());
        ScannerInfo scannerInfo = new ScannerInfo();
        IScanner scanner= AST2BaseTest.createScanner(codeReader, ParserLanguage.CPP, ParserMode.COMPLETE_PARSE, scannerInfo);
        GNUCPPSourceParser parser= new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, new NullLogService(), new GPPParserExtensionConfiguration());
        fTu= parser.parse();
        fSelector= fTu.getNodeSelector(null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void testContainedName(int from, int to, String sig) {
		IASTName name= fSelector.findFirstContainedName(from, to-from);
		verify(sig, name);
	}

	private void verify(String sig, IASTNode node) {
		if (sig == null) {
			assertNull("unexpexted selection: " + (node == null ? "" : node.getRawSignature()), node);
		}
		else {
			assertNotNull("unable to select " + sig, node);
			if (node instanceof IASTName) {
				assertEquals(sig, ((IASTName) node).toString());
			}
			else {
				assertEquals(sig, node.getRawSignature());
			}
		}
	}

	private void testContainedNode(int from, int to, String sig) {
		IASTNode node= fSelector.findFirstContainedNode(from, to-from);
		verify(sig, node);
	}

	private void testName(int from, int to, String sig) {
		IASTName name= fSelector.findName(from, to-from);
		verify(sig, name);
	}

	private void testNode(int from, int to, String sig) {
		IASTNode node= fSelector.findNode(from, to-from);
		verify(sig, node);
	}

	private void testEnclosingName(int from, int to, String sig) {
		IASTName name= fSelector.findEnclosingName(from, to-from);
		verify(sig, name);
	}

	private void testEnclosingNode(int from, int to, String sig) {
		IASTNode node= fSelector.findEnclosingNode(from, to-from);
		verify(sig, node);
	}

	private void testExpansion(int from, int to, String sig) {
		IASTPreprocessorMacroExpansion exp= fSelector.findEnclosingMacroExpansion(from, to-from);
		verify(sig, exp);
	}

	// #define shift_offsets
	// int shift= shift_offsets;
	//
	// #include <test>
	// int a;
	public void testInclusion() {
		int include_start= fCode.indexOf("#include");
		int name_start= fCode.indexOf("test");
		int include_end= fCode.indexOf(">") + 1;
		
		testContainedName(include_start-1, include_end+1, "test");
		testContainedName(name_start, include_end, "test");
		testContainedName(include_start+1, name_start+1, null);
		testContainedName(name_start+1, name_start+7, null);

		testContainedNode(include_start-1, include_end+1, "#include <test>");
		testContainedNode(name_start, include_end, "test");
		testContainedNode(include_start+1, name_start+1, null);
		testContainedNode(name_start+1, name_start+7, null);
		
		testEnclosingName(name_start, name_start+4, "test");
		testEnclosingName(name_start, name_start, "test");
		testEnclosingName(name_start+4, name_start+4, "test");
		testEnclosingName(name_start-1, name_start+1, null);
		testEnclosingName(name_start+4, name_start+5, null);

		testEnclosingNode(name_start, name_start+4, "test");
		testEnclosingNode(name_start, name_start, "test");
		testEnclosingNode(name_start+4, name_start+4, "test");
		testEnclosingNode(name_start-1, name_start+1, "#include <test>");
		testEnclosingNode(name_start+4-1, name_start+4+1, "#include <test>");
		
		testExpansion(name_start, name_start+4, null);
	}

	// #define shift_offsets
	// int shift= shift_offsets;
	//
	// #define EMPTY
	// #define TEST_H "test.h"
	// void func() {
	// #include EMPTY TEST_H
	// }
	public void testInclusionWithExpansions() {
		int inclusion_start= fCode.indexOf("#include");
		int empty_start= fCode.indexOf("EMPTY", inclusion_start);
		int testh_start= fCode.indexOf("TEST_H", empty_start);
		int file_end= fCode.length();
		
		testContainedName(inclusion_start-1, file_end-1, "EMPTY");
		testContainedName(testh_start, file_end, "TEST_H");
		testContainedName(testh_start+1, file_end, null);
		testContainedName(testh_start, testh_start+5, null);

		testContainedNode(inclusion_start-1, file_end+1, "#include EMPTY TEST_H");
		testContainedNode(testh_start, file_end, "TEST_H");
		testContainedNode(testh_start+1, file_end, null);
		testContainedNode(testh_start, testh_start+5, null);
		
		testName(empty_start, empty_start+5, "EMPTY");
		testName(empty_start-1, empty_start+5, null);
		testName(empty_start+1, empty_start+5, null);
		testName(empty_start, empty_start+4, null);
		testName(empty_start, empty_start+6, null);

		testNode(empty_start, empty_start+5, "EMPTY");
		testNode(empty_start-1, empty_start+5, null);
		testNode(empty_start+1, empty_start+5, null);
		testNode(empty_start, empty_start+4, null);
		testNode(empty_start, empty_start+6, null);

		testEnclosingName(empty_start, empty_start+5, "EMPTY");
		testEnclosingName(empty_start, empty_start, "EMPTY");
		testEnclosingName(empty_start+5, empty_start+5, "EMPTY");
		testEnclosingName(empty_start-1, empty_start, null);
		testEnclosingName(empty_start+5, empty_start+6, "test.h");
		testEnclosingName(testh_start, testh_start+6, "TEST_H");
		testEnclosingName(testh_start, testh_start, "TEST_H");
		testEnclosingName(testh_start+6, testh_start+6, "TEST_H");
		testEnclosingName(testh_start-1, testh_start+1, "test.h");
		testEnclosingName(testh_start+5, testh_start+7, null);

		testEnclosingNode(empty_start, empty_start+5, "EMPTY");
		testEnclosingNode(empty_start, empty_start, "EMPTY");
		testEnclosingNode(empty_start+5, empty_start+5, "EMPTY");
		testEnclosingNode(empty_start-1, empty_start, "#include EMPTY TEST_H");
		testEnclosingNode(empty_start+5, empty_start+6, "test.h");
		testEnclosingNode(testh_start, testh_start+6, "TEST_H");
		testEnclosingNode(testh_start, testh_start, "TEST_H");
		testEnclosingNode(testh_start+6, testh_start+6, "TEST_H");
		testEnclosingNode(testh_start-1, testh_start+1, "test.h");
		testEnclosingNode(testh_start+6-1, testh_start+6+1, "{\n #include EMPTY TEST_H\n }");
		
		testExpansion(empty_start, empty_start+5, "EMPTY");
		testExpansion(empty_start, empty_start, "EMPTY");
		testExpansion(empty_start+5, empty_start+5, "EMPTY");
		testExpansion(empty_start-1, empty_start, null);
		testExpansion(empty_start+5, empty_start+6, null);
		testExpansion(testh_start, testh_start+6, "TEST_H");
	}
	
	
	// #define shift_offsets
	// int shift= shift_offsets;
	//
	// #define xx 1
	// #if  xx == 2
	// #elif xx == 1
	// #endif
	public void testMacroInConditionalExpression() {
		int x1= fCode.indexOf("xx");
		int x2= fCode.indexOf("xx", x1+1);
		int x3= fCode.indexOf("xx", x2+1);
		
		testContainedName(x1, x1+2, "xx");
		testContainedName(x2-1, x2+2, "xx");
		testContainedName(x3, x3+3, "xx");
		testContainedName(x1, x1+1, null);
		testContainedName(x2+1, x2+2, null);
		testContainedName(x3+1, x3+1, null);

		testName(x1, x1+2, "xx");
		testName(x2, x2+2, "xx");
		testName(x3, x3+2, "xx");
		testName(x1+1, x1+2, null);
		testName(x2-1, x2+2, null);
		testName(x3, x3+3, null);
		testName(x3, x3+1, null);

		testEnclosingName(x1, x1+2, "xx");
		testEnclosingName(x2+2, x2+2, "xx");
		testEnclosingName(x3, x3, "xx");
		testEnclosingName(x1-1, x1+2, null);
		testEnclosingName(x2+2, x2+3, null);
		testEnclosingName(x3-1, x3-1, null);

		testExpansion(x1, x1+2, null);
		testExpansion(x2+2, x2+2, "xx");
		testExpansion(x3, x3, "xx");
		testExpansion(x2+2, x2+3, null);
		testExpansion(x3-1, x3-1, null);
	}

	// #define shift_offsets
	// int shift= shift_offsets;
	//
	// #define xx 1
	// #if !defined(xx)
	// #elif defined(xx) == 1
	// #endif
	public void testMacroInDefinedExpression() {
		int x1= fCode.indexOf("xx");
		int x2= fCode.indexOf("xx", x1+1);
		int x3= fCode.indexOf("xx", x2+1);
		
		testContainedName(x1, x1+2, "xx");
		testContainedName(x2-1, x2+2, "xx");
		testContainedName(x3, x3+3, "xx");
		testContainedName(x1, x1+1, null);
		testContainedName(x2+1, x2+2, null);
		testContainedName(x3+1, x3+1, null);

		testName(x1, x1+2, "xx");
		testName(x2, x2+2, "xx");
		testName(x3, x3+2, "xx");
		testName(x1+1, x1+2, null);
		testName(x2-1, x2+2, null);
		testName(x3, x3+3, null);
		testName(x3, x3+1, null);

		testEnclosingName(x1, x1+2, "xx");
		testEnclosingName(x2+2, x2+2, "xx");
		testEnclosingName(x3, x3, "xx");
		testEnclosingName(x1-1, x1+2, null);
		testEnclosingName(x2+2, x2+3, null);
		testEnclosingName(x3-1, x3-1, null);

		testExpansion(x1, x1+2, null);
		testExpansion(x2+2, x2+2, null);
		testExpansion(x3, x3, null);
		testExpansion(x2+2, x2+3, null);
		testExpansion(x3-1, x3-1, null);
	}

	// #define shift_offsets
	// int shift= shift_offsets;
	//
	// #define xx 1
	// #ifndef xx
	// #endif
	// #ifdef xx
	// #endif
	// #undef xx
	public void testMacroInConditional() {
		int x1= fCode.indexOf("xx");
		x1= fCode.indexOf("xx", x1+1);
		int x2= fCode.indexOf("xx", x1+1);
		int x3= fCode.indexOf("xx", x2+1);
		
		testContainedName(x1, x1+2, "xx");
		testContainedName(x2-1, x2+2, "xx");
		testContainedName(x3, x3+3, "xx");
		testContainedName(x1, x1+1, null);
		testContainedName(x2+1, x2+2, null);
		testContainedName(x3+1, x3+1, null);

		testName(x1, x1+2, "xx");
		testName(x2, x2+2, "xx");
		testName(x3, x3+2, "xx");
		testName(x1+1, x1+2, null);
		testName(x2-1, x2+2, null);
		testName(x3, x3+3, null);
		testName(x3, x3+1, null);

		testEnclosingName(x1, x1+2, "xx");
		testEnclosingName(x2+2, x2+2, "xx");
		testEnclosingName(x3, x3, "xx");
		testEnclosingName(x1-1, x1+2, null);
		testEnclosingName(x2+2, x2+3, null);
		testEnclosingName(x3-1, x3-1, null);

		testExpansion(x1, x1+2, null);
		testExpansion(x2+2, x2+2, null);
		testExpansion(x3, x3, null);
		testExpansion(x2+2, x2+3, null);
		testExpansion(x3-1, x3-1, null);
	}
	
	// #define shift_offsets
	// int shift= shift_offsets;
	//
	// #define IMPLICIT 1
	// #define EXPLICIT IMPLICIT
	// int a= EXPLICIT;
	public void testUnreachableImplicitMacro() {
		int x1= fCode.indexOf("EXPLICIT;");
		testContainedName(x1, fCode.length(), "EXPLICIT");
		testName(x1, x1+8, "EXPLICIT");
		testEnclosingName(x1, x1, "EXPLICIT");
	}

	// #define shift_offsets
	// int shift= shift_offsets;
	//
	// #define NESTED 1
	// #define EXPLICIT(x) x
	// int a= EXPLICIT(NESTED);
	public void testReachableNestedMacro() {
		int x1= fCode.indexOf("NESTED)");
		testContainedName(x1, fCode.length(), "NESTED");
		testName(x1, x1+6, "NESTED");
		testEnclosingName(x1, x1, "NESTED");
	}
	
	// #define id(x,y) x y
	// id(int a, =1);
	// id(int b=, a);
	public void testImageLocations() {
		int a1= fCode.indexOf("a");
		int a2= fCode.indexOf("a", a1+1);
		int b1= fCode.indexOf("b");

		testName(a1, a1+1, "a");
		testContainedName(a1-1, a2+2, "a");
		testEnclosingName(a1, a1, "a");
		testEnclosingName(a1+1, a1+1, "a");

		testName(a2, a2+1, "a");
		testContainedName(a2-1, a2+2, "a");
		testEnclosingName(a2, a2, "a");
		testEnclosingName(a2+1, a2+1, "a");

		testEnclosingNode(a1-1, a1+1, "id(int a, =1)");
		testContainedNode(a1-8, a1+1, "id");
	}
	
	// namespace ns {int a;}
	// int x= ns::a;
	// #define M int b;
	// M
	// #define N() int c;
	// N()
	// #define O
	// #define P()
	// P()O
	public void testOrdering() {
		int x1= fCode.indexOf("ns::a");
		int x2= x1 + "ns::a".length();
		testContainedName(x1, x2, "ns");
		testEnclosingName(x2-1, x2-1, "a");
		testEnclosingName(x2, x2, "a");
		
		x1= fCode.indexOf("M"); x1= fCode.indexOf("M", x1+1);
		testNode(x1, x1+1, "M");
		testEnclosingNode(x1, x1, "M");
		testEnclosingNode(x1+1, x1+1, "M");
		testContainedNode(x1-1, x1+2, "M");

		x1= fCode.indexOf("N"); x1= fCode.indexOf("N", x1+1);
		testNode(x1, x1+1, "N");
		testEnclosingNode(x1, x1, "N");
		testEnclosingNode(x1+1, x1+1, "N");
		testContainedNode(x1-1, x1+2, "N");
		
		x1= fCode.indexOf("O"); x1= fCode.indexOf("O", x1+1);
		testNode(x1, x1+1, "O");
		testEnclosingNode(x1, x1, "O");
		testEnclosingNode(x1+1, x1+1, "O");
		testContainedNode(x1-1, x1+2, "O");
	}
	
	// #define MACRO void m
	// MACRO();
	public void testEnclosingAMacro() {
		int x1= fCode.indexOf("MACRO(");
		int x2= x1 + "MACRO(".length();
		testContainedName(x1, x2, "MACRO");
		testEnclosingNode(x1, x2, "MACRO();");
	}
}
