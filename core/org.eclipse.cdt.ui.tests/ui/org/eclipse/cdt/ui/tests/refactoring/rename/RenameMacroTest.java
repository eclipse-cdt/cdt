/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc.
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
package org.eclipse.cdt.ui.tests.refactoring.rename;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameMacroTests extends RenameTestBase {

	public RenameMacroTests(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(true);
	}

	public static Test suite(boolean cleanup) {
		TestSuite suite = new TestSuite(RenameMacroTests.class);
		if (cleanup) {
			suite.addTest(new RefactoringTests("cleanupProject"));
		}
		return suite;
	}

	public void testMacroRename() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("#define HALLO x   \n");
		buf.append("class v1 {                \n");
		buf.append(" int HALLO;                   \n");
		buf.append("};                        \n");
		buf.append("class HALLO {                \n");
		buf.append(" int v;                   \n");
		buf.append("};                        \n");
		buf.append("class v3 {                \n");
		buf.append(" int v;                   \n");
		buf.append("};                        \n");
		buf.append("class v4 {                \n");
		buf.append(" int HALLO();          \n");
		buf.append("};                        \n");
		buf.append("int v4::HALLO(){}      \n");
		buf.append("void f(int par1){         \n");
		buf.append("  {                       \n");
		buf.append("     int HALLO; v1::v++;    \n");
		buf.append("  }                       \n");
		buf.append("}                         \n");
		String contents = buf.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("HALLO");
		int offset2 = contents.indexOf("HALLO", offset1 + 1);

		Change ch = getRefactorChanges(cpp, offset1, "WELT");
		assertTotalChanges(6, ch);
		int off = offset1;
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);

		ch = getRefactorChanges(cpp, offset2, "WELT");
		assertTotalChanges(6, ch);
		off = offset1;
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
		assertChange(ch, cpp, off, 5, "WELT");
		off = contents.indexOf("HALLO", off + 1);
	}

	public void testMacroNameConflicts() throws Exception {
		createCppFwdDecls("cpp_fwd.hh");
		createCppDefs("cpp_def.hh");
		StringBuilder buf = new StringBuilder();
		buf.append("#include \"cpp_fwd.hh\"   \n");
		buf.append("#include \"cpp_def.hh\"   \n");
		buf.append("#define MACRO 1           \n");
		buf.append("int v1(); int v2(); int v3();  \n");
		buf.append("static int s1();          \n");
		buf.append("static int s2();          \n");
		buf.append("void f(int par1){         \n");
		buf.append("     int w1; v1();        \n");
		buf.append("     extern_var;          \n");
		buf.append("     var_def;             \n");
		buf.append("     enum_item;           \n");
		buf.append("}                         \n");
		String contents = buf.toString();
		IFile cpp = importFile("test.cpp", contents);

		buf = new StringBuilder();
		buf.append("static int static_other_file();     \n");
		importFile("other.cpp", buf.toString());
		waitForIndexer();

		int offset1 = contents.indexOf("MACRO");

		// conflicts after renaming
		RefactoringStatus status = checkConditions(cpp, offset1, "w1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Name conflict  \n"
						+ "New element: w1  \n" + "Conflicting element type: Local variable");
		status = checkConditions(cpp, contents.indexOf("par1"), "MACRO");
		assertRefactoringError(status, "'MACRO' conflicts with the name of an existing macro.");
		status = checkConditions(cpp, offset1, "par1");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Name conflict  \n"
						+ "New element: par1  \n" + "Conflicting element type: Parameter");
		status = checkConditions(cpp, offset1, "extern_var");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Name conflict  \n"
						+ "New element: extern_var  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "var_def");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Name conflict  \n"
						+ "New element: var_def  \n" + "Conflicting element type: Global variable");
		status = checkConditions(cpp, offset1, "enum_item");
		assertRefactoringError(status,
				"A conflict was encountered during refactoring.  \n" + "Type of problem: Name conflict  \n"
						+ "New element: enum_item  \n" + "Conflicting element type: Enumerator");
	}

	public void testClassMacroClash() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("class CC {int a;};         \n");
		String contents = buf.toString();
		IFile cpp = importFile("test.cpp", contents);

		buf = new StringBuilder();
		buf.append("#define CC mm              \n");
		buf.append("int CC;                   \n");
		String contents2 = buf.toString();
		IFile cpp2 = importFile("test2.cpp", contents2);

		int offset1 = contents.indexOf("CC");
		Change ch = getRefactorChanges(cpp, offset1, "CCC");
		assertTotalChanges(1, ch);

		int offset2 = contents2.indexOf("CC");
		ch = getRefactorChanges(cpp2, offset2, "CCC");
		assertTotalChanges(2, ch);
	}

	public void testMacroRename_434917() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("#define CC mm\n");
		String contents = buf.toString();
		IFile header = importFile("test.h", contents);

		buf = new StringBuilder();
		buf.append("#include \"test.h\"\n");
		buf.append("int CC;\n");
		String contents2 = buf.toString();
		IFile source = importFile("test.cpp", contents2);

		int offset = contents.indexOf("CC");
		Change ch = getRefactorChanges(header, offset, "CCC");
		assertTotalChanges(2, ch);
	}

	public void testIncludeGuard() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("#ifndef _guard            \n");
		buf.append("#define _guard            \n");
		buf.append(" int HALLO                \n");
		buf.append("#endif /* _guard */       \n");
		String contents = buf.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("_guard");
		int offset2 = contents.indexOf("_guard", offset1 + 1);
		Change ch = getRefactorChanges(cpp, offset2, "WELT");
		assertTotalChanges(2, 0, 1, ch);
		int off = offset1;
		assertChange(ch, cpp, off, 6, "WELT");
		off = contents.indexOf("_guard", off + 1);
		assertChange(ch, cpp, off, 6, "WELT");
	}

	public void testMacroParameters() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("int var;                  \n");
		buf.append("#define M1(var) var       \n");
		buf.append("#define M2(var, x) (var+x)*var  \n");
		buf.append("#define M3 var            \n");
		String contents = buf.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("var");
		Change ch = getRefactorChanges(cpp, offset1, "xxx");
		assertTotalChanges(1, 1, 0, ch);
	}

	public void testRenameMacroAsMacroArgument() throws Exception {
		StringBuilder buf = new StringBuilder();
		buf.append("#define M1(var) var       \n");
		buf.append("#define M2 1              \n");
		buf.append("int b= M2;                \n");
		buf.append("int a= M1(M2);            \n");
		String contents = buf.toString();
		IFile cpp = importFile("test.cpp", contents);

		int offset1 = contents.indexOf("M2");
		Change ch = getRefactorChanges(cpp, offset1, "xxx");
		assertTotalChanges(countOccurrences(contents, "M2"), ch);
	}
}
