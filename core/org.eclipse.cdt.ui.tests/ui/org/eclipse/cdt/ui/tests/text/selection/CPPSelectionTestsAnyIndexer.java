/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.tests.text.selection;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Test Ctrl_F3/F3 with the DOM Indexer for a C++ project.
 */
public abstract class CPPSelectionTestsAnyIndexer extends BaseSelectionTestsIndexer {
	private static final int MAX_WAIT_TIME = 8000;

	private String sourceIndexerID;
	private IIndex index;
	
	public CPPSelectionTestsAnyIndexer(String name, String indexerID) {
		super(name);
		sourceIndexerID= indexerID;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		//Create temp project
		fCProject= createProject("CPPSelectionTestsDOMIndexerProject"); //$NON-NLS-1$
		assertNotNull("Unable to create project", fCProject);
//		MakeProjectNature.addNature(project, new NullProgressMonitor());
//		ScannerConfigNature.addScannerConfigNature(project);
//		PerProjectSICollector.calculateCompilerBuiltins(project);

		CCorePlugin.getIndexManager().setIndexerId(fCProject, sourceIndexerID);
		index= CCorePlugin.getIndexManager().getIndex(fCProject);
	}

	protected void tearDown() throws Exception {
		closeAllEditors();
		CProjectHelper.delete(fCProject);
		super.tearDown();
	}

	private ICProject createProject(String projectName) throws CoreException {
		ICProject cPrj = CProjectHelper.createCCProject(projectName, "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$
		return cPrj;
	}
	
	protected StringBuffer[] getContents(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "ui", CPPSelectionTestsAnyIndexer.class, getName(), sections);
	}

	private void assertNode(String name, int offset, IASTNode node) {
		assertNotNull(node);
		assertEquals(name, node.toString());
		IASTFileLocation loc= node.getFileLocation();
		assertEquals(offset, loc.getNodeOffset());
		assertEquals(name.length(), loc.getNodeLength());
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
	// #incluce "test93281.h"
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
    public void _testBug93281() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("test93281.h", hcode); 
        IFile file = importFile("test93281.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        
        int offset = scode.indexOf("p2->operator") + 6; //$NON-NLS-1$
        IASTNode node = testF3(file, offset);
        
        assertTrue(node instanceof IASTName);
        assertEquals("operator new[]", ((IASTName)node).toString()); //$NON-NLS-1$
        assertEquals(hcode.indexOf("operator new"), ((ASTNode)node).getOffset());
        assertEquals(16, ((ASTNode)node).getLength());
        
        offset = scode.indexOf("p2->    operator") + 11; //$NON-NLS-1$
        node = testF3(file, offset);
        
        assertTrue(node instanceof IASTName);
        assertEquals("operator =", ((IASTName)node).toString()); //$NON-NLS-1$
        assertEquals(hcode.indexOf("operator="), ((ASTNode)node).getOffset());
        assertEquals(9, ((ASTNode)node).getLength());
    }
    
    // // the header
    // extern int MyInt;       // MyInt is in another file
    // extern const int MyConst;   // MyConst is in another file
    // void MyFunc(int);       // often used in header files
    // struct MyStruct;        // often used in header files
    // typedef int NewInt;     // a normal typedef statement
    // class MyClass;          // often used in header files
    
    // #include "basicDefinition.h"
    // int MyInt;
    // extern const int MyConst = 42;
    // void MyFunc(int a) { cout << a << endl; }
    // struct MyStruct { int Member1; int Member2; };
    // class MyClass { int MemberVar; };
    public void testBasicDefinition() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("basicDefinition.h", hcode); 
        IFile file = importFile("testBasicDefinition.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        
        int hoffset= hcode.indexOf("MyInt"); 
        int soffset = scode.indexOf("MyInt"); 
        IASTNode decl = testF3(file, soffset+2);
        IASTNode def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), hoffset);
        assertEquals(((ASTNode)decl).getLength(), 5);
        assertEquals(((IASTName)def).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(soffset, def.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)def).getLength(), 5);
        
        hoffset= hcode.indexOf("MyConst"); 
        soffset = scode.indexOf("MyConst"); 
        decl = testF3(file, soffset+2);
        def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(hoffset, ((ASTNode)decl).getOffset());
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(soffset, def.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)def).getLength(), 7);
        
        hoffset= hcode.indexOf("MyFunc"); 
        soffset = scode.indexOf("MyFunc"); 
        decl = testF3(file, soffset+2);
        def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(hoffset, ((ASTNode)decl).getOffset());
        assertEquals(((ASTNode)decl).getLength(), 6);
        assertEquals(((IASTName)def).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(soffset, def.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)def).getLength(), 6);
        
        hoffset= hcode.indexOf("MyStruct"); 
        soffset = scode.indexOf("MyStruct"); 
        decl = testF3(file, soffset+2);
        def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(hoffset, ((ASTNode)decl).getOffset());
        assertEquals(((ASTNode)decl).getLength(), 8);
        assertEquals(((IASTName)def).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(soffset, def.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)def).getLength(), 8);
        
        hoffset= hcode.indexOf("MyClass"); 
        soffset = scode.indexOf("MyClass"); 
        decl = testF3(file, soffset+2);
        def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyClass"); //$NON-NLS-1$
        assertEquals(hoffset, ((ASTNode)decl).getOffset());
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyClass"); //$NON-NLS-1$
        assertEquals(soffset, def.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)def).getLength(), 7);
    }
    
	// // the header
    // namespace N {
    //    template < class T > class AAA { T _t; };
    // };
    
    // #include "testBasicTemplateInstance.h"
    // N::AAA<int> a;
	public void _testBasicTemplateInstance() throws Exception{
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBasicTemplateInstance.h", hcode); 
        IFile file = importFile("testBasicTemplateInstance.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        
        
        int hoffset= hcode.indexOf("AAA"); 
        int soffset = scode.indexOf("AAA<int>"); //$NON-NLS-1$
        IASTNode decl1 = testF3(file, soffset, 3);
        assertTrue(decl1 instanceof IASTName);
        assertEquals(((IASTName)decl1).toString(), "AAA"); //$NON-NLS-1$
        assertEquals(hoffset, decl1.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)decl1).getLength(), 3);
		
        IASTNode decl2 = testF3(file, soffset, 8);
        assertEquals(((IASTName)decl2).toString(), "AAA"); //$NON-NLS-1$
        assertEquals(hoffset, decl2.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)decl2).getLength(), 3);
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug86829A.h", hcode); 
        IFile file = importFile("testBug86829A.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        
        int offset = scode.indexOf("X(2)"); 
        int doffset= hcode.indexOf("X(int)");
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(doffset, decl.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)decl).getLength(), 1);
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug86829B.h", hcode); 
        IFile file = importFile("testBug86829B.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);

		
        int offset = scode.indexOf("X(a)"); 
        int doffset = hcode.indexOf("X()"); 
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals("X", decl.toString());
        assertEquals(doffset, decl.getFileLocation().getNodeOffset());
        assertEquals(((ASTNode)decl).getLength(), 1);
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
	// X anX; 						// defines 
	// extern const int c; 			// declares
	// int f(int x) {return x+a;}   // defines
	// struct S; 					// declares
	// typedef int Int; 			// declares 
	// using N::d; 					// declares 
	// S s;
	// Int lhs= s.a+s.b+up+down+anX+0;			
	public void testCPPSpecDeclsDefs() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testCPPSpecDeclsDefs.h", hcode); 
        IFile file = importFile("testCPPSpecDeclsDefs.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
		        
        int offset0= hcode.indexOf("a;");
        int offset1= scode.indexOf("a;"); 
        IASTNode decl= testF3(hfile, offset0);
        assertNode("a", offset1, decl);
        decl= testF3(file, offset1);
        assertNode("a", offset0, decl);

        offset0= hcode.indexOf("int c") + 4;
        offset1= scode.indexOf("int c") + 4; 
        decl= testF3(hfile, offset0);
        assertNode("c", offset1, decl);
        decl= testF3(file, offset1);
        assertNode("c", offset0, decl);

        offset0= hcode.indexOf("f(int");
        offset1= scode.indexOf("f(int");
        decl= testF3(hfile, offset0);
        assertNode("f", offset1, decl);
        decl= testF3(file, offset1);
        assertNode("f", offset0, decl);

        offset0= scode.indexOf("x)");
        decl= testF3(file, offset0);
        assertNode("x", offset0, decl);

        offset1= scode.indexOf("x+a");
        decl= testF3(file, offset1);
        assertNode("x", offset0, decl);

        offset0= scode.indexOf("a;");
        offset1= scode.indexOf("a;}"); 
        decl= testF3(file, offset1);
        assertNode("a", offset0, decl);

        offset0= hcode.indexOf("S");
        offset1= scode.indexOf("S;"); 
        int offset2= scode.indexOf("S", offset1); 
        decl= testF3(hfile, offset0);
        assertNode("S", offset1, decl);
        decl= testF3(file, offset1);
        assertNode("S", offset0, decl);
        decl= testF3(file, offset2);
        assertNode("S", offset0, decl);
		
        offset0 = hcode.indexOf("a; int b;};");
        offset1 = scode.indexOf("a+s.b");
        decl= testF3(hfile, offset0);
        assertNode("a", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("a", offset0, decl);

        offset0= hcode.indexOf("b;};");
        offset1= scode.indexOf("s.b") + 2;
        decl= testF3(hfile, offset0);
        assertNode("b", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("b", offset0, decl);

        offset0= hcode.indexOf("X");
        offset1= scode.indexOf("X");
        offset2= scode.indexOf("X", offset1+1);
        decl= testF3(hfile, offset0);
        assertNode("X", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("X", offset0, decl);
        decl= testF3(file, offset2);
        assertNode("X", offset0, decl);

        offset0= hcode.indexOf("x;");
        offset1= hcode.indexOf("x", offset0+1);
        decl= testF3(hfile, offset0);
        assertNode("x", offset0, decl);
        decl= testF3(hfile, offset1);
        assertNode("x", offset0, decl);

        offset0= hcode.indexOf("y;");
        offset1= scode.indexOf("y");
        decl= testF3(hfile, offset0);
        assertNode("y", offset1, decl);
        decl= testF3(file, offset1);
        assertNode("y", offset0, decl);
		
		offset0= hcode.indexOf("X()"); 
        decl = testF3(hfile, offset0);
        assertNode("X", offset0, decl);
				
        offset0= hcode.indexOf("up");
        offset1= scode.indexOf("up");
        decl= testF3(hfile, offset0);
        assertNode("up", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("up", offset0, decl);

        offset0= hcode.indexOf("down");
        offset1= scode.indexOf("down");
        decl= testF3(hfile, offset0);
        assertNode("down", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("down", offset0, decl);

        offset0= hcode.indexOf("N");
        offset1= hcode.indexOf("N;", offset0+1);
        offset2= scode.indexOf("N");
        decl= testF3(hfile, offset0);
        assertNode("N", offset0, decl);
        decl= testF3(hfile, offset1);
        assertNode("N", offset0, decl);
        decl= testF3(file, offset2);
        assertNode("N", offset0, decl);
		
        offset0= hcode.indexOf("d;");
        offset1= scode.indexOf("d;");
        decl= testF3(hfile, offset0);
        assertNode("d", offset0, decl);
// 	    does not work, created separate testcase        
//        decl= testF3(file, offset1);
//        assertNode("d", offset0, decl);

        offset0= hcode.indexOf("N1");
        decl= testF3(hfile, offset0);
        assertNode("N1", offset0, decl);

        offset0= scode.indexOf("anX");
        offset1= scode.indexOf("anX", offset0+1);
        decl= testF3(file, offset0);
        assertNode("anX", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("anX", offset0, decl);

        offset0= scode.indexOf("Int");
        offset1= scode.indexOf("Int", offset0+1);
        decl= testF3(file, offset0);
        assertNode("Int", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("Int", offset0, decl);
	}

	// // the header
	// namespace N {int d;} 		// defines 
	
	// #include "testBug168533.h"
	// using N::d; 					// declares 
	// int a= d;
	public void testBug168533() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug168533.h", hcode); 
        IFile file = importFile("testBug168533.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
		        
        int offset0= hcode.indexOf("d;");
        int offset1= scode.indexOf("d;");
        int offset2= scode.indexOf("d", offset1);
        IASTNode decl= testF3(hfile, offset0);
        assertNode("d", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("d", offset0, decl);
        decl= testF3(file, offset2);
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug95225.h", hcode); 
        IFile file = importFile("testBug95225.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        IASTNode decl;
        int offset0, offset1;

        offset0= hcode.indexOf("Overflow"); 
        offset1= scode.indexOf("rflow&"); 
        decl = testF3(file, offset1);
        assertNode("Overflow", offset0, decl);
        decl = testF3(hfile, offset0);
        assertNode("Overflow", offset0, decl);

        offset0= hcode.indexOf("Overflow("); 
        offset1= scode.indexOf("rflow('+'"); 
        decl = testF3(file, offset1);
        assertNode("Overflow", offset0, decl);
        decl = testF3(hfile, offset0);
        assertNode("Overflow", offset0, decl);

        offset0= scode.indexOf("x"); 
        offset1= scode.indexOf("x", offset0); 
        decl = testF3(file, offset0);
        assertNode("x", offset0, decl);
        decl = testF3(file, offset1);
        assertNode("x", offset0, decl);
    }
	
    // struct A { }; // implicitlydeclared A::operator=
    // struct B : A {
    //    B& operator=(const B &);
    // };
	
	// #include "testBug95202.h"
    // B& B::operator=(const B& s) {
    //    this->B::operator=(s); // wellformed
    //    return *this;
    // }
    public void testBug95202() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug95202.h", hcode); 
        IFile file = importFile("testBug95202.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        IASTNode decl;
        int offset0, offset1;

        offset0= scode.indexOf("s)"); 
        offset1= scode.indexOf("s);", offset0+1); 
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug101287.h", hcode); 
        IFile file = importFile("testBug101287.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug102258.h", hcode); 
        IFile file = importFile("testBug102258.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug103323.h", hcode); 
        IFile file = importFile("testBug103323.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
    // main()
    // {
    //    TestTypeOne myFirstLink = 5;
    //    TestTypeTwo mySecondLink = 6;
    //    return 0;
    // }
    public void testBug78354() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug78354.h", hcode); 
        IFile file = importFile("testBug78354.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFileWithLink("testBug103697.h", hcode); 
        IFile file = importFileWithLink("testBug103697.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug108202.h", hcode); 
        IFile file = importFile("testBug108202.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
        StringBuffer[] buffers= getContents(4);
        String hccode= buffers[0].toString();
        String ccode= buffers[1].toString();
        String hcppcode= buffers[2].toString();
        String cppcode= buffers[3].toString();
        IFile hcfile = importFile("c.h", hccode); 
        IFile cfile = importFile("c.c", ccode); 
        IFile hcppfile = importFile("cpp.h", hcppcode); 
        IFile cppfile = importFile("cpp.cpp", cppcode); 
        CCorePlugin.getIndexManager().reindex(fCProject);
        waitForIndex(MAX_WAIT_TIME);
        
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug190730.h", hcode); 
        IFile file = importFile("testBug190730.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testBug190730_2.h", hcode); 
        IFile file = importFile("testBug190730_2.cpp", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        IASTNode decl;
        int offset0, offset1;
        
        offset0 = hcode.indexOf("func");
        offset1 = scode.indexOf("func");
        decl = testF3(hfile, offset0);
        assertNode("func", offset1, decl);
        decl = testF3(file, offset1);
        assertNode("func", offset0, decl);        
    }
}
