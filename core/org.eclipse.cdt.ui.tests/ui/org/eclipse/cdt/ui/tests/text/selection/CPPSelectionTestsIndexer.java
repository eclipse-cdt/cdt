/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.selection;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import junit.framework.Test;

/**
 * Test Ctrl-F3/F3 with the DOM Indexer for a C++ project.
 */
public class CPPSelectionTestsIndexer extends BaseSelectionTestsIndexer {
	protected String sourceIndexerID;
	protected IIndex index;

	public CPPSelectionTestsIndexer(String name) {
		super(name);
		sourceIndexerID = IPDOMManager.ID_FAST_INDEXER;
	}

	public static Test suite() {
		return suite(CPPSelectionTestsIndexer.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Create temp project
		fCProject = createProject("CPPSelectionTestsDOMIndexerProject");
		assertNotNull("Unable to create project", fCProject);
		//		MakeProjectNature.addNature(project, new NullProgressMonitor());
		//		ScannerConfigNature.addScannerConfigNature(project);
		//		PerProjectSICollector.calculateCompilerBuiltins(project);

		CCorePlugin.getIndexManager().setIndexerId(fCProject, sourceIndexerID);
		index = CCorePlugin.getIndexManager().getIndex(fCProject);
	}

	@Override
	protected void tearDown() throws Exception {
		closeAllEditors();
		CProjectHelper.delete(fCProject);
		super.tearDown();
	}

	private ICProject createProject(String projectName) throws CoreException {
		ICProject cPrj = CProjectHelper.createCCProject(projectName, "bin", IPDOMManager.ID_NO_INDEXER);
		return cPrj;
	}

	protected StringBuilder[] getContents(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "ui",
				CPPSelectionTestsIndexer.class, getName(), sections);
	}

	private void assertNode(String name, int offset, IASTNode node) {
		assertNotNull(node);
		assertEquals(node.toString(), name);
		IASTFileLocation loc = node.getFileLocation();
		assertEquals(loc.getNodeOffset(), offset);
		assertEquals(loc.getNodeLength(), name.length());
	}

	// // header
	// class Point{
	//  public:
	//     Point(): xCoord(0){}
	//     Point& operator=(const Point &rhs){return *this;}
	//     void* operator new [ ] (unsigned int);
	//  private:
	//     int xCoord;
	//  };

	// // source
	// #include "test93281.h"
	// static const Point zero;
	// int main(int argc, char **argv) {
	//    Point *p2 = new Point();
	//    p2->    operator // /* operator */ // F3 in the middle
	//    // of "operator" should work
	//    // \
	//    /* */
	//    =(zero);           // line B
	//    p2->operator /* oh yeah */ new // F3 in the middle of "operator"
	//    // should work
	//    //
	//    [ /* sweet */ ] //
	//    (2);
	//    return (0);
	// }
	public void testBug93281() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("test93281.h", hcode);
		IFile file = importFile("test93281.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int offset = scode.indexOf("p2->operator") + 6;
		IASTNode node = testF3(file, offset);

		assertTrue(node instanceof IASTName);
		assertEquals(((IASTName) node).toString(), "operator new[]");
		assertEquals(((ASTNode) node).getOffset(), hcode.indexOf("operator new"));
		assertEquals(((ASTNode) node).getLength(), 16);

		offset = scode.indexOf("p2->    operator") + 11;
		node = testF3(file, offset);

		assertTrue(node instanceof IASTName);
		assertEquals(((IASTName) node).toString(), "operator =");
		assertEquals(((ASTNode) node).getOffset(), hcode.indexOf("operator="));
		assertEquals(((ASTNode) node).getLength(), 9);
	}

	//	template <class T>
	//	inline void testTemplate(T& aRef);
	//
	//	class Temp {
	//	};

	//	#include <stdio.h>
	//	#include <stdlib.h>
	//	#include "test.h"
	//	int main(void) {
	//	        puts("Hello World!!!");
	//
	//	        Temp testFile;
	//	        testTemplate(testFile);
	//
	//	        return EXIT_SUCCESS;
	//	}
	public void testBug207320() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("test.h", hcode);
		IFile file = importFile("test.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int hoffset = hcode.indexOf("testTemplate");
		int soffset = scode.indexOf("testTemplate");
		IASTNode def = testF3(file, soffset + 2);
		assertTrue(def instanceof IASTName);
		assertEquals("testTemplate", ((IASTName) def).toString());
		assertEquals(hoffset, ((ASTNode) def).getOffset());
		assertEquals(12, ((ASTNode) def).getLength());
	}

	// template<typename T>
	// class C {
	//   public: void assign(const T* s) {}
	// };

	// #include "testTemplateClassMethod.h"
	// void main() {
	//   C<char> a;
	//   a.assign("aaa");
	// }
	public void testTemplateClassMethod_207320() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testTemplateClassMethod.h", hcode);
		IFile file = importFile("testTemplateClassMethod.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int hoffset = hcode.indexOf("assign");
		int soffset = scode.indexOf("assign");
		IASTNode def = testF3(file, soffset + 2);
		assertTrue(def instanceof IASTName);
		assertEquals("assign", ((IASTName) def).toString());
		assertEquals(hoffset, ((ASTNode) def).getOffset());
		assertEquals(6, ((ASTNode) def).getLength());
	}

	// // the header
	// extern int MyInt;       // MyInt is in another file
	// extern const int MyConst;   // MyConst is in another file
	// void MyFunc(int);       // often used in header files
	// typedef int NewInt;     // a normal typedef statement
	// struct MyStruct { int Member1; int Member2; };
	// class MyClass { int MemberVar; };

	// #include "basicDefinition.h"
	// int MyInt;
	// extern const int MyConst = 42;
	// void MyFunc(int a) { cout << a << endl; }
	// class MyClass;
	// struct MyStruct;
	public void testBasicDefinition() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("basicDefinition.h", hcode);
		IFile file = importFile("testBasicDefinition.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int hoffset = hcode.indexOf("MyInt");
		int soffset = scode.indexOf("MyInt");
		IASTNode decl = testF3(file, soffset + 2);
		IASTNode def = testF3(hfile, hoffset + 2);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyInt", ((IASTName) decl).toString());
		assertEquals(hoffset, ((ASTNode) decl).getOffset());
		assertEquals(5, ((ASTNode) decl).getLength());
		assertEquals("MyInt", ((IASTName) def).toString());
		assertEquals(soffset, def.getFileLocation().getNodeOffset());
		assertEquals(5, ((ASTNode) def).getLength());

		hoffset = hcode.indexOf("MyConst");
		soffset = scode.indexOf("MyConst");
		decl = testF3(file, soffset + 2);
		def = testF3(hfile, hoffset + 2);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyConst", ((IASTName) decl).toString());
		assertEquals(hoffset, ((ASTNode) decl).getOffset());
		assertEquals(7, ((ASTNode) decl).getLength());
		assertEquals("MyConst", ((IASTName) def).toString());
		assertEquals(soffset, def.getFileLocation().getNodeOffset());
		assertEquals(7, ((ASTNode) def).getLength());

		hoffset = hcode.indexOf("MyFunc");
		soffset = scode.indexOf("MyFunc");
		decl = testF3(file, soffset + 2);
		def = testF3(hfile, hoffset + 2);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyFunc", ((IASTName) decl).toString());
		assertEquals(hoffset, ((ASTNode) decl).getOffset());
		assertEquals(6, ((ASTNode) decl).getLength());
		assertEquals("MyFunc", ((IASTName) def).toString());
		assertEquals(soffset, def.getFileLocation().getNodeOffset());
		assertEquals(6, ((ASTNode) def).getLength());

		hoffset = hcode.indexOf("MyStruct");
		soffset = scode.indexOf("MyStruct");
		decl = testF3(file, soffset + 2);
		def = testF3(hfile, hoffset + 2);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyStruct", ((IASTName) decl).toString());
		assertEquals(hoffset, ((ASTNode) decl).getOffset());
		assertEquals(8, ((ASTNode) decl).getLength());
		assertEquals("MyStruct", ((IASTName) def).toString());
		assertEquals(hoffset, def.getFileLocation().getNodeOffset());
		assertEquals(8, ((ASTNode) def).getLength());

		hoffset = hcode.indexOf("MyClass");
		soffset = scode.indexOf("MyClass");
		decl = testF3(file, soffset + 2);
		def = testF3(hfile, hoffset + 2);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyClass", ((IASTName) decl).toString());
		assertEquals(hoffset, ((ASTNode) decl).getOffset());
		assertEquals(7, ((ASTNode) decl).getLength());
		assertEquals("MyClass", ((IASTName) def).toString());
		assertEquals(hoffset, def.getFileLocation().getNodeOffset());
		assertEquals(7, ((ASTNode) def).getLength());
	}

	// // the header
	// namespace N {
	//    template < class T > class AAA { T _t; };
	// };

	// #include "testBasicTemplateInstance.h"
	// N::AAA<int> a;
	public void testBasicTemplateInstance_207320() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBasicTemplateInstance.h", hcode);
		IFile file = importFile("testBasicTemplateInstance.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int hoffset = hcode.indexOf("AAA");
		int soffset = scode.indexOf("AAA<int>");
		IASTNode decl1 = testF3(file, soffset, 3);
		assertTrue(decl1 instanceof IASTName);
		assertEquals("AAA", ((IASTName) decl1).toString());
		assertEquals(hoffset, decl1.getFileLocation().getNodeOffset());
		assertEquals(3, ((ASTNode) decl1).getLength());

		IASTNode decl2 = testF3(file, soffset, 8);
		assertEquals("AAA", ((IASTName) decl2).toString());
		assertEquals(hoffset, decl2.getFileLocation().getNodeOffset());
		assertEquals(3, ((ASTNode) decl2).getLength());
	}

	// // the header
	// class X {
	// public:
	//    X(int); // openReferences fails to find the constructor in g()
	// };

	// #include "testBug86829A.h"
	// X f(X);
	// void g()
	// {
	//    X b = f(X(2)); // openDeclarations on X(int) shall find constructor
	// }
	public void testBug86829A() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug86829A.h", hcode);
		IFile file = importFile("testBug86829A.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int offset = scode.indexOf("X(2)");
		int doffset = hcode.indexOf("X(int)");
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("X", ((IASTName) decl).toString());
		assertEquals(doffset, decl.getFileLocation().getNodeOffset());
		assertEquals(1, ((ASTNode) decl).getLength());
	}

	// // the header
	// class X {
	// public:
	//    operator int();
	// };
	// class Y {
	// public:
	//    operator X();
	// };

	// #include "testBug86829B.h"
	// void testfunc() {
	// Y a;
	// int c = X(a); // OK: a.operator X().operator int()
	// }
	public void _testBug86829B() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug86829B.h", hcode);
		IFile file = importFile("testBug86829B.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int offset = scode.indexOf("X(a)");
		int doffset = hcode.indexOf("X()");
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals(decl.toString(), "X");
		assertEquals(doffset, decl.getFileLocation().getNodeOffset());
		assertEquals(1, ((ASTNode) decl).getLength());
	}

	// // the header
	// extern int a; 				// declares
	// extern const int c = 1; 		// defines
	// struct S {int a; int b;}; 	// defines
	// struct X { 					// defines
	//    int x; 					// defines nonstatic data member
	//    static int y; 			// declares static data member
	//    X(): x(0) { } 			// defines a constructor of
	// };
	// enum E {up, down}; 			// defines
	// namespace N {int d;} 		// defines
	// namespace N1 = N; 			// defines
	// int f(int);  				// declares
	// extern X anotherX; 			// declares

	// #include "testCPPSpecDeclsDefs.h"
	// int a; 						// defines
	// int X::y = 1; 				// defines
	// X anX; 						// defines variable, implicitly calls ctor
	// extern const int c; 			// declares
	// int f(int x) {return x+a;}   // defines
	// struct S; 					// declares
	// typedef int Int; 			// declares
	// using N::d; 					// declares
	// S s;
	// Int lhs= s.a+s.b+up+down+anX+0;
	public void testCPPSpecDeclsDefs() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testCPPSpecDeclsDefs.h", hcode);
		IFile file = importFile("testCPPSpecDeclsDefs.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int offset0 = hcode.indexOf("a;");
		int offset1 = scode.indexOf("a;");
		IASTNode decl = testF3(hfile, offset0);
		assertNode("a", offset1, decl);
		decl = testF3(file, offset1);
		assertNode("a", offset0, decl);

		offset0 = hcode.indexOf("int c") + 4;
		offset1 = scode.indexOf("int c") + 4;
		decl = testF3(hfile, offset0);
		assertNode("c", offset1, decl);
		decl = testF3(file, offset1);
		assertNode("c", offset0, decl);

		offset0 = hcode.indexOf("f(int");
		offset1 = scode.indexOf("f(int");
		decl = testF3(hfile, offset0);
		assertNode("f", offset1, decl);
		decl = testF3(file, offset1);
		assertNode("f", offset0, decl);

		offset0 = scode.indexOf("x)");
		decl = testF3(file, offset0);
		assertNode("x", offset0, decl);

		offset1 = scode.indexOf("x+a");
		decl = testF3(file, offset1);
		assertNode("x", offset0, decl);

		offset0 = scode.indexOf("a;");
		offset1 = scode.indexOf("a;}");
		decl = testF3(file, offset1);
		assertNode("a", offset0, decl);

		offset0 = hcode.indexOf("S");
		offset1 = scode.indexOf("S;");
		int offset2 = scode.indexOf("S", offset1);
		decl = testF3(hfile, offset0);
		assertNode("S", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("S", offset0, decl);
		decl = testF3(file, offset2);
		assertNode("S", offset0, decl);

		offset0 = hcode.indexOf("a; int b;};");
		offset1 = scode.indexOf("a+s.b");
		decl = testF3(hfile, offset0);
		assertNode("a", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("a", offset0, decl);

		offset0 = hcode.indexOf("b;};");
		offset1 = scode.indexOf("s.b") + 2;
		decl = testF3(hfile, offset0);
		assertNode("b", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("b", offset0, decl);

		offset0 = hcode.indexOf("X");
		offset1 = scode.indexOf("X");
		offset2 = scode.indexOf("X", offset1 + 1);
		decl = testF3(hfile, offset0);
		assertNode("X", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("X", offset0, decl);
		decl = testF3(file, offset2);
		assertNode("X", offset0, decl);

		offset0 = hcode.indexOf("x;");
		offset1 = hcode.indexOf("x", offset0 + 1);
		decl = testF3(hfile, offset0);
		assertNode("x", offset0, decl);
		decl = testF3(hfile, offset1);
		assertNode("x", offset0, decl);

		offset0 = hcode.indexOf("y;");
		offset1 = scode.indexOf("y");
		decl = testF3(hfile, offset0);
		assertNode("y", offset1, decl);
		decl = testF3(file, offset1);
		assertNode("y", offset0, decl);

		offset0 = hcode.indexOf("X()");
		decl = testF3(hfile, offset0);
		assertNode("X", offset0, decl);

		offset0 = hcode.indexOf("up");
		offset1 = scode.indexOf("up");
		decl = testF3(hfile, offset0);
		assertNode("up", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("up", offset0, decl);

		offset0 = hcode.indexOf("down");
		offset1 = scode.indexOf("down");
		decl = testF3(hfile, offset0);
		assertNode("down", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("down", offset0, decl);

		offset0 = hcode.indexOf("N");
		offset1 = hcode.indexOf("N;", offset0 + 1);
		offset2 = scode.indexOf("N");
		decl = testF3(hfile, offset0);
		assertNode("N", offset0, decl);
		decl = testF3(hfile, offset1);
		assertNode("N", offset0, decl);
		decl = testF3(file, offset2);
		assertNode("N", offset0, decl);

		offset0 = hcode.indexOf("d;");
		offset1 = scode.indexOf("d;");
		decl = testF3(hfile, offset0);
		assertNode("d", offset0, decl);
		// 	    does not work, created separate testcase
		//        decl= testF3(file, offset1);
		//        assertNode("d", offset0, decl);

		offset0 = hcode.indexOf("N1");
		decl = testF3(hfile, offset0);
		assertNode("N1", offset0, decl);

		offset0 = scode.indexOf("anX");
		offset1 = scode.indexOf("anX", offset0 + 1);
		decl = testF3(file, offset0);
		assertNode("X", hcode.indexOf("X()"), decl);
		decl = testF3(file, offset1);
		assertNode("anX", offset0, decl);

		offset0 = scode.indexOf("Int");
		offset1 = scode.indexOf("Int", offset0 + 1);
		decl = testF3(file, offset0);
		assertNode("Int", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("Int", offset0, decl);
	}

	// // the header
	// namespace N {int d;} 		// defines

	// #include "testBug168533.h"
	// using N::d; 					// declares
	// int a= d;
	public void testBug168533() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug168533.h", hcode);
		IFile file = importFile("testBug168533.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int offset0 = hcode.indexOf("d;");
		int offset1 = scode.indexOf("d;");
		int offset2 = scode.indexOf("d", offset1);
		IASTNode decl = testF3(hfile, offset0);
		assertNode("d", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("d", offset0, decl);
		decl = testF3(file, offset2);
		assertNode("d", offset0, decl);
	}

	// class Overflow {
	// public:
	//    Overflow(char,double,double);
	// };

	// #include "testBug95225.h"
	// void f(double x) {
	//    throw Overflow('+',x,3.45e107);
	// }
	// int foo() {
	//    try {
	//       f(1.2);
	//    }
	//    catch(Overflow& oo) {
	// 				// handle exceptions of type Overflow here
	//    }
	// }
	public void testBug95225() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug95225.h", hcode);
		IFile file = importFile("testBug95225.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("Overflow");
		offset1 = scode.indexOf("rflow&");
		decl = testF3(file, offset1);
		assertNode("Overflow", offset0, decl);
		decl = testF3(hfile, offset0);
		assertNode("Overflow", offset0, decl);

		offset0 = hcode.indexOf("Overflow(");
		offset1 = scode.indexOf("rflow('+'");
		decl = testF3(file, offset1);
		assertNode("Overflow", offset0, decl);
		decl = testF3(hfile, offset0);
		assertNode("Overflow", offset0, decl);

		offset0 = scode.indexOf("x");
		offset1 = scode.indexOf("x", offset0);
		decl = testF3(file, offset0);
		assertNode("x", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("x", offset0, decl);
	}

	// struct A { }; // implicitly declared A::operator=
	// struct B : A {
	//    B& operator=(const B &);
	// };

	// #include "testBug95202.h"
	// B& B::operator=(const B& s) {
	//    this->B::operator=(s); // wellformed
	//    return *this;
	// }
	public void testBug95202() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug95202.h", hcode);
		IFile file = importFile("testBug95202.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = scode.indexOf("s)");
		offset1 = scode.indexOf("s);", offset0 + 1);
		decl = testF3(file, offset0);
		assertNode("s", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("s", offset0, decl);
	}

	// extern int abc;

	// #include "testBug101287.h"
	// int main(int argc, char **argv) {
	//    abc;
	// }
	public void testBug101287() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug101287.h", hcode);
		IFile file = importFile("testBug101287.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("abc");
		offset1 = scode.indexOf("abc");
		decl = testF3(hfile, offset0);
		assertNode("abc", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("abc", offset0, decl);
	}

	// struct RTBindingEnd
	// {
	//    int index;
	// };

	// #include "testBug102258.h"
	// void f(RTBindingEnd & end) {
	// }
	public void testBug102258() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug102258.h", hcode);
		IFile file = importFile("testBug102258.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("RTBindingEnd");
		offset1 = scode.indexOf("RTBindingEnd");
		decl = testF3(hfile, offset0);
		assertNode("RTBindingEnd", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("RTBindingEnd", offset0, decl);
	}

	// namespace foo {
	//    int g() {
	//       return 0;
	//    }
	// }

	// #include "testBug103323.h"
	// int f() {
	//    return foo::g();
	// }
	public void testBug103323() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug103323.h", hcode);
		IFile file = importFile("testBug103323.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("g()");
		offset1 = scode.indexOf("g()");
		decl = testF3(hfile, offset0);
		assertNode("g", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("g", offset0, decl);

		testSimple_Ctrl_G_Selection(file, offset1, 1, 1);
	}

	// typedef int TestTypeOne;
	// typedef int TestTypeTwo;

	// #include "testBug78354.h"
	// int main()
	// {
	//    TestTypeOne myFirstLink = 5;
	//    TestTypeTwo mySecondLink = 6;
	//    return 0;
	// }
	public void testBug78354() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug78354.h", hcode);
		IFile file = importFile("testBug78354.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("TestTypeOne");
		offset1 = scode.indexOf("TestTypeOne");
		decl = testF3(hfile, offset0);
		assertNode("TestTypeOne", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("TestTypeOne", offset0, decl);

		offset0 = hcode.indexOf("TestTypeTwo");
		offset1 = scode.indexOf("TestTypeTwo");
		decl = testF3(hfile, offset0);
		assertNode("TestTypeTwo", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("TestTypeTwo", offset0, decl);
	}

	// int x;

	// #include "testBug103697.h"
	// int foo() {
	//     return x;
	// }
	public void testBug103697() throws Exception {
		if (System.getProperty("cdt.skip.known.test.failures") == null) {
			return;
		}
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFileWithLink("testBug103697.h", hcode);
		IFile file = importFileWithLink("testBug103697.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("x");
		offset1 = scode.indexOf("x");
		decl = testF3(hfile, offset0);
		assertNode("x", offset0, decl);
		decl = testF3(file, offset1);
		assertNode("x", offset0, decl);
	}

	// class __attribute__((visibility("default"))) FooClass
	// {
	//    int foo();
	// };

	// #include "testBug108202.h"
	// int FooClass::foo() {
	//    return 0;
	// }
	public void testBug108202() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug108202.h", hcode);
		IFile file = importFile("testBug108202.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("foo");
		offset1 = scode.indexOf("foo");
		decl = testF3(hfile, offset0);
		assertNode("foo", offset1, decl);
		decl = testF3(file, offset1);
		assertNode("foo", offset0, decl);
	}

	// void c();

	// #include "c.h"
	// void c() {}

	// void cpp();

	// #include "cpp.h"
	// void cpp() {}
	public void testCNavigationInCppProject_bug183973() throws Exception {
		StringBuilder[] buffers = getContents(4);
		String hccode = buffers[0].toString();
		String ccode = buffers[1].toString();
		String hcppcode = buffers[2].toString();
		String cppcode = buffers[3].toString();
		IFile hcfile = importFile("c.h", hccode);
		IFile cfile = importFile("c.c", ccode);
		IFile hcppfile = importFile("cpp.h", hcppcode);
		IFile cppfile = importFile("cpp.cpp", cppcode);
		CCorePlugin.getIndexManager().reindex(fCProject);
		waitForIndexer(fCProject);

		IASTNode decl;
		int offset0, offset1;
		// cpp navigation
		offset0 = hcppcode.indexOf("cpp(");
		offset1 = cppcode.indexOf("cpp(");
		decl = testF3(hcppfile, offset0);
		assertNode("cpp", offset1, decl);
		decl = testF3(cppfile, offset1);
		assertNode("cpp", offset0, decl);

		// plain-c navigation
		offset0 = hccode.indexOf("c(");
		offset1 = ccode.indexOf("c(");
		decl = testF3(hcfile, offset0);
		assertNode("c", offset1, decl);
		decl = testF3(cfile, offset1);
		assertNode("c", offset0, decl);
	}

	// typedef struct {
	//    int a;
	// } usertype;
	// void func(usertype t);

	// #include "testBug190730.h"
	// void func(usertype t) {
	// }
	public void testFuncWithTypedefForAnonymousStruct_190730() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug190730.h", hcode);
		IFile file = importFile("testBug190730.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("func");
		offset1 = scode.indexOf("func");
		decl = testF3(hfile, offset0);
		assertNode("func", offset1, decl);
		decl = testF3(file, offset1);
		assertNode("func", offset0, decl);
	}

	// typedef enum {
	//    int eitem
	// } userEnum;
	// void func(userEnum t);

	// #include "testBug190730_2.h"
	// void func(userEnum t) {
	// }
	public void testFuncWithTypedefForAnonymousEnum_190730() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testBug190730_2.h", hcode);
		IFile file = importFile("testBug190730_2.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("func");
		offset1 = scode.indexOf("func");
		decl = testF3(hfile, offset0);
		assertNode("func", offset1, decl);
		decl = testF3(file, offset1);
		assertNode("func", offset0, decl);
	}

	//    #define MY_MACRO 0xDEADBEEF
	//    #define MY_FUNC() 00
	//    #define MY_PAR( aRef );

	//  #include "macrodef.h"
	//	int basictest(void){
	//	   int tester = MY_MACRO;  //OK: F3 works
	//	   int xx= MY_FUNC();
	//     MY_PAR(0);
	//  }
	public void testMacroNavigation() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("macrodef.h", hcode);
		IFile file = importFile("macronavi.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("MY_MACRO");
		offset1 = scode.indexOf("MY_MACRO");
		decl = testF3(file, offset1);
		assertNode("MY_MACRO", offset0, decl);

		offset0 = hcode.indexOf("MY_FUNC");
		offset1 = scode.indexOf("MY_FUNC");
		decl = testF3(file, offset1);
		assertNode("MY_FUNC", offset0, decl);

		offset0 = hcode.indexOf("MY_PAR");
		offset1 = scode.indexOf("MY_PAR");
		decl = testF3(file, offset1);
		assertNode("MY_PAR", offset0, decl);
	}

	//  #define MY_MACRO 0xDEADBEEF
	//  #define MY_PAR( aRef ) aRef;
	//  int gvar;

	//  #include "macrodef.h"
	//	int basictest(void){
	//	   int tester = MY_PAR(MY_MACRO);
	//     tester= MY_PAR(gvar);
	//  }
	public void testMacroNavigation_Bug208300() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("macrodef.h", hcode);
		IFile file = importFile("macronavi.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset0 = hcode.indexOf("MY_PAR");
		offset1 = scode.indexOf("MY_PAR");
		decl = testF3(file, offset1);
		assertNode("MY_PAR", offset0, decl);

		offset0 = hcode.indexOf("MY_MACRO");
		offset1 = scode.indexOf("MY_MACRO");
		decl = testF3(file, offset1);
		assertNode("MY_MACRO", offset0, decl);

		offset0 = hcode.indexOf("gvar");
		offset1 = scode.indexOf("gvar");
		decl = testF3(file, offset1);
		assertNode("gvar", offset0, decl);
	}

	//	#define MYMACRO

	//	#undef MYMACRO
	public void testUndef_312399() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testUndef_312399.h", hcode);
		IFile file = importFile("testUndef_312399.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		IASTNode target = testF3(file, scode.indexOf("MYMACRO"));
		assertTrue(target instanceof IASTName);
		assertEquals("MYMACRO", ((IASTName) target).toString());
		assertEquals(hcode.indexOf("MYMACRO"), target.getFileLocation().getNodeOffset());
		assertEquals("MYMACRO".length(), ((ASTNode) target).getLength());
	}

	//  int wurscht;

	//  #include "aheader.h"
	public void testIncludeNavigation() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("aheader.h", hcode);
		IFile file = importFile("includenavi.cpp", scode);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset0, offset1;

		offset1 = scode.indexOf("aheader.h");
		testF3(file, offset1);
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput input = part.getEditorInput();
		assertEquals("aheader.h", ((FileEditorInput) input).getFile().getName());
	}

	// void cfunc();
	// void cxcpp() {
	//    cfunc();
	// }

	// extern "C" void cxcpp();
	// void cppfunc() {
	//    cxcpp();
	// }
	public void testNavigationCppCallsC() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String ccode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile cfile = importFile("s.c", ccode);
		IFile cppfile = importFile("s.cpp", scode);
		waitUntilFileIsIndexed(index, cppfile);
		IASTNode decl;
		int offset1, offset2;

		offset1 = scode.indexOf("cxcpp");
		offset2 = scode.indexOf("cxcpp", offset1 + 1);
		testF3(cppfile, offset1);
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput input = part.getEditorInput();
		assertEquals("s.c", ((FileEditorInput) input).getFile().getName());

		testF3(cppfile, offset2);
		part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		input = part.getEditorInput();
		assertEquals("s.c", ((FileEditorInput) input).getFile().getName());

		offset1 = ccode.indexOf("cxcpp");
		testF3(cfile, offset1);
		part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		input = part.getEditorInput();
		assertEquals("s.cpp", ((FileEditorInput) input).getFile().getName());
	}

	// void cxcpp();
	// void cfunc() {
	//    cxcpp();
	// }

	// void cppfunc() {}
	// extern "C" {void cxcpp() {
	//    cppfunc();
	// }}
	public void testNavigationCCallsCpp() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String ccode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile cfile = importFile("s.c", ccode);
		IFile cppfile = importFile("s.cpp", scode);
		waitUntilFileIsIndexed(index, cppfile);
		IASTNode decl;
		int offset1, offset2;

		offset1 = ccode.indexOf("cxcpp");
		offset2 = ccode.indexOf("cxcpp", offset1 + 1);
		testF3(cfile, offset1);
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput input = part.getEditorInput();
		assertEquals("s.cpp", ((FileEditorInput) input).getFile().getName());

		testF3(cfile, offset2);
		part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		input = part.getEditorInput();
		assertEquals("s.cpp", ((FileEditorInput) input).getFile().getName());

		offset1 = scode.indexOf("cxcpp");
		testF3(cppfile, offset1);
		part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		input = part.getEditorInput();
		assertEquals("s.c", ((FileEditorInput) input).getFile().getName());
	}

	//    #define ADD_TEXT(txt1,txt2) txt1" "txt2
	//    #define ADD(a,b) (a + b)
	//    void main(void) {
	//    #if defined(ADD_TEXT) && defined(ADD)
	//    #endif
	//    }
	public void testNavigationInDefinedExpression_215906() throws Exception {
		StringBuilder[] buffers = getContents(1);
		String code = buffers[0].toString();
		IFile file = importFile("s.cpp", code);
		waitUntilFileIsIndexed(index, file);
		IASTNode decl;
		int offset1, offset2;

		offset1 = code.indexOf("ADD_TEXT");
		offset2 = code.indexOf("ADD_TEXT", offset1 + 1);
		decl = testF3(file, offset2);
		assertNode("ADD_TEXT", offset1, decl);

		offset1 = code.indexOf("ADD", offset1 + 1);
		offset2 = code.indexOf("ADD", offset2 + 1);
		decl = testF3(file, offset2);
		assertNode("ADD", offset1, decl);
	}

	//  struct X {
	//		int operator +(X);
	//      int operator [](int);
	//		~X();
	//	};
	//
	//	int test(X x) {
	//		x + x;
	//		x[6];
	//		X* xx = new X();
	//		delete xx;
	//	}
	public void testNavigationToImplicitNames() throws Exception {
		StringBuilder[] buffers = getContents(1);
		String code = buffers[0].toString();
		IFile file = importFile("in.cpp", code);
		waitUntilFileIsIndexed(index, file);

		int offset1 = code.indexOf("operator +");
		int offset2 = code.indexOf("+ x;");
		IASTNode decl = testF3(file, offset2);
		assertNode("operator +", offset1, decl);
		decl = testF3(file, offset2 + 1);
		assertNode("operator +", offset1, decl);

		offset1 = code.indexOf("operator []");
		offset2 = code.indexOf("[6];");
		decl = testF3(file, offset2 + 1);
		assertNode("operator []", offset1, decl);
		offset2 = code.indexOf("];");
		decl = testF3(file, offset2);
		assertNode("operator []", offset1, decl);
		decl = testF3(file, offset2 + 1);
		assertNode("operator []", offset1, decl);

		offset1 = code.indexOf("~X()");
		offset2 = code.indexOf("delete");
		decl = testF3(file, offset2);
		assertNode("~X", offset1, decl);
	}

	// template<typename T>
	// class C {
	// public:
	//    T operator+(int);
	// };

	// #include "test.h"
	// void main() {
	//   C<char> a;
	//   a + 2;
	// }
	public void testBug272744() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("test.h", hcode);
		IFile file = importFile("test.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int hoffset = hcode.indexOf("operator+");
		int soffset = scode.indexOf("+");
		IASTNode def = testF3(file, soffset + 1);
		assertTrue(def instanceof IASTName);
		assertEquals("operator +", ((IASTName) def).toString());
		assertEquals(hoffset, ((ASTNode) def).getOffset());
		assertEquals(9, ((ASTNode) def).getLength());
	}

	//    void  test(ABC* p);
	//    void  test(ABC* q) {}
	//    void call_test(){
	//    	test(0);
	//    }
	public void testBug305487() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug305487.cpp", code);
		waitUntilFileIsIndexed(index, file);

		int offset = code.indexOf("test(0)");
		IASTNode def = testF3(file, offset + 1);
		assertTrue(def instanceof IASTName);
	}

	//	struct A {
	//	  A();
	//	  A(int x);
	//	};

	//	#include "testImplicitConstructorCall_248855.h"
	//	void func() {
	//	  A a1;
	//	  A a2(5);
	//	}
	//	struct B {
	//	  B() : a3(1) {}
	//	  A a3;
	//	};
	public void testImplicitConstructorCall_248855() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("testImplicitConstructorCall_248855.h", hcode);
		IFile file = importFile("testImplicitConstructorCall_248855.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		IASTNode target = testF3(file, scode.indexOf("a1"));
		assertTrue(target instanceof IASTName);
		assertEquals("A", ((IASTName) target).toString());
		assertEquals(hcode.indexOf("A()"), target.getFileLocation().getNodeOffset());
		assertEquals("A".length(), ((ASTNode) target).getLength());

		target = testF3(file, scode.indexOf("a2"));
		assertTrue(target instanceof IASTName);
		assertEquals("A", ((IASTName) target).toString());
		assertEquals(hcode.indexOf("A(int x)"), target.getFileLocation().getNodeOffset());
		assertEquals("A".length(), ((ASTNode) target).getLength());

		try {
			target = testF3(file, scode.indexOf("a3"));
			fail("Didn't expect navigation to succeed due to multiple choices: B::a3, A::A(int x).");
		} catch (RuntimeException e) {
			assertEquals("ambiguous input: 2", e.getMessage());
		}
	}

	// namespace ns {
	// void func();
	// }

	// #include "test.h"
	// using ns::func;
	//
	// void test() {
	//   func();
	// }
	public void testBug380197() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String hcode = buffers[0].toString();
		String scode = buffers[1].toString();
		IFile hfile = importFile("test.h", hcode);
		IFile file = importFile("test.cpp", scode);
		waitUntilFileIsIndexed(index, file);

		int hoffset = hcode.indexOf("func");
		int offset = scode.indexOf("func()");
		IASTNode def = testF3(file, offset + 1);
		assertTrue(def instanceof IASTName);
		assertEquals("func", def.toString());
		IASTFileLocation location = def.getFileLocation();
		assertEquals(hfile.getLocation().toOSString(), location.getFileName());
		assertEquals(hoffset, location.getNodeOffset());
	}

	//	int waldo(int a, decltype(a) b);
	public void testFunctionParameterReferencingPreviousParameter_432703() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("test.cpp", code);
		waitUntilFileIsIndexed(index, file);

		int offset = code.indexOf("a)");
		IASTNode def = testF3(file, offset + 1);
		assertTrue(def instanceof IASTName);
	}

	//  #define WALDO 42

	//  #define WALDO 98

	//  #include "a.hpp"
	//  int x = WALDO;
	public void testTwoMacrosWithSameName_440940() throws Exception {
		StringBuilder[] buffers = getContents(3);
		String aHpp = buffers[0].toString();
		String bHpp = buffers[1].toString();
		String cpp = buffers[2].toString();
		IFile aHppFile = importFile("a.hpp", aHpp);
		IFile bHppFile = importFile("b.hpp", bHpp);
		IFile cppFile = importFile("test.cpp", cpp);
		waitUntilFileIsIndexed(index, cppFile);

		IASTNode result = testF3(cppFile, cpp.indexOf("WALDO") + 1);
		assertTrue(result instanceof IASTName);
		IBinding binding = ((IASTName) result).resolveBinding();
		assertTrue(binding instanceof IMacroBinding);
		String expansion = new String(((IMacroBinding) binding).getExpansion());
		assertTrue(expansion.contains("42"));
	}

	//    #define DEFINE_FUNC(...) void foo() { __VA_ARGS__ }
	//    struct Waldo {
	//        void find();
	//    };
	//    DEFINE_FUNC
	//    (
	//        Waldo waldo;
	//        waldo.find();
	//    )
	public void testDeclarationInMacroArgment_509733() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("test.cpp", code);
		waitUntilFileIsIndexed(index, file);

		int offset = code.indexOf("waldo.find()");
		IASTNode def = testF3(file, offset + 1);
		assertTrue(def instanceof IASTName);
	}

	//	class Waldo {
	//	    void find();
	//	};

	//	#include "test.hpp"
	//	int Waldo::find() {}
	public void testNavigationToDefinitionWithWrongSignature_525739() throws Exception {
		StringBuilder[] buffers = getContents(3);
		String hpp = buffers[0].toString();
		String cpp = buffers[1].toString();
		IFile hppFile = importFile("test.hpp", hpp);
		IFile cppFile = importFile("test.cpp", cpp);
		waitUntilFileIsIndexed(index, cppFile);

		// We should find the definition, even though the signature doesn't match exactly.
		IASTNode target = testF3(hppFile, hpp.indexOf("void find") + 6);
		assertInstance(target, IASTName.class);
		assertEquals(IASTNameOwner.r_definition, ((IASTName) target).getRoleOfName(false));
	}

	//	class Waldo {
	//	    void find();
	//	};

	//	#include "test.hpp"
	//	void Waldo::find() {}
	//	int Waldo::find() {}
	public void testNavigationPrefersCorrectDefinition_525739() throws Exception {
		StringBuilder[] buffers = getContents(3);
		String hpp = buffers[0].toString();
		String cpp = buffers[1].toString();
		IFile hppFile = importFile("test.hpp", hpp);
		IFile cppFile = importFile("test.cpp", cpp);
		waitUntilFileIsIndexed(index, cppFile);

		// We should find the definition that's an exact match, rather than asking the
		// user to disambiguate between two alternatives.
		IASTNode target = testF3(hppFile, hpp.indexOf("void find") + 6);
		assertInstance(target, IASTName.class);
		assertEquals(IASTNameOwner.r_definition, ((IASTName) target).getRoleOfName(false));
	}

	//	template <typename E>
	//	struct Node {
	//		Node * next;
	//		E value;
	//	};
	//	Node<int> head{nullptr, 42};
	//	auto [h, v] = head;

	//	#include "SBTestHeader.hpp"
	//	auto myH = h;
	//	auto myV = v;
	public void testNavigationToStructuredBinding_522200() throws Exception {
		StringBuilder[] buffers = getContents(2);
		String header = buffers[0].toString();
		IFile headerFile = importFile("SBTestHeader.hpp", header);
		String source = buffers[1].toString();
		IFile sourceFile = importFile("SBTestSource.cpp", source);
		waitUntilFileIsIndexed(index, sourceFile);

		IASTNode targetH = testF3(sourceFile, source.indexOf("myH = h") + 6);
		assertInstance(targetH, IASTName.class);
		assertEquals(IASTNameOwner.r_definition, ((IASTName) targetH).getRoleOfName(false));
		IASTFileLocation locationH = targetH.getFileLocation();
		int targetHOffset = locationH.getNodeOffset();
		assertEquals(header.indexOf("auto [h") + 6, targetHOffset);

		IASTNode targetV = testF3(sourceFile, source.indexOf("myV = v") + 6);
		assertInstance(targetV, IASTName.class);
		assertEquals(IASTNameOwner.r_definition, ((IASTName) targetV).getRoleOfName(false));
		IASTFileLocation locationV = targetV.getFileLocation();
		int targetVOffset = locationV.getNodeOffset();
		assertEquals(header.indexOf("[h, v") + 4, targetVOffset);
	}
}
