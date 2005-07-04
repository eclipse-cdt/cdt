/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.selectiontests;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Test Ctrl_F3/F3 with the DOM Indexer for a C++ project.
 *  
 * @author dsteffle
 */
public class CPPSelectionTestsDOMIndexer extends BaseSelectionTestsIndexer implements IIndexChangeListener {
	private static final String INDEX_TAG = "1196338025.index"; //$NON-NLS-1$
	IFile 					file;
	NullProgressMonitor		monitor;
	IndexManager 			indexManager;
	DOMSourceIndexer			sourceIndexer;

	static final String sourceIndexerID = "org.eclipse.cdt.core.domsourceindexer"; //$NON-NLS-1$
	
	public CPPSelectionTestsDOMIndexer(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		//Create temp project
		project = createProject("CPPSelectionTestsDOMIndexerProject"); //$NON-NLS-1$
		IPath pathLoc = CCorePlugin.getDefault().getStateLocation();
		
		File indexFile = new File(pathLoc.append(INDEX_TAG).toOSString());
		if (indexFile.exists())
			indexFile.delete();
		
		//Set the id of the source indexer extension point as a session property to allow
		//index manager to instantiate it
		project.setSessionProperty(IndexManager.indexerIDKey, sourceIndexerID);
		
		//Enable indexing on test project
		project.setSessionProperty(DOMSourceIndexer.activationKey,new Boolean(true));
		
		if (project==null) fail("Unable to create project");	 //$NON-NLS-1$
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
		CCProjectNature.addCCNature(project,monitor); 
		MakeProjectNature.addNature(project, new NullProgressMonitor());
		ScannerConfigNature.addScannerConfigNature(project);
		PerProjectSICollector.calculateCompilerBuiltins(project);
		
		indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();

		resetIndexer(sourceIndexerID); // set indexer
		
		//indexManager.reset();
		//Get the indexer used for the test project
		sourceIndexer = (DOMSourceIndexer) indexManager.getIndexerForProject(project);
		sourceIndexer.addIndexChangeListener(this);
	}

	protected void tearDown() {
		try {
			super.tearDown();
			sourceIndexer.removeIndexChangeListener(this);
		} catch (Exception e1) {
		}
		//Delete project
		if (project.exists()) {
			try {
				System.gc();
				System.runFinalization();
				project.delete(true, monitor);
			} catch (CoreException e) {
				fail(getMessage(e.getStatus()));
			}
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CPPSelectionTestsDOMIndexer.class.getName());

		suite.addTest(new CPPSelectionTestsDOMIndexer("testBug93281")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBasicDefinition")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBug95224")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBasicTemplateInstance")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBug86829A")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBug86829B")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testCPPSpecDeclsDefs")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBug95225")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testNoDefinitions")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBug95202")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBug95229")); //$NON-NLS-1$
		suite.addTest(new CPPSelectionTestsDOMIndexer("testBug101287")); //$NON-NLS-1$
	
		return suite;
	}

	private IProject createProject(String projectName) throws CoreException {
		ICProject cPrj = CProjectHelper.createCCProject(projectName, "bin"); //$NON-NLS-1$
		return cPrj.getProject();
	}
	
    public void testBug93281() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class Point{                         \n"); //$NON-NLS-1$
        buffer.append("public:                              \n"); //$NON-NLS-1$
        buffer.append("Point(): xCoord(0){}                 \n"); //$NON-NLS-1$
        buffer.append("Point& operator=(const Point &rhs){return *this;}    // line A\n"); //$NON-NLS-1$
        buffer.append("void* operator new [ ] (unsigned int);\n"); //$NON-NLS-1$
        buffer.append("private:                             \n"); //$NON-NLS-1$
        buffer.append("int xCoord;                          \n"); //$NON-NLS-1$
        buffer.append("};                                   \n"); //$NON-NLS-1$
        buffer.append("static const Point zero;\n"); //$NON-NLS-1$
        buffer.append("int main(int argc, char **argv) {        \n"); //$NON-NLS-1$
        buffer.append("Point *p2 = new Point();         \n"); //$NON-NLS-1$
        buffer.append("p2->    operator // /* operator */ // F3 in the middle \n"); //$NON-NLS-1$
        buffer.append("//of \"operator\" should work\n"); //$NON-NLS-1$
        buffer.append("// \\n"); //$NON-NLS-1$
        buffer.append("/* */\n"); //$NON-NLS-1$
        buffer.append("=(zero);           // line B\n"); //$NON-NLS-1$
        buffer.append("p2->operator /* oh yeah */ new // F3 in the middle of \"operator\"\n"); //$NON-NLS-1$
        buffer.append("// should work\n"); //$NON-NLS-1$
        buffer.append("//\n"); //$NON-NLS-1$
        buffer.append("[ /* sweet */ ] //\n"); //$NON-NLS-1$
        buffer.append("(2);\n"); //$NON-NLS-1$
        buffer.append("return (0);                          \n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("test93281.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("p2->operator") + 6; //$NON-NLS-1$
        IASTNode node = testF3(file, offset);
        
        assertTrue(node instanceof IASTName);
        assertEquals(((IASTName)node).toString(), "operator new[]"); //$NON-NLS-1$
        assertEquals(((ASTNode)node).getOffset(), 183);
        assertEquals(((ASTNode)node).getLength(), 16);
        
        offset = code.indexOf("p2->    operator") + 11; //$NON-NLS-1$
        node = testF3(file, offset);
        
        assertTrue(node instanceof IASTName);
        assertEquals(((IASTName)node).toString(), "operator ="); //$NON-NLS-1$
        assertEquals(((ASTNode)node).getOffset(), 121);
        assertEquals(((ASTNode)node).getLength(), 9);
        
    }
        
    public void testBasicDefinition() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("extern int MyInt;       // MyInt is in another file\n"); //$NON-NLS-1$
        buffer.append("extern const int MyConst;   // MyConst is in another file\n"); //$NON-NLS-1$
        buffer.append("void MyFunc(int);       // often used in header files\n"); //$NON-NLS-1$
        buffer.append("struct MyStruct;        // often used in header files\n"); //$NON-NLS-1$
        buffer.append("typedef int NewInt;     // a normal typedef statement\n"); //$NON-NLS-1$
        buffer.append("class MyClass;          // often used in header files\n"); //$NON-NLS-1$
        buffer.append("int MyInt;\n"); //$NON-NLS-1$
        buffer.append("extern const int MyConst = 42;\n"); //$NON-NLS-1$
        buffer.append("void MyFunc(int a) { cout << a << endl; }\n"); //$NON-NLS-1$
        buffer.append("struct MyStruct { int Member1; int Member2; };\n"); //$NON-NLS-1$
        buffer.append("class MyClass { int MemberVar; };\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("testBasicDefinition.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("MyInt;\n") + 2; //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 5);
        assertEquals(((IASTName)def).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 330);
        assertEquals(((ASTNode)def).getLength(), 5);
        
        offset = code.indexOf("MyConst = 42") + 2; //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 69);
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 354);
        assertEquals(((ASTNode)def).getLength(), 7);
        
        offset = code.indexOf("MyFunc(int a)") + 2; //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 115);
        assertEquals(((ASTNode)decl).getLength(), 6);
        assertEquals(((IASTName)def).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 373);
        assertEquals(((ASTNode)def).getLength(), 6);
        
        offset = code.indexOf("MyStruct {") + 2; //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 171);
        assertEquals(((ASTNode)decl).getLength(), 8);
        assertEquals(((IASTName)def).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 417);
        assertEquals(((ASTNode)def).getLength(), 8);
        
        offset = code.indexOf("MyClass {") + 2; //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyClass"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 278);
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyClass"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 463);
        assertEquals(((ASTNode)def).getLength(), 7);
    }
    
	public void testBug95224() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class A{\n"); //$NON-NLS-1$
	    writer.write( "A();\n"); //$NON-NLS-1$
	    writer.write( "A(const A&); // open definition on A finds class A\n"); //$NON-NLS-1$
	    writer.write( "~A(); // open definition on A finds nothing\n"); //$NON-NLS-1$
	    writer.write( "};\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		IFile file = importFile("testBug95224.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("A(); // open definition "); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "~A"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 65);
        assertEquals(((ASTNode)decl).getLength(), 2);
        assertEquals(((IASTName)def).toString(), "A"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 6);
        assertEquals(((ASTNode)def).getLength(), 1);
	}
	
	public void testBasicTemplateInstance() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "namespace N{                               \n"); //$NON-NLS-1$
	    writer.write( "   template < class T > class AAA { T _t; };\n"); //$NON-NLS-1$
	    writer.write( "};                                         \n"); //$NON-NLS-1$
	    writer.write( "N::AAA<int> a;                             \n"); //$NON-NLS-1$
	    
		String code = writer.toString();
		IFile file = importFile("testBasicTemplateInstance.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("AAA<int>"); //$NON-NLS-1$
        IASTNode def1 = testCtrl_F3(file, offset, 3);
        IASTNode decl1 = testF3(file, offset, 3);
        assertTrue(def1 instanceof IASTName);
        assertTrue(decl1 instanceof IASTName);
        assertEquals(((IASTName)decl1).toString(), "AAA"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl1).getOffset(), 74);
        assertEquals(((ASTNode)decl1).getLength(), 3);
        assertEquals(((IASTName)def1).toString(), "AAA"); //$NON-NLS-1$
        assertEquals(((ASTNode)def1).getOffset(), 74);
        assertEquals(((ASTNode)def1).getLength(), 3);
		
		IASTNode deCtrl_F3 = testCtrl_F3(file, offset, 8);
        IASTNode decl2 = testF3(file, offset, 8);
        assertTrue(deCtrl_F3 instanceof IASTName);
        assertTrue(decl2 instanceof IASTName);
        assertEquals(((IASTName)decl2).toString(), "AAA"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl2).getOffset(), 74);
        assertEquals(((ASTNode)decl2).getLength(), 3);
        assertEquals(((IASTName)deCtrl_F3).toString(), "AAA"); //$NON-NLS-1$
        assertEquals(((ASTNode)deCtrl_F3).getOffset(), 74);
        assertEquals(((ASTNode)deCtrl_F3).getLength(), 3);
	}
	
	public void testBug86829A() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class X {\n"); //$NON-NLS-1$
        buffer.append("public:\n"); //$NON-NLS-1$
        buffer.append("X(int); // openReferences fails to find the constructor in g()\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("X f(X);\n"); //$NON-NLS-1$
        buffer.append("void g()\n"); //$NON-NLS-1$
        buffer.append("{\n"); //$NON-NLS-1$
        buffer.append("X b = f(X(2)); // openDeclarations on X(int) finds the class and not \n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$

		String code = buffer.toString();
        IFile file = importFile("testBug86829A.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("X(2)"); //$NON-NLS-1$
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 18);
        assertEquals(((ASTNode)decl).getLength(), 1);
	}
	
	public void testBug86829B() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class X {\n"); //$NON-NLS-1$
        buffer.append("public:\n"); //$NON-NLS-1$
        buffer.append("operator int();\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("class Y {\n"); //$NON-NLS-1$
        buffer.append("public:\n"); //$NON-NLS-1$
        buffer.append("operator X();\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("Y a;\n"); //$NON-NLS-1$
        buffer.append("int c = X(a); // OK: a.operator X().operator int()\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testBug86829B.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("X(a);"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 6);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 6);
        assertEquals(((ASTNode)def).getLength(), 1);
	}
	
	// taken from C++ spec 3.1-3:
	/*
	// all but one of the following are definitions:
	int a; // defines a
	extern const int c = 1; // defines c
	int f(int x) { return x+a; } // defines f and defines x
	struct S { int a; int b; }; // defines S, S::a, and S::b
	struct X { // defines X
		int x; // defines nonstatic data member x
		static int y; // declares static data member y
		X(): x(0) { } // defines a constructor of X
	};
	int X::y = 1; // defines X::y
	enum { up, down }; // defines up and down
	namespace N { int d; } // defines N and N::d
	namespace N1 = N; // defines N1
	X anX; // defines anX
	// whereas these are just declarations:
	extern int a; // declares a
	extern const int c; // declares c
	int f(int); // declares f
	struct S; // declares S
	typedef int Int; // declares Int
	extern X anotherX; // declares anotherX
	using N::d; // declares N::d
	*/
	public void testCPPSpecDeclsDefs() throws Exception {
		StringBuffer buffer = new StringBuffer();
        buffer.append("int a; // defines a\n"); //$NON-NLS-1$
        buffer.append("extern const int c = 1; // defines c\n"); //$NON-NLS-1$
        buffer.append("int f(int x) { return x+a; } // defines f and defines x\n"); //$NON-NLS-1$
        buffer.append("struct S { int a; int b; }; // defines S, S::a, and S::b\n"); //$NON-NLS-1$
        buffer.append("struct X { // defines X\n"); //$NON-NLS-1$
        buffer.append("int x; // defines nonstatic data member x\n"); //$NON-NLS-1$
        buffer.append("static int y; // declares static data member y\n"); //$NON-NLS-1$
        buffer.append("X(): x(0) { } // defines a constructor of X\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("int X::y = 1; // defines X::y\n"); //$NON-NLS-1$
        buffer.append("enum { up, down }; // defines up and down\n"); //$NON-NLS-1$
        buffer.append("namespace N { int d; } // defines N and N::d\n"); //$NON-NLS-1$
        buffer.append("namespace N1 = N; // defines N1\n"); //$NON-NLS-1$
        buffer.append("X anX; // defines anX\n"); //$NON-NLS-1$
        buffer.append("extern int a; // declares a\n"); //$NON-NLS-1$
        buffer.append("extern const int c; // declares c\n"); //$NON-NLS-1$
        buffer.append("int f(int); // declares f\n"); //$NON-NLS-1$
        buffer.append("struct S; // declares S\n"); //$NON-NLS-1$
        buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$
        buffer.append("extern X anotherX; // declares anotherX\n"); //$NON-NLS-1$
        buffer.append("using N::d; // declares N::d\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testCPPSpecDeclsDefs.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("a; // defines a"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("c = 1; // defines c"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 37);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 37);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("f(int x) { return x+a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 61);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 61);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x) { return x+a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 67);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x+a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 67);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x+a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 67);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 67);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("a; } // defines f and defines x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("S { int a; int b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 120);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 120);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("a; int b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 128);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 128);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "b"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 135);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "b"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 135);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("X { // defines X"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x; // defines nonstatic data member x"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 198);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 198);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("y; // declares static data member y"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "y"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 247);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "y"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 337);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("X(): x(0) { } // defines a constructor of X"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 283);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 283);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("x(0) { } // defines a constructor of X"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 198);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 198);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("X::y = 1; // defines X::y"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("y = 1; // defines X::y"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "y"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 247);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "y"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 337);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("up, down }; // defines up and down"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "up"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 367);
        assertEquals(((ASTNode)decl).getLength(), 2);
        assertEquals(((IASTName)def).toString(), "up"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 367);
        assertEquals(((ASTNode)def).getLength(), 2);
		
		offset = code.indexOf("down }; // defines up and down"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "down"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 371);
        assertEquals(((ASTNode)decl).getLength(), 4);
        assertEquals(((IASTName)def).toString(), "down"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 371);
        assertEquals(((ASTNode)def).getLength(), 4);
		
		offset = code.indexOf("N { int d; } // defines N and N::d"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 412);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 412);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("d; } // defines N and N::d"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "d"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 420);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "d"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 420);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("N1 = N; // defines N1"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "N1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 457);
        assertEquals(((ASTNode)decl).getLength(), 2);
        assertEquals(((IASTName)def).toString(), "N1"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 457);
        assertEquals(((ASTNode)def).getLength(), 2);
		
		offset = code.indexOf("N; // defines N1"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 412);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 412);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("X anX; // defines anX"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("anX; // defines anX"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "anX"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 481);
        assertEquals(((ASTNode)decl).getLength(), 3);
        assertEquals(((IASTName)def).toString(), "anX"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 481);
        assertEquals(((ASTNode)def).getLength(), 3);
		
		offset = code.indexOf("a; // declares a"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "a"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 4);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("c; // declares c"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 37);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "c"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 37);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("f(int); // declares f"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 61);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "f"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 61);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("S; // declares S"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 120);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "S"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 120);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("Int; // declares Int"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        try {
        	assertNull(def); // TODO raised bug 96689
        	assertTrue(false); // try/catch/assertTrue(false) added to alert the tester when this test passes!
        } catch (AssertionFailedError e) {}
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "Int"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 625);
        assertEquals(((ASTNode)decl).getLength(), 3);
        
		offset = code.indexOf("X anotherX; // declares anotherX"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 177);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "X"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 177);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("anotherX; // declares anotherX"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        try {
        	assertNull(def); // TODO raised bug 96689
        	assertTrue(false); // try/catch/assertTrue(false) added to alert the tester when this test passes!
        } catch (AssertionFailedError e) {}
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "anotherX"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 655);
        assertEquals(((ASTNode)decl).getLength(), 8);
        		
		offset = code.indexOf("N::d; // declares N::d"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 412);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "N"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 412);
        assertEquals(((ASTNode)def).getLength(), 1);
		
		offset = code.indexOf("d; // declares N::d"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "d"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 420);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "d"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 420);
        assertEquals(((ASTNode)def).getLength(), 1);
	}
	
	public void testBug95225() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class Overflow {\n"); //$NON-NLS-1$
        buffer.append("public:\n"); //$NON-NLS-1$
        buffer.append("Overflow(char,double,double);\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("void f(double x)\n"); //$NON-NLS-1$
        buffer.append("{\n"); //$NON-NLS-1$
        buffer.append("throw Overflow('+',x,3.45e107);\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        buffer.append("int foo() {\n"); //$NON-NLS-1$
        buffer.append("try {\n"); //$NON-NLS-1$
        buffer.append("f(1.2);\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        buffer.append("catch(Overflow& oo) {\n"); //$NON-NLS-1$
        buffer.append("				// handle exceptions of type Overflow here\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testBug95225.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("rflow('+',x,3.45e107);"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "Overflow"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 25);
        assertEquals(((ASTNode)decl).getLength(), 8);
        assertEquals(((IASTName)def).toString(), "Overflow"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 6);
        assertEquals(((ASTNode)def).getLength(), 8);
        
		offset = code.indexOf("x,3.45e107);"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 72);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "x"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 72);
        assertEquals(((ASTNode)def).getLength(), 1);
    }
	
	public void testNoDefinitions() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("extern int a1; // declares a\n"); //$NON-NLS-1$
		buffer.append("extern const int c1; // declares c\n"); //$NON-NLS-1$
		buffer.append("int f1(int); // declares f\n"); //$NON-NLS-1$
		buffer.append("struct S1; // declares S\n"); //$NON-NLS-1$
		buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testNoDefinitions.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("a1; // declares a"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        try {
        	assertNull(def); // TODO raised bug 96689
        	assertTrue(false); // try/catch/assertTrue(false) added to alert the tester when this test passes!
        } catch (AssertionFailedError e) {}
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "a1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("c1; // declares c"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        try {
        	assertNull(def); // TODO raised bug 96694
        	assertTrue(false); // try/catch/assertTrue(false) added to alert the tester when this test passes!
        } catch (AssertionFailedError e) {}
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "c1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 46);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("f1(int); // declares f"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertNull(def);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "f1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 68);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("S1; // declares S"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        assertNull(def);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "S1"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 98);
        assertEquals(((ASTNode)decl).getLength(), 2);
		
		offset = code.indexOf("Int; // declares Int"); //$NON-NLS-1$
        def = testCtrl_F3(file, offset);
        decl = testF3(file, offset);
        try {
        	assertNull(def); // TODO raised bug 96689
        	assertTrue(false); // try/catch/assertTrue(false) added to alert the tester when this test passes!
        } catch (AssertionFailedError e) {}
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "Int"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 128);
        assertEquals(((ASTNode)decl).getLength(), 3);
	}
    
    public void testBug95202() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A { }; // implicitlydeclared A::operator=\n"); //$NON-NLS-1$
        buffer.append("struct B : A {\n"); //$NON-NLS-1$
        buffer.append("B& operator=(const B &);\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("B& B::operator=(const B& s) {\n"); //$NON-NLS-1$
        buffer.append("this->B::operator=(s); // wellformed\n"); //$NON-NLS-1$
        buffer.append("return *this;\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("testBug95202.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("s); // wellformed"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "s"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 117);
        assertEquals(((ASTNode)decl).getLength(), 1);
        assertEquals(((IASTName)def).toString(), "s"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 117);
        assertEquals(((ASTNode)def).getLength(), 1);
        
    }
    
    public void testBug95229() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A {\n"); //$NON-NLS-1$
        buffer.append("operator short(); // F3 on operator causes an infinite loop\n"); //$NON-NLS-1$
        buffer.append("} a;\n"); //$NON-NLS-1$
        buffer.append("int f(int);\n"); //$NON-NLS-1$
        buffer.append("int f(float);\n"); //$NON-NLS-1$
        buffer.append("int i = f(a); // Calls f(int), because short -> int is\n"); //$NON-NLS-1$
                
        String code = buffer.toString();
        IFile file = importFile("testBug95229.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("rator short(); // F3"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertNull(def);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "operator short"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 14);
    }
    
	public void testBug101287() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int abc;\n"); //$NON-NLS-1$
		buffer.append("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
		buffer.append("abc\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		
        String code = buffer.toString();
        IFile file = importFile("testBug101287.c", code); //$NON-NLS-1$
        
        int offset = code.indexOf("abc\n"); //$NON-NLS-1$
        IASTNode def = testCtrl_F3(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "abc"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 3);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "abc"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 4);
        assertEquals(((ASTNode)decl).getLength(), 3);
	}

    // REMINDER: see CPPSelectionTestsDomIndexer#suite() when appending new tests to this suite
}
