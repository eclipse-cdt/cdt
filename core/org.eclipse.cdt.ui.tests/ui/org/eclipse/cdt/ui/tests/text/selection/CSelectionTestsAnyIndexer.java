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
import org.eclipse.core.runtime.NullProgressMonitor;

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
	private IFile 					file;
	private IFile					hfile;
	private NullProgressMonitor		monitor;

	private String sourceIndexerID;
	private IIndex index;
	
	public CSelectionTestsAnyIndexer(String name, String indexerID) {
		super(name);
		sourceIndexerID= indexerID;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		//Create temp project
		fCProject = createProject("CSelectionTestsDOMIndexerProject"); //$NON-NLS-1$
		assertNotNull("Unable to create project", fCProject);

		CCorePlugin.getIndexManager().setIndexerId(fCProject, sourceIndexerID);
		index= CCorePlugin.getIndexManager().getIndex(fCProject);
	}

	protected void tearDown() throws Exception {
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
		assertEquals(name, node.toString());
		IASTFileLocation loc= node.getFileLocation();
		assertEquals(offset, loc.getNodeOffset());
		assertEquals(name.length(), loc.getNodeLength());
	}

    // // the header
    // extern int MyInt;       // MyInt is in another file
    // extern const int MyConst;   // MyConst is in another file
    // void MyFunc(int);       // often used in header files
    // struct MyStruct;        // often used in header files
    // typedef int NewInt;     // a normal typedef statement
    
    // #include "basicDefinition.h"
    // int MyInt;
    // extern const int MyConst = 42;
    // void MyFunc(int a) { cout << a << endl; }
    // struct MyStruct { int Member1; int Member2; };
    public void testBasicDefinition() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        hfile = importFile("basicDefinition.h", hcode); 
        file = importFile("testBasicDefinition.c", scode); 
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
	// extern X anotherX; 			// declares 
	
	// #include "testCPPSpecDeclsDefs.h"
	// int a; 						// defines 
	// X anX; 						// defines 
	// extern const int c; 			// declares
	// int f(int x) {return x+a;}   // defines
	// struct S; 					// declares
	// typedef int Int; 			// declares 
	// S s;
	// Int lhs= s.a+s.b+up+down+anX+0;			
	public void testCPPSpecDeclsDefs() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        hfile = importFile("testCPPSpecDeclsDefs.h", hcode); 
        file = importFile("testCPPSpecDeclsDefs.c", scode); 
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
	//    abc
	// }
	public void _testBug101287() throws Exception {
        StringBuffer[] buffers= getContents(2);
        String hcode= buffers[0].toString();
        String scode= buffers[1].toString();
        hfile = importFile("testBug101287.h", hcode); 
        file = importFile("testBug101287.c", scode); 
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
        hfile = importFileWithLink("testBug103697.h", hcode); 
        file = importFileWithLink("testBug103697.c", scode); 
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
        hfile = importFile("testBug78354.h", hcode); 
        file = importFile("testBug78354.c", scode); 
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
}
