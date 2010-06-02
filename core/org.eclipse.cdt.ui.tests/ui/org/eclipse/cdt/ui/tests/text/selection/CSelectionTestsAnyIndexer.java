/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

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

public abstract class CSelectionTestsAnyIndexer extends BaseSelectionTestsIndexer {

	private static final int MAX_WAIT_TIME = 8000;

	private String sourceIndexerID;
	private IIndex index;

	public CSelectionTestsAnyIndexer(String name, String indexerID) {
		super(name);
		sourceIndexerID= indexerID;
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		//Create temp project
		fCProject = createProject("CSelectionTestsDOMIndexerProject"); //$NON-NLS-1$
		assertNotNull("Unable to create project", fCProject);

		CCorePlugin.getIndexManager().setIndexerId(fCProject, sourceIndexerID);
		index= CCorePlugin.getIndexManager().getIndex(fCProject);
	}

	@Override
	protected void tearDown() throws Exception {
		closeAllEditors();
		CProjectHelper.delete(fCProject);
		super.tearDown();
	}

	private ICProject createProject(String projectName) throws CoreException {
		ICProject cPrj = CProjectHelper.createCProject(projectName, "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$
		return cPrj;
	}
	
	protected StringBuffer[] getContents(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(
				CTestPlugin.getDefault().getBundle(), "ui", CSelectionTestsAnyIndexer.class, getName(), sections);
	}

	private void assertNode(String name, int offset, IASTNode node) {
		assertNotNull(node);
		assertEquals(node.toString(), name);
		IASTFileLocation loc= node.getFileLocation();
		assertEquals(offset, loc.getNodeOffset());
		assertEquals(name.length(), loc.getNodeLength());
	}

    // // the header
    // extern int MyInt;       // MyInt is in another file
    // extern const int MyConst;   // MyConst is in another file
    // void MyFunc(int);       // often used in header files
    // typedef int NewInt;     // a normal typedef statement
	// struct MyStruct { int Member1; int Member2; };
    
    // #include "basicDefinition.h"
    // int MyInt;
    // extern const int MyConst = 42;
    // void MyFunc(int a) { cout << a << endl; }
	// struct MyStruct;        
    public void testBasicDefinition() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("basicDefinition.h", hcode); 
        IFile file = importFile("testBasicDefinition.c", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        
        int hoffset= hcode.indexOf("MyInt"); 
        int soffset = scode.indexOf("MyInt"); 
        IASTNode decl = testF3(file, soffset+2);
        IASTNode def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals("MyInt", ((IASTName) decl).toString()); //$NON-NLS-1$
        assertEquals(hoffset, ((ASTNode) decl).getOffset());
        assertEquals(5, ((ASTNode) decl).getLength());
        assertEquals("MyInt", ((IASTName) def).toString()); //$NON-NLS-1$
        assertEquals(soffset, def.getFileLocation().getNodeOffset());
        assertEquals(5, ((ASTNode) def).getLength());
        
        hoffset= hcode.indexOf("MyConst"); 
        soffset = scode.indexOf("MyConst"); 
        decl = testF3(file, soffset+2);
        def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals("MyConst", ((IASTName) decl).toString()); //$NON-NLS-1$
        assertEquals(hoffset, ((ASTNode) decl).getOffset());
        assertEquals(7, ((ASTNode) decl).getLength());
        assertEquals("MyConst", ((IASTName) def).toString()); //$NON-NLS-1$
        assertEquals(soffset, def.getFileLocation().getNodeOffset());
        assertEquals(7, ((ASTNode) def).getLength());
        
        hoffset= hcode.indexOf("MyFunc"); 
        soffset = scode.indexOf("MyFunc"); 
        decl = testF3(file, soffset+2);
        def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals("MyFunc", ((IASTName) decl).toString()); //$NON-NLS-1$
        assertEquals(hoffset, ((ASTNode) decl).getOffset());
        assertEquals(6, ((ASTNode) decl).getLength());
        assertEquals("MyFunc", ((IASTName) def).toString()); //$NON-NLS-1$
        assertEquals(soffset, def.getFileLocation().getNodeOffset());
        assertEquals(6, ((ASTNode) def).getLength());
        
        hoffset= hcode.indexOf("MyStruct"); 
        soffset = scode.indexOf("MyStruct"); 
        decl = testF3(file, soffset+2);
        def = testF3(hfile, hoffset+2);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals("MyStruct", ((IASTName) decl).toString()); //$NON-NLS-1$
        assertEquals(hoffset, ((ASTNode) decl).getOffset());
        assertEquals(8, ((ASTNode) decl).getLength());
        assertEquals("MyStruct", ((IASTName) def).toString()); //$NON-NLS-1$
        assertEquals(hoffset, def.getFileLocation().getNodeOffset());
        assertEquals(8, ((ASTNode) def).getLength());
    }
	
    
	// // the header
	// extern int a; 				// declares 
	// extern const int c = 1; 		// defines 
	// struct S {int a; int b;}; 	// defines 
	// struct X { 					// defines 
	//    int x; 					// defines nonstatic data member 
	// };
	// enum E {up, down}; 			// defines 
	// int f(int);  				// declares 
	// extern struct X anotherX; 	// declares 
	
	// #include "testCPPSpecDeclsDefs.h"
	// int a; 						// defines 
	// struct X anX; 				// defines 
	// extern const int c; 			// declares
	// int f(int x) {return x+a;}   // defines
	// struct S; 					// declares
	// typedef int Int; 			// declares 
	// struct S s;
	// Int lhs= s.a+s.b+up+down+anX+0;			
	public void testCPPSpecDeclsDefs() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("testCPPSpecDeclsDefs.h", hcode); 
        IFile file = importFile("testCPPSpecDeclsDefs.c", scode); 
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
        assertNode("S", offset0, decl);
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
        decl= testF3(hfile, offset0);
        assertNode("X", offset0, decl);
        decl= testF3(file, offset1);
        assertNode("X", offset0, decl);

        offset0= hcode.indexOf("x;");
        decl= testF3(hfile, offset0);
        assertNode("x", offset0, decl);
				
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
        IFile file = importFile("testBug101287.c", scode); 
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
        IFile file = importFileWithLink("testBug103697.c", scode); 
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
        IFile file = importFile("testBug78354.c", scode); 
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
        IFile file = importFile("testBug190730.c", scode); 
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
        IFile file = importFile("testBug190730_2.c", scode); 
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
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("macrodef.h", hcode); 
        IFile file = importFile("macronavi.c", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
    //  #define MY_PAR( aRef ) aRef

	//  #include "macrodef.h"
	//	int basictest(void){
	//	   int tester = MY_PAR(MY_MACRO);
    //  }
    public void testMacroNavigation_Bug208300() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("macrodef.h", hcode); 
        IFile file = importFile("macronavi.c", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
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
    }

    //  int wurscht;

	//  #include "aheader.h"
    public void testIncludeNavigation() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("aheader.h", hcode); 
        IFile file = importFile("includenavi.c", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        IASTNode decl;
        int offset0, offset1;

        offset1 = scode.indexOf("aheader.h");
        testF3(file, offset1);
        IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(); 
        IEditorInput input = part.getEditorInput();
        assertEquals("aheader.h", ((FileEditorInput) input).getFile().getName());
    }
    
    // #define DR_NUM_DIMENSIONS(DR) VEC_length (tree, DR_ACCESS_FNS (DR))
    
    // #define DR_ACCESS_FNS(DR)
    public void testNavigationInMacroDefinition_Bug102643() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        IFile hfile = importFile("aheader.h", hcode); 
        IFile file = importFile("source.c", scode); 
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        IASTNode decl;
        int offset0, offset1;

        offset1 = hcode.indexOf("DR_ACC");
        testF3(hfile, offset1);
        IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(); 
        IEditorInput input = part.getEditorInput();
        assertEquals("source.c", ((FileEditorInput) input).getFile().getName());
    }    
    
    // int myFunc();
    
    // int myFunc(var) 
    // int var; 
	// { 
	//     return var; 
	// } 
	//
	// int main(void) 
	// { 
	//     return myFunc(0); 
	// }
    public void testKRstyleFunctions_Bug221635() throws Exception {
        final StringBuffer[] contents = getContentsForTest(2);
        String hcode= contents[0].toString();
		String code= contents[1].toString();
        IFile hfile = importFile("aheader.h", hcode); 
        IFile file = importFile("source.c", code); 
        int offset= code.indexOf("myFunc(0)");
        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);

        IASTNode decl= testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        final IASTName name = (IASTName) decl;
		assertTrue(name.isDefinition());
        assertEquals("myFunc", name.toString());
    }
    
    // int x= __LINE__;
    public void testBuiltinMacro_Bug293864() throws Exception {
        final StringBuffer[] contents = getContentsForTest(1);
        String code= contents[0].toString();
        IFile file = importFile("source.c", code); 
        int offset= code.indexOf("__LINE__");

        TestSourceReader.waitUntilFileIsIndexed(index, file, MAX_WAIT_TIME);
        // just make sure that no NPE is thrown.
        testF3(file, offset);
    }
}
