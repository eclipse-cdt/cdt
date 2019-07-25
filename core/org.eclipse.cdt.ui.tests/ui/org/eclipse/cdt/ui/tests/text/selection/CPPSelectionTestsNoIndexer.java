/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.selection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationsAction;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * It is required to test the selection performance independent of the indexer to make sure that the DOM
 * is functioning properly.
 *
 * Indexer bugs can drastically influence the correctness of these tests so the indexer has to be off when
 * performing them.
 *
 * @author dsteffle
 */
public class CPPSelectionTestsNoIndexer extends BaseSelectionTests {
	private static final String INDEX_FILE_ID = "2946365241"; //$NON-NLS-1$
	static NullProgressMonitor monitor;
	static IWorkspace workspace;
	static IProject project;
	static ICProject cPrj;
	static FileManager fileManager;
	static boolean disabledHelpContributions = false;

	static void initProject() {
		if (project != null) {
			return;
		}

		//(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
		monitor = new NullProgressMonitor();

		workspace = ResourcesPlugin.getWorkspace();

		try {
			cPrj = CProjectHelper.createCCProject("CPPSelectionTestsNoIndexer", "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$

			project = cPrj.getProject();

			IPath pathLoc = CCorePlugin.getDefault().getStateLocation();
			File indexFile = new File(pathLoc.append(INDEX_FILE_ID + ".index").toOSString()); //$NON-NLS-1$
			if (indexFile.exists())
				indexFile.delete();
		} catch (CoreException e) {
			/*boo*/
		}
		if (project == null)
			fail("Unable to create project"); //$NON-NLS-1$

		//Create file manager
		fileManager = new FileManager();
	}

	public CPPSelectionTestsNoIndexer() {
		super();
	}

	/**
	 * @param name
	 */
	public CPPSelectionTestsNoIndexer(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = suite(CPPSelectionTestsNoIndexer.class, "_");
		suite.addTest(new CPPSelectionTestsNoIndexer("cleanupProject")); //$NON-NLS-1$
		return suite;
	}

	public void cleanupProject() throws Exception {
		closeAllEditors();
		try {
			project.delete(true, false, monitor);
		} catch (CoreException e) {
			try {
				project.delete(true, false, monitor);
			} catch (CoreException e1) {
			}
		} finally {
			project = null;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initProject();
		OpenDeclarationsAction.sDisallowAmbiguousInput = true;
	}

	@Override
	protected void tearDown() throws Exception {
		if (project == null || !project.exists())
			return;

		closeAllEditors();

		IResource[] members = project.members();
		for (IResource member : members) {
			if (member.getName().equals(".project") || member.getName().equals(".cproject")) //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			if (member.getName().equals(".settings"))
				continue;
			try {
				member.delete(false, monitor);
			} catch (Throwable e) {
				/*boo*/
			}
		}
	}

	protected IFile importFile(String fileName, String contents) throws Exception {
		//Obtain file handle
		IFile file = project.getProject().getFile(fileName);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		//Create file input stream
		if (file.exists())
			file.setContents(stream, false, false, monitor);
		else
			file.create(stream, false, monitor);

		fileManager.addFile(file);

		return file;
	}

	protected IFile importFileWithLink(String fileName, String contents) throws Exception {
		//Obtain file handle
		IFile file = project.getProject().getFile(fileName);

		IPath location = new Path(project.getLocation().removeLastSegments(1).toOSString() + File.separator + fileName);

		File linkFile = new File(location.toOSString());
		if (!linkFile.exists()) {
			linkFile.createNewFile();
		}

		file.createLink(location, IResource.ALLOW_MISSING_LOCAL, null);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		//Create file input stream
		if (file.exists())
			file.setContents(stream, false, false, monitor);
		else
			file.create(stream, false, monitor);

		fileManager.addFile(file);

		return file;
	}

	protected IFile importFileInsideLinkedFolder(String fileName, String contents, String folderName) throws Exception {
		IFolder linkedFolder = project.getFolder(folderName);
		IPath folderLocation = new Path(
				project.getLocation().toOSString() + File.separator + folderName + "_this_is_linked"); //$NON-NLS-1$
		IFolder actualFolder = project.getFolder(folderName + "_this_is_linked"); //$NON-NLS-1$
		if (!actualFolder.exists())
			actualFolder.create(true, true, monitor);

		linkedFolder.createLink(folderLocation, IResource.NONE, monitor);

		actualFolder.delete(true, false, monitor);

		IFile file = linkedFolder.getFile(fileName);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		//Create file input stream
		if (file.exists())
			file.setContents(stream, false, false, monitor);
		else
			file.create(stream, false, monitor);

		fileManager.addFile(file);

		return file;
	}

	private void assertContents(String code, int offset, String expected) {
		assertEquals(expected, code.substring(offset, offset + expected.length()));
	}

	public void testBug93281() throws Exception {
		StringBuilder buffer = new StringBuilder();
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
		assertEquals("operator new[]", ((IASTName) node).toString()); //$NON-NLS-1$
		assertEquals(183, ((ASTNode) node).getOffset());
		assertEquals(16, ((ASTNode) node).getLength());

		offset = code.indexOf("p2->    operator") + 11; //$NON-NLS-1$
		node = testF3(file, offset);

		assertTrue(node instanceof IASTName);
		assertEquals("operator =", ((IASTName) node).toString()); //$NON-NLS-1$
		assertEquals(121, ((ASTNode) node).getOffset());
		assertEquals(9, ((ASTNode) node).getLength());
	}

	public void testBasicDefinition() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("extern int MyInt;       // def is in another file  \n"); //$NON-NLS-1$
		buffer.append("extern const int MyConst;   // def is in another file    \n"); //$NON-NLS-1$
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

		int offset = code.indexOf("MyInt") + 2; //$NON-NLS-1$
		int defOffset = code.indexOf("MyInt", offset) + 2; //$NON-NLS-1$
		IASTNode def = testF3(file, offset);
		IASTNode decl = testF3(file, defOffset);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyInt", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(11, ((ASTNode) decl).getOffset());
		assertEquals(5, ((ASTNode) decl).getLength());
		assertEquals("MyInt", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(330, ((ASTNode) def).getOffset());
		assertEquals(5, ((ASTNode) def).getLength());

		offset = code.indexOf("MyConst") + 2;
		defOffset = code.indexOf("MyConst", offset) + 2;
		def = testF3(file, offset);
		decl = testF3(file, defOffset);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyConst", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(69, ((ASTNode) decl).getOffset());
		assertEquals(7, ((ASTNode) decl).getLength());
		assertEquals("MyConst", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(354, ((ASTNode) def).getOffset());
		assertEquals(7, ((ASTNode) def).getLength());

		offset = code.indexOf("MyFunc") + 2;
		defOffset = code.indexOf("MyFunc", offset) + 2;
		def = testF3(file, offset);
		decl = testF3(file, defOffset);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyFunc", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(115, ((ASTNode) decl).getOffset());
		assertEquals(6, ((ASTNode) decl).getLength());
		assertEquals("MyFunc", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(373, ((ASTNode) def).getOffset());
		assertEquals(6, ((ASTNode) def).getLength());

		offset = code.indexOf("MyStruct") + 2;
		defOffset = code.indexOf("MyStruct", offset) + 2;
		def = testF3(file, offset);
		decl = testF3(file, defOffset);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyStruct", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(417, ((ASTNode) decl).getOffset());
		assertEquals(8, ((ASTNode) decl).getLength());
		assertEquals("MyStruct", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(417, ((ASTNode) def).getOffset());
		assertEquals(8, ((ASTNode) def).getLength());

		offset = code.indexOf("MyClass") + 2;
		defOffset = code.indexOf("MyClass", offset) + 2;
		def = testF3(file, offset);
		decl = testF3(file, defOffset);
		assertTrue(def instanceof IASTName);
		assertTrue(decl instanceof IASTName);
		assertEquals("MyClass", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(463, ((ASTNode) decl).getOffset());
		assertEquals(7, ((ASTNode) decl).getLength());
		assertEquals("MyClass", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(463, ((ASTNode) def).getOffset());
		assertEquals(7, ((ASTNode) def).getLength());
	}

	public void testBug95224() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class A{\n"); //$NON-NLS-1$
		writer.write("A();\n"); //$NON-NLS-1$
		writer.write("A(const A&); // open definition on A finds class A\n"); //$NON-NLS-1$
		writer.write("~A(); // open definition on A finds nothing\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$

		String code = writer.toString();
		IFile file = importFile("testBug95224.cpp", code); //$NON-NLS-1$

		int offset = code.indexOf("A(); // open definition "); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("~A", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(65, ((ASTNode) decl).getOffset());
		assertEquals(2, ((ASTNode) decl).getLength());
	}

	public void testBasicTemplateInstance() throws Exception {
		Writer writer = new StringWriter();
		writer.write("namespace N{                               \n"); //$NON-NLS-1$
		writer.write("   template < class T > class AAA { T _t; };\n"); //$NON-NLS-1$
		writer.write("};                                         \n"); //$NON-NLS-1$
		writer.write("N::AAA<int> a;                             \n"); //$NON-NLS-1$

		String code = writer.toString();
		IFile file = importFile("testBasicTemplateInstance.cpp", code); //$NON-NLS-1$

		int offset = code.indexOf("AAA<int>"); //$NON-NLS-1$
		IASTNode decl1 = testF3(file, offset, 3);
		assertTrue(decl1 instanceof IASTName);
		assertEquals("AAA", ((IASTName) decl1).toString()); //$NON-NLS-1$
		assertEquals(74, ((ASTNode) decl1).getOffset());
		assertEquals(3, ((ASTNode) decl1).getLength());

		IASTNode decl2 = testF3(file, offset, 8);
		assertTrue(decl2 instanceof IASTName);
		assertEquals("AAA", ((IASTName) decl2).toString()); //$NON-NLS-1$
		assertEquals(74, ((ASTNode) decl2).getOffset());
		assertEquals(3, ((ASTNode) decl2).getLength());
	}

	public void testBug86829A() throws Exception {
		StringBuilder buffer = new StringBuilder();
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
		assertEquals("X", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(18, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());
	}

	public void testBug86829B() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("class X {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("class Y {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("operator X();\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		buffer.append("void test() {\n");
		buffer.append("Y a;\n"); //$NON-NLS-1$
		buffer.append("int c = X(a); // OK: a.operator X().operator int()\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$

		String code = buffer.toString();
		IFile file = importFile("testBug86829B.cpp", code); //$NON-NLS-1$

		int offset = code.indexOf("X(a);"); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("X", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(6, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());
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
	X anX; // defines anX, implicitly calls X()
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
		StringBuilder buffer = new StringBuilder();
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
		buffer.append("int f(int y); // declar f\n"); //$NON-NLS-1$
		buffer.append("struct S; // declares S\n"); //$NON-NLS-1$
		buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$
		buffer.append("extern X anotherX; // declares anotherX\n"); //$NON-NLS-1$
		buffer.append("using N::d; // declares N::d\n"); //$NON-NLS-1$

		String code = buffer.toString();
		IFile file = importFile("testCPPSpecDeclsDefs.cpp", code); //$NON-NLS-1$
		int offset = code.indexOf("a; // defines a"); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("a", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(512, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("c = 1; // defines c"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("c", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(546, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("f(int x) { return x+a; } // defines f and defines x"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("f", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(567, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("x) { return x+a; } // defines f and defines x"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("x", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(67, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("x+a; } // defines f and defines x"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("x", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(67, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("x+a; } // defines f and defines x"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("x", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(67, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("a; } // defines f and defines x"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("a", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(4, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("S { int a; int b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("S", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(120, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("a; int b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("a", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(128, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("b; }; // defines S, S::a, and S::b"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("b", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(135, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("X { // defines X"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("X", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(177, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("x; // defines nonstatic data member x"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("x", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(198, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		IASTNode def;
		offset = code.indexOf("y; // declares static data member y"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("y", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(337, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("X(): x(0) { } // defines a constructor of X"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("X", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(283, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("x(0) { } // defines a constructor of X"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("x", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(198, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("X::y = 1; // defines X::y"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("X", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(177, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("y = 1; // defines X::y"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("y", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(247, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("up, down }; // defines up and down"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("up", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(367, ((ASTNode) decl).getOffset());
		assertEquals(2, ((ASTNode) decl).getLength());

		offset = code.indexOf("down }; // defines up and down"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("down", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(371, ((ASTNode) decl).getOffset());
		assertEquals(4, ((ASTNode) decl).getLength());

		offset = code.indexOf("N { int d; } // defines N and N::d"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("N", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(412, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("d; } // defines N and N::d"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("d", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(695, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());

		offset = code.indexOf("N1 = N; // defines N1"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("N1", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(457, ((ASTNode) decl).getOffset());
		assertEquals(2, ((ASTNode) decl).getLength());

		offset = code.indexOf("N; // defines N1"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("N", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(412, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("X anX; // defines anX"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("X", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(177, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("anX; // defines anX"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("X", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(code.indexOf("X()"), ((ASTNode) decl).getOffset());
		assertEquals("X".length(), ((ASTNode) decl).getLength());

		offset = code.indexOf("a; // declares a"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("a", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(4, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("c; // declares c"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("c", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(37, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("f(int y); // declar f"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("f", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(61, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("S; // declares S"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("S", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(120, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("Int; // declares Int"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("Int", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(625, ((ASTNode) decl).getOffset());
		assertEquals(3, ((ASTNode) decl).getLength());

		offset = code.indexOf("X anotherX; // declares anotherX"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("X", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(177, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("anotherX; // declares anotherX"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("anotherX", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(655, ((ASTNode) decl).getOffset());
		assertEquals(8, ((ASTNode) decl).getLength());

		offset = code.indexOf("N::d; // declares N::d"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("N", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(412, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());

		offset = code.indexOf("d; // declares N::d"); //$NON-NLS-1$
		def = testF3(file, offset);
		assertTrue(def instanceof IASTName);
		assertEquals("d", ((IASTName) def).toString()); //$NON-NLS-1$
		assertEquals(420, ((ASTNode) def).getOffset());
		assertEquals(1, ((ASTNode) def).getLength());
	}

	public void testBug95225() throws Exception {
		StringBuilder buffer = new StringBuilder();
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
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("Overflow", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(25, ((ASTNode) decl).getOffset());
		assertEquals(8, ((ASTNode) decl).getLength());

		offset = code.indexOf("x,3.45e107);"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("x", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(72, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());
	}

	public void testNoDefinitions() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("extern int a1; // declares a\n"); //$NON-NLS-1$
		buffer.append("extern const int c1; // declares c\n"); //$NON-NLS-1$
		buffer.append("int f1(int); // declares f\n"); //$NON-NLS-1$
		buffer.append("struct S1; // declares S\n"); //$NON-NLS-1$
		buffer.append("typedef int Int; // declares Int\n"); //$NON-NLS-1$

		String code = buffer.toString();
		IFile file = importFile("testNoDefinitions.cpp", code); //$NON-NLS-1$

		int offset = code.indexOf("a1; // declares a"); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("a1", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(11, ((ASTNode) decl).getOffset());
		assertEquals(2, ((ASTNode) decl).getLength());

		offset = code.indexOf("c1; // declares c"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("c1", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(46, ((ASTNode) decl).getOffset());
		assertEquals(2, ((ASTNode) decl).getLength());

		offset = code.indexOf("f1(int); // declares f"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("f1", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(68, ((ASTNode) decl).getOffset());
		assertEquals(2, ((ASTNode) decl).getLength());

		offset = code.indexOf("S1; // declares S"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("S1", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(98, ((ASTNode) decl).getOffset());
		assertEquals(2, ((ASTNode) decl).getLength());

		offset = code.indexOf("Int; // declares Int"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("Int", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(128, ((ASTNode) decl).getOffset());
		assertEquals(3, ((ASTNode) decl).getLength());
	}

	public void testBug95202() throws Exception {
		StringBuilder buffer = new StringBuilder();
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
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("s", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(117, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());
	}

	public void testBug95229() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("struct A {\n"); //$NON-NLS-1$
		buffer.append("operator short(); // F3 on operator causes an infinite loop\n"); //$NON-NLS-1$
		buffer.append("} a;\n"); //$NON-NLS-1$
		buffer.append("int f(int);\n"); //$NON-NLS-1$
		buffer.append("int f(float);\n"); //$NON-NLS-1$
		buffer.append("int i = f(a); // Calls f(int), because short -> int is\n"); //$NON-NLS-1$

		String code = buffer.toString();
		IFile file = importFile("testBug95229.cpp", code); //$NON-NLS-1$

		int offset = code.indexOf("rator short(); // F3"); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("operator short int", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(11, ((ASTNode) decl).getOffset());
		assertEquals(14, ((ASTNode) decl).getLength());
	}

	public void testBug78354() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("typedef int TestTypeOne;\n"); //$NON-NLS-1$
		buffer.append("typedef int TestTypeTwo;\n"); //$NON-NLS-1$
		buffer.append("int main()\n"); //$NON-NLS-1$
		buffer.append("{\n"); //$NON-NLS-1$
		buffer.append("TestTypeOne myFirstLink = 5;\n"); //$NON-NLS-1$
		buffer.append("TestTypeTwo mySecondLink = 6;\n"); //$NON-NLS-1$
		buffer.append("return 0;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$

		String code = buffer.toString();
		IFile file = importFileWithLink("testBug78354.cpp", code); //$NON-NLS-1$

		int offset = code.indexOf("TestTypeOne myFirstLink = 5;"); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("TestTypeOne", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(12, ((ASTNode) decl).getOffset());
		assertEquals(11, ((ASTNode) decl).getLength());
	}

	public void testBug103697() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append(" return x;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$

		String code = buffer.toString();
		IFile file = importFileWithLink("testBug103697.cpp", code); //$NON-NLS-1$

		int offset = code.indexOf("return x;\n") + "return ".length(); //$NON-NLS-1$ //$NON-NLS-2$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("x", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(4, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());
	}

	public void testBug76043() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("int x;\n"); //$NON-NLS-1$
		buffer.append("int foo() {\n"); //$NON-NLS-1$
		buffer.append(" return x;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		String code = buffer.toString();

		IFile file = importFileInsideLinkedFolder("testBug76043.c", code, "folder"); //$NON-NLS-1$ //$NON-NLS-2$

		assertFalse(file.isLinked()); // I'm not sure why the IResource#isLinked() returns false if it's contained within a linked folder

		int offset = code.indexOf("return x;\n") + "return ".length(); //$NON-NLS-1$ //$NON-NLS-2$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("x", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(4, ((ASTNode) decl).getOffset());
		assertEquals(1, ((ASTNode) decl).getLength());
	}

	//  typedef int (*functionPointer)(int);
	//  functionPointer fctVariable;

	//  typedef int (functionPointerArray[2])(int);
	//  functionPointerArray fctVariablArray;
	public void testBug195822() throws Exception {
		StringBuilder[] contents = getContentsForTest(2);
		String code = contents[0].toString();
		String appendCode = contents[1].toString();

		String[] filenames = { "testBug195822.c", "testBug195822.cpp" };
		for (int i = 0; i < 2; i++) {
			IFile file = importFile(filenames[i], code);
			int od1 = code.indexOf("functionPointer");
			int or1 = code.indexOf("functionPointer", od1 + 1);

			IASTNode decl = testF3(file, or1);
			assertTrue(decl instanceof IASTName);
			assertEquals("functionPointer", ((IASTName) decl).toString()); //$NON-NLS-1$
			assertEquals(od1, ((ASTNode) decl).getOffset());

			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			assertNotNull(editor);
			assertTrue(editor instanceof ITextEditor);
			IDocument doc = ((ITextEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
			doc.replace(doc.getLength(), 0, appendCode);
			int od2 = appendCode.indexOf("functionPointerArray");
			int or2 = appendCode.indexOf("functionPointerArray", od2 + 1);

			decl = testF3(file, code.length() + or2);
			assertTrue(decl instanceof IASTName);
			assertEquals("functionPointerArray", ((IASTName) decl).toString());
			assertEquals(code.length() + od2, ((ASTNode) decl).getOffset());
		}
	}

	// #define EMPTY
	// EMPTY void foo() {}
	public void testEmptyMacro_Bug198649() throws Exception {
		String code = getContentsForTest(1)[0].toString();
		String[] filenames = { "testBug198649.c", "testBug198649.cpp" };
		for (int i = 0; i < 2; i++) {
			IFile file = importFile(filenames[i], code);
			int od1 = code.indexOf("EMPTY");
			int or1 = code.indexOf("EMPTY", od1 + 1);

			IASTNode decl = testF3(file, or1);
			assertTrue(decl instanceof IASTName);
			assertEquals("EMPTY", ((IASTName) decl).toString()); //$NON-NLS-1$
			assertEquals(od1, ((ASTNode) decl).getOffset());
		}
	}

	// static int myFunc(int) {}
	// #define USE_FUNC(x) (myFunc(x) == 0)
	public void testFallBackForStaticFuncs_Bug252549() throws Exception {
		String code = getContentsForTest(1)[0].toString();
		String[] filenames = { "testBug252549.c", "testBug252549.cpp" };
		for (int i = 0; i < 2; i++) {
			IFile file = importFile(filenames[i], code);
			int offset = code.indexOf("myFunc(x)");
			IASTNode decl = testF3(file, offset);
			assertTrue(decl instanceof IASTName);
			final IASTName name = (IASTName) decl;
			assertTrue(name.isDefinition());
			assertEquals("myFunc", name.toString());
		}
	}

	// struct A {
	//   void method(int p) {}
	// };
	//
	// void test(A* a) {
	//   a->method();
	//   a.method(0);
	// }
	// void A::method(int a, int b) {}
	// void B::method(int b) {}
	public void testUnresolvedMethod_278337() throws Exception {
		String code = getContentsForTest(1)[0].toString();
		IFile file = importFile("testBug278337.cpp", code);
		IASTNode node = testF3(file, code.indexOf("method();"));
		assertContents(code, node.getFileLocation().getNodeOffset(), "method(int p)");
		node = testF3(file, code.indexOf("method(0);"));
		assertNull(node);
		node = testF3(file, code.indexOf("method(int a, int b)"));
		assertContents(code, node.getFileLocation().getNodeOffset(), "method(int p)");
		node = testF3(file, code.indexOf("method(int b)"));
		// Should not navigate away since there is no good candidate.
		assertContents(code, node.getFileLocation().getNodeOffset(), "method(int b)");
	}

	// class A {
	//   class B {};
	// };
	//
	// B b;
	public void testUnresolvedType() throws Exception {
		String code = getContentsForTest(1)[0].toString();
		IFile file = importFile("testUndefinedType.cpp", code);
		int offset = code.indexOf("B b;");
		IASTNode node = testF3(file, offset);
		assertNull(node);
	}

	// void func(int a);
	// void func(float a);
	// void func(int* a);
	// void test() {
	//   func();
	// }
	public void testUnresolvedOverloadedFunction() throws Exception {
		String code = getContentsForTest(1)[0].toString();
		IFile file = importFile("testUnresolvedOverloadFunction.cpp", code);
		int offset = code.indexOf("func();");
		try {
			IASTNode node = testF3(file, offset);
			fail("Didn't expect navigation to succeed due to multiple choices.");
		} catch (RuntimeException e) {
			assertEquals("ambiguous input: 3", e.getMessage());
		}
	}

	// namespace nm {
	//   template<typename T> void func(T a, T b){}
	// }
	// template<typename Tmp> void testFunc() {
	//   Tmp val;
	//   nm::func(val, val);
	// }
	public void testDependentNameInNamespace_281736() throws Exception {
		String code = getContentsForTest(1)[0].toString();
		IFile file = importFile("testDependentNameInNamespace.cpp", code);
		int offset = code.indexOf("func(val, val);");
		IASTNode node = testF3(file, offset);
		assertContents(code, node.getFileLocation().getNodeOffset(), "func(T a, T b)");
	}

	//	template<typename T>  void func(T a){}
	//	template<typename T>  void func(T a, T b){}
	//
	//	template<typename Tmp> void testFunc() {
	//	  Tmp val;
	//	  func(val, val);  // F3 could know that 'func(T a)' cannot be a correct match.
	//	}
	public void testDependentNameTwoChoices_281736() throws Exception {
		String code = getContentsForTest(1)[0].toString();
		IFile file = importFile("testDependentNameTwoChoices_281736.cpp", code);
		int offset = code.indexOf("func(val, val);");
		IASTNode node = testF3(file, offset);
		assertContents(code, node.getFileLocation().getNodeOffset(), "func(T a, T b)");
	}

	//    namespace N {
	//    	template <typename T> class AAA { T _t; };
	//    }
	//    N::AAA<int> a;
	public void testBug92632() throws Exception {
		String code = getContentsForTest(1)[0].toString();
		IFile file = importFile("testBug92632.cpp", code);
		int index = code.indexOf("AAA<int>"); //$NON-NLS-1$
		IASTNode node = testF3(file, index);
		assertContents(code, node.getFileLocation().getNodeOffset(), "AAA");
		node = testF3(file, index + 4);
		assertContents(code, node.getFileLocation().getNodeOffset(), "AAA");
	}

	//	void bug(int var) {
	//		int foo = var;
	//		int foo2(var);
	//	}
	public void testBug325135a() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug325135a.cpp", code); //$NON-NLS-1$
		int parOffset = code.indexOf("var)");

		int offset = code.indexOf("var;"); //$NON-NLS-1$
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("var", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(parOffset, ((ASTNode) decl).getOffset());
		assertEquals(3, ((ASTNode) decl).getLength());

		offset = code.indexOf("var);"); //$NON-NLS-1$
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("var", ((IASTName) decl).toString()); //$NON-NLS-1$
		assertEquals(parOffset, ((ASTNode) decl).getOffset());
		assertEquals(3, ((ASTNode) decl).getLength());
	}

	//	template<typename T> class C {
	//		template<typename V> void f(V v) {
	//			T t;
	//			V s;
	//		}
	//	};
	public void testBug325135b() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug325135b.cpp", code); //$NON-NLS-1$

		int offsetT = code.indexOf("T>");
		int offsetV = code.indexOf("V>");

		int offset = code.indexOf("T t;");
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("T", ((IASTName) decl).toString());
		assertEquals(offsetT, ((ASTNode) decl).getOffset());

		offset = code.indexOf("V s;");
		decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
		assertEquals("V", ((IASTName) decl).toString());
		assertEquals(offsetV, ((ASTNode) decl).getOffset());
	}

	//	template <typename>
	//	struct A {
	//	    struct S {
	//	        void foo();
	//	    };
	//	    void test() {
	//	        S s;
	//	        s.foo();
	//	    }
	//	};
	public void testBug399142() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug399142.cpp", code); //$NON-NLS-1$

		int offset = code.indexOf("s.foo()") + 2;
		IASTNode decl = testF3(file, offset);
		assertTrue(decl instanceof IASTName);
	}

	//	int waldo;
	//	void foo() {
	//		extern int waldo;
	//	}
	public void testLocallyDeclaredExternVariable_372004() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug372004.cpp", code);

		int offset = code.indexOf("extern int waldo") + 12;
		assertTrue(testF3(file, offset) instanceof IASTName);
	}

	//	template <typename>
	//	struct A {
	//	    int waldo;
	//	};
	//
	//	template <typename T>
	//	struct B {
	//	    A<T> obj;
	//	    void foo() {
	//	        obj.waldo;
	//	    }
	//	};
	public void testDependentMemberAccess_448764() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug448764.cpp", code);

		int offset = code.indexOf("obj.waldo") + 4;
		assertTrue(testF3(file, offset) instanceof IASTName);
	}

	//	struct A {
	//	    A();
	//	};
	//
	//	template <class>
	//	struct B {
	//	    B() {}
	//	};
	//
	//	struct C {
	//	    C();
	//	    B<A>* b;
	//	};
	//
	//	C::C() : b(new B<A>()) {}
	public void testAmbiguityWithImplicitName_463234() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug463234.cpp", code);

		int offset = code.indexOf("new B<A>") + 6;
		// There should be two ambiguous targets, the class A and the constructor B::B,
		// with the class A being the first one (index 0).
		IASTNode target = testF3WithAmbiguity(file, offset, 0);
		assertTrue(target instanceof IASTName);
		assertEquals("A", ((IASTName) target).toString());
	}

	//	class Other {
	//	  int foo();
	//	};
	//
	//	template<class X>
	//	class Base {
	//	  Other *other;
	//	};
	//
	//	template<class X>
	//	class Child : public Base<X> {
	//	  void bar() {
	//	    this->other->foo();		// can't find other and foo
	//	    Base<X>::other->foo();	// can find other can't find foo
	//	  }
	//	};
	public void testMemberOfDependentBase_421823() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug421823.cpp", code);

		int offset = code.indexOf("this->other") + 6;
		assertTrue(testF3(file, offset) instanceof IASTName);

		offset += 7; // 'foo' in 'this->other->foo'
		assertTrue(testF3(file, offset) instanceof IASTName);

		offset = code.indexOf("::other->foo") + 9;
		assertTrue(testF3(file, offset) instanceof IASTName);
	}

	//	struct Duration {};
	//	Duration operator "" _d(unsigned long long);
	//	Duration dur = 42_d;
	public void testUserDefinedLiteralSuffix_484618() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug484618.cpp", code);

		int offset = code.indexOf("42_d") + 3;
		assertTrue(testF3(file, offset) instanceof IASTName);
	}

	//	struct Base {
	//	    Base(int, int);
	//	};
	//
	//	struct Derived : Base {
	//	    using Base::Base;
	//	};
	public void testInheritedConstructor_484899() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug484899.cpp", code);

		int offset = code.indexOf("Base::Base") + 7;
		IASTNode target = testF3(file, offset);
		assertInstance(target, IASTName.class);
		IBinding targetBinding = ((IASTName) target).resolveBinding();
		assertInstance(targetBinding, ICPPConstructor.class);
	}

	//	template <typename = short>
	//	struct waldo;
	//
	//	template <typename>
	//	struct waldo {};
	public void testNavigationToTemplateForwardDecl_483048() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug483048.cpp", code);

		int offset = code.indexOf("waldo {}");
		IASTNode target = testF3(file, offset);

		// Check that the result of the navigation is the forward declaration,
		// not the definition.
		assertInstance(target.getParent(), IASTElaboratedTypeSpecifier.class);
	}

	//	struct Waldo {};
	//	Waldo find();
	//	int main() {
	//		auto waldo = find();
	//	}
	public void testAutoType_511522() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug511522.cpp", code);

		int offset = code.indexOf("auto");
		IASTNode target = testF3(file, offset);
		assertInstance(target, IASTName.class);
	}

	//	struct Waldo {};
	//  template<typename T>
	//  struct Basket{};
	//	Waldo find();
	//	Waldo myFriend;
	//	int main(decltype(myFriend) p) {
	//		auto waldo = find();
	//      Basket<typeof(waldo)> basket;
	//      decltype(waldo) wuff;
	//	}
	public void testDeclType_520913() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug520913.cpp", code);

		int offset = code.indexOf("main") + 10;
		IASTNode target = testF3(file, offset);
		assertInstance(target, IASTName.class);

		offset = code.indexOf("typeof");
		target = testF3(file, offset);
		assertInstance(target, IASTName.class);
		assertEquals("Waldo", ((IASTName) target).toString());

		offset = code.indexOf("wuff") - 10;
		target = testF3(file, offset);
		assertInstance(target, IASTName.class);
	}

	//	template<typename T>
	//	struct A {
	//	  struct AA{};
	//
	//	  auto test() {
	//	    auto test = A<T>::AA();
	//	    return test;
	//	  }
	//
	//	};
	public void testDependentAutoType_520913() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug520913a.cpp", code);

		int offset = code.indexOf("auto test = ") + 2;
		IASTNode target = testF3(file, offset);
		assertInstance(target, IASTName.class);
		assertEquals("AA", ((IASTName) target).toString());

		offset = code.indexOf("auto test()") + 2;
		target = testF3(file, offset);
		assertInstance(target, IASTName.class);
		assertEquals("AA", ((IASTName) target).toString());
	}

	// template<char T>
	// struct A {};
	//
	// template<>
	// struct A<0> {};
	//
	// void test(){
	//   A<0> a0;
	//   A<1> a1;
	// }
	public void testPartialSpecializationResolution_525288() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;
		String code = getAboveComment();
		IFile file = importFile("testBug525288.cpp", code);

		int offset = code.indexOf("a0") - 5;
		IASTNode target = testF3(file, offset);
		assertInstance(target, IASTName.class);
		assertEquals("A<0>", ((IASTName) target).toString());

		offset = code.indexOf("a1") - 5;
		target = testF3(file, offset);
		assertInstance(target, IASTName.class);
		assertEquals("A", ((IASTName) target).toString());
	}

	// void npeTest() {
	//     auto i = 1;
	// }
	public void testEmptySpace_525794() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug525794.cpp", code);

		int offset = code.indexOf("auto") - 2;
		IASTNode target = testF3(file, offset);
	}

	//	class Waldo {
	//	    void find();
	//	};
	//	int Waldo::find() {}
	public void testNavigationToDefinitionWithWrongSignature_525739() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testBug525739.cpp", code);

		int offset = code.indexOf("void find") + 6;
		IASTNode target = testF3(file, offset);
		assertInstance(target, IASTName.class);
		assertEquals(IASTNameOwner.r_definition, ((IASTName) target).getRoleOfName(false));
	}

	//int arr[2]{1, 2};
	//auto [e1, e2] = arr;
	//auto r1 = e1;
	//auto r2 = e2;
	public void testOpenDeclarationForStructuredBinding_522200() throws Exception {
		String code = getAboveComment();
		IFile file = importFile("testSB_.cpp", code); //$NON-NLS-1$

		int offsetE1Reference = code.indexOf("e1;"); //$NON-NLS-1$
		IASTNode e1Declaration = testF3(file, offsetE1Reference);
		assertTrue(e1Declaration instanceof IASTName);

		String expectedE1Name = "e1"; //$NON-NLS-1$
		assertEquals(expectedE1Name, ((IASTName) e1Declaration).toString());
		assertEquals(code.indexOf(expectedE1Name), ((ASTNode) e1Declaration).getOffset());
		assertEquals(expectedE1Name.length(), ((ASTNode) e1Declaration).getLength());
		assertEquals(IASTNameOwner.r_definition, ((IASTName) e1Declaration).getRoleOfName(false));

		int offsetE2Reference = code.indexOf("e2;"); //$NON-NLS-1$
		IASTNode e2Declaration = testF3(file, offsetE2Reference);
		assertTrue(e2Declaration instanceof IASTName);

		String expectedE2Name = "e2"; //$NON-NLS-1$
		assertEquals(expectedE2Name, ((IASTName) e2Declaration).toString());
		assertEquals(code.indexOf(expectedE2Name), ((ASTNode) e2Declaration).getOffset());
		assertEquals(expectedE2Name.length(), ((ASTNode) e2Declaration).getLength());
		assertEquals(IASTNameOwner.r_definition, ((IASTName) e2Declaration).getRoleOfName(false));
	}
}
